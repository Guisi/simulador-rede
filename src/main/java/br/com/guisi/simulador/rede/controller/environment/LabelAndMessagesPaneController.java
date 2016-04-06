package br.com.guisi.simulador.rede.controller.environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.BrokenConstraintRow;
import br.com.guisi.simulador.rede.view.tableview.PropertyRow;
import br.com.guisi.simulador.rede.view.tableview.SwitchOperationRow;

public class LabelAndMessagesPaneController extends AbstractEnvironmentPaneController {

	public static final String FXML_FILE = "/fxml/environment/LabelAndMessagesPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<BrokenConstraintRow> tvBrokenConstraints;
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
				EventType.POWER_FLOW_COMPLETED,
				EventType.AGENT_NOTIFICATION,
				EventType.BRANCH_SELECTED);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case POWER_FLOW_COMPLETED: this.updateWarningsBrokenConstraints(); break;
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
		tvBrokenConstraints.getItems().clear();
		tvSwitchesOperations.getItems().clear();
		tvAgentLearning.getItems().clear();
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
	}

	private void initializeTables() {
		// tabela de broken constraints
		tvBrokenConstraints.widthProperty().addListener((source, oldWidth, newWidth) -> {
			Pane header = (Pane) tvBrokenConstraints.lookup("TableHeaderRow");
			if (header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
			}
		});
		tvBrokenConstraints.setItems(FXCollections.observableArrayList());
		tvBrokenConstraints.setPlaceholder(new Label("No broken constraints found"));

		TableColumn<BrokenConstraintRow, String> tcBrokenConstraint = new TableColumn<BrokenConstraintRow, String>();
		tcBrokenConstraint.setCellValueFactory(cellData -> cellData.getValue().getMessage());
		tcBrokenConstraint.setStyle("-fx-alignment: center-left; -fx-text-fill: red;");
		tcBrokenConstraint.setPrefWidth(590);
		tvBrokenConstraints.getColumns().add(tcBrokenConstraint);

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
		tcSwitchOperation.setPrefWidth(590);
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
		tcPropertyName.setPrefWidth(395);
		tvAgentLearning.getColumns().add(tcPropertyName);
		
		TableColumn<PropertyRow, String> tcPropertyValue = new TableColumn<PropertyRow, String>();
		tcPropertyValue.setCellValueFactory(cellData -> cellData.getValue().getPropertyValue());
		tcPropertyValue.setStyle("-fx-alignment: center-left;");
		tcPropertyValue.setPrefWidth(395);
		tvAgentLearning.getColumns().add(tcPropertyValue);
	}
	
	/**
	 * Verifica as restri��es quebradas e cria as mensagens
	 */
	private void updateWarningsBrokenConstraints() {
		Environment environment = getEnvironment();
		if (environment != null) {
			tvBrokenConstraints.getItems().clear();
			
			environment.getLoads().forEach((load) -> {
				String msg = null;
				if (load.isOn()) {
					if (load.getFeeder() == null) {
						msg = "Not connected to a feeder";
					} else if (load.isCurrentVoltageAboveLimit()) {
						msg = "Current voltage above limit (" + load.getCurrentVoltagePU() + ")";
					} else if (load.isCurrentVoltageBelowLimit()) {
						msg = "Current voltage below limit (" + load.getCurrentVoltagePU() + ")";
					}
				}
				if (msg != null) {
					BrokenConstraintRow constraint = new BrokenConstraintRow();
					constraint.getMessage().setValue("Load " + load.getNodeNumber() + ": " + msg);
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			environment.getFeeders().forEach((feeder) -> {
				if (feeder.isOn() && feeder.isPowerOverflow()) {
					BrokenConstraintRow constraint = new BrokenConstraintRow();
					constraint.getMessage().setValue("Feeder " + feeder.getNodeNumber() 
							+ ": Power overflow (max: " + feeder.getActivePowerKW() + ", required: " + feeder.getUsedActivePowerMW() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			environment.getBranches().forEach((branch) -> {
				if (branch.isClosed() && branch.isMaxCurrentOverflow()) {
					BrokenConstraintRow constraint = new BrokenConstraintRow();
					constraint.getMessage().setValue("Branch " + branch.getNumber() 
							+ ": Max current overflow (max: " + branch.getMaxCurrent() + ", required: " + branch.getInstantCurrent() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
		}
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
		
		Branch branch = getEnvironment().getBranch((Integer) data);
		
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
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}