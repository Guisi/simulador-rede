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
	 * Retorna uma lista de {@link QValueEvaluator} contendo os valores para as transições passadas
	 * @param state Estado onde o agente se encontra
	 * @param candidateSwitches Transições possíveis
	 * @return
	 */
	private List<QValueEvaluator> getQValuesEvaluators(AgentState state, List<CandidateSwitch> candidateSwitches) {
		List<QValueEvaluator> qValues = new ArrayList<>();
		for (CandidateSwitch candidateSwitch : candidateSwitches) {
			AgentAction action = new AgentAction(candidateSwitch.getSwitchNumber(), candidateSwitch.getSwitchStatus());
			
			QValueEvaluator evaluator = new QValueEvaluator();
			evaluator.setQValue(getQValue(state, action));
			evaluator.setDistance(candidateSwitch.getDistance());
			qValues.add(evaluator);
		}
		return qValues;
	}
	
	/**
	 * Retorna uma lista de {@link QValue} contendo os valores da tabela Q para as transições passadas
	 * @param state Estado onde o agente se encontra
	 * @param candidateSwitches Transições possíveis
	 * @return
	 */
	private List<QValue> getQValues(AgentState state, List<CandidateSwitch> candidateSwitches) {
		List<QValue> qValues = new ArrayList<>();
		for (CandidateSwitch candidateSwitch : candidateSwitches) {
			AgentAction action = new AgentAction(candidateSwitch.getSwitchNumber(), candidateSwitch.getSwitchStatus());
			qValues.add(getQValue(state, action));
		}
		return qValues;
	}
	
	/**
	 * Retorna o melhor valor de recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public synchronized QValue getBestQValue(AgentState state, List<CandidateSwitch> candidateSwitches) {
		List<QValue> qValues = this.getQValues(state, candidateSwitches);

		//Verifica o maior valor de recompensa
		double max = qValues.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
		
		//filtra por todas as acoes cuja recompensa seja igual a maior
		qValues = qValues.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
		
		//retorna uma das melhores acoes aleatoriamente
		QValue qValue = qValues.get(new Random(System.currentTimeMillis()).nextInt(qValues.size()));
		
		return qValue;
	}
	
	/**
	 * Retorna a acao com melhor recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public AgentAction getBestAction(AgentState state, List<CandidateSwitch> candidateSwitches) {
		List<QValueEvaluator> qValues = this.getQValuesEvaluators(state, candidateSwitches);

		//Verifica o maior valor de recompensa
		double max = qValues.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
		
		//filtra por todas as acoes cuja recompensa seja igual a maior
		qValues = qValues.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
		
		//retorna uma das melhores acoes aleatoriamente
		QValueEvaluator evaluator = qValues.get(new Random(System.currentTimeMillis()).nextInt(qValues.size()));
		
		return evaluator.getQValue().getAction();
	}
	
	/**
	 * Retorna uma ação aleatória
	 * @param state
	 * @param candidateSwitches Transições possíveis
	 * @return
	 */
	public AgentAction getRandomAction(AgentState state, List<CandidateSwitch> candidateSwitches, boolean proportional) {
		AgentAction action = null;
		
		//se é randômico proporcional
		if (proportional) {
			List<QValueEvaluator> qValues = this.getQValuesEvaluators(state, candidateSwitches);
			
			boolean hasNegativeQ = qValues.stream().anyMatch(value -> value.getReward() < 0);
			
			//se possui valor Q negativo, faz ajuste para que todos fiquem positivos
			if (hasNegativeQ) {
				double minQ = qValues.stream().min(Comparator.comparing(qValue -> qValue.getReward())).get().getReward();
				
				double average = qValues.stream().mapToDouble(value -> value.getReward()).average().getAsDouble();
				
				double adjustment = Math.abs(minQ) + Math.abs(average);
				
				qValues.forEach(qValue -> qValue.setRewardAdjustment(adjustment));
			}
			
			double random = Math.random();
			double sumQ = 0;
			
			boolean noValue = qValues.stream().allMatch(value -> value.getReward() == 0);
			if (!noValue) {
				//Se existe uma melhor ação específica, remove dos candidados para que agente explore demais opções
				double max = qValues.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
				List<QValueEvaluator> lst = qValues.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
				if (lst.size() == 1) {
					qValues.removeAll(lst);
				}
				
				double totalQ = qValues.stream().mapToDouble(value -> value.getReward()).sum();
				for (QValueEvaluator qValueEvaluator : qValues) {
					double percentage = qValueEvaluator.getReward() / totalQ;
					sumQ += percentage;
					
					if (sumQ >= random) {
						action = qValueEvaluator.getQValue().getAction();
						break;
					}
				}
			}			
		} 
		
		if (action == null) {
			//se não escolheu um no randomico proporcional, escolhe um dos candidatos aleatoriamente
			CandidateSwitch candidateSwitch = candidateSwitches.get(new Random(System.currentTimeMillis()).nextInt(candidateSwitches.size()));
			action = new AgentAction(candidateSwitch.getSwitchNumber(), candidateSwitch.getSwitchStatus());
		}
		
		return action;
	}
	
	/**
	 * Retorna a média dos valores da tabela Q
	 * @return
	 */
	public double getQValuesAverage() {
		return values().stream().mapToDouble(map -> map.values().stream().mapToDouble(qValue -> qValue.getReward()).average().getAsDouble()).average().getAsDouble();
	}
	
	public void getBestSwitchStatus(AgentState agentState) {
		
	}
}
