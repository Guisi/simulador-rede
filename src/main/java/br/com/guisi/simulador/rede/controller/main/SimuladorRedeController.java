package br.com.guisi.simulador.rede.controller.main;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;

public class SimuladorRedeController extends Controller {

	public static final String FXML_FILE = "/fxml/main/SimuladorRede.fxml";

	@FXML
	private VBox root;
	@FXML
	private ScrollPane scrollPane;
	
	@FXML
	private VBox networkBoxLeft;
	@FXML
	private VBox networkBoxRight;
	
	@Inject
	private AgentControl agentControl;
	
	//private NetworkPaneController networkPaneController;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN, EventType.ENVIRONMENT_LOADED);
		
		scrollPane.prefWidthProperty().bind(SimuladorRede.getPrimaryStage().widthProperty());
		scrollPane.prefHeightProperty().bind(SimuladorRede.getPrimaryStage().heightProperty());
		
		//menu
		root.getChildren().add(0, getController(MenuPaneController.class).getView());
		
		//controls
		root.getChildren().add(1, getController(ControlsPaneController.class).getView());
		
		//Painel dos detalhes dos elementos da rede
		networkBoxLeft.getChildren().add(getController(ElementsDetailsPaneController.class).getView());
		
		//Painel de labels e messages
		networkBoxLeft.getChildren().add(getController(LabelAndMessagesPaneController.class).getView());
		
		//Painel das funções
		networkBoxLeft.getChildren().add(getController(FunctionsPaneController.class).getView());
		
		//Painel dos gráficos
		networkBoxLeft.getChildren().add(getController(ChartsPaneController.class).getView());
		
		//NetworkPane
		networkBoxRight.getChildren().add(getController(NetworkPaneController.class).getView());
		//networkPaneController = (NetworkPaneController) SimuladorRede.showUtilityScene("Electric Network", NetworkPaneController.class, false);
		
		this.fireEvent(EventType.RESET_SCREEN);
		
		/*File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.xlsx");
		this.loadEnvironmentFromFile(f);*/
	}
	
	/*private void loadEnvironmentFromFile(File xlsFile) {
		try {
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(xlsFile);
			SimuladorRede.setEnvironment(environment);
			
			if (environment != null) {
				boolean powerFlowSuccess = false;
				
				//primeiro valida se rede está radial
				String errors = EnvironmentUtils.validateRadialState(environment);
				
				if (StringUtils.isEmpty(errors)) {
					//isola as faltas
					EnvironmentUtils.isolateFaultSwitches(environment);
					
					//executa o fluxo de potência
					try {
						PowerFlow.execute(environment);
						powerFlowSuccess = true;
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText(e.getMessage());
						alert.showAndWait();
					}
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText(errors);
					alert.showAndWait();
				}
				
				this.fireEvent(EventType.ENVIRONMENT_LOADED);

				if (powerFlowSuccess) {
					this.fireEvent(EventType.POWER_FLOW_COMPLETED);
				}
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
	}*/
	
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
		//networkPaneController.getStage().show();
	}
	
	private void processResetScreen() {
		this.agentControl.reset();
		/*if (networkPaneController != null) {
			networkPaneController.getStage().hide();
		}*/
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
