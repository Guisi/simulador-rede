package br.com.guisi.simulador.rede.agent;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public abstract class AgentStatus {

	private AgentStatusType agentStatusType;
	private boolean handled;
	
	public AgentStatus() {
	}
	
	public AgentStatus clone() {
		try {
			return (AgentStatus) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
			return null;
		}
	}

	public AgentStatusType getAgentStatusType() {
		return agentStatusType;
	}

	public void setAgentStatusType(AgentStatusType agentStatusType) {
		this.agentStatusType = agentStatusType;
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}
}
