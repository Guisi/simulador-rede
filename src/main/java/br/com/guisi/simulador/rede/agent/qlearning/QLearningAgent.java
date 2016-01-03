package br.com.guisi.simulador.rede.agent.qlearning;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchState;

@Named
public class QLearningAgent extends Agent {
	
	private QTable qTable;
	private Integer currentState;

	@PostConstruct
	public void init() {
		this.qTable = new QTable();
	}
	
	/**
	 * Realiza uma interação no ambiente
	 * @param state
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	@Override
	protected void runNextEpisode() {
		Environment environment = SimuladorRede.getEnvironment();

		currentState = environment.getRandomSwitch().getNumber();
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		//TODO remover
		randomAction = true;
		
		SwitchState action = randomAction 
				? SwitchState.getRandomAction() 
						: qTable.getBestAction(currentState);
		
		//altera o estado do switch
		environment.reverseSwitch(currentState);
		
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
        qValue.setReward(value);
        
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
	
	@Override
	protected void putInformations(AgentStepStatus agentStepStatus) {
		agentStepStatus.putInformation(AgentInformationType.SWITCH_STATE_CHANGED, currentState);
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
}
