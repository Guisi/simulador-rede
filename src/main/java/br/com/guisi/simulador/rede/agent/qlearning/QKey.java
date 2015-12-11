package br.com.guisi.simulador.rede.agent.qlearning;

import br.com.guisi.simulador.rede.enviroment.SwitchState;

/**
 * Classe representando a chave da QTable
 * O estado é representado pelo número da branch switch
 * A ação é representada pelo estado do switch, aberto ou fechado
 * 
 * @author Guisi
 *
 */
public class QKey {

	private final Integer state;
	private final SwitchState action;
	
	public QKey(Integer state, SwitchState action) {
		this.state = state;
		this.action = action;
	}

	public Integer getState() {
		return state;
	}

	public SwitchState getAction() {
		return action;
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
		QKey other = (QKey) obj;
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
		return "QKey [state=" + state + ", action=" + action + "]";
	}
}
