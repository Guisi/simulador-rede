package br.com.guisi.simulador.rede.agent.control.impl;

import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javax.inject.Inject;
import javax.inject.Named;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.annotations.QLearning;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

@Named
public class AgentControlImpl implements AgentControl {

	@Inject
	private EventBus eventBus;
	
	@Inject
	@QLearning
	private Agent agent;
	
	private AgentTask agentTask;
	
	@Override
	public void run(TaskExecutionType taskExecutionType) {
		if (SimuladorRede.getEnvironment().isValidForReconfiguration()) {
			agentTask = new AgentTask(agent, taskExecutionType);
			
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
		agent.reset();
	}
}
