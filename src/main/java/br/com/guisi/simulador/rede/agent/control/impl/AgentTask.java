package br.com.guisi.simulador.rede.agent.control.impl;

import javafx.concurrent.Task;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public class AgentTask extends Task<Void> {

	private final Agent agent;
	private final TaskExecutionType taskExecutionType;
	private StoppingCriteria stoppingCriteria;
	
	public AgentTask(Agent agent, TaskExecutionType taskExecutionType, StoppingCriteria stoppingCriteria) {
		super();
		this.agent = agent;
		this.taskExecutionType = taskExecutionType;
		this.stoppingCriteria = stoppingCriteria;
	}

	@Override
	protected Void call() {
		agent.run(taskExecutionType, stoppingCriteria);
		return null;
	}
}
