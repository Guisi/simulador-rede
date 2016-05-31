package br.com.guisi.simulador.rede.agent.qlearning.v2;

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
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.agent.data.LearningProperty;
import br.com.guisi.simulador.rede.agent.data.LearningPropertyPair;
import br.com.guisi.simulador.rede.agent.data.SwitchOperation;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.NetworkRestrictionsTreatmentType;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.constants.RandomActionType;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;
import br.com.guisi.simulador.rede.enviroment.SwitchDistance;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

/**
 * Q-Learning Agent Versão 2
 * 
 * Interage escolhendo um cluster (um conjunto de 5 switches onde o switch central é um tie-sw), 
 * fazendo a mudança de um par de switches dentro deste cluster, abrindo um switch e fechando o tie-sw
 * 
 * Modelo da tabela: S -> Switch Origem / Estado deste SW
 *                   A -> Switch Destino / Estado deste SW
 * A política do agente é dada por votação de cada SW Origem, qual o estado do SW destino tem maior valor na tabela Q
 * 
 * Para realizar a ação, o agente escolhe um switch fechado dentro de algum dos clusters para abrir, 
 * abre este switch e atualiza o aprendizado.
 * Em seguida escolhe um dos switches abertos dentro do mesmo cluster para fechar,
 * fecha este switch e atualiza o aprendizado
 * 
 * @author Guisi
 *
 */
@Named("qLearningAgentV2")
@Scope("prototype")
public class QLearningAgentV2 extends Agent {
	
	private QTable qTable;
	private Branch currentSwitch;
	private Set<Load> turnedOffLoads;
	
	private List<Cluster> clusters;
	private double initialConfigRate;
	private boolean changedPolicy;

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
		this.turnedOffLoads = new LinkedHashSet<>();
		
		Environment environment = getInteractionEnvironment();

		//verifica se existe alguma falta
		this.currentSwitch = environment.getRandomFault();
		//se não existe, inicia por um switch aberto aleatório
		if (this.currentSwitch == null) {
			this.currentSwitch = environment.getRandomSwitch();
		}
		
		this.initialConfigRate = getConfigRate(environment);
		
		this.clusters = this.getClusters(environment, this.currentSwitch);
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
	 */
	@Override
	protected void runNextEpisode() {
		Environment environment = getInteractionEnvironment();
		
		//reativa loads desativados no episódio anterior
		turnedOffLoads.forEach(load -> load.turnOn());
		turnedOffLoads.clear();
		
		//se o switch atual está aberto ou é falta, seleciona switch fechado para abrir em algum cluster
		if (currentSwitch.isOpen()) {
			
		} else {
			
		}
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		/*boolean randomAction = (Math.random() >= PropertiesUtils.getEGreedy());
		
		AgentState currentState = new AgentState(currentSwitch.getNumber(), currentSwitch.getSwitchStatus());
		
		//guarda melhor ação antes de atualiza tabela Q para verificar se mudou política
		AgentAction previousBestAction = qTable.getBestAction(currentState, candidateSwitches);

		AgentAction action = null;
		if (randomAction) {
			boolean proportional = RandomActionType.PSEUDO_RANDOM_PROPORTIONAL.name().equals(PropertiesUtils.getProperty(PropertyKey.RANDOM_ACTION));
			action = qTable.getRandomAction(currentState, candidateSwitches, proportional);
		} else {
			action = previousBestAction;
		}
		
		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		nextSwitch.reverse();

		//executa o fluxo de potência
		PowerFlow.execute(environment);

		//verifica loads a serem desativados caso existam restrições
		this.turnOffLoadsIfNecessary(environment);
		
		//atualiza o qValue do switch
		this.updateQValue(environment, currentState, action);
		
		//verifica se mudou política
		AgentAction newBestAction = qTable.getBestAction(currentState, candidateSwitches);
		this.changedPolicy = !previousBestAction.equals(newBestAction);
		
		this.currentSwitch = nextSwitch;
		
		//atualiza a rede de aprendizado conforme política do agente
		this.updateNetworkFromLearning(getLearningEnvironment());
		
		//gera os dados do agente
		this.generateAgentData();
		
		//gera os dados dos ambientes
		this.generateEnvironmentData(EnvironmentKeyType.INTERACTION_ENVIRONMENT);
		this.generateEnvironmentData(EnvironmentKeyType.LEARNING_ENVIRONMENT);*/
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
				
				this.turnedOffLoads.addAll(turnedOffLoads);
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
		//monta uma lista com os números dos switches abertos/fechados
		List<Integer> switchNumbers = new ArrayList<>();
		environment.getSwitches().forEach(sw -> {
			if (sw.isOpen() || sw.isClosed()) {
				switchNumbers.add(sw.getNumber());				
			}
		});
		
		environment.getSwitches().forEach(sw -> {
			if (sw.isOpen() || sw.isClosed()) {
				//HEURISTICA - não pode abrir o switch mais próximo ao feeder
				if (sw.getSwitchIndex() == 1) {
					sw.setSwitchStatus(SwitchStatus.CLOSED);
				} else {
					SwitchStatus status = qTable.getBestSwitchStatus(sw.getNumber(), switchNumbers);
					sw.setSwitchStatus(status);
				}
			}
		});
		
		//primeiro valida se rede está radial
		List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
		
		if (exceptions.isEmpty()) {
			//executa o fluxo de potência
			PowerFlow.execute(environment);
			
			//verifica loads a serem desativados caso existam restrições 
			this.turnOffLoadsIfNecessary(environment);
		}
	}
	
	private void generateAgentData() {
		AgentStepData agentStepData = new AgentStepData(getStep());
		getAgentData().getAgentStepData().add(agentStepData);
		
		//atualiza status com o switch alterado
		agentStepData.putData(AgentDataType.SWITCH_OPERATION, new SwitchOperation(currentSwitch.getNumber(), currentSwitch.getSwitchStatus()));
		
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
		if (currentSwitch.isClosed()) {
	        double configRate = getConfigRate(environment);
	        agentStepData.putData(AgentDataType.ENVIRONMENT_REWARD, (initialConfigRate > 0) ? (configRate - initialConfigRate) / initialConfigRate : 0);
		}
        
		//número de switches diferentes da rede inicial
        int differentSwitchStatesCount = EnvironmentUtils.countDifferentSwitchStates(environment, getInitialEnvironment());
        agentStepData.putData(AgentDataType.REQUIRED_SWITCH_OPERATIONS, differentSwitchStatesCount);
	}
	
	private void updateQValue(Environment environment, AgentState state, AgentAction action) {
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(state, action);
        
		double q = qValue.getReward();
        
        //recupera os possíveis candidatos para a próxima iteração
		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		//List<CandidateSwitch> candidateSwitches = this.getCandidateSwitches(environment, nextSwitch, action.getSwitchStatus());

		//recupera em sua QTable o melhor valor para o estado para o qual se moveu
		AgentState nextState = new AgentState(action.getSwitchNumber(), action.getSwitchStatus());
		QValue bestNextQValue = qTable.getBestQValue(nextState, null);
        double nextStateQ = bestNextQValue != null ? bestNextQValue.getReward() : 0;
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a ação
        double configRate = getConfigRate(environment);
        
        double r = initialConfigRate > 0 ? (configRate - initialConfigRate) / initialConfigRate : 0;
        
        final double learningConstant = PropertiesUtils.getLearningConstant();
        final double discountFactor = PropertiesUtils.getDiscountFactor();
        
        //Algoritmo Q-Learning -> calcula o novo valor para o estado/ação que estava antes
        double value = q + learningConstant * (r + (discountFactor * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	private List<Cluster> getClusters(Environment environment, Branch currentSwitch) {
		List<Cluster> clusters = new ArrayList<>();
		
		List<Branch> tieSwitches = environment.getTieSwitches();
		
		//ordena pelos switches em que um dos nodes não esteja energizado, e o node energizado esteja no feeder com menor demanda
		Collections.sort(tieSwitches, new Comparator<Branch>() {
			@Override
			public int compare(Branch branch1, Branch branch2) {
				Load loadFrom1 = (Load)branch1.getNodeFrom();
				Load loadTo1 = (Load)branch1.getNodeTo();
				
				Load loadFrom2 = (Load)branch2.getNodeFrom();
				Load loadTo2 = (Load)branch2.getNodeTo();
				
				if ( (loadFrom1.getFeeder() == null || loadTo1.getFeeder() == null) && (loadFrom2.getFeeder() != null && loadTo2.getFeeder() != null) ) {
					return -1;
				} else if ( (loadFrom2.getFeeder() == null || loadTo2.getFeeder() == null) && (loadFrom1.getFeeder() != null && loadTo1.getFeeder() != null) ) {
					return 1;
				} else if (loadFrom1.getFeeder() == null || loadTo1.getFeeder() == null || loadFrom2.getFeeder() == null || loadTo2.getFeeder() == null) {
					Feeder feeder1 = loadFrom1.getFeeder() != null ? loadFrom1.getFeeder() : loadTo1.getFeeder();
					Feeder feeder2 = loadFrom2.getFeeder() != null ? loadFrom2.getFeeder() : loadTo2.getFeeder();
					
					return feeder1.getUsedActivePowerMW() < feeder2.getUsedActivePowerMW() ? -1 : 1;
				}
				return 0;
			}
		});
		
		//para cada tie-sw, escolhe os switches fechados próximos para criar o cluster
		for (Branch tieSw : tieSwitches) {
			//busca switches próximos
			List<SwitchDistance> switchDistances = environment.getClosedSwitches(tieSw, 2);
			
			//mantém somente 4 switches na lista
			while (switchDistances.size() > 4) {
				Integer max = switchDistances.stream().max(Comparator.comparing(sw -> sw.getDistance())).get().getDistance();
				List<SwitchDistance> switchesToRemove = switchDistances.stream().filter(sw -> sw.getDistance().equals(max)).collect(Collectors.toList());
				SwitchDistance switchToRemove = switchesToRemove.get(new Random(System.currentTimeMillis()).nextInt(switchesToRemove.size()));
				switchDistances.remove(switchToRemove);
			}
			
			//marca como participantes do cluster
			switchDistances.forEach(sw -> sw.getTheSwitch().setInCluster(true));
			
			Cluster cluster = new Cluster();
			cluster.setTieSwitchNumber(tieSw.getNumber());
			cluster.setSwitches(new ArrayList<>());
			cluster.getSwitches().add(tieSw);
			switchDistances.forEach(sd -> cluster.getSwitches().add(sd.getTheSwitch()));
			clusters.add(cluster);
		}
		
		return clusters;
	}
	

	
	@Override
	public Branch getCurrentState() {
		return currentSwitch;
	}
	
	@Override
	public List<LearningPropertyPair> getLearningProperties(Integer switchNumber, boolean onlyUpdated) {
		List<LearningPropertyPair> learningPropertyPairs = new ArrayList<>();
		
		List<QValue> qValuesOpen = qTable.getQValues(new AgentState(switchNumber, SwitchStatus.OPEN));
		List<LearningProperty> learningProperties = mountLearningProperties(qValuesOpen, onlyUpdated);
		learningProperties.forEach(learningProperty -> {
			LearningPropertyPair pair = new LearningPropertyPair();
			pair.setLearningProperty1(learningProperty);
			learningPropertyPairs.add(pair);
		});
		
		List<QValue> qValuesClosed = qTable.getQValues(new AgentState(switchNumber, SwitchStatus.CLOSED));
		learningProperties = mountLearningProperties(qValuesClosed, onlyUpdated);
		
		int i = 0;
		for (LearningProperty learningProperty : learningProperties) {
			LearningPropertyPair pair;
			if (i < learningPropertyPairs.size()) {
				pair = learningPropertyPairs.get(i);
			} else {
				pair = new LearningPropertyPair();
				learningPropertyPairs.add(pair);
			}
			pair.setLearningProperty2(learningProperty);
			i++;
		};

		return learningPropertyPairs;
	}
	
	private List<LearningProperty> mountLearningProperties(List<QValue> qValues, boolean onlyUpdated) {
		List<LearningProperty> learningProperties = new ArrayList<>();
		
		if (onlyUpdated) {
			qValues = qValues.stream().filter(qValue -> qValue.isUpdated()).collect(Collectors.toList());
		}

		qValues.forEach(qValue -> {
			String state = String.format("%02d", qValue.getState().getSwitchNumber()) + "/" + qValue.getState().getSwitchStatus().getDescription();
			String action = String.format("%02d", qValue.getAction().getSwitchNumber()) + "/" + qValue.getAction().getSwitchStatus().getPastTenseDescription();
			
			BigDecimal value = new BigDecimal(qValue.getReward()).setScale(10, RoundingMode.HALF_UP);
			learningProperties.add(new LearningProperty("Q(" + state + ", " + action + "):", value.toPlainString()));
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
