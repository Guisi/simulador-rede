package br.com.guisi.simulador.rede.agent.control.impl;

import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.ApplicationContext;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.qlearning.QLearningAgent;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

@Named
public class AgentControlImpl implements AgentControl {

	@Inject
	private EventBus eventBus;
	
	@Inject
	private ApplicationContext context;
	
	private Agent agent;
	
	@Override
	public void run(TaskExecutionType taskExecutionType) {
		if (SimuladorRede.getEnvironment().isValidForReconfiguration()) {
			if (agent == null) {
				this.reset();
			}
	
			AgentTask agentTask = new AgentTask(agent, taskExecutionType);
			
			agentTask.valueProperty().addListener((observableValue, oldState, newState) -> {
				eventBus.fire(EventType.AGENT_NOTIFICATION, newState);
			});
			
			agentTask.stateProperty().addListener((observableValue, oldState, newState) -> {
	            if (newState == Worker.State.SUCCEEDED) {
	            	eventBus.fire(EventType.AGENT_STOPPED);
	            }
	        });
			
			eventBus.fire(EventType.AGENT_RUNNING);
	
			new Thread(agentTask).start();
		} else {
			Alert alert = new Alert(AlertType.ERROR, "O ambiente é inválido para reconfiguração.");
			alert.showAndWait();
		}
	}

	@Override
	public void stop() {
		agent.stop();
	}
	
	@Override
	public void reset() {
		agent = context.getBean(QLearningAgent.class);
	}
}
