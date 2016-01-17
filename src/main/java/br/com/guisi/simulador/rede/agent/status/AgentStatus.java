package br.com.guisi.simulador.rede.agent.status;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class AgentStatus {

	private int steps;
	private List<AgentStepStatus> stepStatus = new ArrayList<>();

	public List<AgentStepStatus> getStepStatus() {
		return stepStatus;
	}

	public void setStepStatus(List<AgentStepStatus> stepStatus) {
		this.stepStatus = stepStatus;
	}
	
	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
