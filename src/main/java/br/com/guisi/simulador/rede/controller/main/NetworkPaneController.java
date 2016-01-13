package br.com.guisi.simulador.rede.controller.main;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.util.PowerFlow;
import br.com.guisi.simulador.rede.view.custom.BranchStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkNodeStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkPane;
import br.com.guisi.simulador.rede.view.custom.ZoomingPane;

public class NetworkPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/NetworkPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private Slider zoomSlider;
	
	private ZoomingPane zoomingPane;
	private NetworkPane networkPane;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.LOAD_SELECTED);
		this.listenToEvent(EventType.FEEDER_SELECTED);
		this.listenToEvent(EventType.BRANCH_SELECTED);
		this.listenToEvent(EventType.AGENT_NOTIFICATION);
		this.listenToEvent(EventType.AGENT_STOPPED);
		
		this.resetScreen();
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case LOAD_SELECTED: this.processLoadSelected(data); break;
			case FEEDER_SELECTED: this.processFeederSelected(data); break;
			case BRANCH_SELECTED: this.processBranchSelected(data); break;
			//case AGENT_NOTIFICATION : this.processAgentNotification((AgentUpdates) data); break;
			case AGENT_STOPPED: this.processAgentStop(); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.getChildren().clear();
		root.setVisible(false);
		zoomSlider.setValue(1);
		
		networkPane = new NetworkPane();
		zoomingPane = new ZoomingPane(networkPane);
		zoomingPane.getStyleClass().add("networkPane");
		zoomingPane.zoomFactorProperty().bind(zoomSlider.valueProperty());
		
		root.getChildren().add(zoomSlider);
		root.getChildren().add(zoomingPane);
	}
	
	private void onEnvironmentLoaded() {
		root.setVisible(true);
		this.drawNetworkFromEnvironment();
	}
	
	/**
	 * Desenha o ambiente na tela
	 */
	private void drawNetworkFromEnvironment() {
		//limpa o desenho anterior
		networkPane.getChildren().clear();
		
		//Seta tamanho do painel
		zoomingPane.setPrefWidth(getEnvironment().getSizeX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setPrefHeight(getEnvironment().getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10 + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setContentWidth(zoomingPane.getPrefWidth());
		zoomingPane.setContentHeight(zoomingPane.getPrefHeight());
		
		//Desenha loads e feeders
		getEnvironment().getNetworkNodeMap().values().forEach((node) -> {
			NetworkNodeStackPane loadStack = networkPane.drawNetworkNode(node, getEnvironment());
			loadStack.setOnMouseClicked((event) -> {
				Integer nodeNumber = ((NetworkNodeStackPane)event.getSource()).getNetworkNodeNumber();
				this.fireEvent(node.isLoad() ? EventType.LOAD_SELECTED : EventType.FEEDER_SELECTED, nodeNumber);
			});
			 
		});
		
		//Desenha Branches
		for (Branch branch : getEnvironment().getBranches()) {
			EventHandler<MouseEvent> mouseClicked = (event) -> {
				Node node = (Node) event.getSource();
				while (!(node instanceof BranchStackPane)) {
					node = node.getParent();
				}
				this.fireEvent(EventType.BRANCH_SELECTED, ((BranchStackPane) node).getBranchNum());
			};
			networkPane.drawBranch(branch, getEnvironment().getSizeX(), getEnvironment().getSizeY(), mouseClicked);
		}
		
		//Desenha grid
		networkPane.drawGrid(getEnvironment().getSizeX(), getEnvironment().getSizeY());
		networkPane.setSnapToPixel(false);
	}
	
	private void processLoadSelected(Object data) {
		networkPane.selectLoad((Integer) data);
	}
	
	private void processFeederSelected(Object data) {
		networkPane.selectFeeder((Integer) data);
	}
	
	private void processBranchSelected(Object data) {
		networkPane.selectBranch((Integer) data);
	}
	
	/*private void processAgentNotification(AgentUpdates agentUpdates) {
		Integer switchChanged = agentUpdates.getIntegerNotification(AgentNotificationType.SWITCH_STATE_CHANGED);
		
		networkPane.setAgentCirclePosition(switchChanged);
		
		Environment environment = SimuladorRede.getEnvironment();
		
		//atualiza informações das conexões dos feeders e loads
		EnvironmentUtils.updateFeedersConnections(environment);
			
		Branch sw = environment.getBranch(switchChanged);
		networkPane.updateBranch(sw);
			
		//atualiza status dos nós na tela
		environment.getNetworkNodes().forEach((node) -> networkPane.updateNetworkNode(node));
	}*/
	
	private void processAgentStop() {
		Environment environment = SimuladorRede.getEnvironment();
		
		try {
			PowerFlow.execute(environment);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		environment.getSwitches().forEach((sw) -> networkPane.updateBranchDrawing(sw));
			
		//atualiza status dos loads na tela
		environment.getLoads().forEach((load) -> networkPane.updateLoadDrawing(load));
		
		//atualiza status dos feeders na tela
		environment.getFeeders().forEach((feeder) -> networkPane.updateFeederDrawing(feeder));
		
		this.fireEvent(EventType.POWER_FLOW_COMPLETED);
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
