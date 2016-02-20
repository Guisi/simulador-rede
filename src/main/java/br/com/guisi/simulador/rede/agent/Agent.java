package br.com.guisi.simulador.rede.agent;

import java.util.List;

import javafx.application.Platform;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

public abstract class Agent {
	
	@Inject
	private EventBus eventBus;
	
	private AgentStatus agentStatus = new AgentStatus();
	private boolean stopRequest;
	private int step = 0;
	
	public final void run(TaskExecutionType taskExecutionType, StoppingCriteria stoppingCriteria) {
		this.stopRequest = false;
		boolean isEnvironmentValid = SimuladorRede.getEnvironment().isValidForReconfiguration();

		while (!stopRequest && isEnvironmentValid && !stoppingCriteria.wasReached(step)) {
			synchronized (this) {
				AgentStepStatus agentStepStatus = new AgentStepStatus(++step);
				agentStatus.getStepStatus().add(agentStepStatus);

				this.runNextEpisode(agentStepStatus);
				
				if (taskExecutionType.isNotifyEveryStep()) {
					try {
						this.notifyAgentObservers();
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				switch (taskExecutionType) {
					case STEP_BY_STEP: stop(); break;
					default: break;
				}
				
				isEnvironmentValid = SimuladorRede.getEnvironment().isValidForReconfiguration();
			}
		}
		
		this.notifyAgentObservers();
	}
	
	private void notifyAgentObservers() {
		AgentStatus copy = new AgentStatus();
		copy.setSteps(step);
		copy.getStepStatus().addAll(agentStatus.getStepStatus());
		Platform.runLater(() -> {
			eventBus.fire(EventType.AGENT_NOTIFICATION, copy);
		});
	}
	
	protected abstract void runNextEpisode(AgentStepStatus agentStepStatus);
	
	public abstract void reset();
	
	public final void stop() {
		stopRequest = true;
	}
	
	public abstract List<LearningProperty> getLearningProperties(Integer state);
	
	public abstract Branch getCurrentState();
}
