package br.com.guisi.simulador.rede.agent.control.impl;

import javafx.concurrent.Task;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public class AgentTask extends Task<Void> {

	private final Agent agent;
	private final TaskExecutionType taskExecutionType;
	
	public AgentTask(Agent agent, TaskExecutionType taskExecutionType) {
		super();
		this.agent = agent;
		this.taskExecutionType = taskExecutionType;
	}

	@Override
	protected Void call() throws Exception {
		agent.run(taskExecutionType);
		return null;
	}
}
