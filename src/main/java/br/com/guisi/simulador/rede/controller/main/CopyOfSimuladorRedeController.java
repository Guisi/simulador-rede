package br.com.guisi.simulador.rede.controller.main;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.controller.chart.EnvironmentChartsPaneController;
import br.com.guisi.simulador.rede.controller.chart.LearningChartsPaneController;
import br.com.guisi.simulador.rede.controller.environment.NetworkPaneController;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;

public class CopyOfSimuladorRedeController extends Controller {

	public static final String FXML_FILE = "/fxml/main/SimuladorRede.fxml";

	@FXML
	private VBox root;
	@FXML
	private SplitPane splitPane;
	@FXML
	private ScrollPane scrollPaneRight;
	@FXML
	private ScrollPane scrollPaneLeft;
	@FXML
	private VBox networkBoxLeft;
	@FXML
	private VBox networkBoxRight;
	
	@Inject
	private AgentControl agentControl;
	
	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN, EventType.ENVIRONMENT_LOADED);
		
		scrollPaneRight.prefHeightProperty().bind(SimuladorRede.getPrimaryStage().heightProperty());
		scrollPaneLeft.prefHeightProperty().bind(SimuladorRede.getPrimaryStage().heightProperty());
		
		//menu
		/*root.getChildren().add(0, getController(MenuPaneController.class).getView());
		
		//controls
		root.getChildren().add(1, getController(ControlsPaneController.class).getView());
		
		//Painel dos detalhes dos elementos da rede
		networkBoxLeft.getChildren().add(getController(ElementsDetailsPaneController.class).getView());
		
		//Painel de labels e messages
		networkBoxLeft.getChildren().add(getController(LabelAndMessagesPaneController.class).getView());
		
		//Painel das funções
		networkBoxLeft.getChildren().add(getController(FunctionsPaneController.class).getView());
		
		//Painel dos gráficos
		getController(EnvironmentChartsPaneController.class);
		
		getController(LearningChartsPaneController.class);
		
		//NetworkPane
		NetworkPaneController networkPaneController = getController(NetworkPaneController.class);
		networkBoxRight.getChildren().add(networkPaneController.getView());
		
		this.fireEvent(EventType.RESET_SCREEN);*/
		
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
		this.splitPane.setVisible(true);
	}
	
	private void processResetScreen() {
		this.splitPane.setVisible(false);
		this.agentControl.reset();
		
		/*if (networkPaneController != null) {
			networkPaneController.getStage().hide();
		}*/
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
