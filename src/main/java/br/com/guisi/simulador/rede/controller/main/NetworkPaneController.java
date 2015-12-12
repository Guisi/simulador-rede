package br.com.guisi.simulador.rede.controller.main;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.custom.BranchStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkNodeStackPane;
import br.com.guisi.simulador.rede.view.custom.NetworkPane;
import br.com.guisi.simulador.rede.view.custom.ZoomingPane;

public class NetworkPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/NetworkPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private ScrollPane networkScrollPane;
	@FXML
	private Slider zoomSlider;
	
	private ZoomingPane zoomingPane;
	private NetworkPane networkPane;
	
	private Integer selectedLoad;
	private Integer selectedFeeder;
	private Integer selectedBranch;
	
	@Override
	public void initializeController(Object... data) {
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.LOAD_SELECTED);
		this.listenToEvent(EventType.FEEDER_SELECTED);
		this.listenToEvent(EventType.BRANCH_SELECTED);
		
		networkPane = new NetworkPane();
		zoomingPane = new ZoomingPane(networkPane);
		zoomingPane.getStyleClass().add("networkPane");
		zoomingPane.zoomFactorProperty().bind(zoomSlider.valueProperty());
		networkScrollPane.setContent(zoomingPane);
		networkScrollPane.getStyleClass().add("networkPane");
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case LOAD_SELECTED: this.updateLoadDrawing((Integer) data); break;
			case FEEDER_SELECTED: this.updateFeederInformationBox((Integer) data); break;
			case BRANCH_SELECTED: this.updateBranchInformationBox((Integer) data); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		zoomSlider.setValue(1);
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
			NetworkNodeStackPane loadStack = networkPane.drawLoad(node, getEnvironment());
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
	
	private void updateLoadDrawing(Integer selected) {
		if (selectedLoad != null) {
			networkPane.updateNetworkNode(getEnvironment().getLoad(selectedLoad));
		}
		selectedLoad = selected;
		Shape shape = networkPane.getLoadPaneMap().get(selectedLoad).getNetworkNodeShape();
		shape.setStroke(Color.DARKORANGE);
		shape.setStrokeWidth(2);
	}
	
	private void updateFeederInformationBox(Integer selected) {
		if (selectedFeeder != null) {
			networkPane.updateNetworkNode(getEnvironment().getFeeder(selectedFeeder));
		}
		selectedFeeder = selected;
		Shape shape = networkPane.getLoadPaneMap().get(selectedFeeder).getNetworkNodeShape();
		shape.setStroke(Color.DARKORANGE);
		shape.setStrokeWidth(2);
	}
	
	private void updateBranchInformationBox(Integer selected) {
		if (selectedBranch != null) {
			networkPane.updateBranch(getEnvironment().getBranch(selectedBranch));
		}
		selectedBranch = selected;
		Line l = networkPane.getBranchPaneMap().get(selectedBranch).getBranchLine();
		l.setStroke(Color.DARKORANGE);
		l.setStrokeWidth(2);
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
