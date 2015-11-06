package br.com.guisi.simulador.rede.qlearning;

import java.util.Observable;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchState;

public class QLearningAgent extends Observable {
	
	private Environment environment;
	private final QTable qTable;
	private QLearningStatus status;
	
	public QLearningAgent(Environment environment) {
		this.environment = environment;
		this.qTable = new QTable();
		this.status = new QLearningStatus();
		Integer initialState = environment.getRandomSwitch().getNumber();
		this.status.setInitialState(initialState);
		this.status.setLastState(initialState);
		this.status.setCurrentState(initialState);
	}
	
	/**
	 * Inicia a intera��o do agente
	 */
	public void run() {
		this.status.setLastState(this.status.getCurrentState());
		Integer state = nextEpisode(this.status.getCurrentState());
		this.status.setCurrentState(state);

		try {
			QLearningStatus status = (QLearningStatus) this.getStatus().clone();
			setChanged();
			notifyObservers(status);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Realiza uma intera��o no ambiente
	 * @param state
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	private Integer nextEpisode(Integer state) {
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		SwitchState action = randomAction ? SwitchState.getRandomAction() : qTable.getBestAction(state);
		
		//altera o estado do switch
		boolean changed = environment.changeSwitchState(state, action);
		
		//verifica no ambiente qual � o resultado de executar a a��o
		//ActionResult actionResult = environment.executeAction(state, action);
		
		//recupera estado para o qual se moveu (ser� o mesmo atual caso n�o pode realizar a a��o)
		Integer nextState = environment.getRandomSwitch().getNumber();//TODO actionResult.getNextState();
		
		//recupera em sua QTable o valor de recompensa para o estado/a��o que estava antes
		QValue qValue = qTable.getQValue(state, action);
        double q = qValue.getReward();
        
        //recupera em sua QTable o melhor valor para o estado para o qual se moveu
        double nextStateQ = qTable.getBestQValue(nextState).getReward();
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a a��o
        double r = 0;//TODO actionResult.getReward();

        //Algoritmo Q-Learning -> calcula o novo valor para o estado/a��o que estava antes
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
        
        //incrementa contagem de epis�dios
        //this.getStatus().incrementEpisodesCount();

        /*
        //verifica se mudou pol�tica, somente se a��o n�o foi aleat�ria
        boolean changedPolicy = false;
        if (!randomAction) {
        	Action newBestAction = qTable.getBestAction(state);
        	changedPolicy = !action.equals(newBestAction);
        	this.getStatus().incrementPolicyChangeCount();
        }
        
        //crit�rio de parada trata o fim de uma intera��o
        stoppingCriteria.handleEpisode(randomAction, changedPolicy);
        
        //se foi para o objetivo, vai para uma posi��o aleat�ria no ambiente
        if (environment.isTarget(nextState)) {
        	nextState = environment.getRandomInitialStateForAgent();
        }
        */
        return nextState;
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

	public QLearningStatus getStatus() {
		return status;
	}

	public void setStatus(QLearningStatus status) {
		this.status = status;
	}
}
