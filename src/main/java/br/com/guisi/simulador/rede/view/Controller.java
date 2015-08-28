package br.com.guisi.simulador.rede.view;

import javafx.scene.Node;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.enviroment.Environment;

/**
 * Classe abstrata dos controllers
 * @author douglas.guisi
 *
 */
public abstract class Controller {
	
	/**
	 * Retorna a o elemento raiz da view
	 * @return
	 */
	public abstract Node getView();
	
	private Stage stage;

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public Environment getEnvironment() {
		return SimuladorRede.getEnvironment();
	}
	
}
