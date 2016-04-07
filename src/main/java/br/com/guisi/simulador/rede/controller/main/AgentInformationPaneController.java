package br.com.guisi.simulador.rede.controller.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.PropertyRow;
import br.com.guisi.simulador.rede.view.tableview.SwitchOperationRow;

public class AgentInformationPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/AgentInformationPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<SwitchOperationRow> tvSwitchesOperations;
	@FXML
	private TableView<PropertyRow> tvAgentLearning;
	
	@Inject
	private AgentControl agentControl;
	
	private int stepUpdateReceived;
	
	@PostConstruct
	public void initializeController() {
		this.initializeTables();
		
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.AGENT_NOTIFICATION,
				EventType.BRANCH_SELECTED);
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
			case BRANCH_SELECTED: this.processBranchSelected(data); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		this.stepUpdateReceived = 0;
		tvSwitchesOperations.getItems().clear();
		tvAgentLearning.getItems().clear();
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
		root.prefWidthProperty().bind(getStage().getScene().widthProperty());
		tvSwitchesOperations.prefWidthProperty().bind(getStage().getScene().widthProperty());
		tvAgentLearning.prefWidthProperty().bind(getStage().getScene().widthProperty());
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
		tvAgentLearning.setPlaceholder(new Label("No switch selected"));
		TableColumn<PropertyRow, String> tcPropertyName = new TableColumn<PropertyRow, String>();
		tcPropertyName.setCellValueFactory(cellData -> cellData.getValue().getPropertyName());
		tcPropertyName.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
		tcPropertyName.prefWidthProperty().bind(tvAgentLearning.widthProperty().divide(2));
		tvAgentLearning.getColumns().add(tcPropertyName);
		
		TableColumn<PropertyRow, String> tcPropertyValue = new TableColumn<PropertyRow, String>();
		tcPropertyValue.setCellValueFactory(cellData -> cellData.getValue().getPropertyValue());
		tcPropertyValue.setStyle("-fx-alignment: center-left;");
		tcPropertyValue.prefWidthProperty().bind(tvAgentLearning.widthProperty().divide(2));
		tvAgentLearning.getColumns().add(tcPropertyValue);
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		
		if (agentStatus != null) {
			for (int i = stepUpdateReceived; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				SwitchOperation switchOperation = agentStepStatus.getInformation(AgentInformationType.SWITCH_OPERATION, SwitchOperation.class);
				if (switchOperation != null) {
					SwitchOperationRow row = new SwitchOperationRow();
					row.getMessage().setValue(new StringBuilder().append("Switch ").append(switchOperation.getSwitchNumber()).append(" ").append(switchOperation.getSwitchState().getDescription()).toString());
					tvSwitchesOperations.getItems().add(row);
				}
			}
			stepUpdateReceived = agentStatus.getStepStatus().size();
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
	
	private void processBranchSelected(Object data) {
		tvAgentLearning.getItems().clear();
		
		/*Branch branch = getEnvironment().getBranch((Integer) data);
		
		if (branch.isSwitchBranch() && !branch.hasFault() && !branch.isIsolated()) {
			List<LearningProperty> learningProperties = agentControl.getAgent().getLearningProperties(branch.getNumber());
			
			Collections.sort(learningProperties, (LearningProperty o1, LearningProperty o2) -> {
				if (!o1.getValue().equals(o2.getValue())) {
					return o2.getValue().compareTo(o1.getValue());
				} else {
					return o1.getProperty().compareTo(o2.getProperty());
				}
			});
			
			for (LearningProperty learningProperty : learningProperties) {
				PropertyRow row = new PropertyRow(learningProperty.getProperty(), learningProperty.getValue());
				tvAgentLearning.getItems().add(row);
			}
		}*/
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
