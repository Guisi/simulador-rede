package br.com.guisi.simulador.rede.controller.main;

import java.text.DecimalFormat;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.PropertyRow;

public class ElementsDetailsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/ElementsDetailsPane.fxml";

	@FXML
	private VBox root;
	
	/** Loads */
	@FXML
	private ComboBox<Integer> cbLoadNumber;
	@FXML
	private TableView<PropertyRow> tvLoadDetails;
	@FXML
	private Button btnPreviousLoad;
	@FXML
	private Button btnNextLoad;
	
	/** Feeders */
	@FXML
	private ComboBox<Integer> cbFeederNumber;
	@FXML
	private TableView<PropertyRow> tvFeederDetails;
	@FXML
	private Button btnPreviousFeeder;
	@FXML
	private Button btnNextFeeder;

	/** Branches */
	@FXML
	private ComboBox<Integer> cbBranchNumber;
	@FXML
	private TableView<PropertyRow> tvBranchDetails;
	@FXML
	private Button btnPreviousBranch;
	@FXML
	private Button btnNextBranch;
	

	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.LOAD_SELECTED);
		this.listenToEvent(EventType.FEEDER_SELECTED);
		this.listenToEvent(EventType.BRANCH_SELECTED);
		
		// tabela de propriedades do load
		this.initializeTable(tvLoadDetails);
		this.initializeTable(tvFeederDetails);
		this.initializeTable(tvBranchDetails);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case LOAD_SELECTED: this.updateLoadInformationBox((Integer) data); break;
			case FEEDER_SELECTED: this.updateFeederInformationBox((Integer) data); break;
			case BRANCH_SELECTED: this.updateBranchInformationBox((Integer) data); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		
		cbLoadNumber.setValue(null);
		cbLoadNumber.setItems(FXCollections.observableArrayList());
		tvLoadDetails.getItems().clear();
		
		cbFeederNumber.setValue(null);
		cbFeederNumber.setItems(FXCollections.observableArrayList());
		tvFeederDetails.getItems().clear();
		
		cbBranchNumber.setValue(null);
		cbBranchNumber.setItems(FXCollections.observableArrayList());
		tvBranchDetails.getItems().clear();
	}

	private void initializeTable(TableView<PropertyRow> tableView) {
		tableView.widthProperty().addListener((source, oldWidth, newWidth) -> {
			Pane header = (Pane) tableView.lookup("TableHeaderRow");
			if (header.isVisible()) {
				header.setMaxHeight(0);
				header.setMinHeight(0);
				header.setPrefHeight(0);
				header.setVisible(false);
			}
		});
		tableView.setItems(FXCollections.observableArrayList());
		tableView.setPlaceholder(new Label(""));
		tableView.setStyle("-fx-focus-color: transparent; -fx-box-border: transparent;");

		TableColumn<PropertyRow, String> tcPropertyName = new TableColumn<PropertyRow, String>();
		tcPropertyName.setCellValueFactory(cellData -> cellData.getValue().getPropertyName());
		tcPropertyName.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");
		tcPropertyName.setPrefWidth(150);
		tableView.getColumns().add(tcPropertyName);
		
		TableColumn<PropertyRow, String> tcPropertyValue = new TableColumn<PropertyRow, String>();
		tcPropertyValue.setCellValueFactory(cellData -> cellData.getValue().getPropertyValue());
		tcPropertyValue.setStyle("-fx-alignment: center-left;");
		tcPropertyValue.setPrefWidth(100);
		tableView.getColumns().add(tcPropertyValue);
	}
	
	private void onEnvironmentLoaded() {
		root.setVisible(true);

		getEnvironment().getLoads().forEach(load -> cbLoadNumber.getItems().add(load.getNodeNumber()));
		getEnvironment().getFeeders().forEach(feeder -> cbFeederNumber.getItems().add(feeder.getNodeNumber()));
		getEnvironment().getBranches().forEach(branch -> cbBranchNumber.getItems().add(branch.getNumber()));
		Collections.sort(cbLoadNumber.getItems());
		Collections.sort(cbFeederNumber.getItems());
		Collections.sort(cbBranchNumber.getItems());
	}
	
	/**
	 * Exibe na tela as informações do Load selecionado
	 * @param networkNodeStackPane
	 */
	private void updateLoadInformationBox(Integer selectedLoad) {
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_5);
		Load load = getEnvironment().getLoad(selectedLoad);
		
		tvLoadDetails.getItems().clear();
		tvLoadDetails.getItems().add(new PropertyRow("Feeder:", load.getFeeder() != null ? load.getFeeder().getNodeNumber().toString() : ""));
		tvLoadDetails.getItems().add(new PropertyRow("Active Power kW:", df.format(load.getActivePower())));
		tvLoadDetails.getItems().add(new PropertyRow("Reactive Power kVar:", df.format(load.getReactivePower())));
		tvLoadDetails.getItems().add(new PropertyRow("Priority:", String.valueOf(load.getPriority())));
		tvLoadDetails.getItems().add(new PropertyRow("Status:", load.isOn() ? "On" : "Off"));
		tvLoadDetails.getItems().add(new PropertyRow("Current Voltage pu:", df.format(load.getCurrentVoltagePU())));
		cbLoadNumber.valueProperty().setValue(selectedLoad);
	}
	
	/**
	 * Exibe na tela as informações do Feeder selecionado
	 * @param networkNodeStackPane
	 */
	private void updateFeederInformationBox(Integer selectedFeeder) {
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_5);
		Feeder feeder = getEnvironment().getFeeder(selectedFeeder);
		
		tvFeederDetails.getItems().clear();
		tvFeederDetails.getItems().add(new PropertyRow("Active Power kW:", df.format(feeder.getActivePower())));
		tvFeederDetails.getItems().add(new PropertyRow("Reactive Power kVar:", df.format(feeder.getReactivePower())));
		tvFeederDetails.getItems().add(new PropertyRow("Energized Loads:", String.valueOf(feeder.getEnergizedLoads())));
		tvFeederDetails.getItems().add(new PropertyRow("Used Active Power:", df.format(feeder.getUsedPower())));
		tvFeederDetails.getItems().add(new PropertyRow("Available Active Power:", df.format(feeder.getAvailablePower())));
		cbFeederNumber.valueProperty().set(selectedFeeder);
	}
	
	/**
	 * Exibe na tela as informações do Branch selecionado
	 * @param branchNode
	 */
	private void updateBranchInformationBox(Integer selectedBranch) {
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_3);
		Branch branch = getEnvironment().getBranch(selectedBranch);
		
		tvBranchDetails.getItems().clear();
		tvBranchDetails.getItems().add(new PropertyRow("From:", branch.getNode1().getNodeNumber().toString()));
		tvBranchDetails.getItems().add(new PropertyRow("To:", branch.getNode2().getNodeNumber().toString()));
		tvBranchDetails.getItems().add(new PropertyRow("Max Current A:", df.format(branch.getMaxCurrent())));
		tvBranchDetails.getItems().add(new PropertyRow("Instant Current A:", df.format(branch.getInstantCurrent())));
		tvBranchDetails.getItems().add(new PropertyRow("Resistance \u03A9:", df.format(branch.getResistance())));
		tvBranchDetails.getItems().add(new PropertyRow("Reactance \u03A9:", df.format(branch.getReactance())));
		tvBranchDetails.getItems().add(new PropertyRow("Status:", branch.getSwitchState().getDescription()));
		tvBranchDetails.getItems().add(new PropertyRow("Losses MW:", df.format(branch.getLossesMW())));
		cbBranchNumber.valueProperty().set(selectedBranch);
	}
	
	/**
	 * Seleciona o load anterior
	 */
	public void previousLoad() {
		this.setPreviousFromList(cbLoadNumber);
	}
	
	/**
	 * Seleciona o próximo load
	 */
	public void nextLoad() {
		this.setNextFromList(cbLoadNumber);
	}
	
	/**
	 * Listener da ação de alteração da combo do número do load
	 */
	public void changeCbLoadNumber() {
		Integer selected = cbLoadNumber.valueProperty().get();
		if (selected != null) {
			this.fireEvent(EventType.LOAD_SELECTED, selected);
		}
	}
	
	/**
	 * Seleciona o feeder anterior
	 */
	public void previousFeeder() {
		this.setPreviousFromList(cbFeederNumber);
	}
	
	/**
	 * Seleciona o próximo feeder
	 */
	public void nextFeeder() {
		this.setNextFromList(cbFeederNumber);
	}
	
	/**
	 * Listener da ação de alteração da combo do número do feeder
	 */
	public void changeCbFeederNumber() {
		Integer selected = cbFeederNumber.valueProperty().get();
		if (selected != null) {
			this.fireEvent(EventType.FEEDER_SELECTED, selected);
		}
	}
	
	/**
	 * Seleciona o branch anterior
	 */
	public void previousBranch() {
		this.setPreviousFromList(cbBranchNumber);
	}
	
	/**
	 * Seleciona o próximo branch
	 */
	public void nextBranch() {
		this.setNextFromList(cbBranchNumber);
	}
	
	/**
	 * Listener da ação de alteração da combo do número da branch
	 */
	public void changeCbBranchNumber() {
		Integer selected = cbBranchNumber.valueProperty().get();
		if (selected != null) {
			this.fireEvent(EventType.BRANCH_SELECTED, selected);
		}
	}
	
	private void setPreviousFromList(ComboBox<Integer> cb) {
		Integer selected = cb.getValue();
		ObservableList<Integer> list = cb.getItems();
		
		if (!list.isEmpty()) {
			if (selected == null) {
				selected = list.get(list.size() - 1);
			} else {
				int previousIndex = list.indexOf(selected) - 1;
				selected = (previousIndex < 0) ? list.get(list.size()-1) : list.get(previousIndex);
			}
			cb.setValue(selected);
		}
	}
	
	private void setNextFromList(ComboBox<Integer> cb) {
		Integer selected = cb.getValue();
		ObservableList<Integer> list = cb.getItems();
		
		if (!list.isEmpty()) {
			if (selected == null) {
				selected = list.get(0);
			} else {
				int nextIndex = list.indexOf(selected) + 1;
				selected = (nextIndex == list.size()) ? list.get(0) : list.get(nextIndex);
			}
			cb.setValue(selected);
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
