package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class QTable extends HashMap<AgentState, AgentActionMap> {

	private static final long serialVersionUID = 1L;

	/**
	 * Recupera a recompensa para o estado e ação passados na {@link QKey}
	 * @param qKey
	 * @return
	 */
	public QValue getQValue(AgentState state, AgentAction action) {
		AgentActionMap actionMap = get(state);
		if (actionMap == null) {
			actionMap = new AgentActionMap();
			put(state, actionMap);
		}
		QValue qValue = actionMap.get(action);
		if (qValue == null) {
			qValue = new QValue(state, action);
			actionMap.put(action, qValue);
		}
		return qValue;
	}
	
	/**
	 * Recupera a lista de recompensas para cada ação do estado passado
	 * @param state
	 * @return
	 */
	public List<QValue> getQValues(AgentState state) {
		List<QValue> qValues = null;
		AgentActionMap actionMap = get(state);
		if (actionMap != null) {
			qValues = new ArrayList<>(actionMap.values());
		}
		return qValues;
	}
	
	/**
	 * Retorna o melhor valor de recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public synchronized QValue getBestQValue(AgentState state) {
		QValue bestQ = null;

		//Recupera valores para o estado
		List<QValue> qValues = getQValues(state);
		if (qValues != null && !qValues.isEmpty()) {
			//Verifica o maior valor de recompensa
			double max = qValues.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
			
			//filtra por todas as acoes cuja recompensa seja igual a maior
			qValues = qValues.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
			
			//retorna uma das melhores acoes aleatoriamente
			bestQ = qValues.get(new Random(System.currentTimeMillis()).nextInt(qValues.size()));
		}
		
		return bestQ;
	}
	
	/**
	 * Retorna a acao com melhor recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public AgentAction getBestAction(AgentState state) {
		return this.getBestQValue(state).getAction();
	}
}
