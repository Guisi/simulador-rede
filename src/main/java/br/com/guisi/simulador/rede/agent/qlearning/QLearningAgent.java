package br.com.guisi.simulador.rede.agent.qlearning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

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
import br.com.guisi.simulador.rede.enviroment.NetworkNode;
import br.com.guisi.simulador.rede.enviroment.SwitchDistance;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;

@Named
@Scope("prototype")
public class QLearningAgent extends Agent {
	
	private final Random RANDOM = new Random(System.currentTimeMillis());

	private QTable qTable;
	private Branch currentSwitch;
	private Set<Load> turnedOffLoads;
	
	private double initialConfigRate;

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
		this.turnedOffLoads = new LinkedHashSet<>();
		
		Environment environment = SimuladorRede.getEnvironment();
		//verifica se existe alguma falta
		this.currentSwitch = environment.getRandomFault();
		//se não existe, inicia por um switch aberto aleatório
		if (this.currentSwitch == null) {
			this.currentSwitch = environment.getRandomSwitch();
		}
		
		this.initialConfigRate = getConfigRate(environment);
		
		System.out.println(initialConfigRate);
	}
	
	private double getConfigRate(Environment environment) {
		/*double activePowerLossPercentage = environment.getActivePowerLostPercentage();
        double suppliedActivePowerPercentage = environment.getSuppliedActivePowerPercentage();
		return (100 - activePowerLossPercentage) * suppliedActivePowerPercentage;*/
		
		double suppliedActivePowerPercentage = environment.getSuppliedActivePowerPercentage();
		return suppliedActivePowerPercentage;
	}
	
	/**
	 * Realiza uma interação no ambiente
	 * @param agentStepStatus
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	@Override
	protected void runNextEpisode(AgentStepStatus agentStepStatus) {
		Environment environment = SimuladorRede.getEnvironment();
		
		//reativa loads desativados no episódio anterior
		turnedOffLoads.forEach(load -> load.turnOn());
		turnedOffLoads.clear();
		
		//escolhe o próximo switch com base no aprendizado
		SwitchStatus switchStatus = currentSwitch.isClosed() ? SwitchStatus.CLOSED : SwitchStatus.OPEN;
		List<SwitchDistance> switchesDistances = this.getSwitchesDistances(environment, currentSwitch, switchStatus);
		
		AgentState currentState = new AgentState(currentSwitch.getNumber(), currentSwitch.getSwitchState());
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		AgentAction action = null;
		if (randomAction) {
			action = qTable.getRandomAction(currentState, switchesDistances);
		} else {
			action = qTable.getBestAction(currentState, switchesDistances);
		}

		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		nextSwitch.reverse();

		//executa o fluxo de potência
		PowerFlow.execute(environment);

		//verifica loads a serem desativados caso existam restrições 
		this.turnOffLoadsIfNecessary(environment);
		
		//atualiza o qValue do switch
		this.updateQValue(environment, currentState, action);
		
		this.currentSwitch = nextSwitch;
		
		this.generateAgentStatus(environment, agentStepStatus);
	}
	
	/**
	 * Desliga loads se existirem restrições
	 * @param environment
	 * @throws Exception
	 */
	private void turnOffLoadsIfNecessary(Environment environment) {
		
		for (Feeder feeder : environment.getFeeders()) {
			List<Load> turnedOffLoads = new ArrayList<>();
			
			List<Load> onLoads = getFeederLoadsOn(feeder);
			//primeiro desliga os loads com restrição em grupos até que não existam mais restrições
			long brokenLoadsQuantity = getFeederBrokenLoadsQuantity(feeder);
			while (brokenLoadsQuantity > 0) {
				
				double quantity = Math.ceil((double)brokenLoadsQuantity/2);
				for (int i = 0; i < quantity; i++) {
					//Verifica a menor prioridade encontrada entre os loads do feeder em questão
					int minPriority = onLoads.stream().min(Comparator.comparing(load -> load.getPriority())).get().getPriority();
					
					//filtra por todos os loads com a menor prioridade
					List<Load> minPriorityLoads = onLoads.stream().filter(load -> load.getPriority() == minPriority).collect(Collectors.toList());
					
					//desliga o load com a menor tensão
					double minCurrent = minPriorityLoads.stream().min(Comparator.comparing(load -> load.getCurrentVoltagePU())).get().getCurrentVoltagePU();
					Load minCurrentLoad = minPriorityLoads.stream().filter(load -> load.getCurrentVoltagePU() == minCurrent).findFirst().get();
					
					minCurrentLoad.turnOff();
					turnedOffLoads.add(minCurrentLoad);
					onLoads.remove(minCurrentLoad);
				}
				
				//executa o fluxo de potência
				PowerFlow.execute(environment);
				
				//verifica novamente se continuam existindo loads com restrição violada
				brokenLoadsQuantity = getFeederBrokenLoadsQuantity(feeder);
			}
			
			//depois religa um a um até que alguma restrição seja criada (e reverte a ação que criou esta restrição)
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
			
			this.turnedOffLoads.addAll(turnedOffLoads);
		}
	}
	
	private long getFeederBrokenLoadsQuantity(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn() && load.hasBrokenConstraint()).count();
	}
	
	private List<Load> getFeederLoadsOn(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn()).collect(Collectors.toList());
	}
	
	private void generateAgentStatus(Environment environment, AgentStepStatus agentStepStatus) {
		//atualiza status com o switch alterado
		agentStepStatus.putInformation(AgentInformationType.SWITCH_OPERATION, new SwitchOperation(currentSwitch.getNumber(), currentSwitch.getSwitchState()));
		
		//seta total de perdas
		agentStepStatus.putInformation(AgentInformationType.ACTIVE_POWER_LOST, environment.getActivePowerLostMW());
		agentStepStatus.putInformation(AgentInformationType.REACTIVE_POWER_LOST, environment.getReactivePowerLostMVar());
		
		//seta demanda atendida
		agentStepStatus.putInformation(AgentInformationType.SUPPLIED_LOADS_ACTIVE_POWER, environment.getSuppliedActivePowerDemandMW());
		agentStepStatus.putInformation(AgentInformationType.SUPPLIED_LOADS_REACTIVE_POWER, environment.getSuppliedReactivePowerDemandMVar());
		
		//seta demanda não atendida
		agentStepStatus.putInformation(AgentInformationType.NOT_SUPPLIED_LOADS_ACTIVE_POWER, environment.getNotSuppliedActivePowerDemandMW());
		agentStepStatus.putInformation(AgentInformationType.NOT_SUPPLIED_LOADS_REACTIVE_POWER, environment.getNotSuppliedReactivePowerDemandMVar());
		
		//seta demanda desligada
		agentStepStatus.putInformation(AgentInformationType.OUT_OF_SERVICE_LOADS_ACTIVE_POWER, environment.getOutOfServiceActivePowerDemandMW());
		agentStepStatus.putInformation(AgentInformationType.OUT_OF_SERVICE_LOADS_REACTIVE_POWER, environment.getOutOfServiceReactivePowerDemandMVar());
		
		//seta soma das prioridades dos loads atendidos e não atendidos
		agentStepStatus.putInformation(AgentInformationType.SUPPLIED_LOADS_VS_PRIORITY, environment.getSuppliedLoadsVsPriority());
		agentStepStatus.putInformation(AgentInformationType.NOT_SUPPLIED_LOADS_VS_PRIORITY, environment.getNotSuppliedLoadsVsPriority());
		
		//seta soma das prioridades dos loads atendidos e não atendidos x potência ativa MW
		agentStepStatus.putInformation(AgentInformationType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getSuppliedLoadsActivePowerMWVsPriority());
		agentStepStatus.putInformation(AgentInformationType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getNotSuppliedLoadsActivePowerMWVsPriority());
		
		//min load current voltage pu
		agentStepStatus.putInformation(AgentInformationType.MIN_LOAD_CURRENT_VOLTAGE_PU, environment.getMinLoadCurrentVoltagePU());
		
		 //nota da configuração da rede
        double configRate = getConfigRate(environment);
        agentStepStatus.putInformation(AgentInformationType.ENVIRONMENT_CONFIGURATION_RATE, (configRate - initialConfigRate) / initialConfigRate);
        
        int differentSwitchStatesCount = EnvironmentUtils.countDifferentSwitchStates(environment, SimuladorRede.getInitialEnvironment());
        agentStepStatus.putInformation(AgentInformationType.REQUIRED_SWITCH_OPERATIONS, differentSwitchStatesCount);
	}
	
	private void updateQValue(Environment environment, AgentState state, AgentAction action) {
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(state, action);
        
		double q = qValue.getReward();
        
        //recupera os possíveis candidatos para a próxima iteração
		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		List<SwitchDistance> switchesDistances = this.getSwitchesDistances(environment, nextSwitch, action.getSwitchStatus());

		//recupera em sua QTable o melhor valor para o estado para o qual se moveu
		AgentState nextState = new AgentState(action.getSwitchNumber(), action.getSwitchStatus());
		QValue bestNextQValue = qTable.getBestQValue(nextState, switchesDistances);
        double nextStateQ = bestNextQValue != null ? bestNextQValue.getReward() : 0;
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a ação
        double configRate = getConfigRate(environment);
        
        System.out.println(configRate);
        
        double r = (configRate - initialConfigRate) / initialConfigRate;
        
        //Algoritmo Q-Learning -> calcula o novo valor para o estado/ação que estava antes
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	/**
	 * Busca uma lista com as distâncias dos switches candidatos
	 * @param environment
	 * @param refSwitch switch onde o agente está atualmente
	 * @param switchStatus status do switch a ser procurado
	 * @return
	 */
	public List<SwitchDistance> getSwitchesDistances(Environment environment, Branch refSwitch, SwitchStatus switchStatus) {
		//busca a lista das distâncias dos switches
		List<SwitchDistance> switchesDistances = environment.getSwitchesDistances(refSwitch, switchStatus);
		
		//caso esteja procurando por switches abertos para fechar
		if (switchStatus == SwitchStatus.OPEN) {
			List<SwitchDistance> swRemover = new ArrayList<>();
			switchesDistances.forEach(switchDistance -> {
				NetworkNode node1 = switchDistance.getTheSwitch().getNodeFrom(); 
				NetworkNode node2 = switchDistance.getTheSwitch().getNodeTo();
				
				if (node1.isLoad() && node2.isLoad()) {
					Load load1 = (Load) node1;
					Load load2 = (Load) node2;
					
					//remove os switches que ligam dois loads onde ambos estão ligados a algum feeder, para evitar criar circuitos fechados
					//e remove os switches que ligam dois loads que não estejam ligados a nenhum feeder, pois sabe-se que não irão gerar uma melhoria na rede 
					if ( (load1.getFeeder() != null && load2.getFeeder() != null) || (load1.getFeeder() == null && load2.getFeeder() == null)) {
						swRemover.add(switchDistance);
					}
				} else if ( (node1.isLoad() && ((Load)node1).getFeeder() != null && node2.isFeeder()) || (node2.isLoad() && ((Load)node2).getFeeder() != null && node1.isFeeder()) ) {
					//remove também switches onde uma das pontas é um load que já está conectado a um feeder, e a outra ponta possui um feeder, para evitar circuitos fechados
					swRemover.add(switchDistance);
				}
			});
			switchesDistances.removeAll(swRemover);
			
			//adiciona o próprio switch como opção, caso não seja uma falta
			if (refSwitch.isClosed() || refSwitch.isOpen()) {
				Integer distance = switchesDistances.isEmpty() ? 0 : switchesDistances.get(0).getDistance();
				SwitchDistance switchDistance = new SwitchDistance(distance, refSwitch);
				switchesDistances.add(0, switchDistance);
			}
			
			//Se não existe nenhum switch aberto candidato ou somente o próprio switch onde o agente está,
			//irá retornar uma lista de switches fechados candidatos usando como referência um dos switches abertos que não podiam ser fechados
			if (switchesDistances.size() <= 1) {
				Integer minDistance = swRemover.stream().min(Comparator.comparing(value -> value.getDistance())).get().getDistance();
				
				//filtra por todos os switches da lista com a menor distância
				List<SwitchDistance> switchesMin = swRemover.stream().filter(valor -> valor.getDistance() == minDistance).collect(Collectors.toList());
				
				//retorna um dos switches mais próximos aleatoriamente
				Branch sw = switchesMin.get(RANDOM.nextInt(switchesMin.size())).getTheSwitch();

				switchesDistances = environment.getSwitchesDistances(sw, SwitchStatus.CLOSED);
			}
		}

		return switchesDistances;
	}
	
	@Override
	public Branch getCurrentState() {
		return currentSwitch;
	}
	
	@Override
	public List<LearningProperty> getLearningProperties(Integer switchNumber) {
		List<LearningProperty> learningProperties = new ArrayList<>();

		List<QValue> qValues = new ArrayList<>();
		List<QValue> qValuesOpen = qTable.getQValues(new AgentState(switchNumber, SwitchStatus.OPEN));
		if (qValuesOpen != null) {
			qValues.addAll(qValuesOpen);
		}

		List<QValue> qValuesClosed = qTable.getQValues(new AgentState(switchNumber, SwitchStatus.CLOSED));
		if (qValuesClosed != null) {
			qValues.addAll(qValuesClosed);
		}
		
		for (QValue qValue : qValues) {
			String state = String.format("%02d", qValue.getState().getSwitchNumber()) + "/" + qValue.getState().getSwitchStatus().getDescription();
			String action = String.format("%02d", qValue.getAction().getSwitchNumber()) + "/" + qValue.getAction().getSwitchStatus().getPastTenseDescription();
			
			BigDecimal value = new BigDecimal(qValue.getReward()).setScale(10, RoundingMode.HALF_UP);
			LearningProperty row = new LearningProperty("Q(" + state + ", " + action + "):", value.toPlainString());
			learningProperties.add(row);
		}

		return learningProperties;
	}
}
