package br.com.guisi.simulador.rede.agent;

import java.util.Observable;

import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public abstract class Agent extends Observable {
	
	protected AgentNotification agentNotification;
	private boolean stopRequest;
	private int stepCount = 1;
	
	public final void run(TaskExecutionType taskExecutionType) {
		this.stopRequest = false;

		switch (taskExecutionType) {
			case STEP_BY_STEP:
				this.runNextEpisode();
				break;

			case CONTINUOUS_UPDATE_END_ONLY:
			case CONTINUOUS_UPDATE_EVERY_STEP:
				while (!stopRequest) {
					synchronized (this) {
						this.runNextEpisode();
						
						if (taskExecutionType.isNotifyObservers()) {
							generateAgentNotification();
						}
					}
				}
			default:
				break;
		}
	}
	
	public void generateAgentNotification() {
		agentNotification = new AgentNotification(stepCount++);
		setNotifications();
		setChanged();
		notifyObservers(agentNotification);
	}
	
	protected abstract void runNextEpisode();
	
	protected abstract void setNotifications();
	
	public final void stop() {
		stopRequest = true;
	}

	public AgentNotification getAgentNotification() {
		return agentNotification;
	}
	
}
