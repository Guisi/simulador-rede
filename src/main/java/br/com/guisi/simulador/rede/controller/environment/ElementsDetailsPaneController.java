package br.com.guisi.simulador.rede.controller.environment;

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

import javax.annotation.PostConstruct;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.events.EnvironmentEventData;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.tableview.PropertyRow;

public class ElementsDetailsPaneController extends AbstractEnvironmentPaneController {

	public static final String FXML_FILE = "/fxml/environment/ElementsDetailsPane.fxml";

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
	
	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.LOAD_SELECTED,
				EventType.FEEDER_SELECTED,
				EventType.BRANCH_SELECTED,
				EventType.AGENT_RUNNING);
		
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
			case LOAD_SELECTED: this.updateLoadInformationBox((EnvironmentEventData) data); break;
			case FEEDER_SELECTED: this.updateFeederInformationBox((EnvironmentEventData) data); break;
			case BRANCH_SELECTED: this.updateBranchInformationBox((EnvironmentEventData) data); break;
			case AGENT_RUNNING: this.processAgentRunning(); break;
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
	
	private void processAgentRunning() {
		tvLoadDetails.getItems().clear();
		tvFeederDetails.getItems().clear();
		tvBranchDetails.getItems().clear();
		cbLoadNumber.valueProperty().setValue(null);
		cbFeederNumber.valueProperty().setValue(null);
		cbBranchNumber.valueProperty().setValue(null);
	}
	
	/**
	 * Exibe na tela as informações do Load selecionado
	 * @param networkNodeStackPane
	 */
	private void updateLoadInformationBox(EnvironmentEventData eventData) {
		if (eventData.getEnvironmentKeyType().equals(getEnvironmentKeyType())) {
			Integer selectedLoad = (Integer) eventData.getData();
			DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_5);
			Load load = getEnvironment().getLoad(selectedLoad);
			
			tvLoadDetails.getItems().clear();
			tvLoadDetails.getItems().add(new PropertyRow("Feeder:", load.getFeeder() != null ? load.getFeeder().getNodeNumber().toString() : ""));
			tvLoadDetails.getItems().add(new PropertyRow("Active Power MW:", df.format(load.getActivePowerMW())));
			tvLoadDetails.getItems().add(new PropertyRow("Reactive Power MVar:", df.format(load.getReactivePowerMVar())));
			tvLoadDetails.getItems().add(new PropertyRow("Priority:", String.valueOf(load.getPriority())));
			tvLoadDetails.getItems().add(new PropertyRow("Status:", load.isOn() ? "On" : "Off"));
			tvLoadDetails.getItems().add(new PropertyRow("Voltage pu:", df.format(load.getCurrentVoltagePU())));
			cbLoadNumber.valueProperty().setValue(selectedLoad);
		}
	}
	
	/**
	 * Exibe na tela as informações do Feeder selecionado
	 * @param networkNodeStackPane
	 */
	private void updateFeederInformationBox(EnvironmentEventData eventData) {
		if (eventData.getEnvironmentKeyType().equals(getEnvironmentKeyType())) {
			Integer selectedFeeder = (Integer) eventData.getData();
			DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_5);
			Feeder feeder = getEnvironment().getFeeder(selectedFeeder);
			
			tvFeederDetails.getItems().clear();
			tvFeederDetails.getItems().add(new PropertyRow("Active Power MW:", df.format(feeder.getActivePowerMW())));
			tvFeederDetails.getItems().add(new PropertyRow("Reactive Power MVar:", df.format(feeder.getReactivePowerMVar())));
			tvFeederDetails.getItems().add(new PropertyRow("Connected Loads:", String.valueOf(feeder.getServedLoads().size())));
			tvFeederDetails.getItems().add(new PropertyRow("Supplied Loads:", String.valueOf(feeder.getServedLoads().stream().filter(load -> load.isSupplied()).count())));
			tvFeederDetails.getItems().add(new PropertyRow("Not Supplied Loads:", String.valueOf(feeder.getServedLoads().stream().filter(load -> !load.isSupplied()).count())));
			tvFeederDetails.getItems().add(new PropertyRow("Used active power MW:", df.format(feeder.getUsedActivePowerMW())));
			tvFeederDetails.getItems().add(new PropertyRow("Available Active Power:", df.format(feeder.getAvailableActivePowerMW())));
			
			tvFeederDetails.getItems().add(new PropertyRow("Voltage pu:", df.format(feeder.getCurrentVoltagePU())));
			
			cbFeederNumber.valueProperty().set(selectedFeeder);
		}
	}
	
	/**
	 * Exibe na tela as informações do Branch selecionado
	 * @param branchNode
	 */
	private void updateBranchInformationBox(EnvironmentEventData eventData) {
		if (eventData.getEnvironmentKeyType().equals(getEnvironmentKeyType())) {
			Integer selectedBranch = (Integer) eventData.getData();
			DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_5);
			Branch branch = getEnvironment().getBranch(selectedBranch);
			
			tvBranchDetails.getItems().clear();
			tvBranchDetails.getItems().add(new PropertyRow("From:", branch.getNodeFrom().getNodeNumber().toString()));
			tvBranchDetails.getItems().add(new PropertyRow("To:", branch.getNodeTo().getNodeNumber().toString()));
			tvBranchDetails.getItems().add(new PropertyRow("Max Current A:", df.format(branch.getMaxCurrent())));
			tvBranchDetails.getItems().add(new PropertyRow("Instant Current A:", df.format(branch.getInstantCurrent())));
			tvBranchDetails.getItems().add(new PropertyRow("Resistance \u03A9:", df.format(branch.getResistance())));
			tvBranchDetails.getItems().add(new PropertyRow("Reactance \u03A9:", df.format(branch.getReactance())));
			tvBranchDetails.getItems().add(new PropertyRow("Status:", branch.getSwitchStatus().getDescription()));
			tvBranchDetails.getItems().add(new PropertyRow("Active Loss MW:", df.format(branch.getActiveLossMW())));
			tvBranchDetails.getItems().add(new PropertyRow("Reactive Loss MVar:", df.format(branch.getReactiveLossMVar())));
			cbBranchNumber.valueProperty().set(selectedBranch);
		}
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
			this.fireEvent(EventType.LOAD_SELECTED, new EnvironmentEventData(getEnvironmentKeyType(), selected));
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
			this.fireEvent(EventType.FEEDER_SELECTED, new EnvironmentEventData(getEnvironmentKeyType(), selected));
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
			this.fireEvent(EventType.BRANCH_SELECTED, new EnvironmentEventData(getEnvironmentKeyType(), selected));
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
