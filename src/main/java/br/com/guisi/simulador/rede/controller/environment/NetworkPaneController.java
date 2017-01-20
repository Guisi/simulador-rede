package br.com.guisi.simulador.rede.controller.environment;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.agent.data.SwitchOperation;
import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EnvironmentEventData;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;
import br.com.guisi.simulador.rede.util.PropertiesUtils;
import br.com.guisi.simulador.rede.view.custom.NetworkNodeStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkPane;
import br.com.guisi.simulador.rede.view.custom.ZoomingPane;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

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
				EventType.AGENT_STOPPED,
				EventType.FAULT_CREATED);
		
		root = new VBox();
		
		networkPane = new NetworkPane(getEnvironmentKeyType());
		zoomingPane = new ZoomingPane(networkPane);
		zoomingPane.getStyleClass().add("networkPane");
		
		zoomSlider = new Slider();
		zoomSlider.setBlockIncrement(0.1);
		zoomSlider.setMin(0.1);
		zoomSlider.setMax(2);
		zoomSlider.setMinWidth(300);
		zoomSlider.setMaxWidth(300);
		zoomSlider.setPrefWidth(300);
		root.getChildren().add(zoomSlider);
		
		//bind do slider para o zoom do pane da rede
		zoomingPane.zoomFactorProperty().bind(zoomSlider.valueProperty());
		
		zoomSlider.valueProperty().addListener((v, oldValue, newValue) -> {
			PropertiesUtils.saveProperty(PropertyKey.ZOOM_SLIDER, getEnvironmentKeyType().name(), String.valueOf(newValue));
		});
		
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
			case FAULT_CREATED: this.processFaultCreated(data); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		networkPane.reset();
		this.stepProcessed = 0;
		zoomSlider.setValue(PropertiesUtils.getDoubleProperty(PropertyKey.ZOOM_SLIDER, getEnvironmentKeyType().name()));
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
			networkPane.drawBranch(branch, getEnvironment().getSizeX(), getEnvironment().getSizeY(), this, getEnvironmentKeyType());
		}
		
		//Desenha grid
		networkPane.drawGrid(getEnvironment().getSizeX(), getEnvironment().getSizeY());
		networkPane.setSnapToPixel(false);
		
		//Desenha a posição inicial do agent
		Object currentState = agentControl.getAgent().getCurrentState();
		if (currentState != null && currentState instanceof Branch) {
			networkPane.changeAgentCirclePosition( ((Branch) currentState).getNumber() );
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
		AgentData agentData = (AgentData) data;
		
		for (int i = stepProcessed; i < agentData.getAgentStepData().size(); i++) {
			AgentStepData agentStepStatus = agentData.getAgentStepData().get(i);
			
			SwitchOperation switchOperation = agentStepStatus.getData(AgentDataType.SWITCH_OPERATION, SwitchOperation.class);
			if (switchOperation != null) {
				Branch sw = getEnvironment().getBranch(switchOperation.getSwitchNumber());
				networkPane.updateBranchDrawing(sw);
				networkPane.changeAgentCirclePosition(sw.getNumber());

				//atualiza status dos nós na tela
				getEnvironment().getLoads().forEach((load) -> networkPane.updateLoadDrawing(load));
			}
		}
		
		stepProcessed = agentData.getAgentStepData().size();
	}
	
	private void processAgentStop() {
		getEnvironment().getSwitches().forEach((sw) -> networkPane.updateBranchDrawing(sw));

		//atualiza status dos loads na tela
		getEnvironment().getLoads().forEach((load) -> networkPane.updateLoadDrawing(load));
		
		//atualiza status dos feeders na tela
		getEnvironment().getFeeders().forEach((feeder) -> networkPane.updateFeederDrawing(feeder));
	}
	
	private void processFaultCreated(Object data) {
		Branch branch = (Branch) data;
		
		//primeiro valida se rede está radial
		Environment environment = getEnvironment();

		List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
		if (exceptions.isEmpty()) {
			//isola as faltas
			EnvironmentUtils.isolateFaultSwitches(environment);
			
			//marca switches que podem ser tie-sw
			EnvironmentUtils.validateTieSwitches(environment);
			
			//executa o fluxo de potência
			try {
				PowerFlow.execute(environment);
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
		
		List<Cluster> clusters = EnvironmentUtils.mountClusters(environment);
		environment.setClusters(clusters);
		
		networkPane.updateBranchDrawing(branch);

		//atualiza status dos nós na tela
		getEnvironment().getLoads().forEach((load) -> networkPane.updateLoadDrawing(load));
		getEnvironment().getBranches().forEach((brc) -> networkPane.updateBranchDrawing(brc));
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
