package br.com.guisi.simulador.rede.controller.main;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.BrokenConstraint;
import br.com.guisi.simulador.rede.view.tableview.SwitchOperation;

public class LabelAndMessagesPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/LabelAndMessagesPane.fxml";

	@FXML
	private VBox root;
	@FXML
	private TableView<BrokenConstraint> tvBrokenConstraints;
	@FXML
	private TableView<SwitchOperation> tvSwitchesOperations;

	@Override
	public void initializeController(Object... data) {
		this.initializeTables();
		
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.POWER_FLOW_COMPLETED);
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case POWER_FLOW_COMPLETED: this.updateWarningsBrokenConstraints(); break;
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
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

		TableColumn<BrokenConstraint, String> tcBrokenConstraint = new TableColumn<BrokenConstraint, String>();
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
		tvSwitchesOperations.setItems(FXCollections.observableArrayList());
		tvSwitchesOperations.setPlaceholder(new Label("No switches operations yet"));
		TableColumn<SwitchOperation, String> tcSwitchOperation = new TableColumn<SwitchOperation, String>();
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
					BrokenConstraint constraint = new BrokenConstraint();
					constraint.getMessage().setValue("Load " + load.getNodeNumber() + ": " + msg);
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			getEnvironment().getFeeders().forEach((feeder) -> {
				if (feeder.isOn() && feeder.isPowerOverflow()) {
					BrokenConstraint constraint = new BrokenConstraint();
					constraint.getMessage().setValue("Feeder " + feeder.getNodeNumber() 
							+ ": Power overflow (max: " + feeder.getActivePower() + ", required: " + feeder.getUsedPower() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
			
			getEnvironment().getBranches().forEach((branch) -> {
				if (branch.isClosed() && branch.isMaxCurrentOverflow()) {
					BrokenConstraint constraint = new BrokenConstraint();
					constraint.getMessage().setValue("Branch " + branch.getNumber() 
							+ ": Max current overflow (max: " + branch.getMaxCurrent() + ", required: " + branch.getInstantCurrent() + ")");
					tvBrokenConstraints.getItems().add(constraint);
				}
			});
		}
	}
	
	private void addSwitchesOperations() {
		/*switchesChanged.forEach((swNum) -> {
			SwitchOperation switchOperation = new SwitchOperation();
			switchOperation.getMessage().setValue("Switch " + swNum + (sw.isClosed() ? " closed" : " opened") );
			tvSwitchesOperations.getItems().add(switchOperation);
		});*/
	}

	@Override
	public Node getView() {
		return root;
	}

}
