package br.com.guisi.simulador.rede.controller.main;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.SimuladorRede;
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
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.RESET_SCREEN);
		
		scrollPane.prefWidthProperty().bind(SimuladorRede.getPrimaryStage().widthProperty());
		scrollPane.prefHeightProperty().bind(SimuladorRede.getPrimaryStage().heightProperty());
		
		//menu
		root.getChildren().add(0, getController(MenuPaneController.class).getView());
		
		//controls
		networkBoxLeft.getChildren().add(getController(ControlsPaneController.class).getView());
		
		//Painel dos detalhes dos elementos da rede
		networkBoxLeft.getChildren().add(getController(ElementsDetailsPaneController.class).getView());
		
		//Painel das funções
		networkBoxLeft.getChildren().add(getController(FunctionsPaneController.class).getView());
		
		//Painel de labels e messages
		networkBoxLeft.getChildren().add(getController(LabelAndMessagesPaneController.class).getView());
		
		//NetworkPane
		networkBoxRight.getChildren().add(getController(NetworkPaneController.class).getView());
		//networkPaneController = SimuladorRede.showUtilityScene("Electric Network", NetworkPaneController.class, false);
		
		this.fireEvent(EventType.RESET_SCREEN);
		
		/*File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.csv");
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
		//networkPaneController.getStage().show();
	}
	
	private void processResetScreen() {
		/*if (networkPaneController != null) {
			networkPaneController.getStage().hide();
		}*/
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
