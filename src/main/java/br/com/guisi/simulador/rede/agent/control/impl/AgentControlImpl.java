package br.com.guisi.simulador.rede.agent.control.impl;

import javafx.concurrent.Worker;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.annotations.QLearning;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

@Service
public class AgentControlImpl implements AgentControl {

	@Inject
	private EventBus eventBus;
	
	@Inject
	@QLearning
	private Agent agent;
	
	@PostConstruct
	private void init() {
		agent.init();
	}
	
	@Override
	public void run(TaskExecutionType taskExecutionType) {
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
	}

	@Override
	public void stop() {
		agent.stop();
	}
	
	@Override
	public void reset() {
		agent.init();
	}
}
