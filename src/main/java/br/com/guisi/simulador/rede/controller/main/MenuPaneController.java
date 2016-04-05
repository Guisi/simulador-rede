package br.com.guisi.simulador.rede.controller.main;

import java.io.File;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.annotation.PostConstruct;

import br.com.guisi.simulador.rede.SimuladorRede;
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

public class MenuPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/MenuPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private MenuBar menuBar;
	@FXML
	private MenuItem miExpressionEvaluator;
	@FXML
	private MenuItem miEnvironmentCharts;
	@FXML
	private MenuItem miLearningCharts;
	@FXML
	private MenuItem miInteractionEnvironment;
	@FXML
	private Menu menuEnvironment;
	@FXML
	private Menu menuView;
	@FXML
	private Menu menuOptions;
	
	private File xlsFile;
	
	private EnvironmentController interactionEnvironmentController;
	
	@PostConstruct
	public void initializeController() {
		menuBar.prefWidthProperty().bind(SimuladorRede.getPrimaryStage().widthProperty());
		
		this.listenToEvent(EventType.ENVIRONMENT_LOADED,
				EventType.AGENT_RUNNING,
				EventType.AGENT_STOPPED,
				EventType.RESET_SCREEN,
				EventType.RELOAD_ENVIRONMENT);
		
		//inicializa controlers para que escutem os eventos 
		getController(EnvironmentChartsPaneController.class);
		getController(LearningChartsPaneController.class);
		interactionEnvironmentController = getController(EnvironmentController.class);
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
			this.fireEvent(EventType.RESET_SCREEN);
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
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(xlsFile);
			SimuladorRede.setEnvironment(environment);
			
			boolean powerFlowSuccess = false;
			
			//primeiro valida se rede está radial
			List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
			
			if (exceptions.isEmpty()) {
				//isola as faltas
				EnvironmentUtils.isolateFaultSwitches(SimuladorRede.getInitialEnvironment());
				EnvironmentUtils.isolateFaultSwitches(SimuladorRede.getInteractionEnvironment());
				EnvironmentUtils.isolateFaultSwitches(SimuladorRede.getLearningEnvironment());
				
				//executa o fluxo de potência
				try {
					powerFlowSuccess = PowerFlow.execute(SimuladorRede.getInitialEnvironment());
					powerFlowSuccess = PowerFlow.execute(SimuladorRede.getInteractionEnvironment());
					powerFlowSuccess = PowerFlow.execute(SimuladorRede.getLearningEnvironment());
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
	
	public void showEnvironmentChartsWindow() {
		SimuladorRede.showUtilityScene("Environment Charts", EnvironmentChartsPaneController.class, true, false);
	}
	
	public void showLearningChartsWindow() {
		SimuladorRede.showUtilityScene("Learning Charts", LearningChartsPaneController.class, true, false);
	}
	
	public void showInteractionEnvironmentController() {
		SimuladorRede.showUtilityScene("Interaction Environment Controller", interactionEnvironmentController, true, false);
	}

	private void resetScreen() {
		miExpressionEvaluator.setDisable(true);
		miEnvironmentCharts.setDisable(true);
		miLearningCharts.setDisable(true);
		miInteractionEnvironment.setDisable(true);
	}
	
	private void onEnvironmentLoaded() {
		miExpressionEvaluator.setDisable(false);
		miEnvironmentCharts.setDisable(false);
		miLearningCharts.setDisable(false);
		miInteractionEnvironment.setDisable(false);
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
