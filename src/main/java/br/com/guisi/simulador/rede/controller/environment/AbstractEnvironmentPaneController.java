package br.com.guisi.simulador.rede.controller.environment;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Environment;

public abstract class AbstractEnvironmentPaneController extends Controller {

	private Environment environment;

	public Environment getEnvironment() {
		//return environment;
		return SimuladorRede.getInteractionEnvironment();
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
