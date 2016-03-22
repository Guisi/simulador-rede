package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.SwitchDistance;


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
	 * Retorna uma lista de {@link QValueEvaluator} contendo os valores para as transições passadas
	 * @param state Estado onde o agente se encontra
	 * @param switchesDistances Distâncias para transições possíveis
	 * @return
	 */
	private List<QValueEvaluator> getQValues(AgentState state, List<SwitchDistance> switchesDistances) {
		List<QValueEvaluator> qValues = new ArrayList<>();
		for (SwitchDistance switchDistance : switchesDistances) {
			Branch nextSwitch = switchDistance.getTheSwitch();
			AgentAction action = new AgentAction(nextSwitch.getNumber(), nextSwitch.getReverseStatus());
			
			QValueEvaluator evaluator = new QValueEvaluator();
			evaluator.setQValue(getQValue(state, action));
			evaluator.setDistance(switchDistance.getDistance());
			qValues.add(evaluator);
		}
		return qValues;
	}
	
	/**
	 * Retorna o melhor valor de recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public synchronized QValue getBestQValue(AgentState state, List<SwitchDistance> switchesDistances) {
		List<QValueEvaluator> qValues = this.getQValues(state, switchesDistances);

		//Verifica o maior valor de recompensa
		double max = qValues.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
		
		//filtra por todas as acoes cuja recompensa seja igual a maior
		qValues = qValues.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
		
		//retorna uma das melhores acoes aleatoriamente
		QValueEvaluator evaluator = qValues.get(new Random(System.currentTimeMillis()).nextInt(qValues.size()));
		
		return evaluator.getQValue();
	}
	
	/**
	 * Retorna a acao com melhor recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public AgentAction getBestAction(AgentState state, List<SwitchDistance> switchesDistances) {
		return this.getBestQValue(state, switchesDistances).getAction();
	}
	
	/**
	 * Retorna uma ação aleatória
	 * @param state
	 * @param switchesDistances
	 * @return
	 */
	public AgentAction getRandomAction(AgentState state, List<SwitchDistance> switchesDistances, boolean proportional) {
		//se é randômico proporcional
		if (proportional) {
			List<QValueEvaluator> qValues = this.getQValues(state, switchesDistances);
			
			boolean hasNegativeQ = qValues.stream().anyMatch(value -> value.getReward() < 0);
			
			//se possui valor Q negativo, faz ajuste para que todos fiquem positivos
			if (hasNegativeQ) {
				double minQ = qValues.stream().min(Comparator.comparing(qValue -> qValue.getReward())).get().getReward();
				
				double average = qValues.stream().mapToDouble(value -> value.getReward()).average().getAsDouble();
				
				double adjustment = Math.abs(minQ) + Math.abs(average);
				
				qValues.forEach(qValue -> qValue.setRewardAdjustment(adjustment));
			}
		} else {
			//se não, escolhe um dos candidatos aleatoriamente
			SwitchDistance switchDistance = switchesDistances.get(new Random(System.currentTimeMillis()).nextInt(switchesDistances.size()));
		
			return new AgentAction(switchDistance.getTheSwitch().getNumber(), switchDistance.getTheSwitch().getReverseStatus());
		}
	}
}
