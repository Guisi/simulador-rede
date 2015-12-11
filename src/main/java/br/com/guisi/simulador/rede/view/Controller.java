package br.com.guisi.simulador.rede.view;

import javafx.scene.Node;
import javafx.stage.Stage;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventBus;
import br.com.guisi.simulador.rede.events.EventListener;
import br.com.guisi.simulador.rede.events.EventType;

/**
 * Classe abstrata dos controllers
 * @author douglas.guisi
 *
 */
public abstract class Controller implements EventListener {
	
	@Inject
	private EventBus eventBus;
	
	/**
	 * Retorna a o elemento raiz da view
	 * @return
	 */
	public abstract Node getView();
	
	public abstract void initializeController(Object... data);
	
	private Stage stage;

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
	}
	
	public Environment getEnvironment() {
		return SimuladorRede.getEnvironment();
	}
	
	public void listenToEvent(EventType eventType){
		eventBus.register(eventType, this);
	}
	
}
