package br.com.guisi.simulador.rede.qlearning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchState;

public class QLearningAgent {
	
	private Environment environment;
	private final QTable qTable;
	
	public QLearningAgent(Environment environment) {
		this.environment = environment;
		this.qTable = new QTable();
	}
	
	/**
	 * Inicia a interação do agente
	 */
	public void run() {
		//State lastState = initialState;
		Integer initialState = nextEpisode(null);
	}
	
	/**
	 * Realiza uma interação no ambiente
	 * @param state
	 * @return {@link State} estado para o qual o agente se moveu
	 */
	private Integer nextEpisode(Integer state) {
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		SwitchState action = randomAction ? SwitchState.getRandomAction() : qTable.getBestAction(state);
		
		//verifica no ambiente qual é o resultado de executar a ação
		//ActionResult actionResult = environment.executeAction(state, action);
		
		//recupera estado para o qual se moveu (será o mesmo atual caso não pode realizar a ação)
		Integer nextState = 0;//TODO actionResult.getNextState();
		
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
}
