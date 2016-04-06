package br.com.guisi.simulador.rede.controller.environment;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.events.EnvironmentEventData;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.custom.BranchStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkNodeStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkPane;
import br.com.guisi.simulador.rede.view.custom.ZoomingPane;

@Named
@Scope("prototype")
public class NetworkPaneController extends AbstractEnvironmentPaneController {

	@Inject
	private AgentControl agentControl;
	
	private VBox root;
	private ZoomingPane zoomingPane;
	private NetworkPane networkPane;
	private Slider zoomSlider;
	
	private int stepProcessed;
	
	public NetworkPaneController(EnvironmentKeyType environmentKeyType) {
		super(environmentKeyType);
	}
	
	@PostConstruct
	protected void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.LOAD_SELECTED,
				EventType.FEEDER_SELECTED,
				EventType.BRANCH_SELECTED,
				EventType.AGENT_NOTIFICATION,
				EventType.AGENT_STOPPED);
		
		root = new VBox();
		
		networkPane = new NetworkPane(getEnvironmentKeyType());
		zoomingPane = new ZoomingPane(networkPane);
		zoomingPane.getStyleClass().add("networkPane");
		
		zoomSlider = new Slider();
		zoomSlider.setBlockIncrement(0.1);
		zoomSlider.setMin(0.1);
		zoomSlider.setMax(2);
		zoomSlider.setMinWidth(150);
		zoomSlider.setMaxWidth(150);
		zoomSlider.setPrefWidth(150);
		root.getChildren().add(zoomSlider);
		
		//bind do slider para o zoom do pane da rede
		zoomingPane.zoomFactorProperty().bind(zoomSlider.valueProperty());
		
		root.getChildren().add(zoomingPane);
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
			case AGENT_NOTIFICATION : this.processAgentNotification(data); break;
			case AGENT_STOPPED: this.processAgentStop(); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		networkPane.reset();
		this.stepProcessed = 0;
		zoomSlider.setValue(0.7);
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
				this.fireEvent(node.isLoad() ? EventType.LOAD_SELECTED : EventType.FEEDER_SELECTED, new EnvironmentEventData(getEnvironmentKeyType(), nodeNumber));
			});
			 
		});
		
		//Desenha Branches
		for (Branch branch : getEnvironment().getBranches()) {
			EventHandler<MouseEvent> mouseClicked = (event) -> {
				Node node = (Node) event.getSource();
				while (!(node instanceof BranchStackPane)) {
					node = node.getParent();
				}
				this.fireEvent(EventType.BRANCH_SELECTED, new EnvironmentEventData(getEnvironmentKeyType(), ((BranchStackPane) node).getBranchNum()));
			};
			networkPane.drawBranch(branch, getEnvironment().getSizeX(), getEnvironment().getSizeY(), mouseClicked);
		}
		
		//Desenha grid
		networkPane.drawGrid(getEnvironment().getSizeX(), getEnvironment().getSizeY());
		networkPane.setSnapToPixel(false);
		
		//Desenha a posição inicial do agent
		Branch currentState = agentControl.getAgent().getCurrentState();
		if (currentState != null) {
			networkPane.changeAgentCirclePosition(currentState.getNumber());
		}
	}
	
	private void processLoadSelected(Object data) {
		EnvironmentEventData eventData = (EnvironmentEventData) data;
		if (eventData.getEnvironmentKeyType().equals(getEnvironmentKeyType())) {
			networkPane.selectLoad((Integer) eventData.getData());
		}
	}
	
	private void processFeederSelected(Object data) {
		EnvironmentEventData eventData = (EnvironmentEventData) data;
		if (eventData.getEnvironmentKeyType().equals(getEnvironmentKeyType())) {
			networkPane.selectFeeder((Integer) eventData.getData());
		}
	}
	
	private void processBranchSelected(Object data) {
		EnvironmentEventData eventData = (EnvironmentEventData) data;
		if (eventData.getEnvironmentKeyType().equals(getEnvironmentKeyType())) {
			networkPane.selectBranch((Integer) eventData.getData());
		}
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		
		if (agentStatus != null) {
			for (int i = stepProcessed; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				SwitchOperation switchOperation = agentStepStatus.getInformation(AgentInformationType.SWITCH_OPERATION, SwitchOperation.class);
				if (switchOperation != null) {
					Branch sw = getEnvironment().getBranch(switchOperation.getSwitchNumber());
					networkPane.updateBranchDrawing(sw);
					networkPane.changeAgentCirclePosition(sw.getNumber());

					//atualiza status dos nós na tela
					getEnvironment().getLoads().forEach((load) -> networkPane.updateLoadDrawing(load));
				}
			}
			
			stepProcessed = agentStatus.getStepStatus().size();
		}
	}
	
	private void processAgentStop() {
		getEnvironment().getSwitches().forEach((sw) -> networkPane.updateBranchDrawing(sw));

		//atualiza status dos loads na tela
		getEnvironment().getLoads().forEach((load) -> networkPane.updateLoadDrawing(load));
		
		//atualiza status dos feeders na tela
		getEnvironment().getFeeders().forEach((feeder) -> networkPane.updateFeederDrawing(feeder));
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
