package br.com.guisi.simulador.rede.controller;

import javafx.scene.Node;
import javafx.stage.Stage;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
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
	
	public abstract void initializeController();
	
	public abstract void initializeControllerData(Object... data);
	
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
	
	public <T extends Controller> T getController(Class<T> controllerClass, Object...data) {
		T controller = SimuladorRede.getCtx().getBean(controllerClass);
		controller.initializeController();
		controller.initializeControllerData(data);
		return controller;
	}
	
	/**
	 * Registra este controller para escutar um determinada evento
	 * @param eventType
	 */
	public void listenToEvent(EventType... eventTypes){
		if (eventTypes != null) {
			for (EventType eventType : eventTypes) {
				eventBus.register(eventType, this);
			}
		}
	}
	
	/**
	 * Lanca um novo evento no barramento
	 * @param eventType
	 * @param data
	 */
	public void fireEvent(EventType eventType, Object data){
		eventBus.fire(eventType, data);
	}
	
	public void fireEvent(EventType eventType){
		eventBus.fire(eventType, null);
	}
}
