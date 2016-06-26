package br.com.guisi.simulador.rede.agent.qlearning.v3;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AgentStateAction {

	private final AgentState agentState;
	private final AgentAction agentAction;

	public AgentStateAction(AgentState agentState, AgentAction agentAction) {
		super();
		this.agentState = agentState;
		this.agentAction = agentAction;
	}

	public AgentState getAgentState() {
		return agentState;
	}

	public AgentAction getAgentAction() {
		return agentAction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agentAction == null) ? 0 : agentAction.hashCode());
		result = prime * result + ((agentState == null) ? 0 : agentState.hashCode());
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
		AgentStateAction other = (AgentStateAction) obj;
		if (agentAction == null) {
			if (other.agentAction != null)
				return false;
		} else if (!agentAction.equals(other.agentAction))
			return false;
		if (agentState == null) {
			if (other.agentState != null)
				return false;
		} else if (!agentState.equals(other.agentState))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
