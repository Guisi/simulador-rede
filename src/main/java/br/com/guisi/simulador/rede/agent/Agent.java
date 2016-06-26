package br.com.guisi.simulador.rede.agent;

import java.util.List;

import javafx.application.Platform;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.LearningPropertyPair;
import br.com.guisi.simulador.rede.agent.data.LearningState;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

public abstract class Agent {
	
	@Inject
	private EventBus eventBus;
	
	private AgentData agentData = new AgentData();
	private boolean stopRequest;
	private int step = 0;
	
	public final void run(TaskExecutionType taskExecutionType, StoppingCriteria stoppingCriteria) {
		this.stopRequest = false;
		boolean isEnvironmentValid = getInteractionEnvironment().isValidForReconfiguration();

		while (!stopRequest && isEnvironmentValid && !stoppingCriteria.wasReached(step)) {
			synchronized (this) {
				agentData.setSteps(++step);

				this.runNextEpisode();
				
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
				
				isEnvironmentValid = getInteractionEnvironment().isValidForReconfiguration();
			}
		}
		
		this.notifyAgentObservers();
	}
	
	private void notifyAgentObservers() {
		AgentData copy = SerializationUtils.clone(agentData);
		Platform.runLater(() -> {
			eventBus.fire(EventType.AGENT_NOTIFICATION, copy);
		});
	}
	
	protected abstract void runNextEpisode();
	
	public abstract void reset();
	
	public final void stop() {
		stopRequest = true;
	}
	
	public int getStep() {
		return step;
	}

	public AgentData getAgentData() {
		return agentData;
	}

	public boolean isStopRequest() {
		return stopRequest;
	}

	public abstract List<LearningState> getLearningStates();
	
	public abstract List<LearningPropertyPair> getLearningProperties(LearningState learningState, boolean onlyUpdated);
	
	public abstract Object getCurrentState();
	
	public Environment getInteractionEnvironment() {
		return SimuladorRede.getEnvironment(EnvironmentKeyType.INTERACTION_ENVIRONMENT);
	}
	
	public Environment getLearningEnvironment() {
		return SimuladorRede.getEnvironment(EnvironmentKeyType.LEARNING_ENVIRONMENT);
	}
	
	public Environment getInitialEnvironment() {
		return SimuladorRede.getEnvironment(EnvironmentKeyType.INITIAL_ENVIRONMENT);
	}
}
