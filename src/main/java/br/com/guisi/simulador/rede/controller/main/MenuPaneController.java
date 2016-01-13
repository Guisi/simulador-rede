package br.com.guisi.simulador.rede.controller.main;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.controller.modal.ExpressionEvaluatorController;
import br.com.guisi.simulador.rede.controller.modal.FunctionsController;
import br.com.guisi.simulador.rede.controller.modal.PriorityConfigController;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
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
	
	@Override
	public void initializeController() {
		menuBar.prefWidthProperty().bind(SimuladorRede.getPrimaryStage().widthProperty());
		
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.AGENT_RUNNING);
		this.listenToEvent(EventType.AGENT_STOPPED);
		this.listenToEvent(EventType.RESET_SCREEN);
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
		File csvFile = fileChooser.showOpenDialog(null);
		
		if (csvFile != null) {
			this.fireEvent(EventType.RESET_SCREEN);
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
			
			if (getEnvironment() != null) {
				boolean powerFlowSuccess = false;
				
				//primeiro valida se rede está radial
				String errors = EnvironmentUtils.validateRadialState(environment);
				
				if (StringUtils.isEmpty(errors)) {
					//isola as faltas
					EnvironmentUtils.isolateFaultSwitches(environment);
					
					//executa o fluxo de potência
					try {
						PowerFlow.execute(environment);
						powerFlowSuccess = true;
					} catch (Exception e) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setContentText(e.getMessage());
						alert.showAndWait();
					}
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setContentText(errors);
					alert.showAndWait();
				}
				
				this.fireEvent(EventType.ENVIRONMENT_LOADED);

				if (powerFlowSuccess) {
					this.fireEvent(EventType.POWER_FLOW_COMPLETED);
				}
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
		SimuladorRede.showUtilityScene("Expression Evaluator", ExpressionEvaluatorController.class, true);
	}
	
	public void showFunctionsWindow() {
		SimuladorRede.showModalScene("Functions", FunctionsController.class, true);
	}

	private void resetScreen() {
		miExpressionEvaluator.setDisable(true);
	}
	
	private void onEnvironmentLoaded() {
		miExpressionEvaluator.setDisable(false);
	}
	
	private void enableDisableScreen(boolean disable) {
		menuBar.setDisable(disable);
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
