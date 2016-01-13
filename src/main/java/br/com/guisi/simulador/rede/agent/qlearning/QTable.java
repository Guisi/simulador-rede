package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.guisi.simulador.rede.enviroment.SwitchState;

public class QTable extends HashMap<QKey, QValue> {

	private static final long serialVersionUID = 1L;

	/**
	 * Recupera a recompensa para o estado e ação passados na {@link QKey}
	 * @param qKey
	 * @return
	 */
	public QValue getQValue(QKey qKey) {
		QValue qValue = get(qKey);
		if (qValue == null) {
			qValue = new QValue(qKey);
			put(qKey, qValue);
		}
		return qValue;
	}
	
	public QValue getQValue(Integer state, SwitchState action) {
		return getQValue(new QKey(state, action));
	}
	
	/**
	 * Recupera a lista de recompensas para cada ação do estado passado
	 * @param state
	 * @return
	 */
	public List<QValue> getQValues(Integer state) {
		List<QValue> qValues = new ArrayList<>();
		for (SwitchState action : SwitchState.OPERATIONAL_SWITCHES) {
			QValue qValue = getQValue(new QKey(state, action));
			qValues.add(qValue);
		}
		return qValues;
	}
	
	/**
	 * Retorna o melhor valor de recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public synchronized QValue getBestQValue(Integer state) {
		//Recupera valores para o estado
		List<QValue> qValues = getQValues(state);
		
		//Verifica o maior valor de recompensa
		double max = qValues.stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
		
		//filtra por todas as acoes cuja recompensa seja igual a maior
		qValues = qValues.stream().filter(valor -> valor.getReward() == max).collect(Collectors.toList());
		
		//retorna uma das melhores acoes aleatoriamente
		return qValues.get(new Random(System.currentTimeMillis()).nextInt(qValues.size()));
	}
	
	/**
	 * Retorna a acao com melhor recompensa para o estado passado
	 * @param state
	 * @return
	 */
	public SwitchState getBestAction(Integer state) {
		return this.getBestQValue(state).getQKey().getAction();
	}
	
	/**
	 * Retorna o maior valor da maior recompensa existente na tabela
	 * @return
	 */
	public double getGreaterReward() {
		return values().stream().max(Comparator.comparing(value -> value.getReward())).get().getReward();
	}
	
	/**
	 * Retorna o menor valor da maior recompensa existente na tabela
	 * @return
	 */
	public double getLowerReward() {
		return values().stream().min(Comparator.comparing(value -> value.getReward())).get().getReward();
	}
}
