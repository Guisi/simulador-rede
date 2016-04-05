package br.com.guisi.simulador.rede.controller.environment;

import javafx.stage.Stage;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Environment;

public abstract class AbstractEnvironmentPaneController extends Controller {

	private EnvironmentKeyType environmentKeyType;

	public AbstractEnvironmentPaneController() {
	}
	
	public AbstractEnvironmentPaneController(EnvironmentKeyType environmentKeyType) {
		super();
		this.environmentKeyType = environmentKeyType;
	}
	
	public AbstractEnvironmentPaneController(Stage stage, EnvironmentKeyType environmentKeyType) {
		super(stage);
		this.environmentKeyType = environmentKeyType;
	}

	public Environment getEnvironment() {
		return SimuladorRede.getEnvironment(environmentKeyType);
	}

	public EnvironmentKeyType getEnvironmentKeyType() {
		return environmentKeyType;
	}
	
}
