package br.com.guisi.simulador.rede.agent.control.impl;

import java.util.Observable;
import java.util.Observer;

import javafx.concurrent.Task;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.AgentStatus;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public class AgentTask extends Task<AgentStatus> implements Observer {

	private final Agent agent;
	private final TaskExecutionType taskExecutionType;
	
	public AgentTask(Agent agent, TaskExecutionType taskExecutionType) {
		super();
		this.agent = agent;
		this.agent.addObserver(this);
		this.taskExecutionType = taskExecutionType;
	}

	@Override
	protected AgentStatus call() throws Exception {
		agent.run(taskExecutionType);
		
		agent.generateAgentNotification();
		
		return agent.getAgentNotification();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		updateValue((AgentStatus) arg);
	}
}
