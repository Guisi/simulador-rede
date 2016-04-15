package br.com.guisi.simulador.rede.functions;

import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.enviroment.Environment;

/**
 * Class that agregates objects used for the Expression Evaluator
 * 
 * @author douglasguisi
 */
public class EvaluationObject {

	private Environment environment;
	private Agent agent;

	/**
	 * Returns the environment instance
	 * @return {@link Environment}
	 */
	public Environment getEnvironment() {
		return environment;
	}

	/**
	 * Sets the environment instance
	 * @param environment The instance of the enviroment loaded from a CSV file
	 */
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
}
