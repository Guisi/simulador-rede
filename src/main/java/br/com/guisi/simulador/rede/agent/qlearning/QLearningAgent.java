package br.com.guisi.simulador.rede.agent.qlearning;

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
import br.com.guisi.simulador.rede.enviroment.SwitchState;
import br.com.guisi.simulador.rede.util.PowerFlow;

@Named
@Scope("prototype")
public class QLearningAgent extends Agent {
	
	private final Random RANDOM = new Random(System.currentTimeMillis());

	private QTable qTable;
	private Branch currentSwitch;
	private Set<Load> turnedOffLoads;

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
	}
	
	/**
	 * Realiza uma interação no ambiente
	 * @param agentStepStatus
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	@Override
	protected void runNextEpisode(AgentStepStatus agentStepStatus) {
		Environment environment = SimuladorRede.getEnvironment();
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		//reativa loads desativados no episódio anterior
		turnedOffLoads.forEach(load -> load.turnOn());
		turnedOffLoads.clear();
		
		//faz as mudança de status do switch
		SwitchState switchState = currentSwitch.isClosed() ? SwitchState.CLOSED : SwitchState.OPEN;
		Branch nextSwitch = getNextSwitch(environment, currentSwitch, switchState);
		
		if (nextSwitch != null) {
			nextSwitch.reverse();
	
			//executa o fluxo de potência
			PowerFlow.execute(environment);
	
			//verifica loads a serem desativados caso existam restrições 
			this.turnOffLoadsIfNecessary(environment);
			
			//atualiza o qValue do switch
			updateQValue(currentSwitch);
			
			this.currentSwitch = nextSwitch;
			
			this.generateAgentStatus(environment, agentStepStatus);
		}
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
		
		//seta demanda
		agentStepStatus.putInformation(AgentInformationType.ACTIVE_POWER_DEMAND, environment.getActivePowerDemandMW());
		agentStepStatus.putInformation(AgentInformationType.REACTIVE_POWER_DEMAND, environment.getReactivePowerDemandMVar());
		
		//seta soma das prioridades dos loads atendidos e não atendidos
		agentStepStatus.putInformation(AgentInformationType.SUPPLIED_LOADS_VS_PRIORITY, environment.getSuppliedLoadsVsPriority());
		agentStepStatus.putInformation(AgentInformationType.NOT_SUPPLIED_LOADS_VS_PRIORITY, environment.getNotSuppliedLoadsVsPriority());
		
		//seta soma das prioridades dos loads atendidos e não atendidos x potência ativa MW
		agentStepStatus.putInformation(AgentInformationType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getSuppliedLoadsActivePowerMWVsPriority());
		agentStepStatus.putInformation(AgentInformationType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getNotSuppliedLoadsActivePowerMWVsPriority());
		
		//min load current voltage pu
		agentStepStatus.putInformation(AgentInformationType.MIN_LOAD_CURRENT_VOLTAGE_PU, environment.getMinLoadCurrentVoltagePU());			
	}
	
	private void updateQValue(Branch sw) {
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(sw.getNumber(), sw.getSwitchState());
        double q = qValue.getReward();
        
        //recupera em sua QTable o melhor valor para o estado para o qual se moveu
        //TODO ver o que vai ser esse nextState 
        //double nextStateQ = qTable.getBestQValue(nextState).getReward();
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a ação
        double r = 0;//TODO actionResult.getReward();

        //Algoritmo Q-Learning -> calcula o novo valor para o estado/ação que estava antes
        //TODO double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * q) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	/**
	 * Busca o próximo switch com base na recompensa da tabela Q e distância
	 * @param environment
	 * @param refSwitch switch onde o agente está atualmente
	 * @param switchState status do switch a ser procurado
	 * @return
	 */
	public Branch getNextSwitch(Environment environment, Branch refSwitch, SwitchState switchState) {
		//busca a lista das distâncias dos switches
		List<SwitchDistance> switchesDistances = environment.getSwitchesDistances(refSwitch, switchState);
		
		if (switchState == SwitchState.OPEN) {
			List<SwitchDistance> swRemover = new ArrayList<>();
			switchesDistances.forEach(switchDistance -> {
				NetworkNode node1 = switchDistance.getTheSwitch().getNode1(); 
				NetworkNode node2 = switchDistance.getTheSwitch().getNode2();
				
				if (node1.isLoad() && node2.isLoad()) {
					Load load1 = (Load) node1;
					Load load2 = (Load) node2;
	
					//caso esteja procurando por switches abertos para fechar,
					//remove os switches que ligam dois loads onde ambos estão ligados a algum feeder, para evitar criar circuitos fechados
					//TODO HEURÍSTICA e remove os switches que ligam dois loads que não estejam ligados a nenhum feeder, pois sabe-se que não irão gerar uma melhoria na rede 
					if ( (load1.getFeeder() != null && load2.getFeeder() != null) || (load1.getFeeder() == null && load2.getFeeder() == null)) {
						swRemover.add(switchDistance);
					}
				}
			});
			switchesDistances.removeAll(swRemover);
		}
		
		if (!switchesDistances.isEmpty()) {
			//TODO escolher switch de acordo com tabela Q x distancia
			//     se optarmos por permitir escolher o sw onde o agente já está para fechar/abrir,
			//     o peso da distância terá que ser pequeno para evitar que o agente insista em se manter no mesmo sw
			
			//Verifica o menor valor de distância encontrado
			Integer minDistance = switchesDistances.stream().min(Comparator.comparing(value -> value.getDistance())).get().getDistance();
			
			//filtra por todos os switches da lista com a menor distância
			switchesDistances = switchesDistances.stream().filter(valor -> valor.getDistance() == minDistance).collect(Collectors.toList());
			
			//retorna um dos switches mais próximos aleatoriamente
			return switchesDistances.get(RANDOM.nextInt(switchesDistances.size())).getTheSwitch();
		} else {
			return refSwitch;
		}
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
	public Branch getCurrentState() {
		return currentSwitch;
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
