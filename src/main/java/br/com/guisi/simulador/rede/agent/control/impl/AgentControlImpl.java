package br.com.guisi.simulador.rede.agent.control.impl;

import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.context.ApplicationContext;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

@Named
@Singleton
public class AgentControlImpl implements AgentControl {

	@Inject
	private EventBus eventBus;
	
	@Inject
	private ApplicationContext context;
	
	private AgentTask agentTask;
	private Agent agent;
	
	@Override
	public void run(TaskExecutionType taskExecutionType, StoppingCriteria stoppingCriteria) {
		if (getInteractionEnvironment().isValidForReconfiguration()) {
			agentTask = new AgentTask(getAgent(), taskExecutionType, stoppingCriteria);
			
			agentTask.stateProperty().addListener((observableValue, oldState, newState) -> {
	            if (newState == Worker.State.SUCCEEDED) {
	            	eventBus.fire(EventType.AGENT_STOPPED, agent.isStopRequest());
	            	eventBus.fire(EventType.POWER_FLOW_COMPLETED);
	            }
	        });
			
			agentTask.exceptionProperty().addListener((observable, oldValue, newValue) ->  {
				if (newValue != null) {
					eventBus.fire(EventType.AGENT_STOPPED, agent.isStopRequest());
					eventBus.fire(EventType.POWER_FLOW_COMPLETED);
					
					Exception ex = (Exception) newValue;
					ex.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText(ex.getMessage());
					alert.showAndWait();
				}
			});
			
			eventBus.fire(EventType.AGENT_RUNNING);
	
			new Thread(agentTask).start();
		} else {
			Alert alert = new Alert(AlertType.ERROR, "O ambiente � inv�lido para reconfigura��o.");
			alert.showAndWait();
		}
	}

	@Override
	public void stop() {
		getAgent().stop();
	}
	
	@Override
	public void reset() {
		agent = null;
	}
	
	@Override
	public Agent getAgent() {
		if (agent == null) {
			agent = (Agent) context.getBean("qLearningAgentV3");
		}
		return agent;
	}
	
	public Environment getInteractionEnvironment() {
		return SimuladorRede.getEnvironment(EnvironmentKeyType.INTERACTION_ENVIRONMENT);
	}
}
