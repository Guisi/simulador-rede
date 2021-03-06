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
	
	private Stage stage;
	
	public Controller() {
	}
	
	public Controller(Stage stage) {
		super();
		this.stage = stage;
	}

	/**
	 * Retorna a o elemento raiz da view
	 * @return
	 */
	public abstract Node getView();
	
	public abstract void initializeControllerData(Object... data);

	@Override
	public void onEvent(EventType eventType, Object data) {
	}
	
	public <T extends Controller> T getController(Class<T> controllerClass, Object... params) {
		T controller = SimuladorRede.getCtx().getBean(controllerClass, params);
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

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
		onSetStage(stage);
	}
	
	protected void onSetStage(Stage stage) {
	}
	
	public String getControllerKey() {
		return getClass().getSimpleName().toUpperCase();
	}
}
