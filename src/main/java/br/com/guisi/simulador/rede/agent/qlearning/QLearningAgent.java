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
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.agent.data.LearningProperty;
import br.com.guisi.simulador.rede.agent.data.SwitchOperation;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
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
	private boolean changedPolicy;
	private boolean isSameState;
	

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
		this.turnedOffLoads = new LinkedHashSet<>();
		this.isSameState = false;
		
		Environment environment = getInteractionEnvironment();

		//verifica se existe alguma falta
		this.currentSwitch = environment.getRandomFault();
		//se n�o existe, inicia por um switch aberto aleat�rio
		if (this.currentSwitch == null) {
			this.currentSwitch = environment.getRandomSwitch();
		}
		
		this.initialConfigRate = getConfigRate(environment);
	}
	
	private double getConfigRate(Environment environment) {
		/*double activePowerLossPercentage = environment.getActivePowerLostPercentage();
        double suppliedActivePowerPercentage = environment.getSuppliedActivePowerPercentage();
		return (100 - activePowerLossPercentage) * suppliedActivePowerPercentage;*/
		
		double suppliedActivePowerPercentage = environment.getSuppliedActivePowerPercentage();
		return suppliedActivePowerPercentage;
	}
	
	/**
	 * Realiza uma intera��o no ambiente
	 */
	@Override
	protected void runNextEpisode() {
		Environment environment = getInteractionEnvironment();
		
		//reativa loads desativados no epis�dio anterior
		turnedOffLoads.forEach(load -> load.turnOn());
		turnedOffLoads.clear();
		
		//se o switch atual est� fechado, vai procurar switch fechado para abrir, sen�o ir� procurar switch aberto para fechar
		SwitchStatus switchStatus = currentSwitch.isClosed() ? SwitchStatus.CLOSED : SwitchStatus.OPEN;
		
		//Retorna uma lista com os switches candidatos com respectivas dist�ncias a partir do switch atual, conforme status de switch procurado
		List<CandidateSwitch> candidateSwitches = this.getCandidateSwitches(environment, currentSwitch, switchStatus);
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		AgentState currentState = new AgentState(currentSwitch.getNumber(), currentSwitch.getSwitchStatus());
		
		//guarda melhor a��o antes de atualiza tabela Q para verificar se mudou pol�tica
		AgentAction previousBestAction = qTable.getBestAction(currentState, candidateSwitches);

		AgentAction action = null;
		if (randomAction) {
			action = qTable.getRandomAction(currentState, candidateSwitches, true); //TODO criar campo na tela para passar op��o de randomico proporcional
		} else {
			action = previousBestAction;
		}
		
		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		nextSwitch.reverse();

		//executa o fluxo de pot�ncia
		PowerFlow.execute(environment);

		//verifica loads a serem desativados caso existam restri��es 
		this.turnOffLoadsIfNecessary(environment);
		
		//atualiza o qValue do switch
		this.updateQValue(environment, currentState, action);
		
		//verifica se mudou pol�tica
		AgentAction newBestAction = qTable.getBestAction(currentState, candidateSwitches);
		this.changedPolicy = !previousBestAction.equals(newBestAction);
		
		//HEUR�STICA - se o agente permaneceu no mesmo switch, na pr�xima itera��o dever� ir para um switch diferente
		this.isSameState = this.currentSwitch.equals(nextSwitch);
		
		this.currentSwitch = nextSwitch;
		
		//gera os dados do agente
		this.generateAgentData();
		
		//gera os dados dos ambientes
		this.generateEnvironmentData(EnvironmentKeyType.INTERACTION_ENVIRONMENT);
		this.generateEnvironmentData(EnvironmentKeyType.LEARNING_ENVIRONMENT);
	}
	
	/**
	 * Desliga loads se existirem restri��es
	 * @param environment
	 * @throws Exception
	 */
	private void turnOffLoadsIfNecessary(Environment environment) {
		
		for (Feeder feeder : environment.getFeeders()) {
			List<Load> turnedOffLoads = new ArrayList<>();
			
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
			
			this.turnedOffLoads.addAll(turnedOffLoads);
		}
	}
	
	private long getFeederBrokenLoadsQuantity(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn() && load.hasBrokenConstraint()).count();
	}
	
	private List<Load> getFeederLoadsOn(Feeder feeder) {
		return feeder.getServedLoads().stream().filter((load) -> load.isOn()).collect(Collectors.toList());
	}
	
	private void generateAgentData() {
		AgentStepData agentStepData = new AgentStepData(getStep());
		getAgentData().getAgentStepData().add(agentStepData);
		
		//atualiza status com o switch alterado
		agentStepData.putData(AgentDataType.SWITCH_OPERATION, new SwitchOperation(currentSwitch.getNumber(), currentSwitch.getSwitchStatus()));
		
		 //trocou pol�tica
		agentStepData.putData(AgentDataType.CHANGED_POLICY, this.changedPolicy);
        
        //m�dia dos valores Q
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
		
		//seta demanda n�o atendida
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER, environment.getNotSuppliedActivePowerDemandMW());
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_REACTIVE_POWER, environment.getNotSuppliedReactivePowerDemandMVar());
		
		//seta demanda desligada
		agentStepData.putData(AgentDataType.OUT_OF_SERVICE_LOADS_ACTIVE_POWER, environment.getOutOfServiceActivePowerDemandMW());
		agentStepData.putData(AgentDataType.OUT_OF_SERVICE_LOADS_REACTIVE_POWER, environment.getOutOfServiceReactivePowerDemandMVar());
		
		//seta soma das prioridades dos loads atendidos e n�o atendidos
		agentStepData.putData(AgentDataType.SUPPLIED_LOADS_VS_PRIORITY, environment.getSuppliedLoadsVsPriority());
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_VS_PRIORITY, environment.getNotSuppliedLoadsVsPriority());
		
		//seta soma das prioridades dos loads atendidos e n�o atendidos x pot�ncia ativa MW
		agentStepData.putData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getSuppliedLoadsActivePowerMWVsPriority());
		agentStepData.putData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, environment.getNotSuppliedLoadsActivePowerMWVsPriority());
		
		//min load current voltage pu
		agentStepData.putData(AgentDataType.MIN_LOAD_CURRENT_VOLTAGE_PU, environment.getMinLoadCurrentVoltagePU());
		
		 //nota da configura��o da rede
		if (currentSwitch.isClosed()) {
	        double configRate = getConfigRate(environment);
	        agentStepData.putData(AgentDataType.ENVIRONMENT_CONFIGURATION_RATE, (initialConfigRate > 0) ? (configRate - initialConfigRate) / initialConfigRate : 0);
		}
        
		//n�mero de switches diferentes da rede inicial
        int differentSwitchStatesCount = EnvironmentUtils.countDifferentSwitchStates(environment, getInitialEnvironment());
        agentStepData.putData(AgentDataType.REQUIRED_SWITCH_OPERATIONS, differentSwitchStatesCount);
	}
	
	private void updateQValue(Environment environment, AgentState state, AgentAction action) {
		//recupera em sua QTable o valor de recompensa para o estado/a��o que estava antes
		QValue qValue = qTable.getQValue(state, action);
        
		double q = qValue.getReward();
        
        //recupera os poss�veis candidatos para a pr�xima itera��o
		Branch nextSwitch = environment.getBranch(action.getSwitchNumber());
		List<CandidateSwitch> candidateSwitches = this.getCandidateSwitches(environment, nextSwitch, action.getSwitchStatus());

		//recupera em sua QTable o melhor valor para o estado para o qual se moveu
		AgentState nextState = new AgentState(action.getSwitchNumber(), action.getSwitchStatus());
		QValue bestNextQValue = qTable.getBestQValue(nextState, candidateSwitches);
        double nextStateQ = bestNextQValue != null ? bestNextQValue.getReward() : 0;
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a a��o
        double configRate = getConfigRate(environment);
        
        double r = initialConfigRate > 0 ? (configRate - initialConfigRate) / initialConfigRate : 0;
        
        //Algoritmo Q-Learning -> calcula o novo valor para o estado/a��o que estava antes
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	/**
	 * Busca uma lista com os switches candidatos
	 * @param environment
	 * @param refSwitch switch onde o agente est� atualmente
	 * @param switchStatus status do switch a ser procurado
	 * @return
	 */
	public List<CandidateSwitch> getCandidateSwitches(Environment environment, Branch refSwitch, SwitchStatus switchStatus) {
		//busca a lista das dist�ncias dos switches
		List<SwitchDistance> switchesDistances = environment.getSwitchesDistances(refSwitch, switchStatus);
		
		List<CandidateSwitch> candidateSwitches = new ArrayList<>();
		
		for (SwitchDistance switchDistance : switchesDistances) {
			boolean ignore = false;
			
			//HEURISTICA - n�o pode abrir o switch mais pr�ximo ao feeder
			if (switchDistance.getTheSwitch().getSwitchIndex() == 1) {
				ignore = true;
			} else {
				NetworkNode node1 = switchDistance.getTheSwitch().getNodeFrom(); 
				NetworkNode node2 = switchDistance.getTheSwitch().getNodeTo();
				
				if (node1.isLoad() && node2.isLoad()) {
					Load load1 = (Load) node1;
					Load load2 = (Load) node2;
					
					//HEURISTICA - em regi�es de ilha n�o s�o feitas opera��es de switch
					//remove os switches que ligam dois loads que n�o estejam ligados a nenhum feeder, pois sabe-se que n�o ir�o gerar uma melhoria na rede
					if (load1.getFeeder() == null && load2.getFeeder() == null) {
						ignore = true;	
					} else if (switchStatus == SwitchStatus.OPEN && load1.getFeeder() != null && load2.getFeeder() != null) {
						//caso esteja procurando sw para fechar, remove os switches que ligam dois loads onde ambos est�o ligados a algum feeder, para evitar criar circuitos fechados
						ignore = true;
					}
					
				} else {
					if (switchStatus == SwitchStatus.OPEN && 
							((node1.isLoad() && ((Load)node1).getFeeder() != null && node2.isFeeder()) || (node2.isLoad() && ((Load)node2).getFeeder() != null && node1.isFeeder())) ) {
						//remove tamb�m switches onde uma das pontas � um load que j� est� conectado a um feeder, e a outra ponta possui um feeder, para evitar circuitos fechados
						ignore = true;
					}
				}
			}
			
			if (!ignore) {
				candidateSwitches.add(new CandidateSwitch(switchDistance.getDistance(), 
						switchDistance.getTheSwitch().getNumber(), switchDistance.getTheSwitch().getReverseStatus()));
			}
		};
			
		//adiciona o pr�prio switch como op��o, caso n�o seja uma falta
		if (!this.isSameState && refSwitch.isClosed() || refSwitch.isOpen()) {
			Integer distance = switchesDistances.isEmpty() ? 0 : switchesDistances.get(0).getDistance();
			candidateSwitches.add(0, new CandidateSwitch(distance, refSwitch.getNumber(), refSwitch.getReverseStatus()));
		}
			
		//Se n�o existe nenhum switch aberto candidato ou somente o pr�prio switch onde o agente est�,
		//ir� retornar uma lista de switches fechados candidatos usando como refer�ncia um dos switches abertos que n�o podiam ser fechados
		if (switchStatus == SwitchStatus.OPEN && candidateSwitches.size() <= 1) {
			Integer minDistance = switchesDistances.stream().min(Comparator.comparing(value -> value.getDistance())).get().getDistance();
			
			//filtra por todos os switches da lista com a menor dist�ncia
			List<SwitchDistance> switchesMin = switchesDistances.stream().filter(valor -> valor.getDistance() == minDistance).collect(Collectors.toList());
			
			//retorna um dos switches mais pr�ximos aleatoriamente
			Branch sw = switchesMin.get(RANDOM.nextInt(switchesMin.size())).getTheSwitch();

			candidateSwitches = getCandidateSwitches(environment, sw, SwitchStatus.CLOSED);
		}

		return candidateSwitches;
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
