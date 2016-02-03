package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.application.Platform;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.SwitchDistance;
import br.com.guisi.simulador.rede.enviroment.SwitchState;
import br.com.guisi.simulador.rede.util.AlertUtils;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;

@Named
@Scope("prototype")
public class QLearningAgent extends Agent {
	
	private QTable qTable;
	private Branch firstSwitch;
	private Branch secondSwitch;
	
	private Map<Integer, List<SwitchDistance>> visitedSwitchesMap;
	private List<Load> turnedOffLoads;
	
	private final Random RANDOM = new Random(System.currentTimeMillis());

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
		this.visitedSwitchesMap = new HashMap<>();
		this.turnedOffLoads = new ArrayList<>();
	}
	
	/**
	 * Realiza uma intera��o no ambiente
	 * @param agentStepStatus
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	@Override
	protected void runNextEpisode(AgentStepStatus agentStepStatus) {
		Environment environment = SimuladorRede.getEnvironment();
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		//TODO remover
		randomAction = true;
		
		if (randomAction) {
			//reativa loads desativados no epis�dio anterior
			turnedOffLoads.forEach((load) -> load.turnOn());
			turnedOffLoads.clear();
			
			//faz as mudan�as de status dos switches
			this.doSwitchChanges(environment, agentStepStatus);
			
			try {
				//executa o fluxo de pot�ncia
				PowerFlow.execute(environment);
				
				//verifica loads a serem desativados caso existam restri��es 
				this.turnOffLoadsIfNecessary(environment);
				
				//TODO s� atualizar quando mexer nos switches mesmo??
				
				//atualiza o qValue do primeiro switch
				updateQValue(firstSwitch);
				
				//atualiza o qValue do segundo switch
				if (secondSwitch != null) {
					updateQValue(secondSwitch);
				}
				
				this.generateAgentStatus(environment, agentStepStatus);
				
			} catch (Exception e) {
				stop();
				e.printStackTrace();
				Platform.runLater(() -> {
					AlertUtils.showStacktraceAlert(e);
				});
			}
		}
	}
	
	/**
	 * Identifica o par de switch para ser aberto/fechado, garantindo que a radialidade seja mantida
	 * @param environment
	 * @param agentStepStatus
	 */
	private void doSwitchChanges(Environment environment, AgentStepStatus agentStepStatus) {
		//busca o switch a ser aberto
		firstSwitch = getClosestSwitch(environment, firstSwitch, SwitchState.CLOSED, null).getTheSwitch();

		if (!firstSwitch.hasFault()) {
			firstSwitch.reverse();
		}
		
		//busca o switch a ser fechado
		//continua procurando enquanto n�o estiver radial
		//somente para o switch fechado, pois abrir switch n�o causa perda de radialidade
		String errors = null;
		List<SwitchDistance> switchesToIgnore = new ArrayList<>();
		do {
			SwitchDistance secondSwitchDistance = getClosestSwitch(environment, firstSwitch, SwitchState.OPEN, switchesToIgnore);
			secondSwitch = secondSwitchDistance != null ? secondSwitchDistance.getTheSwitch() : null;
			if (secondSwitchDistance != null) {
				secondSwitch.reverse();
			
				errors = EnvironmentUtils.validateRadialState(environment);
		
				if (!errors.isEmpty()) {
					secondSwitch.reverse();
					switchesToIgnore.add(secondSwitchDistance);
				}
			}
		} while (secondSwitch != null && !errors.isEmpty());
		
		//atualiza status com os switches alterados
		List<SwitchOperation> switchOperations = new ArrayList<>();
		switchOperations.add(new SwitchOperation(firstSwitch.getNumber(), firstSwitch.getSwitchState()));
		if (secondSwitch != null) {
			switchOperations.add(new SwitchOperation(secondSwitch.getNumber(), secondSwitch.getSwitchState()));
		}
		agentStepStatus.putInformation(AgentInformationType.SWITCH_OPERATIONS, switchOperations);
	}
	
	/**
	 * Desliga loads se existirem restri��es
	 * @param environment
	 * @throws Exception
	 */
	private void turnOffLoadsIfNecessary(Environment environment) throws Exception {
		
		for (Feeder feeder : environment.getFeeders()) {
			
			List<Load> onLoads = getFeederLoadsOn(feeder);

			//primeiro desliga os loads com restri��o em grupos at� que n�o existam mais restri��es
			long brokenLoadsQuantity = getFeederBrokenLoadsQuantity(feeder);
			while (brokenLoadsQuantity > 0) {
				
				double quantity = Math.ceil((double)brokenLoadsQuantity/2);
				for (int i = 0; i < quantity; i++) {
					//Verifica a menor prioridade encontrada entre os loads do feeder em quest�o
					int minPriority = onLoads.stream().min(Comparator.comparing(load -> load.getPriority())).get().getPriority();
					
					//filtra por todos os loads com a menor prioridade
					List<Load> minPriorityLoads = onLoads.stream().filter(load -> load.getPriority() == minPriority).collect(Collectors.toList());
					
					//desliga um dos loads de menor prioridade aleatoriamente
					//Load minPriorityLoad = minPriorityLoads.get(RANDOM.nextInt(minPriorityLoads.size()));
					
					//desliga o load com a menor tens�o
					double minCurrent = minPriorityLoads.stream().min(Comparator.comparing(load -> load.getCurrentVoltagePU())).get().getCurrentVoltagePU();
					Load minCurrentLoad = minPriorityLoads.stream().filter(load -> load.getCurrentVoltagePU() == minCurrent).findFirst().get();
					
					minCurrentLoad.turnOff();
					turnedOffLoads.add(minCurrentLoad);
					onLoads.remove(minCurrentLoad);
				}
				
				//executa o fluxo de pot�ncia
				PowerFlow.execute(environment);
				
				//verifica novamente se continuam existindo loads com restri��o violada
				brokenLoadsQuantity = getFeederBrokenLoadsQuantity(feeder);
			}
			
			//depois religa um a um at� que alguma restri��o seja criada (e reverte a a��o que criou esta restri��o)
			List<Load> offLoads = new ArrayList<>(turnedOffLoads);
			Collections.reverse(offLoads);
			for (Load load : offLoads) {
				load.turnOn();
				PowerFlow.execute(environment);
				
				if (getFeederBrokenLoadsQuantity(feeder) > 0) {
					load.turnOff();
					PowerFlow.execute(environment);
					break;
				}
				turnedOffLoads.remove(load);
			}
		}
	}
	
	private long getFeederBrokenLoadsQuantity(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn() && load.hasBrokenConstraint()).count();
	}
	
	private List<Load> getFeederLoadsOn(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn()).collect(Collectors.toList());
	}
	
	private void generateAgentStatus(Environment environment, AgentStepStatus agentStepStatus) {
		//seta total de perdas
		double activePowerLostMW = environment.getActivePowerLostMW();
		double activePowerDemandMW = environment.getActivePowerDemandMW();
		if (activePowerDemandMW > 0) {
			agentStepStatus.putInformation(AgentInformationType.ACTIVE_POWER_LOST_PERCENTUAL, activePowerLostMW / activePowerDemandMW * 100);
		}
		
		double reactivePowerLostMVar = environment.getReactivePowerLostMVar();
		double reactivePowerDemandMVar = environment.getReactivePowerDemandMVar();
		if (reactivePowerLostMVar > 0) {
			agentStepStatus.putInformation(AgentInformationType.REACTIVE_POWER_LOST_PERCENTUAL, reactivePowerLostMVar / reactivePowerDemandMVar * 100);
		}
	}
	
	private void updateQValue(Branch sw) {
		//recupera em sua QTable o valor de recompensa para o estado/a��o que estava antes
		QValue qValue = qTable.getQValue(sw.getNumber(), sw.getSwitchState());
        double q = qValue.getReward();
        
        //recupera em sua QTable o melhor valor para o estado para o qual se moveu
        //TODO ver o que vai ser esse nextState 
        //double nextStateQ = qTable.getBestQValue(nextState).getReward();
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a a��o
        double r = 0;//TODO actionResult.getReward();

        //Algoritmo Q-Learning -> calcula o novo valor para o estado/a��o que estava antes
        //TODO double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * q) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	/**
	 * Busca o switch mais pr�ximo ao switch passado
	 * @param environment
	 * @param refSwitch switch de refer�ncia, a partir de onde ser�o buscados os mais pr�ximos
	 * @param switchState status do switch a ser procurado
	 * @param switchesToIgnore lista de switches a ignorar, para os casos onde o switch j� foi retornado e causou perda de radialidade
	 * @return
	 */
	public SwitchDistance getClosestSwitch(Environment environment, Branch refSwitch, SwitchState switchState, List<SwitchDistance> switchesToIgnore) {
		SwitchDistance switchDistance = null;

		//se o estado atual est� nulo, � a primeira intera��o
		if (refSwitch == null) {
			//verifica se existe alguma falta 
			Branch sw = environment.getRandomFault();
			//se n�o existe, inicia por um switch aberto aleat�rio
			if (sw == null) {
				sw = environment.getRandomSwitch(switchState);
			}
			
			switchDistance = new SwitchDistance(0, sw);
		} else {
			//sen�o, busca o switch mais pr�ximo
			
			//busca uma lista dos switches mais pr�ximos
			List<SwitchDistance> closestSwitches = environment.getClosestSwitches(refSwitch, switchState);
			
			if (switchesToIgnore != null) {
				closestSwitches.removeAll(switchesToIgnore);
			}
			
			if (!closestSwitches.isEmpty()) {
				//quando est� procurando um switch para abrir, verifica switches j� visitados
				List<SwitchDistance> visitedSwitches = null;
				if (switchState == SwitchState.CLOSED) {
					visitedSwitches = visitedSwitchesMap.get(refSwitch.getNumber());
					if (visitedSwitches != null) {
						//se para o switch de refer�ncia, j� foi visitado todos os mais pr�ximos, limpa a lista de visitados
						if (visitedSwitches.containsAll(closestSwitches)) {
							visitedSwitches.clear();
						} else {
							//sen�o, remove os j� visitados para que visite os demais
							closestSwitches.removeAll(visitedSwitches);
						}
					} else {
						visitedSwitches = new ArrayList<>();
						visitedSwitchesMap.put(refSwitch.getNumber(), visitedSwitches);
					}
				}
	
				//Verifica o menor valor de dist�ncia encontrado
				Integer minDistance = closestSwitches.stream().min(Comparator.comparing(value -> value.getDistance())).get().getDistance();
				
				//filtra por todos os switches da lista com a menor dist�ncia
				closestSwitches = closestSwitches.stream().filter(valor -> valor.getDistance() == minDistance).collect(Collectors.toList());
				
				//retorna um dos switches mais pr�ximos aleatoriamente
				switchDistance = closestSwitches.get(RANDOM.nextInt(closestSwitches.size()));
				
				//adiciona o switch na lista de visitados
				if (switchState == SwitchState.CLOSED) {
					visitedSwitches.add(switchDistance);
				}
			}
		}

		return switchDistance;
	}
	
	public QValue getBestQValue(Integer state) {
		return qTable.getBestQValue(state);
	}
	
	public double getGreaterReward() {
		return qTable.getGreaterReward();
	}
	
	public double getLowerReward() {
		return qTable.getLowerReward();
	}
	
	public List<QValue> getQValues(Integer state) {
		return qTable.getQValues(state);
	}
	
	@Override
	public List<LearningProperty> getLearningProperties(Integer state) {
		List<QValue> qValues = this.getQValues(state);
		List<LearningProperty> learningProperties = new ArrayList<>();
		for (QValue qValue : qValues) {
			LearningProperty row = new LearningProperty("Q(s, " + qValue.getQKey().getAction().getDescription() + "):", String.valueOf(qValue.getReward()));
			learningProperties.add(row);
		}
		return learningProperties;
	}
}
