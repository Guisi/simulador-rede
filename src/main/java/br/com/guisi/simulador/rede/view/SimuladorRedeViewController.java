package br.com.guisi.simulador.rede.view;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.view.layout.BranchStackPane;
import br.com.guisi.simulador.rede.view.layout.NetworkNodeStackPane;
import br.com.guisi.simulador.rede.view.layout.NetworkPane;
import br.com.guisi.simulador.rede.view.layout.ZoomingPane;

public class SimuladorRedeViewController {

	private Stage mainStage;

	@FXML
	private ScrollPane networkScrollPane;
	@FXML
	private Button btnImportNetwork;
	@FXML
	private Slider zoomSlider;
	@FXML
	private HBox networkBox;

	/** Loads */
	@FXML
	private VBox boxLoadInfo;
	@FXML
	private ComboBox<Integer> cbLoadNumber;
	@FXML
	private Label lblLoadFeeder;
	@FXML
	private Label lblLoadPower;
	@FXML
	private Label lblLoadReceivedPower;
	@FXML
	private Label lblLoadPriority;
	@FXML
	private Label lblLoadStatus;
	@FXML
	private Label lblLoadMessages;
	@FXML
	private Button btnPreviousLoad;
	@FXML
	private Button btnNextLoad;
	
	/** Feeders */
	@FXML
	private VBox boxFeederInfo;
	@FXML
	private ComboBox<Integer> cbFeederNumber;
	@FXML
	private Label lblFeederPower;
	@FXML
	private Label lblFeederMinPower;
	@FXML
	private Label lblFeederMaxPower;
	@FXML
	private Label lblFeederMessages;
	@FXML
	private Button btnPreviousFeeder;
	@FXML
	private Button btnNextFeeder;
	
	/** Branches */
	@FXML
	private VBox boxBranchInfo;
	@FXML
	private ComboBox<Integer> cbBranchNumber;
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
	@FXML
	private Label lblBranchMessages;
	@FXML
	private Button btnPreviousBranch;
	@FXML
	private Button btnNextBranch;
	
	/** Power flow */
	@FXML
	private Label lblLoadsSupplied;
	@FXML
	private Label lblLoadsPartiallySupplied;
	@FXML
	private Label lblLoadsNotSupplied;
	@FXML
	private Label lblPowerSupplied;
	@FXML
	private Label lblPowerNotSupplied;
	@FXML
	private Label lblFeederUsedPower;
	@FXML
	private Label lblFeederAvailablePower;
	
	
	private Environment environment;
	private ZoomingPane zoomingPane;
	private NetworkPane networkPane;
	
	private Integer selectedLoad;
	private Integer selectedFeeder;
	private Integer selectedBranch;
	
	public void initialize() {
		this.resetScreen();
		
		/*File f = new File("C:/Users/Guisi/Desktop/modelo.csv");
		this.loadEnvironmentFromFile(f);*/
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

		networkBox.setVisible(false);
		zoomSlider.setVisible(false);

		cbLoadNumber.setValue(null);
		lblLoadFeeder.setText("");
		lblLoadPower.setText("");
		lblLoadReceivedPower.setText("");
		lblLoadPriority.setText("");
		lblLoadStatus.setText("");
		lblLoadMessages.setText("");
		
		cbFeederNumber.setValue(null);
		lblFeederPower.setText("");
		lblFeederMinPower.setText("");
		lblFeederMaxPower.setText("");
		lblFeederMessages.setText("");
		
		cbBranchNumber.setValue(null);
		lblBranchDe.setText("");
		lblBranchPara.setText("");
		lblBranchPower.setText("");
		lblBranchDistance.setText("");
		lblBranchStatus.setText("");
		lblBranchMessages.setText("");
		
		lblLoadsSupplied.setText("");
		lblLoadsPartiallySupplied.setText("");
		lblLoadsNotSupplied.setText("");
		lblPowerSupplied.setText("");
		lblPowerNotSupplied.setText("");
		lblFeederUsedPower.setText("");
		lblFeederAvailablePower.setText("");
		
		selectedLoad = null;
		selectedFeeder = null;
		selectedBranch = null;
	}
	
	/**
	 * Abre diálogo de seleção de arquivo
	 */
	public void openNetworkFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open CSV File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File csvFile = fileChooser.showOpenDialog(null);
		
		if (csvFile != null) {
			this.resetScreen();
			this.loadEnvironmentFromFile(csvFile);
		}
	}
	
	private void loadEnvironmentFromFile(File csvFile) {
		try {
			environment = EnvironmentUtils.getEnvironmentFromFile(csvFile);
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
		
		if (environment != null) {
			String msgs = EnvironmentUtils.validateEnvironment(environment);
			
			if (StringUtils.isNotEmpty(msgs)) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(msgs);
				alert.show();
			}
			
			this.drawNetworkFromEnvironment();
			
			this.calculatePowerFlowResult();
		}
	}
	
	private void calculatePowerFlowResult() {
		lblLoadsSupplied.setText(String.valueOf(environment.getLoadsSupplied()));
		lblLoadsPartiallySupplied.setText(String.valueOf(environment.getLoadsPartiallySupplied()));
		lblLoadsNotSupplied.setText(String.valueOf(environment.getLoadsNotSupplied()));

		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblPowerSupplied.setText(df.format(environment.getLoadPowerSupplied()));
		lblPowerNotSupplied.setText(df.format(environment.getLoadPowerNotSupplied()));
		
		lblFeederUsedPower.setText(df.format(environment.getFeederUsedPower()));
		lblFeederAvailablePower.setText(df.format(environment.getFeederAvailablePower()));
	}
	
	/**
	 * Desenha o ambiente na tela
	 */
	private void drawNetworkFromEnvironment() {
		
		//Seta visibilidade e tamanho dos panes da tela
		zoomingPane.setPrefWidth(environment.getSizeX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setPrefHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10 + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setContentWidth(zoomingPane.getPrefWidth());
		zoomingPane.setContentHeight(zoomingPane.getPrefHeight());
		
		networkBox.setVisible(true);
		zoomSlider.setVisible(true);
		
		//limpa o desenho anterior
		networkPane.getChildren().clear();
		
		//Desenha loads
		cbLoadNumber.setItems(FXCollections.observableArrayList());
		cbFeederNumber.setItems(FXCollections.observableArrayList());
		cbBranchNumber.setItems(FXCollections.observableArrayList());
		environment.getNetworkNodeMap().values().forEach((node) -> {
			NetworkNodeStackPane loadStack = networkPane.drawLoad(node, environment);
			loadStack.setOnMouseClicked((event) -> {
				if (node.isLoad()) { 
					cbLoadNumber.setValue(((NetworkNodeStackPane)event.getSource()).getNetworkNodeNumber());
				} else {
					cbFeederNumber.setValue(((NetworkNodeStackPane)event.getSource()).getNetworkNodeNumber());
				}
			});
			if (node.isLoad()) {
				cbLoadNumber.getItems().add(node.getNodeNumber());
			} else {
				cbFeederNumber.getItems().add(node.getNodeNumber());
			}
		});
		
		//Desenha Branches
		for (Branch branch : environment.getBranchMap().values()) {
			EventHandler<MouseEvent> mouseClicked = (event) -> {
				Node node = (Node) event.getSource();
				while (!(node instanceof BranchStackPane)) {
					node = node.getParent();
				}
				cbBranchNumber.setValue(((BranchStackPane) node).getBranchNum());
			};
			networkPane.drawBranch(branch, environment.getSizeX(), environment.getSizeY(), mouseClicked);
			cbBranchNumber.getItems().add(branch.getNumber());
		}
		
		//Desenha grid
		networkPane.drawGrid(environment.getSizeX(), environment.getSizeY());
		networkPane.setSnapToPixel(false);
	}
	
	/**
	 * Exibe na tela as informações do Load selecionado
	 * @param networkNodeStackPane
	 */
	private void updateLoadInformationBox(NetworkNodeStackPane networkNodeStackPane) {
		if (selectedLoad != null) {
			Shape shape = networkPane.getLoadPaneMap().get(selectedLoad).getNetworkNodeShape();
			shape.setStroke(Color.BLACK);
			shape.setStrokeWidth(1);
		}
		selectedLoad = networkNodeStackPane.getNetworkNodeNumber();
		Shape shape = networkPane.getLoadPaneMap().get(selectedLoad).getNetworkNodeShape();
		shape.setStroke(Color.DARKORANGE);
		shape.setStrokeWidth(2);
		
		Load load = environment.getLoad(selectedLoad);
		lblLoadFeeder.setText(load.getFeeder() != null ? load.getFeeder().getNodeNumber().toString() : "");
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblLoadPower.setText(df.format(load.getPower()));
		lblLoadReceivedPower.setText(df.format(load.getPowerSupplied()));
		lblLoadPriority.setText(String.valueOf(load.getPriority()));
		lblLoadStatus.setText(load.isOn() ? "On" : "Off");
		cbLoadNumber.setValue(load.getNodeNumber());
		
		String msgs = null;
		if (load.isOn() && !load.isSupplied()) {
			msgs = load.getSupplyStatus().getDescription();
		}
		lblLoadMessages.setText(msgs);
	}
	
	/**
	 * Exibe na tela as informações do Feeder selecionado
	 * @param networkNodeStackPane
	 */
	private void updateFeederInformationBox(NetworkNodeStackPane networkNodeStackPane) {
		if (selectedFeeder != null) {
			Shape shape = networkPane.getLoadPaneMap().get(selectedFeeder).getNetworkNodeShape();
			shape.setStroke(Color.BLACK);
			shape.setStrokeWidth(1);
		}
		selectedFeeder = networkNodeStackPane.getNetworkNodeNumber();
		Shape shape = networkPane.getLoadPaneMap().get(selectedFeeder).getNetworkNodeShape();
		shape.setStroke(Color.DARKORANGE);
		shape.setStrokeWidth(2);
		
		Feeder feeder = environment.getFeeder(selectedFeeder);
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblFeederPower.setText(df.format(feeder.getPower()));
		lblFeederMinPower.setText(df.format(feeder.getMinPower()));
		lblFeederMaxPower.setText(df.format(feeder.getMaxPower()));
		cbFeederNumber.setValue(feeder.getNodeNumber());
	}
	
	/**
	 * Exibe na tela as informações do Branch selecionado
	 * @param branchNode
	 */
	private void updateBranchInformationBox(BranchStackPane branchStackPane) {
		if (selectedBranch != null) {
			Line l = networkPane.getBranchPaneMap().get(selectedBranch).getBranchLine();
			l.setStroke(Color.BLACK);
			l.setStrokeWidth(1);
		}
		selectedBranch = branchStackPane.getBranchNum();
		Line l = networkPane.getBranchPaneMap().get(selectedBranch).getBranchLine();
		l.setStroke(Color.DARKORANGE);
		l.setStrokeWidth(2);

		Branch branch = environment.getBranch(branchStackPane.getBranchNum());
		lblBranchDe.setText(branch.getLoad1().getNodeNumber().toString());
		lblBranchPara.setText(branch.getLoad2().getNodeNumber().toString());
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		lblBranchPower.setText(df.format(branch.getPower()));
		lblBranchDistance.setText(DecimalFormat.getNumberInstance().format(branch.getDistance()));
		lblBranchStatus.setText(branch.isOn() ? "On" : "Off");
		cbBranchNumber.setValue(branch.getNumber());
	}
	
	/**
	 * Seleciona o load anterior
	 */
	public void previousLoad() {
		List<Integer> loadKeySet = new ArrayList<Integer>(networkPane.getLoadPaneMap().keySet());
		Integer selected = selectedLoad;
		
		do {
			if (selected == null) {
				selected = loadKeySet.get(loadKeySet.size() - 1);
			} else {
				int previousIndex = loadKeySet.indexOf(selected) - 1;
				selected = (previousIndex < 0) ? loadKeySet.get(loadKeySet.size()-1) : loadKeySet.get(previousIndex);
			}
		} while (environment.getNetworkNode(selected).isFeeder());

		cbLoadNumber.setValue(selected);
		this.updateLoadInformationBox(networkPane.getLoadPaneMap().get(selected));
	}
	
	/**
	 * Seleciona o próximo load
	 */
	public void nextLoad() {
		List<Integer> loadKeySet = new ArrayList<Integer>(networkPane.getLoadPaneMap().keySet());
		Integer selected = selectedLoad;
		do {
			if (selected == null) {
				selected = loadKeySet.get(0);
			} else {
				int nextIndex = loadKeySet.indexOf(selected) + 1;
				selected = (nextIndex == loadKeySet.size()) ? loadKeySet.get(0) : loadKeySet.get(nextIndex);
			}
		} while (environment.getNetworkNode(selected).isFeeder());

		cbLoadNumber.setValue(selected);
		this.updateLoadInformationBox(networkPane.getLoadPaneMap().get(selected));
	}
	
	/**
	 * Listener da ação de alteração da combo do número do load
	 */
	public void changeCbLoadNumber() {
		if (cbLoadNumber.valueProperty().get() != null) {
			this.updateLoadInformationBox(networkPane.getLoadPaneMap().get(cbLoadNumber.valueProperty().get()));
		}
	}
	
	/**
	 * Seleciona o feeder anterior
	 */
	public void previousFeeder() {
		List<Integer> loadKeySet = new ArrayList<Integer>(networkPane.getLoadPaneMap().keySet());
		Integer selected = selectedFeeder;
		
		do {
			if (selected == null) {
				selected = loadKeySet.get(loadKeySet.size() - 1);
			} else {
				int previousIndex = loadKeySet.indexOf(selected) - 1;
				selected = (previousIndex < 0) ? loadKeySet.get(loadKeySet.size()-1) : loadKeySet.get(previousIndex);
			}
		} while (environment.getNetworkNode(selected).isLoad());

		cbFeederNumber.setValue(selected);
		this.updateFeederInformationBox(networkPane.getLoadPaneMap().get(selected));
	}
	
	/**
	 * Seleciona o próximo feeder
	 */
	public void nextFeeder() {
		List<Integer> loadKeySet = new ArrayList<Integer>(networkPane.getLoadPaneMap().keySet());
		Integer selected = selectedFeeder;
		do {
			if (selected == null) {
				selected = loadKeySet.get(0);
			} else {
				int nextIndex = loadKeySet.indexOf(selected) + 1;
				selected = (nextIndex == loadKeySet.size()) ? loadKeySet.get(0) : loadKeySet.get(nextIndex);
			}
		} while (environment.getNetworkNode(selected).isLoad());

		cbFeederNumber.setValue(selected);
		this.updateFeederInformationBox(networkPane.getLoadPaneMap().get(selected));
	}
	
	/**
	 * Listener da ação de alteração da combo do número do feeder
	 */
	public void changeCbFeederNumber() {
		if (cbFeederNumber.valueProperty().get() != null) {
			this.updateFeederInformationBox(networkPane.getLoadPaneMap().get(cbFeederNumber.valueProperty().get()));
		}
	}
	
	/**
	 * Seleciona o branch anterior
	 */
	public void previousBranch() {
		List<Integer> branchKeySet = new ArrayList<Integer>(networkPane.getBranchPaneMap().keySet());
		Integer selected = selectedBranch;
		
		if (selected == null) {
			selected = branchKeySet.get(branchKeySet.size() - 1);
		} else {
			int previousIndex = branchKeySet.indexOf(selected) - 1;
			selected = (previousIndex < 0) ? branchKeySet.get(branchKeySet.size()-1) : branchKeySet.get(previousIndex);
		}

		cbBranchNumber.setValue(selected);
		this.updateBranchInformationBox(networkPane.getBranchPaneMap().get(selected));
	}
	
	/**
	 * Seleciona o próximo branch
	 */
	public void nextBranch() {
		List<Integer> branchKeySet = new ArrayList<Integer>(networkPane.getBranchPaneMap().keySet());
		Integer selected = selectedBranch;
		
		if (selected == null) {
			selected = branchKeySet.get(0);
		} else {
			int nextIndex = branchKeySet.indexOf(selected) + 1;
			selected = (nextIndex == branchKeySet.size()) ? branchKeySet.get(0) : branchKeySet.get(nextIndex);
		}

		cbBranchNumber.setValue(selected);
		this.updateBranchInformationBox(networkPane.getBranchPaneMap().get(selected));
	}
	
	/**
	 * Listener da ação de alteração da combo do número da branch
	 */
	public void changeCbBranchNumber() {
		if (cbBranchNumber.valueProperty().get() != null) {
			this.updateBranchInformationBox(networkPane.getBranchPaneMap().get(cbBranchNumber.valueProperty().get()));
		}
	}
	
	public Stage getMainStage() {
		return mainStage;
	}

	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
}
