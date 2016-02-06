package br.com.guisi.simulador.rede.agent.control;

import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public interface AgentControl {

	void run(TaskExecutionType taskExecutionType, StoppingCriteria stoppingCriteria);
	
	void stop();
	
	void reset();
	
	Agent getAgent();
}