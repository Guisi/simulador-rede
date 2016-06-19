package br.com.guisi.simulador.rede.agent.qlearning.v3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
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
	private Branch currentSwitch;
	private Set<Load> turnedOffLoads;
	
	private double lastIterationConfigRate;
	private double lastLearningConfigRate;
	private boolean changedPolicy;
	private Cluster currentCluster;
	private boolean isRadial;

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
		
		this.lastIterationConfigRate = getConfigRate(environment);
		this.lastLearningConfigRate = this.lastIterationConfigRate;
		this.isRadial = true;
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
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= PropertiesUtils.getEGreedy());
		boolean proportional = RandomActionType.PSEUDO_RANDOM_PROPORTIONAL.name().equals(PropertiesUtils.getProperty(PropertyKey.RANDOM_ACTION));
		
		AgentState currentState = new AgentState(currentSwitch.getNumber(), currentSwitch.getSwitchStatus());
		
		AgentAction action = null;
		Branch nextSwitch = null;
		final List<Branch> candidateSwitches = this.getCandidateSwitches(environment.getClusters(), this.currentSwitch, this.currentCluster);
		
		//se o switch atual está fechado ou é falta, seleciona switch aberto para fechar em algum cluster
		if (currentSwitch.hasFault() || isRadial) {
			if (randomAction) {
				action = qTable.getRandomAction(currentState, candidateSwitches, proportional);
			} else {
				action = qTable.getBestAction(currentState, candidateSwitches);
			}
			
			nextSwitch = environment.getBranch(action.getSwitchNumber());
			nextSwitch.reverse();
			
			//guarda o cluster atual
			this.currentCluster = nextSwitch.getCluster();
		} else {
			if (randomAction) {
				action = qTable.getRandomAction(currentState, candidateSwitches, proportional);
			} else {
				action = qTable.getBestAction(currentState, candidateSwitches);
			}
			
			nextSwitch = environment.getBranch(action.getSwitchNumber());
			nextSwitch.reverse();
		}
		
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
			environment.getLoads().forEach(load -> {
				this.turnedOffLoads.add(load);
				load.turnOff();
			});
		}
		
		//atualiza o qValue do switch
		this.updateQValue(environment, currentState, action);
		
		this.currentSwitch = nextSwitch;
		
		//atualiza a rede de aprendizado conforme política do agente
		this.updateNetworkFromLearning(getLearningEnvironment());
		
		//gera os dados do agente
		this.generateAgentData();
		
		//gera os dados dos ambientes
		this.generateEnvironmentData(EnvironmentKeyType.INTERACTION_ENVIRONMENT, lastIterationConfigRate);
		this.generateEnvironmentData(EnvironmentKeyType.LEARNING_ENVIRONMENT, lastLearningConfigRate);
		
		//this.lastIterationConfigRate = getConfigRate(getInteractionEnvironment());
		//this.lastLearningConfigRate = getConfigRate(getLearningEnvironment());
	}
	
	private List<Branch> getCandidateSwitches(List<Cluster> clusters, Branch currentSwitch, Cluster currentCluster) {
		List<Branch> candidateSwitches = new ArrayList<>();
		
		if (currentSwitch.hasFault() || isRadial) {
			//monta lista dos switches abertos
			clusters.forEach(cluster -> {
				candidateSwitches.addAll(cluster.getSwitches().stream().filter(branch -> branch.isOpen()).collect(Collectors.toList()));
			});
		} else {
			//monta lista dos switches fechados do cluster como candidatos
			candidateSwitches.addAll( currentCluster.getSwitches().stream().filter(branch -> branch.isClosed()).collect(Collectors.toList()) );
		}
		
		return candidateSwitches;
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
		//monta uma lista com os números dos switches abertos/fechados dos clusters
		List<Integer> switchNumbers = new ArrayList<>();
		
		environment.getClusters().forEach(cluster -> {
			cluster.getSwitches().forEach(sw -> {
				if (sw.isOpen() || sw.isClosed()) {
					switchNumbers.add(sw.getNumber());				
				}
			});
		});
		
		this.changedPolicy = false;
		environment.getClusters().forEach(cluster -> {
			cluster.getSwitches().forEach(sw -> {
				if (sw.isOpen() || sw.isClosed()) {
					SwitchStatus status = qTable.getBestSwitchStatus(sw.getNumber(), switchNumbers);
					
					if (!sw.getSwitchStatus().equals(status)) {
						this.changedPolicy = true;
					}

					sw.setSwitchStatus(status);
				}
			});
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
	
	private void generateEnvironmentData(EnvironmentKeyType environmentKeyType, double lastConfigRate) {
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
	        agentStepData.putData(AgentDataType.ENVIRONMENT_REWARD, (lastConfigRate > 0) ? (configRate - lastConfigRate) / lastConfigRate : 0);
		}
        
		//número de switches diferentes da rede inicial
        int differentSwitchStatesCount = EnvironmentUtils.countDifferentSwitchStates(environment, getInitialEnvironment());
        agentStepData.putData(AgentDataType.REQUIRED_SWITCH_OPERATIONS, differentSwitchStatesCount);
	}
	
	private void updateQValue(Environment environment, AgentState state, AgentAction action) {
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(state, action);
        
		double q = qValue.getReward();

		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		List<Branch> candidateSwitches = this.getCandidateSwitches(environment.getClusters(), nextSwitch, this.currentCluster);
		
		//recupera em sua QTable o melhor valor para o estado para o qual se moveu
		AgentState nextState = new AgentState(action.getSwitchNumber(), action.getSwitchStatus());
		QValue bestNextQValue = qTable.getBestQValue(nextState, candidateSwitches);
        double nextStateQ = bestNextQValue != null ? bestNextQValue.getReward() : 0;
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a ação
        double configRate = getConfigRate(environment);
        
        double r = (lastIterationConfigRate > 0) ? (configRate - lastIterationConfigRate) / lastIterationConfigRate : 0;
        		//(configRate - lastIterationConfigRate) / 100;
        
        final double learningConstant = PropertiesUtils.getLearningConstant();
        final double discountFactor = PropertiesUtils.getDiscountFactor();
        
        //Algoritmo Q-Learning -> calcula o novo valor para o estado/ação que estava antes
        double value = q + learningConstant * (r + (discountFactor * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
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
