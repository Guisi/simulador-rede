package br.com.guisi.simulador.rede.agent.qlearning;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.annotations.QLearning;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchState;

@QLearning
public class QLearningAgent extends Agent {
	
	private QTable qTable;
	private Integer initialState;
	
	@Override
	public void init() {
		super.init();
		
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
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		SwitchState action = randomAction ? SwitchState.getRandomAction() : qTable.getBestAction(state);
		
		//altera o estado do switch
		boolean changed = environment.changeSwitchState(state, action);
		if (changed) {
			this.status.getSwitchesChanged().add(state);
		}
		
		//verifica no ambiente qual é o resultado de executar a ação
		//ActionResult actionResult = environment.executeAction(state, action);
		
		//recupera estado para o qual se moveu (será o mesmo atual caso não pode realizar a ação)
		Integer nextState = environment.getRandomSwitch().getNumber();//TODO actionResult.getNextState();
		
		//recupera em sua QTable o valor de recompensa para o estado/ação que estava antes
		QValue qValue = qTable.getQValue(state, action);
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
	protected void setNotifications() {
		
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
