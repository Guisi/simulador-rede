package br.com.guisi.simulador.rede.agent.qlearning.v3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class QTable extends HashMap<AgentStateAction, QValue> {

	private static final long serialVersionUID = 1L;

	/**
	 * Recupera a recompensa para o estado e ação passados na {@link QKey}
	 * @param qKey
	 * @return
	 */
	public QValue getQValue(AgentState state, AgentAction action) {
		AgentStateAction agentStateAction = new AgentStateAction(state, action);
		QValue qValue = get(agentStateAction);
		if (qValue == null) {
			qValue = new QValue(state, action);
			put(agentStateAction, qValue);
		}
		return qValue;
	}
	
	/**
	 * Recupera a lista de recompensas para cada ação do estado passado
	 * @param state
	 * @return
	 */
	public List<QValue> getQValues(AgentState state) {
		return values().stream().filter(qValue -> qValue.getState().equals(state)).collect(Collectors.toList());
	}
	
	/**
	 * Retorna uma lista de {@link QValue} contendo os valores da tabela Q para as transições passadas
	 * @return
	 */
	private List<QValue> getQValues(AgentState state, List<AgentAction> agentActions) {
		List<QValue> qValues = new ArrayList<>();
		
		agentActions.forEach(action -> {
			qValues.add( getQValue(state, action) );
		});
		
		return qValues;
	}
	
	/**
	 * Retorna uma lista de {@link QValueEvaluator} contendo os valores para as transições passadas
	 * @return
	 */
	private List<QValueEvaluator> getQValuesEvaluators(AgentState state, List<AgentAction> agentActions) {
		List<QValue> qValues = getQValues(state, agentActions);

		List<QValueEvaluator> qValueEvaluators = new ArrayList<>();
		qValues.forEach(qValue -> {
			QValueEvaluator evaluator = new QValueEvaluator();
			evaluator.setQValue(qValue);
			qValueEvaluators.add(evaluator);
		});
		
		return qValueEvaluators;
	}
	
	/**
	 * Retorna o melhor valor de recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public synchronized QValue getBestQValue(AgentState state, List<AgentAction> agentActions) {
		List<QValue> qValues = this.getQValues(state, agentActions);

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
	public AgentAction getBestAction(AgentState state, List<AgentAction> agentActions) {
		List<QValueEvaluator> qValueEvalutors = getQValuesEvaluators(state, agentActions);

		//Verifica o maior valor de recompensa
		double max = qValueEvalutors.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
		
		//filtra por todas as acoes cuja recompensa seja igual a maior
		qValueEvalutors = qValueEvalutors.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
		
		//retorna uma das melhores acoes aleatoriamente
		QValueEvaluator evaluator = qValueEvalutors.get(new Random(System.currentTimeMillis()).nextInt(qValueEvalutors.size()));
		
		return evaluator.getQValue().getAction();
	}
	
	/**
	 * Retorna uma ação aleatória
	 * @param state
	 * @param candidateSwitches Transições possíveis
	 * @return
	 */
	public AgentAction getRandomAction(AgentState state, List<AgentAction> agentActions, boolean proportional) {
		AgentAction action = null;
		
		//se é randômico proporcional
		if (proportional) {
			List<QValueEvaluator> qValues = getQValuesEvaluators(state, agentActions);
			
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
			action = agentActions.get(new Random(System.currentTimeMillis()).nextInt(agentActions.size()));
		}
		
		return action;
	}
	
	/**
	 * Retorna a média dos valores da tabela Q
	 * @return
	 */
	public double getQValuesAverage() {
		return values().stream().mapToDouble(qValue -> qValue.getReward()).average().getAsDouble();
	}
}
