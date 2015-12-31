package br.com.guisi.simulador.rede.agent;

import java.util.HashMap;
import java.util.Map;

import br.com.guisi.simulador.rede.constants.AgentNotificationType;

public class AgentStatus {

	private final int step;
	private final Map<AgentNotificationType, Object> notificationMap = new HashMap<>();
	
	public AgentStatus(int step) {
		this.step = step;
	}
	
	public int getStep() {
		return step;
	}

	public Integer getIntegerNotification(AgentNotificationType agentNotificationType) {
		return (Integer) getNotification(agentNotificationType);
	}
	
	public Object getNotification(AgentNotificationType agentNotificationType) {
		return notificationMap.get(agentNotificationType);
	}
	
	public void putNotification(AgentNotificationType agentNotificationType, Object value) {
		notificationMap.put(agentNotificationType, value);
	}
	
}
