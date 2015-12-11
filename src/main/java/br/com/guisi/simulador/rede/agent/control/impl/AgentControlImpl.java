package br.com.guisi.simulador.rede.agent.control.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventType;

@Service
public class AgentControlImpl implements AgentControl {

	@Inject
	private EventBus eventBus;
	
	@Override
	public void run() {
		eventBus.fire(EventType.AGENT_RUNNING, null);
	}

	@Override
	public void stop() {
		eventBus.fire(EventType.AGENT_STOPPED, null);
	}

	
	/*if (getEnvironment().isValidForReconfiguration()) {
		this.enableDisableScreen(true);
		if (qLearningAgent == null) {
			qLearningAgent = new QLearningAgent(getEnvironment());
		}
		agentTask = new AgentTask(cbTaskExecutionType.getValue(), qLearningAgent);
		
		agentTask.valueProperty().addListener((observableValue, oldState, newState) -> {
			if (!newState.isHandled()) {
				updateAgentStatus(newState);
			}
		});
		
		agentTask.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
            	stopAgent();
            }
        });
		
		new Thread(agentTask).start();
	} else {
		Alert alert = new Alert(AlertType.ERROR, "O ambiente é inválido para reconfiguração.");
		alert.showAndWait();
	}*/
	
	/*this.enableDisableScreen(false);
	agentTask.cancel();*/
	/*QLearningStatus qLearningStatus = agentTask.getValue();
	updateAgentStatus(qLearningStatus);*/
}
