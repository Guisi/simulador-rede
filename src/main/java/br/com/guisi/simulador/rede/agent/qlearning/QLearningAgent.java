package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchState;

@Named
public class QLearningAgent extends Agent {
	
	private QTable qTable;
	private Branch firstSwitch;
	private Branch secondSwitch;

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
	}
	
	/**
	 * Realiza uma interação no ambiente
	 * @param agentStepStatus
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	@Override
	protected void runNextEpisode(AgentStepStatus agentStepStatus) {
		Environment environment = SimuladorRede.getEnvironment();
		
		List<SwitchOperation> switchOperations = new ArrayList<>();
		
		//busca o switch a ser aberto
		firstSwitch = getClosestSwitch(environment, secondSwitch, SwitchState.CLOSED);
		if (!firstSwitch.hasFault()) {
			firstSwitch.reverse();
			switchOperations.add(new SwitchOperation(firstSwitch.getNumber(), firstSwitch.getSwitchState()));
		}
		
		//busca o switch a ser fechado
		secondSwitch = getClosestSwitch(environment, firstSwitch, SwitchState.OPEN);
		secondSwitch.reverse();
		switchOperations.add(new SwitchOperation(secondSwitch.getNumber(), secondSwitch.getSwitchState()));
		
		agentStepStatus.putInformation(AgentInformationType.SWITCH_OPERATIONS, switchOperations);

		/*currentState = environment.getRandomSwitch().getNumber();
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		//TODO remover
		randomAction = true;
		
		SwitchState action = randomAction 
				? SwitchState.getRandomAction() 
						: qTable.getBestAction(currentState);
		
		//altera o estado do switch
		environment.reverseSwitch(currentState);
		
		try {
			PowerFlow.execute(environment);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//verifica no ambiente qual é o resultado de executar a ação
		//ActionResult actionResult = environment.executeAction(state, action);
		
		//recupera estado para o qual se moveu (será o mesmo atual caso não pode realizar a ação)
		Integer nextState = environment.getRandomSwitch().getNumber();//TODO actionResult.getNextState();
		
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(currentState, action);
        double q = qValue.getReward();
        
        //recupera em sua QTable o melhor valor para o estado para o qual se moveu
        double nextStateQ = qTable.getBestQValue(nextState).getReward();
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a ação
        double r = 0;//TODO actionResult.getReward();

        //Algoritmo Q-Learning -> calcula o novo valor para o estado/ação que estava antes
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);*/
        
        //incrementa contagem de episódios
        //this.getStatus().incrementEpisodesCount();

        /*
        //verifica se mudou política, somente se ação não foi aleatória
        boolean changedPolicy = false;
        if (!randomAction) {
        	Action newBestAction = qTable.getBestAction(state);
        	changedPolicy = !action.equals(newBestAction);
        	this.getStatus().incrementPolicyChangeCount();
        }
        
        //critério de parada trata o fim de uma interação
        stoppingCriteria.handleEpisode(randomAction, changedPolicy);
        
        //se foi para o objetivo, vai para uma posição aleatória no ambiente
        if (environment.isTarget(nextState)) {
        	nextState = environment.getRandomInitialStateForAgent();
        }
        */
	}
	
	/**
	 * Busca o switch mais próximo ao switch passado
	 * @param environment
	 * @param refSwitch Switch de referência para busca do mais próximo 
	 * @param switchState Estado a ser utilizado como filtro na busca
	 * @return
	 */
	public Branch getClosestSwitch(Environment environment, Branch refSwitch, SwitchState switchState) {
		Branch sw = null;

		//se o estado atual está nulo, é a primeira interação
		if (refSwitch == null) {
			//verifica se existe alguma falta 
			sw = environment.getRandomFault();
			//se não existe, inicia por um switch aberto aleatório
			if (sw == null) {
				sw = environment.getRandomSwitch(switchState);
			}
		} else {
			//senão, busca o switch
			sw = environment.getClosestSwitch(refSwitch, switchState);
		}
		
		return sw;
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
