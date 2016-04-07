package br.com.guisi.simulador.rede.controller.environment;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;

import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.BrokenConstraintRow;

public class LabelAndMessagesPaneController extends AbstractEnvironmentPaneController {

	public static final String FXML_FILE = "/fxml/environment/LabelAndMessagesPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<BrokenConstraintRow> tvBrokenConstraints;
	
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
	
	@Override
	public Node getView() {
		return root;
	}

}
