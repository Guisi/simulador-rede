package br.com.guisi.simulador.rede.controller.main;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.controller.chart.EnvironmentChartsPaneController;
import br.com.guisi.simulador.rede.controller.chart.LearningChartsPaneController;
import br.com.guisi.simulador.rede.controller.environment.EnvironmentController;
import br.com.guisi.simulador.rede.controller.options.ExpressionEvaluatorController;
import br.com.guisi.simulador.rede.controller.options.FunctionsController;
import br.com.guisi.simulador.rede.controller.options.PriorityConfigController;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

public class MenuPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/MenuPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private MenuBar menuBar;
	@FXML
	private MenuItem miExpressionEvaluator;
	@FXML
	private MenuItem miLearningCharts;
	@FXML
	private MenuItem miInitialEnvironment;
	@FXML
	private MenuItem miInteractionEnvironment;
	@FXML
	private MenuItem miInteractionEnvironmentCharts;
	@FXML
	private MenuItem miLearningEnvironment;
	@FXML
	private MenuItem miLearningEnvironmentCharts;
	@FXML
	private Menu menuEnvironment;
	@FXML
	private Menu menuView;
	@FXML
	private Menu menuOptions;
	
	private File xlsFile;
	
	private EnvironmentController initialEnvironmentController;
	private EnvironmentController interactionEnvironmentController;
	private EnvironmentChartsPaneController interactionEnvironmentChartsPaneController;
	private EnvironmentController learningEnvironmentController;
	private EnvironmentChartsPaneController learningEnvironmentChartsPaneController;
	private LearningChartsPaneController learningChartsPaneController;
	
	@PostConstruct
	public void initializeController() {
		menuBar.prefWidthProperty().bind(SimuladorRede.getPrimaryStage().widthProperty());
		
		this.listenToEvent(EventType.ENVIRONMENT_LOADED,
				EventType.AGENT_RUNNING,
				EventType.AGENT_STOPPED,
				EventType.RESET_SCREEN,
				EventType.RELOAD_ENVIRONMENT);
		
		//inicializa controlers para que escutem os eventos
		initialEnvironmentController = getController(EnvironmentController.class, EnvironmentKeyType.INITIAL_ENVIRONMENT);
		
		interactionEnvironmentController = getController(EnvironmentController.class, EnvironmentKeyType.INTERACTION_ENVIRONMENT);
		interactionEnvironmentChartsPaneController = getController(EnvironmentChartsPaneController.class, EnvironmentKeyType.INTERACTION_ENVIRONMENT);
		
		learningEnvironmentController = getController(EnvironmentController.class, EnvironmentKeyType.LEARNING_ENVIRONMENT);
		learningEnvironmentChartsPaneController = getController(EnvironmentChartsPaneController.class, EnvironmentKeyType.LEARNING_ENVIRONMENT);
		
		learningChartsPaneController = getController(LearningChartsPaneController.class);
		
		String lastEnvironmentFile = PropertiesUtils.getProperty(PropertyKey.LAST_ENVIRONMENT_FILE);
		if (lastEnvironmentFile != null && Files.exists(Paths.get(lastEnvironmentFile))) {
			menuEnvironment.getItems().add(new SeparatorMenuItem());
			
			MenuItem lastEnvironmentItem = new MenuItem(Paths.get(lastEnvironmentFile).getFileName().toString());
			lastEnvironmentItem.setOnAction(event -> this.loadLastEnvironment());
			menuEnvironment.getItems().add(lastEnvironmentItem);
		}
	}
	
	@Override
	protected void onSetStage(Stage stage) {
		super.onSetStage(stage);
		stage.setOnShown(event -> loadLastEnvironment());
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case AGENT_RUNNING: this.enableDisableScreen(true); break;
			case AGENT_STOPPED: this.enableDisableScreen(false); break;
			case RELOAD_ENVIRONMENT: this.reloadEnvironment(); break;
			default: break;
		}
	}
	
	private void loadLastEnvironment() {
		String lastEnvironmentFile = PropertiesUtils.getProperty(PropertyKey.LAST_ENVIRONMENT_FILE);
		if (lastEnvironmentFile != null && Files.exists(Paths.get(lastEnvironmentFile))) {
			xlsFile = new File(lastEnvironmentFile);
			this.loadEnvironmentFromFile(xlsFile);
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
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLSX", "*.xlsx"));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLS", "*.xls"));
		xlsFile = fileChooser.showOpenDialog(null);
		
		if (xlsFile != null) {
			this.loadEnvironmentFromFile(xlsFile);
		}
	}
	
	private void reloadEnvironment() {
		this.fireEvent(EventType.RESET_SCREEN);
		this.loadEnvironmentFromFile(xlsFile);
	}
	
	/**
	 * Carrega o ambiente a partir do arquivo
	 * @param xlsFile
	 */
	private void loadEnvironmentFromFile(File xlsFile) {
		try {
			this.fireEvent(EventType.RESET_SCREEN);

			Environment environment = EnvironmentUtils.getEnvironmentFromFile(xlsFile);
			
			boolean powerFlowSuccess = false;
			
			//primeiro valida se rede está radial
			List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
			
			if (exceptions.isEmpty()) {
				//isola as faltas
				EnvironmentUtils.isolateFaultSwitches(environment);
				
				//marca switches que podem ser tie-sw
				EnvironmentUtils.validateTieSwitches(environment);
				
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
			
			List<Cluster> clusters = EnvironmentUtils.mountClusters(environment);
			environment.setClusters(clusters);
			
			SimuladorRede.setEnvironment(environment);
			
			PropertiesUtils.saveProperty(PropertyKey.LAST_ENVIRONMENT_FILE, xlsFile.getAbsolutePath());
			
			this.fireEvent(EventType.ENVIRONMENT_LOADED);

			if (powerFlowSuccess) {
				this.fireEvent(EventType.POWER_FLOW_COMPLETED);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("Newton's method power flow did not converge");
				alert.showAndWait();
			}
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
	}
	
	public void showPriorityModal() {
		SimuladorRede.showModalScene("Priority Values", PriorityConfigController.class, true);
	}
	
	public void showExpressionEvaluatorWindow() {
		SimuladorRede.showUtilityScene("Expression Evaluator", ExpressionEvaluatorController.class, true, false);
	}
	
	public void showFunctionsWindow() {
		SimuladorRede.showModalScene("Functions", FunctionsController.class, true);
	}
	
	public void showLearningChartsWindow() {
		SimuladorRede.showUtilityScene("Learning Charts", learningChartsPaneController, true, false);
	}
	
	public void showInitialEnvironmentController() {
		SimuladorRede.showUtilityScene("Initial Environment", initialEnvironmentController, true, false);
	}
	
	public void showInteractionEnvironmentController() {
		SimuladorRede.showUtilityScene("Interaction Environment", interactionEnvironmentController, true, false);
	}
	
	public void showInteractionEnvironmentChartsWindow() {
		SimuladorRede.showUtilityScene("Interaction Environment Charts", interactionEnvironmentChartsPaneController, true, false);
	}
	
	public void showLearningEnvironmentController() {
		SimuladorRede.showUtilityScene("Learning Environment", learningEnvironmentController, true, false);
	}
	
	public void showLearningEnvironmentChartsWindow() {
		SimuladorRede.showUtilityScene("Learning Environment Charts", learningEnvironmentChartsPaneController, true, false);
	}

	private void resetScreen() {
		miExpressionEvaluator.setDisable(true);
		miLearningCharts.setDisable(true);
		miInitialEnvironment.setDisable(true);
		miInteractionEnvironment.setDisable(true);
		miInteractionEnvironmentCharts.setDisable(true);
		miLearningEnvironment.setDisable(true);
		miLearningEnvironmentCharts.setDisable(true);
	}
	
	private void onEnvironmentLoaded() {
		miExpressionEvaluator.setDisable(false);
		miInteractionEnvironmentCharts.setDisable(false);
		miLearningCharts.setDisable(false);
		miInitialEnvironment.setDisable(false);
		miInteractionEnvironment.setDisable(false);
		miLearningEnvironment.setDisable(false);
		miLearningEnvironmentCharts.setDisable(false);
	}
	
	private void enableDisableScreen(boolean disable) {
		menuEnvironment.setDisable(disable);
		menuOptions.setDisable(disable);
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
