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
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.BrokenConstraintRow;
import br.com.guisi.simulador.rede.view.tableview.SwitchOperationRow;

public class LabelAndMessagesPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/LabelAndMessagesPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<BrokenConstraintRow> tvBrokenConstraints;
	@FXML
	private TableView<SwitchOperationRow> tvSwitchesOperations;
	
	@Override
	public void initializeController() {
		this.initializeTables();
		
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.POWER_FLOW_COMPLETED);
		this.listenToEvent(EventType.AGENT_NOTIFICATION);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case POWER_FLOW_COMPLETED: this.updateWarningsBrokenConstraints(); break;
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case AGENT_NOTIFICATION : this.processAgentNotification(data); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		tvBrokenConstraints.getItems().clear();
		tvSwitchesOperations.getItems().clear();
	}
	
	private void onEnvironmentLoaded() {
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
				SwitchOperation switchOperation = agentStepStatus.getInformation(AgentInformationType.SWITCH_OPERATION, SwitchOperation.class);
				if (switchOperation != null) {
					SwitchOperationRow row = new SwitchOperationRow();
					row.getMessage().setValue(new StringBuilder().append("Switch ").append(switchOperation.getSwitchNumber()).append(" ").append(switchOperation.getSwitchState()).toString());
					tvSwitchesOperations.getItems().add(row);
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
	
	@Override
	public Node getView() {
		return root;
	}

}
