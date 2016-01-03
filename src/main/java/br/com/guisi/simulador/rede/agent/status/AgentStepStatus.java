package br.com.guisi.simulador.rede.agent.status;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AgentStepStatus {

	private final int step;
	private final Map<AgentInformationType, Object> informationMap = new HashMap<>();
	
	public AgentStepStatus(int step) {
		this.step = step;
	}
	
	public int getStep() {
		return step;
	}

	public Integer getIntegerInformation(AgentInformationType agentInformationType) {
		return (Integer) getInformation(agentInformationType);
	}
	
	public Object getInformation(AgentInformationType agentInformationType) {
		return informationMap.get(agentInformationType);
	}
	
	public void putInformation(AgentInformationType agentInformationType, Object value) {
		informationMap.put(agentInformationType, value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
