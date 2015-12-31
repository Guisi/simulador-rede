package br.com.guisi.simulador.rede.agent;

import java.util.Observable;

import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public abstract class Agent extends Observable {
	
	protected AgentStatus agentStatus;
	private boolean stopRequest;
	private int stepCount = 1;
	
	public final void run(TaskExecutionType taskExecutionType) {
		this.stopRequest = false;

		while (!stopRequest) {
			synchronized (this) {
				this.runNextEpisode();
				
				if (taskExecutionType.isNotifyObservers()) {
					generateAgentNotification();
				}
				
				switch (taskExecutionType) {
					case STEP_BY_STEP: stop(); break;
					default: break;
				}
			}
		}
	}
	
	public void generateAgentNotification() {
		agentStatus = new AgentStatus(stepCount++);
		setNotifications();
		setChanged();
		notifyObservers(agentStatus);
	}
	
	protected abstract void runNextEpisode();
	
	protected abstract void setNotifications();
	
	public final void stop() {
		stopRequest = true;
	}

	public AgentStatus getAgentNotification() {
		return agentStatus;
	}
	
}
