package br.com.guisi.simulador.rede.agent.qlearning.v1;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Classe representando o valor de recompensa para um estado/ação do agente
 * 
 * @author Guisi
 *
 */
public class QValue {

	private final AgentState state;
	private final AgentAction action;
	private double reward;
	private boolean updated;
	
	public QValue(AgentState state, AgentAction action) {
		super();
		this.state = state;
		this.action = action;
	}
	
	public boolean isUpdated() {
		return updated;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
		this.updated = true;
	}

	public AgentState getState() {
		return state;
	}

	public AgentAction getAction() {
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
		QValue other = (QValue) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
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
		return ToStringBuilder.reflectionToString(this);
	}
}
