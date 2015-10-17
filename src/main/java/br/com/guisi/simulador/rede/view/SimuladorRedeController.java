package br.com.guisi.simulador.rede.view;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.FunctionType;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.functions.EvaluationObject;
import br.com.guisi.simulador.rede.functions.FunctionItem;
import br.com.guisi.simulador.rede.task.AgentTask;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.EvaluatorUtils;
import br.com.guisi.simulador.rede.util.FunctionsUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;
import br.com.guisi.simulador.rede.view.layout.BranchStackPane;
import br.com.guisi.simulador.rede.view.layout.NetworkNodeStackPane;
import br.com.guisi.simulador.rede.view.layout.NetworkPane;
import br.com.guisi.simulador.rede.view.layout.ZoomingPane;

public class SimuladorRedeController extends Controller {

	public static final String FXML_FILE = "/fxml/SimuladorRede.fxml";
	
	@FXML
	private VBox root;
	@FXML
	private MenuBar menuBar;
	@FXML
	private ScrollPane networkScrollPane;
	@FXML
	private Slider zoomSlider;
	@FXML
	private HBox networkBox;
	@FXML
	private MenuItem miExpressionEvaluator;
	@FXML
	private VBox controlsVBox;
	@FXML
	private Button btnRunAgent;
	@FXML
	private Button btnStopAgent;
	@FXML
	private Label lblCount;
	@FXML
	private ComboBox<TaskExecutionType> cbTaskExecutionType;

	/** Loads */
	@FXML
	private VBox boxLoadInfo;
	@FXML
	private ComboBox<Integer> cbLoadNumber;
	@FXML
	private Label lblLoadFeeder;
	@FXML
	private Label lblLoadActivePower;
	@FXML
	private Label lblLoadReactivePower;
	@FXML
	private Label lblLoadPriority;
	@FXML
	private Label lblLoadStatus;
	@FXML
	private Label lblLoadCurrentVoltage;
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
	private Label lblFeederActivePower;
	@FXML
	private Label lblFeederReactivePower;
	@FXML
	private Label lblFeederEnergizedLoads;
	@FXML
	private Label lblFeederPartiallyEnergizedLoadsLabel;
	@FXML
	private Label lblFeederPartiallyEnergizedLoads;
	@FXML
	private Label lblFeederNotEnergizedLoadsLabel;
	@FXML
	private Label lblFeederNotEnergizedLoads;
	@FXML
	private Label lblFeederUsedPower;
	@FXML
	private Label lblFeederAvailablePower;
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
	private Label lblBranchMaxCurrent;
	@FXML
	private Label lblBranchInstantCurrent;
	@FXML
	private Label lblBranchLossesMW;
	@FXML
	private Label lblBranchResistance;
	@FXML
	private Label lblBranchReactance;
	@FXML
	private Label lblBranchStatus;
	@FXML
	private Button btnPreviousBranch;
	@FXML
	private Button btnNextBranch;
	
	@FXML
	private TabPane tabPaneFunctions;
	
	private ZoomingPane zoomingPane;
	private NetworkPane networkPane;
	
	private Integer selectedLoad;
	private Integer selectedFeeder;
	private Integer selectedBranch;
	
	private AgentTask agentTask;
	private Integer count = 0;
	
	@Override
	public void initializeController(Object... data) {
		this.createFunctionTables();
		this.resetScreen();
		
		Image imageCheck = new Image(getClass().getResourceAsStream("/img/check.png"));
		btnRunAgent.setGraphic(new ImageView(imageCheck));
		
		Image imageDelete = new Image(getClass().getResourceAsStream("/img/delete.png"));
		btnStopAgent.setGraphic(new ImageView(imageDelete));
		
		cbTaskExecutionType.setItems(FXCollections.observableArrayList(Arrays.asList(TaskExecutionType.values())));
		cbTaskExecutionType.setValue(TaskExecutionType.CONTINUOUS_UPDATE_EVERY_STEP);
		
		/*File f = new File("C:/Users/Guisi/Desktop/modelo.csv");
		this.loadEnvironmentFromFile(f);*/
	}
	
	/**
	 * Volta a tela ao estado original
	 */
	public void resetScreen() {
		miExpressionEvaluator.setDisable(true);
		zoomSlider.setValue(1);
		
		networkPane = new NetworkPane();
		zoomingPane = new ZoomingPane(networkPane);
		zoomingPane.getStyleClass().add("networkPane");
		zoomingPane.zoomFactorProperty().bind(zoomSlider.valueProperty());
		networkScrollPane.setContent(zoomingPane);
		networkScrollPane.getStyleClass().add("networkPane");

		networkBox.setVisible(false);
		controlsVBox.setVisible(false);
		
		cbLoadNumber.setValue(null);
		lblLoadFeeder.setText("");
		lblLoadActivePower.setText("");
		lblLoadReactivePower.setText("");
		lblLoadPriority.setText("");
		lblLoadStatus.setText("");
		lblLoadCurrentVoltage.setText("");
		lblLoadMessages.setText("");
		
		cbFeederNumber.setValue(null);
		lblFeederActivePower.setText("");
		lblFeederReactivePower.setText("");
		lblFeederEnergizedLoads.setText("");
		lblFeederPartiallyEnergizedLoads.setText("");
		lblFeederPartiallyEnergizedLoadsLabel.setTooltip(new Tooltip("Partially energized loads"));
		lblFeederNotEnergizedLoads.setText("");
		lblFeederNotEnergizedLoadsLabel.setTooltip(new Tooltip("Not energized loads"));
		lblFeederUsedPower.setText("");
		lblFeederAvailablePower.setText("");
		
		cbBranchNumber.setValue(null);
		lblBranchDe.setText("");
		lblBranchPara.setText("");
		lblBranchMaxCurrent.setText("");
		lblBranchInstantCurrent.setText("");
		lblBranchLossesMW.setText("");
		lblBranchResistance.setText("");
		lblBranchReactance.setText("");
		lblBranchStatus.setText("");
		
		selectedLoad = null;
		selectedFeeder = null;
		selectedBranch = null;
		
		for (Tab tab: tabPaneFunctions.getTabs()) {
			@SuppressWarnings("unchecked")
			TableView<FunctionItem> tv = (TableView<FunctionItem>) tab.getContent();
			tv.getItems().clear();
		}
	}
	
	/**
	 * Cria os tabs e tabelas para cada tipo de função
	 */
	private void createFunctionTables() {
		tabPaneFunctions.getTabs().clear();
		for (FunctionType type : FunctionType.values()) {
			TableView<FunctionItem> tv = new TableView<FunctionItem>();
			tv.widthProperty().addListener((source, oldWidth, newWidth) -> {
                Pane header = (Pane) tv.lookup("TableHeaderRow");
                if (header.isVisible()){
                    header.setMaxHeight(0);
                    header.setMinHeight(0);
                    header.setPrefHeight(0);
                    header.setVisible(false);
                }
			});
			tv.setPrefHeight(170);
			tv.setItems(FXCollections.observableArrayList());
			
			TableColumn<FunctionItem, String> tcFunctionName = new TableColumn<FunctionItem, String>();
			tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionName());
			tcFunctionName.setStyle("-fx-font-weight: bold; -fx-alignment: center-right;");
			tcFunctionName.setPrefWidth(300);
			tv.getColumns().add(tcFunctionName);
			
			TableColumn<FunctionItem, String> tcFunctionResult = new TableColumn<FunctionItem, String>();
			tcFunctionResult.setPrefWidth(300);
			tcFunctionResult.setCellValueFactory(cellData -> cellData.getValue().getFunctionResult());
			tv.getColumns().add(tcFunctionResult);
			
			Tab tab = new Tab(type.getDescription());
			tab.setContent(tv);
			tabPaneFunctions.getTabs().add(tab);
		}
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
	
	/**
	 * Carrega o ambiente a partir do arquivo
	 * @param csvFile
	 */
	private void loadEnvironmentFromFile(File csvFile) {
		try {
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(csvFile);
			SimuladorRede.setEnvironment(environment);
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
		
		if (SimuladorRede.getEnvironment() != null) {
			String msgs = EnvironmentUtils.validateEnvironment(getEnvironment());
			
			if (StringUtils.isNotEmpty(msgs)) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(msgs);
				alert.show();
			}
			
			this.drawNetworkFromEnvironment();
			
			this.updateFunctionsTables();
		}
	}
	
	/**
	 * Com base nas funções cadastradas, executa as respectivas expressôes
	 * e inclui nas tabelas
	 */
	public void updateFunctionsTables() {
		if (getEnvironment() != null) {
			try {
				for (Tab tab: tabPaneFunctions.getTabs()) {
					@SuppressWarnings("unchecked")
					TableView<FunctionItem> tv = (TableView<FunctionItem>) tab.getContent();
					tv.getItems().clear();
				}
				
				EvaluationObject evaluationObject = new EvaluationObject();
				evaluationObject.setEnvironment(getEnvironment());
				
				List<FunctionItem> functions = FunctionsUtils.loadProperties();
				for (FunctionItem functionItem : functions) {
					FunctionType ft = FunctionType.getByDescription(functionItem.getFunctionType().get());
					functionItem.getFunctionName().set(functionItem.getFunctionName().get() + ":");
					String expression = functionItem.getFunctionExpression();
					
					String functionResult;
					try {
						Object result = EvaluatorUtils.evaluateExpression(evaluationObject, expression);
						functionResult = String.valueOf(result);
					} catch (Exception e) {
						functionResult = "Error in expression evaluation!";
					}
					functionItem.getFunctionResult().set(functionResult);
					
					@SuppressWarnings("unchecked")
					TableView<FunctionItem> tv = (TableView<FunctionItem>) tabPaneFunctions
						.getTabs().get(ft.ordinal()).getContent();
					tv.getItems().add(functionItem);
				}
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			}
		}
	}
	
	/**
	 * Desenha o ambiente na tela
	 */
	private void drawNetworkFromEnvironment() {
		//Seta visibilidade e tamanho dos panes da tela
		zoomingPane.setPrefWidth(getEnvironment().getSizeX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setPrefHeight(getEnvironment().getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10 + Constants.NETWORK_PANE_PADDING);
		zoomingPane.setContentWidth(zoomingPane.getPrefWidth());
		zoomingPane.setContentHeight(zoomingPane.getPrefHeight());
		
		networkBox.setVisible(true);
		controlsVBox.setVisible(true);
		miExpressionEvaluator.setDisable(false);
		
		//limpa o desenho anterior
		networkPane.getChildren().clear();
		
		//Desenha loads
		cbLoadNumber.setItems(FXCollections.observableArrayList());
		cbFeederNumber.setItems(FXCollections.observableArrayList());
		cbBranchNumber.setItems(FXCollections.observableArrayList());

		getEnvironment().getNetworkNodeMap().values().forEach((node) -> {
			NetworkNodeStackPane loadStack = networkPane.drawLoad(node, getEnvironment());
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
		for (Branch branch : getEnvironment().getBranches()) {
			EventHandler<MouseEvent> mouseClicked = (event) -> {
				Node node = (Node) event.getSource();
				while (!(node instanceof BranchStackPane)) {
					node = node.getParent();
				}
				cbBranchNumber.setValue(((BranchStackPane) node).getBranchNum());
			};
			networkPane.drawBranch(branch, getEnvironment().getSizeX(), getEnvironment().getSizeY(), mouseClicked);
			cbBranchNumber.getItems().add(branch.getNumber());
		}
		
		//Desenha grid
		networkPane.drawGrid(getEnvironment().getSizeX(), getEnvironment().getSizeY());
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
		
		Load load = getEnvironment().getLoad(selectedLoad);
		lblLoadFeeder.setText(load.getFeeder() != null ? load.getFeeder().getNodeNumber().toString() : "");
		lblLoadActivePower.setText(DecimalFormat.getNumberInstance().format(load.getActivePower()));
		lblLoadReactivePower.setText(DecimalFormat.getNumberInstance().format(load.getReactivePower()));
		lblLoadPriority.setText(String.valueOf(load.getPriority()));
		lblLoadStatus.setText(load.isOn() ? "On" : "Off");
		lblLoadCurrentVoltage.setText(DecimalFormat.getNumberInstance().format(load.getCurrentVoltagePU()));
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
		
		Feeder feeder = getEnvironment().getFeeder(selectedFeeder);
		lblFeederActivePower.setText(DecimalFormat.getNumberInstance().format(feeder.getActivePower()));
		lblFeederReactivePower.setText(DecimalFormat.getNumberInstance().format(feeder.getReactivePower()));
		/*lblFeederEnergizedLoads.setText(String.valueOf(feeder.getEnergizedLoads()));
		lblFeederPartiallyEnergizedLoads.setText(String.valueOf(feeder.getPartiallyEnergizedLoads()));
		lblFeederNotEnergizedLoads.setText(String.valueOf(feeder.getNotEnergizedLoads()));*/
		lblFeederUsedPower.setText(DecimalFormat.getNumberInstance().format(feeder.getUsedPower()));
		lblFeederAvailablePower.setText(DecimalFormat.getNumberInstance().format(feeder.getAvailablePower()));
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

		Branch branch = getEnvironment().getBranch(branchStackPane.getBranchNum());
		lblBranchDe.setText(branch.getNode1().getNodeNumber().toString());
		lblBranchPara.setText(branch.getNode2().getNodeNumber().toString());
		lblBranchMaxCurrent.setText(DecimalFormat.getNumberInstance().format(branch.getMaxCurrent()));
		lblBranchInstantCurrent.setText(DecimalFormat.getNumberInstance().format(branch.getInstantCurrent()));
		lblBranchLossesMW.setText(DecimalFormat.getNumberInstance().format(branch.getLossesMW()));
		lblBranchResistance.setText(DecimalFormat.getNumberInstance().format(branch.getResistance()));
		lblBranchReactance.setText(DecimalFormat.getNumberInstance().format(branch.getReactance()));
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
		} while (getEnvironment().getNetworkNode(selected).isFeeder());

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
		} while (getEnvironment().getNetworkNode(selected).isFeeder());

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
		} while (getEnvironment().getNetworkNode(selected).isLoad());

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
		} while (getEnvironment().getNetworkNode(selected).isLoad());

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
	
	public void showPriorityModal() {
		SimuladorRede.showModalScene("Priority Values", PriorityConfigController.FXML_FILE);
	}
	
	public void showExpressionEvaluatorWindow() {
		SimuladorRede.showUtilityScene("Expression Evaluator", ExpressionEvaluatorController.FXML_FILE);
	}
	
	public void showFunctionsWindow() {
		SimuladorRede.showModalScene("Functions", FunctionsController.FXML_FILE, this);
	}
	
	public void runAgent() {
		try {
			PowerFlow.executePowerFlow(getEnvironment());
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
		
		/*this.enableDisableScreen(true);
		agentTask = new AgentTask(count, cbTaskExecutionType.getValue());
		
		agentTask.valueProperty().addListener((observableValue, oldState, newState) -> {
			updateAgentStatus(newState);
		});
		
		agentTask.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
            	stopAgent();
            }
        });
		
		new Thread(agentTask).start();*/
	}
	
	public void stopAgent() {
		this.enableDisableScreen(false);
		agentTask.cancel();
    	count = agentTask.getValue();
    	updateAgentStatus(count);
	}
	
	private void updateAgentStatus(Integer newState) {
		lblCount.setText(String.valueOf(newState));
	}
	
	private void enableDisableScreen(boolean disable) {
		btnRunAgent.setDisable(disable);
		btnStopAgent.setDisable(!disable);
		cbTaskExecutionType.setDisable(disable);
		menuBar.setDisable(disable);
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
	@Override
	public String getFxmlFile() {
		return FXML_FILE;
	}
}
