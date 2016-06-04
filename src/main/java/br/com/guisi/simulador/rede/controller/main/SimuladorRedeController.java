package br.com.guisi.simulador.rede.controller.main;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Lazy;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;

@Named
@Lazy
public class SimuladorRedeController extends Controller {

	@FXML
	private VBox root;
	
	@Inject
	private AgentControl agentControl;
	
	private MenuPaneController menuPaneController;
	private ControlsPaneController controlsPaneController;
	private AgentInformationPaneController agentInformationPaneController;
	
	public SimuladorRedeController(Stage stage) {
		super(stage);
	}
	
	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN, EventType.ENVIRONMENT_LOADED);
		
		root = new VBox();
		root.setPrefWidth(1200);
		root.setPrefHeight(715);
		
		//menu
		menuPaneController = getController(MenuPaneController.class, getStage());
		menuPaneController.setStage(getStage());
		root.getChildren().add(menuPaneController.getView());
		
		//controls
		controlsPaneController = getController(ControlsPaneController.class, getStage());
		controlsPaneController.setStage(getStage());
		root.getChildren().add(controlsPaneController.getView());
		
		//agent information
		agentInformationPaneController = getController(AgentInformationPaneController.class, getStage());
		agentInformationPaneController.setStage(getStage());
		root.getChildren().add(agentInformationPaneController.getView());
		
		this.fireEvent(EventType.RESET_SCREEN);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case ENVIRONMENT_LOADED: processEnvironmentLoaded(); break;
			case RESET_SCREEN: processResetScreen(); break; 
			default: break;
		}
	}
	
	private void processEnvironmentLoaded() {
	}
	
	private void processResetScreen() {
		this.agentControl.reset();
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
