package br.com.guisi.simulador.rede.controller.main;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Lazy;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;

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
		
		/*File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.xlsx");
		this.loadEnvironmentFromFile(f);*/
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
	
	private void loadEnvironmentFromFile(File xlsFile) {
		try {
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(xlsFile);
			SimuladorRede.setEnvironment(environment);
			
			if (environment != null) {
				boolean powerFlowSuccess = false;
				
				//primeiro valida se rede está radial
				List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
				
				if (exceptions.isEmpty()) {
					//isola as faltas
					EnvironmentUtils.isolateFaultSwitches(environment);
					
					//executa o fluxo de potência
					try {
						powerFlowSuccess = PowerFlow.execute(environment);
					} catch (Exception e) {
						e.printStackTrace();
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText(e.getMessage());
						alert.showAndWait();
					}
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					StringBuilder sb = new StringBuilder();
					exceptions.forEach(ex -> sb.append(ex.getMessage()).append("\n")); 
					alert.setContentText(sb.toString());
					alert.showAndWait();
				}
				
				this.fireEvent(EventType.ENVIRONMENT_LOADED);

				if (powerFlowSuccess) {
					this.fireEvent(EventType.POWER_FLOW_COMPLETED);
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText("Newton's method power flow did not converge");
					alert.showAndWait();
				}
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
	}
}
