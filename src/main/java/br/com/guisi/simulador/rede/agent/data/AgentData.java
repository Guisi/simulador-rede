package br.com.guisi.simulador.rede.agent.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;

public class AgentData implements Serializable {

	private static final long serialVersionUID = 1L;

	private int steps;
	private Map<EnvironmentKeyType, List<AgentStepData>> environmentStepDataMap;
	private List<AgentStepData> agentStepData = new ArrayList<>();

	public AgentData() {
		environmentStepDataMap = new HashMap<>();
		for (EnvironmentKeyType type : EnvironmentKeyType.values()) {
			environmentStepDataMap.put(type, new ArrayList<>());
		}
	}
	
	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}
	
	public List<AgentStepData> getEnvironmentStepData(EnvironmentKeyType environmentKeyType) {
		return environmentStepDataMap.get(environmentKeyType);
	}

	public List<AgentStepData> getAgentStepData() {
		return agentStepData;
	}

	public void setAgentStepData(List<AgentStepData> agentStepData) {
		this.agentStepData = agentStepData;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
