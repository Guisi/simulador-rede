package br.com.guisi.simulador.rede.agent;

import java.util.Observable;

import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public abstract class Agent extends Observable {
	
	private AgentStatus agentStatus = new AgentStatus();
	private boolean stopRequest;
	private int stepCount = 1;
	
	public final void run(TaskExecutionType taskExecutionType) {
		this.stopRequest = false;

		while (!stopRequest) {
			synchronized (this) {
				this.runNextEpisode();
				this.generateAgentInformations();
				
				if (taskExecutionType.isNotifyObservers()) {
					this.notifyAgentObservers();
				}
				
				switch (taskExecutionType) {
					case STEP_BY_STEP: stop(); break;
					default: break;
				}
			}
		}
	}
	
	private void generateAgentInformations() {
		AgentStepStatus agentStepStatus = new AgentStepStatus(stepCount++);
		putInformations(agentStepStatus);
		agentStatus.getStepStatus().add(agentStepStatus);
	}
	
	private void notifyAgentObservers() {
		setChanged();
		AgentStatus copy = new AgentStatus();
		copy.getStepStatus().addAll(agentStatus.getStepStatus());
		notifyObservers(copy);
	}
	
	protected abstract void runNextEpisode();
	
	protected abstract void putInformations(AgentStepStatus agentStepStatus);
	
	public final void stop() {
		stopRequest = true;
	}

	public AgentStatus getAgentNotification() {
		return agentStatus;
	}
	
}
