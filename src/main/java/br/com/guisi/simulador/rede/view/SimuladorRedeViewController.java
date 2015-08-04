package br.com.guisi.simulador.rede.view;

import java.io.File;
import java.text.DecimalFormat;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.view.layout.BranchNode;
import br.com.guisi.simulador.rede.view.layout.LoadStackPane;
import br.com.guisi.simulador.rede.view.layout.NetworkPane;
import br.com.guisi.simulador.rede.view.layout.ZoomingPane;

public class SimuladorRedeViewController {

	private Stage mainStage;

	@FXML
	private ScrollPane networkScrollPane;
	@FXML
	private Button btnImportNetwork;
	@FXML
	private Pane labelPanel;
	@FXML
	private Slider zoomSlider;

	@FXML
	private VBox boxLoadInfo;
	@FXML
	private Label lblLoadNumber;
	@FXML
	private Label lblLoadFeeder;
	@FXML
	private Label lblLoadPower;
	@FXML
	private Label lblLoadPriority;
	
	@FXML
	private VBox boxFeederInfo;
	@FXML
	private Label lblFeederNumber;
	@FXML
	private Label lblFeederPower;
	
	@FXML
	private VBox boxBranchInfo;
	@FXML
	private Label lblBranchNumber;
	@FXML
	private Label lblBranchDe;
	@FXML
	private Label lblBranchPara;
	@FXML
	private Label lblBranchPower;
	@FXML
	private Label lblBranchDistance;
	@FXML
	private Label lblBranchStatus;
	
	private Environment environment;
	private ZoomingPane zoomingPane;
	private NetworkPane networkPane;
	
	private Shape selectedLoad;
	private Shape selectedFeeder;
	private Shape selectedBranch;

	public void initialize() {
		this.resetScreen();
	}
	
	/**
	 * Volta a tela ao estado original
	 */
	public void resetScreen() {
		zoomSlider.setValue(1);
		
		networkPane = new NetworkPane();
		zoomingPane = new ZoomingPane(networkPane);
		zoomingPane.getStyleClass().add("networkPane");
		zoomingPane.zoomFactorProperty().bind(zoomSlider.valueProperty());
		networkScrollPane.setContent(zoomingPane);
		networkScrollPane.getStyleClass().add("networkPane");

		networkScrollPane.setVisible(false);
		zoomingPane.setVisible(false);
		labelPanel.setVisible(false);
		zoomSlider.setVisible(false);
		
		boxLoadInfo.setVisible(false);
		lblLoadNumber.setText("");
		lblLoadFeeder.setText("");
		lblLoadPower.setText("");
		lblLoadPriority.setText("");
		
		boxFeederInfo.setVisible(false);
		lblFeederNumber.setText("");
		lblFeederPower.setText("");
		
		boxBranchInfo.setVisible(false);
		lblBranchNumber.setText("");
		lblBranchDe.setText("");
		lblBranchPara.setText("");
		lblBranchPower.setText("");
		lblBranchDistance.setText("");
		lblBranchStatus.setText("");
	}

	/**
	 * Abre di�logo de sele��o de arquivo
	 */
	public void openNetworkFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open CSV File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File csvFile = fileChooser.showOpenDialog(null);
		
		if (csvFile != null) {
			this.initialize();

			try {
				environment = EnvironmentUtils.getEnvironmentFromFile(csvFile);
				//EnvironmentUtils.validateEnvironment(environment);
				this.drawNetworkFromEnvironment();
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(e.getMessage());
				e.printStackTrace();
				alert.showAndWait();
			}
		}
	}
	
	/**
	 * Desenha o ambiente na tela
	 */
	private void drawNetworkFromEnvironment() {
		
		//Seta visibilidade e tamanho dos panes da tela
		networkScrollPane.setVisible(true);
		zoomingPane.setVisible(true);
		zoomingPane.setPrefWidth(environment.getSizeX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setPrefHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10 + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setContentWidth(zoomingPane.getPrefWidth());
		zoomingPane.setContentHeight(zoomingPane.getPrefHeight());
		
		labelPanel.setVisible(true);
		zoomSlider.setVisible(true);
		
		boxLoadInfo.setVisible(true);
		boxFeederInfo.setVisible(true);
		boxBranchInfo.setVisible(true);

		//limpa o desenho anterior
		networkPane.getChildren().clear();
		
		//Desenha loads
		for (Load node : environment.getLoadMap().values()) {
			LoadStackPane loadStack = networkPane.drawNode(node, environment);
			loadStack.setOnMouseClicked((event) -> {
				if (node.isLoad()) { 
					updateLoadInformationBox((LoadStackPane)event.getSource()); 
				} else {
					updateFeederInformationBox((LoadStackPane)event.getSource());	
				}
			});
		}
		
		//Desenha Branches
		for (Branch branch : environment.getBranchMap().values()) {
			EventHandler<MouseEvent> mouseClicked = (event) -> updateBranchInformationBox((BranchNode)event.getSource());
			networkPane.drawBranch(branch, environment.getSizeX(), environment.getSizeY(), mouseClicked);
		}
		
		//Desenha grid
		networkPane.drawGrid(environment.getSizeX(), environment.getSizeY());
		networkPane.setSnapToPixel(false);
	}
	
	/**
	 * Exibe na tela as informa��es do Load selecionado
	 * @param loadStackPane
	 */
	private void updateLoadInformationBox(LoadStackPane loadStackPane) {
		if (selectedLoad != null) {
			selectedLoad.setStroke(Color.BLACK);
			selectedLoad.setStrokeWidth(1);
		}
		selectedLoad = loadStackPane.getLoadShape();
		selectedLoad.setStroke(Color.TOMATO);
		selectedLoad.setStrokeWidth(2);
		
		Load load = environment.getLoad(loadStackPane.getLoadNum());
		lblLoadNumber.setText(load.getLoadNum().toString());
		lblLoadFeeder.setText(load.getFeeder() != null ? load.getFeeder().toString() : "");
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblLoadPower.setText(df.format(load.getLoadPower()));
		lblLoadPriority.setText(String.valueOf(load.getLoadPriority()));
	}
	
	/**
	 * Exibe na tela as informa��es do Feeder selecionado
	 * @param loadStackPane
	 */
	private void updateFeederInformationBox(LoadStackPane loadStackPane) {
		if (selectedFeeder != null) {
			selectedFeeder.setStroke(Color.BLACK);
			selectedFeeder.setStrokeWidth(1);
		}
		selectedFeeder = loadStackPane.getLoadShape();
		selectedFeeder.setStroke(Color.TOMATO);
		selectedFeeder.setStrokeWidth(2);
		
		Load load = environment.getLoad(loadStackPane.getLoadNum());
		lblFeederNumber.setText(load.getLoadNum().toString());
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblFeederPower.setText(df.format(load.getLoadPower()));
	}
	
	/**
	 * Exibe na tela as informa��es do Branch selecionado
	 * @param branchNode
	 */
	private void updateBranchInformationBox(BranchNode branchNode) {
		if (selectedBranch != null) {
			selectedBranch.setStroke(Color.BLACK);
			selectedBranch.setStrokeWidth(1);
		}
		selectedBranch = branchNode.getBranchLine();
		selectedBranch.setStroke(Color.TOMATO);
		selectedBranch.setStrokeWidth(2);

		Branch branch = environment.getBranch(branchNode.getBranchNum());
		lblBranchNumber.setText(branch.getBranchNum().toString());
		lblBranchDe.setText(branch.getLoad1().getLoadNum().toString());
		lblBranchPara.setText(branch.getLoad2().getLoadNum().toString());
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblBranchPower.setText(df.format(branch.getBranchPower()));
		lblBranchDistance.setText(DecimalFormat.getNumberInstance().format(branch.getDistance()));
		lblBranchStatus.setText(branch.isOn() ? "Ligado" : "Desligado");
	}
	
	public Stage getMainStage() {
		return mainStage;
	}

	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
}
