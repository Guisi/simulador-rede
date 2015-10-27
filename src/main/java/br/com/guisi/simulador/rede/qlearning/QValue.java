package br.com.guisi.simulador.rede.qlearning;

import br.com.guisi.simulador.rede.constants.SwitchState;

/**
 * Classe representando o valor de recompensa para 
 * 
 * @author Guisi
 *
 */
public class QValue {

	private final Integer state;
	private final SwitchState action;
	private double reward;
	
	public QValue(Integer state, SwitchState action) {
		this.state = state;
		this.action = action;
	}

	public Integer getState() {
		return state;
	}

	public SwitchState getAction() {
		return action;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QValue other = (QValue) obj;
		if (action != other.action)
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QValue [state=" + state + ", action=" + action + ", reward=" + reward + "]";
	}
	
}
