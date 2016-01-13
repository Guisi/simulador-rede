package br.com.guisi.simulador.rede.agent;

import javafx.application.Platform;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

public abstract class Agent {
	
	@Inject
	private EventBus eventBus;
	
	private AgentStatus agentStatus = new AgentStatus();
	private boolean stopRequest;
	private int step = 1;
	
	public final void run(TaskExecutionType taskExecutionType) {
		this.stopRequest = false;

		while (!stopRequest) {
			synchronized (this) {
				this.runNextEpisode();
				this.generateAgentInformations();
				
				if (taskExecutionType.isNotifyEveryStep()) {
					try {
						if (step % 10 == 0) {
							this.notifyAgentObservers();
							Thread.sleep(100);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				step++;
				
				switch (taskExecutionType) {
					case STEP_BY_STEP: stop(); break;
					default: break;
				}
			}
		}
		
		this.notifyAgentObservers();
	}
	
	private void generateAgentInformations() {
		AgentStepStatus agentStepStatus = new AgentStepStatus(step);
		putInformations(agentStepStatus);
		agentStatus.getStepStatus().add(agentStepStatus);
	}
	
	private void notifyAgentObservers() {
		AgentStatus copy = new AgentStatus();
		copy.getStepStatus().addAll(agentStatus.getStepStatus());
		Platform.runLater(() -> {
			eventBus.fire(EventType.AGENT_NOTIFICATION, copy);
			eventBus.fire(EventType.POWER_FLOW_COMPLETED);
		});
	}
	
	protected abstract void runNextEpisode();
	
	protected abstract void putInformations(AgentStepStatus agentStepStatus);
	
	public abstract void reset();
	
	public final void stop() {
		stopRequest = true;
	}
}
