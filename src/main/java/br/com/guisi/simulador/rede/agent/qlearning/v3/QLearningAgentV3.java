package br.com.guisi.simulador.rede.agent.qlearning.v3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.agent.data.LearningProperty;
import br.com.guisi.simulador.rede.agent.data.LearningPropertyPair;
import br.com.guisi.simulador.rede.agent.data.LearningState;
import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.NetworkRestrictionsTreatmentType;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.constants.RandomActionType;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

/**
 * Q-Learning Agent Versão 3
 * 
 * @author Guisi
 *
 */
@Named("qLearningAgentV3")
@Scope("prototype")
public class QLearningAgentV3 extends Agent {
	
	private QTable qTable;
	private AgentState currentState;
	
	private boolean changedPolicy;
	private boolean isRadial;
	private double initialConfigurationRate;

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
		
		Environment environment = getInteractionEnvironment();

		this.currentState = getRandomAgentState(environment);

		this.initialConfigurationRate = getConfigRate(environment);
		this.isRadial = true;
	}
	
	private double getConfigRate(Environment environment) {
		//return environment.getSuppliedActivePowerPercentage();
		return environment.getSuppliedLoadsActivePowerVsPriorityPercentage();
	}
	
	/**
	 * Realiza uma interação no ambiente
	 */
	@Override
	protected void runNextEpisode() {
		Environment environment = getInteractionEnvironment();
		
		//reativa loads desativados no episódio anterior
		environment.turnOnAllLoads();
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= PropertiesUtils.getEGreedy());
		boolean proportional = RandomActionType.PSEUDO_RANDOM_PROPORTIONAL.name().equals(PropertiesUtils.getProperty(PropertyKey.RANDOM_ACTION));
		
		List<AgentAction> agentActions = getAgentActions(environment, this.currentState);

		AgentAction action = null;
		AgentAction bestAction = qTable.getBestAction(currentState, agentActions);
		
		if (randomAction) {
			action = qTable.getRandomAction(currentState, agentActions, proportional);
		} else {
			action = bestAction;
		}
		
		//efetiva a transição
		Map<Integer, SwitchStatus> clusterStatus = new HashMap<Integer, SwitchStatus>();
		for (Entry<Integer, SwitchStatus> entry : action.getSwitches().entrySet()) {
			Branch branch = environment.getBranch(entry.getKey());
			clusterStatus.put(branch.getNumber(), branch.getSwitchStatus());
			branch.setSwitchStatus(entry.getValue());
		}
		
		this.executePowerFlow(environment);
		
		//atualiza o qValue do switch
		this.updateQValue(environment, this.currentState, action);
		
		//verifica se mudou melhor ação
		AgentAction newBestAction = qTable.getBestAction(this.currentState, agentActions);
		this.changedPolicy = !bestAction.equals(newBestAction);
		
		//se escolheu ação aleatória e não mudou política, desfaz ação
		/*if (randomAction && !this.changedPolicy) {
			//reativa loads desativados
			turnedOffLoads.forEach(load -> load.turnOn());
			turnedOffLoads.clear();

			for (Entry<Integer, SwitchStatus> entry : clusterStatus.entrySet()) {
				Branch branch = environment.getBranch(entry.getKey());
				branch.setSwitchStatus(entry.getValue());
			}
			
			this.executePowerFlow(environment);
			
			this.currentState = new AgentState(action.getClusterNumber(), clusterStatus);
			
		} else {
			this.currentState = new AgentState(action.getClusterNumber(), action.getSwitches());
		}*/
		this.currentState = new AgentState(action.getClusterNumber(), action.getSwitches());
		
		//atualiza learning environment de acordo com aprendizado do agente
		this.updateNetworkFromLearning(getLearningEnvironment());
		
		//gera os dados do agente
		this.generateAgentData();
		
		//gera os dados dos ambientes
		this.generateEnvironmentData(EnvironmentKeyType.INTERACTION_ENVIRONMENT);
		this.generateEnvironmentData(EnvironmentKeyType.LEARNING_ENVIRONMENT);
	}
	
	private void executePowerFlow(Environment environment) {
		//primeiro valida se rede está radial
		List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
		
		this.isRadial = exceptions.isEmpty();
		
		if (isRadial) {
			//executa o fluxo de potência
			PowerFlow.execute(environment);
	
			//verifica loads a serem desativados caso existam restrições
			this.turnOffLoadsIfNecessary(environment);
		} else {
			//se estiver nao radial, desliga todos os loads para que o % de atendimento seja 0 
			environment.turnOffAllLoads();
		}
	}
	
	/**
	 * Desliga loads se existirem restrições
	 * @param environment
	 * @throws Exception
	 */
	private void turnOffLoadsIfNecessary(Environment environment) {
		if (NetworkRestrictionsTreatmentType.LOAD_SHEDDING.name().equals(PropertiesUtils.getProperty(PropertyKey.NETWORK_RESTRICTIONS_TREATMENT))) {
			
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
			}
		}
	}
	
	private long getFeederBrokenLoadsQuantity(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn() && load.hasBrokenConstraint()).count();
	}
	
	private List<Load> getFeederLoadsOn(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn()).collect(Collectors.toList());
	}
	
	private void updateNetworkFromLearning(Environment environment) {
		List<Cluster> clusters = environment.getClusters();
		
		//para cada cluster
		for (Cluster cluster : clusters) {
			
			//filtra pelos qValues cuja ação é alterar o cluster
			List<QValue> values = qTable.values().stream()
					.filter(qValue -> qValue.getAction().getClusterNumber().equals(cluster.getNumber())).collect(Collectors.toList());

			//cria um mapa somando as recompensas para cada possível combinação (action) deste cluster
			Map<AgentAction, Double> totals = new HashMap<>(); 
			for (QValue qValue : values) {
				AgentAction action = qValue.getAction();
				Double value = totals.get(action);
				if (value == null) {
					value = 0d;
				}
				value += qValue.getReward();
				totals.put(action, value);
			}
			
			Double max = totals.entrySet().stream().max(Comparator.comparing(entry -> entry.getValue())).get().getValue();
			
			List<Entry<AgentAction, Double>> bestActions = totals.entrySet().stream().filter(entry -> entry.getValue().equals(max)).collect(Collectors.toList());
			
			Entry<AgentAction, Double> bestAction = bestActions.get(new Random(System.currentTimeMillis()).nextInt(bestActions.size()));
			
			for (Entry<Integer, SwitchStatus> entry : bestAction.getKey().getSwitches().entrySet()) {
				Branch branch = environment.getBranch(entry.getKey());
				branch.setSwitchStatus(entry.getValue());
			}
		}
		
		this.executePowerFlow(environment);
	}
	
	private void generateAgentData() {
		AgentStepData agentStepData = new AgentStepData(getStep());
		getAgentData().getAgentStepData().add(agentStepData);
		
		 //trocou política
		agentStepData.putData(AgentDataType.CHANGED_POLICY, this.changedPolicy);
        
        //média dos valores Q
		agentStepData.putData(AgentDataType.QVALUES_AVERAGE, qTable.getQValuesAverage());
	}
	
	private void generateEnvironmentData(EnvironmentKeyType environmentKeyType) {
		Environment environment = SimuladorRede.getEnvironment(environmentKeyType);
		
		AgentStepData agentStepData = new AgentStepData(getStep());
		getAgentData().getEnvironmentStepData(environmentKeyType).add(agentStepData);
		
		//seta total de perdas
		agentStepData.putData(AgentDataType.ACTIVE_POWER_LOST, environment.getActivePowerLostMW());
		agentStepData.putData(AgentDataType.REACTIVE_POWER_LOST, environment.getReactivePowerLostMVar());
		
		//seta demanda atendida
		agentStepData.putData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER, environment.getSuppliedActivePowerDemandMW());
		agentStepData.putData(AgentDataType.SUPPLIED_LOADS_REACTIVE_POWER, environment.getSuppliedReactivePowerDemandMVar());
		
		//seta demanda não atendida
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER, environment.getNotSuppliedActivePowerDemandMW());
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_REACTIVE_POWER, environment.getNotSuppliedReactivePowerDemandMVar());
		
		//seta demanda desligada
		agentStepData.putData(AgentDataType.OUT_OF_SERVICE_LOADS_ACTIVE_POWER, environment.getOutOfServiceActivePowerDemandMW());
		agentStepData.putData(AgentDataType.OUT_OF_SERVICE_LOADS_REACTIVE_POWER, environment.getOutOfServiceReactivePowerDemandMVar());
		
		//seta soma das prioridades dos loads atendidos e não atendidos
		agentStepData.putData(AgentDataType.SUPPLIED_LOADS_VS_PRIORITY, environment.getSuppliedLoadsVsPriority());
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_VS_PRIORITY, environment.getNotSuppliedLoadsVsPriority());
		
		//seta soma das prioridades dos loads atendidos e não atendidos x potência ativa MW
		agentStepData.putData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getSuppliedLoadsActivePowerMWVsPriority());
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getNotSuppliedLoadsActivePowerMWVsPriority());
		
		//min load voltage pu
		agentStepData.putData(AgentDataType.MIN_LOAD_VOLTAGE_PU, environment.getMinLoadCurrentVoltagePU());
		
		 //nota da configuração da rede
		double configRate = getConfigRate(environment);
        agentStepData.putData(AgentDataType.ENVIRONMENT_REWARD, (initialConfigurationRate > 0) ? (configRate - initialConfigurationRate) / initialConfigurationRate : 0);
        
		//número de switches diferentes da rede inicial
        int differentSwitchStatesCount = EnvironmentUtils.countDifferentSwitchStates(environment, getInitialEnvironment());
        agentStepData.putData(AgentDataType.REQUIRED_SWITCH_OPERATIONS, differentSwitchStatesCount);
	}
	
	private void updateQValue(Environment environment, AgentState state, AgentAction action) {
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(state, action);
        
		double q = qValue.getReward();

		AgentState nextState = new AgentState(action.getClusterNumber(), action.getSwitches());
		List<AgentAction> nextAgentActions = getAgentActions(environment, nextState);
		
		//recupera em sua QTable o melhor valor para o estado para o qual se moveu
		QValue bestNextQValue = qTable.getBestQValue(nextState, nextAgentActions);
        double nextStateQ = bestNextQValue != null ? bestNextQValue.getReward() : 0;
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a ação
        double configRate = getConfigRate(environment);
        
        double r = (initialConfigurationRate > 0) ? (configRate - initialConfigurationRate) / initialConfigurationRate : 0;
        
        final double learningConstant = PropertiesUtils.getLearningConstant();
        final double discountFactor = PropertiesUtils.getDiscountFactor();
        
        //Algoritmo Q-Learning -> calcula o novo valor para o estado/ação que estava antes
        double value = q + learningConstant * (r + (discountFactor * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	@Override
	public AgentState getCurrentState() {
		return currentState;
	}
	
	private AgentState getRandomAgentState(Environment environment) {
		List<Cluster> clusters = environment.getClusters();
		
		List<AgentState> states = new ArrayList<>();
		for (Cluster cluster : clusters) {
			AgentState agentState = new AgentState(cluster.getNumber(), cluster.getSwitchesMap());
			states.add(agentState);
		}
		
		return states.get(new Random(System.currentTimeMillis()).nextInt(states.size()));
	}
	
	private static List<AgentAction> getAgentActions(Environment environment, AgentState agentState) {
		List<AgentAction> agentActions = new ArrayList<>();
		
		List<Cluster> clusters = environment.getClusters();
		for (Cluster cluster : clusters) {

			//todos fechados
			final Map<Integer, SwitchStatus> closedSwitchMap = cluster.getSwitchesMap();
			closedSwitchMap.keySet().forEach(key -> closedSwitchMap.put(key, SwitchStatus.CLOSED));
			agentActions.add(new AgentAction(cluster.getNumber(), closedSwitchMap));
			
			//uma combinação para cada branch do cluster onde este está aberto e os demais fechados
			List<Branch> clusterSws = cluster.getSwitches();
			for (Branch branch : clusterSws) {
				final Map<Integer, SwitchStatus> switchMap = cluster.getSwitchesMap();
				switchMap.keySet().forEach(key -> switchMap.put(key, SwitchStatus.CLOSED));
				switchMap.put(branch.getNumber(), SwitchStatus.OPEN);
				
				agentActions.add(new AgentAction(cluster.getNumber(), switchMap));
			}
		}
		
		//remove o estado atual das possiveis ações
		if (agentState != null) {
			agentActions.remove(new AgentAction(agentState.getClusterNumber(), agentState.getSwitches()));
		}
		
		return agentActions;
	}
	
	@Override
	public List<LearningState> getLearningStates() {
		List<LearningState> learningStates = new ArrayList<>();
		
		List<AgentAction> actions = getAgentActions(getInteractionEnvironment(), null);
		actions.forEach(action -> learningStates.add(new LearningState(action.getClusterNumber(), action.getSwitches())));
		
		return learningStates;
	}
	
	@Override
	public List<LearningPropertyPair> getLearningProperties(LearningState learningState, boolean onlyUpdated) {
		
		AgentState agentState = new AgentState(learningState.getClusterNumber(), learningState.getSwitches());
		List<QValue> qValues = qTable.getQValues(agentState);
		List<LearningProperty> learningProperties = mountLearningProperties(qValues, onlyUpdated);
		
		List<LearningPropertyPair> learningPropertyPairs = new ArrayList<>();
		for (Iterator<LearningProperty> iterator = learningProperties.iterator(); iterator.hasNext();) {
			LearningPropertyPair pair = new LearningPropertyPair();

			LearningProperty learningProperty = iterator.next();
			pair.setLearningProperty1(learningProperty);
			
			if (iterator.hasNext()) {
				learningProperty = iterator.next();
				pair.setLearningProperty2(learningProperty);
			}
			learningPropertyPairs.add(pair);
		}
		
		return learningPropertyPairs;
	}
	
	private List<LearningProperty> mountLearningProperties(List<QValue> qValues, boolean onlyUpdated) {
		List<LearningProperty> learningProperties = new ArrayList<>();
		
		if (onlyUpdated) {
			qValues = qValues.stream().filter(qValue -> qValue.isUpdated()).collect(Collectors.toList());
		}

		qValues.forEach(qValue -> {
			String action = "ClusterNumber=" + qValue.getAction().getClusterNumber() + ", " + qValue.getAction().getSwitches();
			
			BigDecimal value = new BigDecimal(qValue.getReward()).setScale(10, RoundingMode.HALF_UP);
			learningProperties.add(new LearningProperty(action + "):", value.toPlainString()));
		});
		
		Collections.sort(learningProperties, (LearningProperty o1, LearningProperty o2) -> {
			if (!o1.getValue().equals(o2.getValue())) {
				return o2.getValue().compareTo(o1.getValue());
			} else {
				return o1.getProperty().compareTo(o2.getProperty());
			}
		});
		
		return learningProperties;
	}
}
