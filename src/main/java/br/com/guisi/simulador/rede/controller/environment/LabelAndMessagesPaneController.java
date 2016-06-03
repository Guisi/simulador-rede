package br.com.guisi.simulador.rede.controller.environment;

import java.util.Iterator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;

import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.MessageRow;

public class LabelAndMessagesPaneController extends AbstractEnvironmentPaneController {

	public static final String FXML_FILE = "/fxml/environment/LabelAndMessagesPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<MessageRow> tvBrokenConstraints;
	@FXML
	private TableView<MessageRow> tvClusters;
	
	@PostConstruct
	public void initializeController() {
		this.initializeTables();
		
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.POWER_FLOW_COMPLETED);
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
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		tvBrokenConstraints.getItems().clear();
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
		updateClustersTable();
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

		TableColumn<MessageRow, String> tcBrokenConstraint = new TableColumn<MessageRow, String>();
		tcBrokenConstraint.setCellValueFactory(cellData -> cellData.getValue().getMessage());
		tcBrokenConstraint.setStyle("-fx-alignment: center-left; -fx-text-fill: red;");
		tcBrokenConstraint.setPrefWidth(590);
		tvBrokenConstraints.getColumns().add(tcBrokenConstraint);
		
		// tabela de clusters
		tvClusters.widthProperty().addListener((source, oldWidth, newWidth) -> {
			Pane header = (Pane) tvClusters.lookup("TableHeaderRow");
			if (header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
			}
		});
		tvClusters.setItems(FXCollections.observableArrayList());
		tvClusters.setPlaceholder(new Label("No clusters found"));

		TableColumn<MessageRow, String> tcCluster = new TableColumn<MessageRow, String>();
		tcCluster.setCellValueFactory(cellData -> cellData.getValue().getMessage());
		tcCluster.setStyle("-fx-alignment: center-left; -fx-text-fill: red;");
		tcCluster.setPrefWidth(590);
		tvClusters.getColumns().add(tcCluster);
	}
	
	/**
	 * Verifica as restrições quebradas e cria as mensagens
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
						msg = "Voltage above limit (" + load.getCurrentVoltagePU() + ")";
					} else if (load.isCurrentVoltageBelowLimit()) {
						msg = "Voltage below limit (" + load.getCurrentVoltagePU() + ")";
					}
				}
				if (msg != null) {
					MessageRow constraint = new MessageRow();
					constraint.getMessage().setValue("Load " + load.getNodeNumber() + ": " + msg);
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			environment.getFeeders().forEach((feeder) -> {
				if (feeder.isOn() && feeder.isPowerOverflow()) {
					MessageRow constraint = new MessageRow();
					constraint.getMessage().setValue("Feeder " + feeder.getNodeNumber() 
							+ ": Power overflow (max: " + feeder.getActivePowerKW() + ", required: " + feeder.getUsedActivePowerMW() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			environment.getBranches().forEach((branch) -> {
				if (branch.isClosed() && branch.isMaxCurrentOverflow()) {
					MessageRow constraint = new MessageRow();
					constraint.getMessage().setValue("Branch " + branch.getNumber() 
							+ ": Max current overflow (max: " + branch.getMaxCurrent() + ", required: " + branch.getInstantCurrent() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
		}
	}
	
	/**
	 * Atualiza a tabela de clusters
	 */
	private void updateClustersTable() {
		Environment environment = getEnvironment();
		if (environment != null) {
			tvClusters.getItems().clear();
			
			environment.getClusters().forEach(cluster -> {
				StringBuilder sb = new StringBuilder();
				sb.append("Initial Tie-switch: " + cluster.getInitialTieSwitch().getNumber());
				sb.append(" - Closed switches: ");
				for (Iterator<Branch> iterator = cluster.getSwitches().iterator(); iterator.hasNext();) {
					Branch sw = iterator.next();
					if (sw.isClosed()) {
						sb.append(sw.getNumber());
						if (iterator.hasNext()) {
							sb.append(", ");
						}
					}
				}
				MessageRow row = new MessageRow();
				row.getMessage().setValue(sb.toString());
				tvClusters.getItems().add(row);
			});
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
