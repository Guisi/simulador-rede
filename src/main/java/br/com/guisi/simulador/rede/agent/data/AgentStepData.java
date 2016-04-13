package br.com.guisi.simulador.rede.agent.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AgentStepData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final int step;
	private final Map<AgentDataType, Object> dataMap = new HashMap<>();
	
	public AgentStepData(int step) {
		this.step = step;
	}
	
	public int getStep() {
		return step;
	}

	public Integer getIntegerData(AgentDataType agentDataType) {
		return (Integer) getData(agentDataType);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData(AgentDataType agentInformationType, Class<T> clazz) {
		return (T) getData(agentInformationType);
	}
	
	public Object getData(AgentDataType agentInformationType) {
		return dataMap.get(agentInformationType);
	}
	
	public void putData(AgentDataType agentInformationType, Object value) {
		dataMap.put(agentInformationType, value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
