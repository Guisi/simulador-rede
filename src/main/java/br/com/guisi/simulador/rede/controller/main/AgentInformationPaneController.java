package br.com.guisi.simulador.rede.controller.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.agent.data.LearningPropertyPair;
import br.com.guisi.simulador.rede.agent.data.SwitchOperation;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.PropertyRow;
import br.com.guisi.simulador.rede.view.tableview.PropertyRowPair;
import br.com.guisi.simulador.rede.view.tableview.SwitchOperationRow;

public class AgentInformationPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/AgentInformationPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<SwitchOperationRow> tvSwitchesOperations;
	@FXML
	private TableView<PropertyRowPair> tvAgentLearning;
	@FXML
	private ComboBox<Integer> cbSelectedSwitch;
	@FXML
	private CheckBox cbOnlyPerformedActions;
	
	@Inject
	private AgentControl agentControl;
	
	private int stepProcessed;
	
	@PostConstruct
	public void initializeController() {
		this.initializeTables();
		
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.AGENT_NOTIFICATION,
				EventType.AGENT_RUNNING);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.processEnvironmentLoaded(); break;
			case AGENT_NOTIFICATION : this.processAgentNotification(data); break;
			case AGENT_RUNNING : this.processAgentRunning(); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		this.stepProcessed = 0;
		tvSwitchesOperations.getItems().clear();
		tvAgentLearning.getItems().clear();
		
		cbSelectedSwitch.setValue(null);
		cbSelectedSwitch.setItems(FXCollections.observableArrayList());
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
		root.prefWidthProperty().bind(getStage().getScene().widthProperty());
		tvSwitchesOperations.prefWidthProperty().bind(getStage().getScene().widthProperty());
		tvAgentLearning.prefWidthProperty().bind(getStage().getScene().widthProperty());
		
		getLearningEnvironment().getSwitches().forEach(sw -> cbSelectedSwitch.getItems().add(sw.getNumber()));
	}
	
	private void processAgentRunning() {
		cbSelectedSwitch.valueProperty().setValue(null);
	}

	private void processAgentNotification(Object data) {
		AgentData agentData = (AgentData) data;
		
		if (agentData != null) {
			for (int i = stepProcessed; i < agentData.getAgentStepData().size(); i++) {
				AgentStepData agentStepStatus = agentData.getAgentStepData().get(i);
				
				SwitchOperation switchOperation = agentStepStatus.getData(AgentDataType.SWITCH_OPERATION, SwitchOperation.class);
				if (switchOperation != null) {
					SwitchOperationRow row = new SwitchOperationRow();
					row.getMessage().setValue(new StringBuilder().append("Switch ").append(switchOperation.getSwitchNumber()).append(" ").append(switchOperation.getSwitchState().getDescription()).toString());
					tvSwitchesOperations.getItems().add(row);
				}
			}
			stepProcessed = agentData.getAgentStepData().size();
		}
	}
	
	public void exportSwitchOperations() {
		StringBuilder sb = new StringBuilder();
		tvSwitchesOperations.getItems().forEach((operation) -> sb.append(operation.getMessage().getValue()).append(System.lineSeparator()));
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save .txt file");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT", "*.txt"));
		File txtFile = fileChooser.showSaveDialog(null);
		
		if (txtFile != null) {
			try (FileWriter writer = new FileWriter(txtFile)) {
				writer.write(sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(e.getMessage());
				alert.showAndWait();
			}
		}
	}
	
	public void changeCbSelectedSwitch() {
		tvAgentLearning.getItems().clear();
		
		Integer selected = cbSelectedSwitch.valueProperty().get();
		
		if (selected != null) {
			List<LearningPropertyPair> learningProperties = agentControl.getAgent().getLearningProperties(selected, cbOnlyPerformedActions.isSelected());
			
			for (LearningPropertyPair learningPropertyPair : learningProperties) {
				PropertyRowPair pair = new PropertyRowPair();
				if (learningPropertyPair.getLearningProperty1() != null) {
					pair.setPropertyRow1(new PropertyRow(learningPropertyPair.getLearningProperty1().getProperty(), learningPropertyPair.getLearningProperty1().getValue()));
				}
				if (learningPropertyPair.getLearningProperty2() != null) {
					pair.setPropertyRow2(new PropertyRow(learningPropertyPair.getLearningProperty2().getProperty(), learningPropertyPair.getLearningProperty2().getValue()));
				}
				tvAgentLearning.getItems().add(pair);
			}
		}
	}
	
	private Environment getLearningEnvironment() {
		return SimuladorRede.getEnvironment(EnvironmentKeyType.LEARNING_ENVIRONMENT);
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
	private void initializeTables() {
		// tabela de switch operations
		tvSwitchesOperations.widthProperty().addListener((source, oldWidth, newWidth) -> {
			Pane header = (Pane) tvSwitchesOperations.lookup("TableHeaderRow");
			if (header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
			}
		});
		tvSwitchesOperations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tvSwitchesOperations.setItems(FXCollections.observableArrayList());
		tvSwitchesOperations.setPlaceholder(new Label("No switches operations yet"));
		TableColumn<SwitchOperationRow, String> tcSwitchOperation = new TableColumn<SwitchOperationRow, String>();
		tcSwitchOperation.setCellValueFactory(cellData -> cellData.getValue().getMessage());
		tcSwitchOperation.setStyle("-fx-alignment: center-left; -fx-text-fill: red;");
		tcSwitchOperation.prefWidthProperty().bind(tvSwitchesOperations.widthProperty());
		tvSwitchesOperations.getColumns().add(tcSwitchOperation);
		
		// tabela de aprendizado do agente
		tvAgentLearning.widthProperty().addListener((source, oldWidth, newWidth) -> {
			Pane header = (Pane) tvAgentLearning.lookup("TableHeaderRow");
			if (header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
			}
		});
		
		tvAgentLearning.setItems(FXCollections.observableArrayList());
		tvAgentLearning.setPlaceholder(new Label("No data"));
		TableColumn<PropertyRowPair, String> tcPropertyName = new TableColumn<PropertyRowPair, String>();
		tcPropertyName.setCellValueFactory(cellData -> cellData.getValue().getPropertyRow1() != null ? cellData.getValue().getPropertyRow1().getPropertyName() : null);
		tcPropertyName.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
		tcPropertyName.prefWidthProperty().bind(tvAgentLearning.widthProperty().divide(4));
		tvAgentLearning.getColumns().add(tcPropertyName);
		
		TableColumn<PropertyRowPair, String> tcPropertyValue = new TableColumn<PropertyRowPair, String>();
		tcPropertyValue.setCellValueFactory(cellData -> cellData.getValue().getPropertyRow1() != null ? cellData.getValue().getPropertyRow1().getPropertyValue() : null);
		tcPropertyValue.setStyle("-fx-alignment: center-left;");
		tcPropertyValue.prefWidthProperty().bind(tvAgentLearning.widthProperty().divide(4));
		tvAgentLearning.getColumns().add(tcPropertyValue);
		
		tcPropertyName = new TableColumn<PropertyRowPair, String>();
		tcPropertyName.setCellValueFactory(cellData -> cellData.getValue().getPropertyRow2() != null ? cellData.getValue().getPropertyRow2().getPropertyName() : null);
		tcPropertyName.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
		tcPropertyName.prefWidthProperty().bind(tvAgentLearning.widthProperty().divide(4));
		tvAgentLearning.getColumns().add(tcPropertyName);
		
		tcPropertyValue = new TableColumn<PropertyRowPair, String>();
		tcPropertyValue.setCellValueFactory(cellData -> cellData.getValue().getPropertyRow2() != null ? cellData.getValue().getPropertyRow2().getPropertyValue() : null);
		tcPropertyValue.setStyle("-fx-alignment: center-left;");
		tcPropertyValue.prefWidthProperty().bind(tvAgentLearning.widthProperty().divide(4));
		tvAgentLearning.getColumns().add(tcPropertyValue);
	}

}