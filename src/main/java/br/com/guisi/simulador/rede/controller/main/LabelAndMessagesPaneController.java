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
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.BrokenConstraintRow;
import br.com.guisi.simulador.rede.view.tableview.PropertyRow;
import br.com.guisi.simulador.rede.view.tableview.SwitchOperationRow;

public class LabelAndMessagesPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/LabelAndMessagesPane.fxml";

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
	
	@Override
	public void initializeController() {
		this.initializeTables();
		
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.POWER_FLOW_COMPLETED);
		this.listenToEvent(EventType.AGENT_NOTIFICATION);
		this.listenToEvent(EventType.BRANCH_SELECTED);
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
		tvBrokenConstraints.getItems().clear();
		tvSwitchesOperations.getItems().clear();
		tvSwitchesOperations.getItems().clear();
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
	 * Verifica as restrições quebradas e cria as mensagens
	 */
	private void updateWarningsBrokenConstraints() {
		if (getEnvironment() != null) {
			tvBrokenConstraints.getItems().clear();
			
			getEnvironment().getLoads().forEach((load) -> {
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
			
			getEnvironment().getFeeders().forEach((feeder) -> {
				if (feeder.isOn() && feeder.isPowerOverflow()) {
					BrokenConstraintRow constraint = new BrokenConstraintRow();
					constraint.getMessage().setValue("Feeder " + feeder.getNodeNumber() 
							+ ": Power overflow (max: " + feeder.getActivePower() + ", required: " + feeder.getUsedPower() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			getEnvironment().getBranches().forEach((branch) -> {
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
			int ini = tvSwitchesOperations.getItems().size();
			for (int i = ini; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				@SuppressWarnings("unchecked")
				List<SwitchOperation> switchOperations = agentStepStatus.getInformation(AgentInformationType.SWITCH_OPERATIONS, List.class);
				if (switchOperations != null) {
					for (SwitchOperation switchOperation : switchOperations) {
						SwitchOperationRow row = new SwitchOperationRow();
						row.getMessage().setValue(new StringBuilder().append("Switch ").append(switchOperation.getSwitchNumber()).append(" ").append(switchOperation.getSwitchState().getDescription()).toString());
						tvSwitchesOperations.getItems().add(row);
					}
				}
			}
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
		
		Branch branch = SimuladorRede.getEnvironment().getBranch((Integer) data);
		
		if (branch.isSwitchBranch() && !branch.hasFault() && !branch.isIsolated()) {
			PropertyRow row = new PropertyRow("S: ", "Switch " + branch.getNumber());
			tvAgentLearning.getItems().add(row);
			
			List<LearningProperty> learningProperties = agentControl.getAgent().getLearningProperties(branch.getNumber());
			for (LearningProperty learningProperty : learningProperties) {
				row = new PropertyRow(learningProperty.getProperty(), learningProperty.getValue());
				tvAgentLearning.getItems().add(row);
			}
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
