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
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.events.EventType;

public class ElementsDetailsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/ElementsDetailsPane.fxml";

	@FXML
	private VBox root;
	
	/** Loads */
	@FXML
	private VBox boxLoadInfo;
	@FXML
	private ComboBox<Integer> cbLoadNumber;
	@FXML
	private Label lblLoadFeeder;
	@FXML
	private Label lblLoadActivePower;
	@FXML
	private Label lblLoadReactivePower;
	@FXML
	private Label lblLoadPriority;
	@FXML
	private Label lblLoadStatus;
	@FXML
	private Label lblLoadCurrentVoltage;
	@FXML
	private Button btnPreviousLoad;
	@FXML
	private Button btnNextLoad;
	
	/** Feeders */
	@FXML
	private VBox boxFeederInfo;
	@FXML
	private ComboBox<Integer> cbFeederNumber;
	@FXML
	private Label lblFeederActivePower;
	@FXML
	private Label lblFeederReactivePower;
	@FXML
	private Label lblFeederEnergizedLoads;
	@FXML
	private Label lblFeederUsedPower;
	@FXML
	private Label lblFeederAvailablePower;
	@FXML
	private Button btnPreviousFeeder;
	@FXML
	private Button btnNextFeeder;
	
	/** Branches */
	@FXML
	private VBox boxBranchInfo;
	@FXML
	private ComboBox<Integer> cbBranchNumber;
	@FXML
	private Label lblBranchDe;
	@FXML
	private Label lblBranchPara;
	@FXML
	private Label lblBranchMaxCurrent;
	@FXML
	private Label lblBranchInstantCurrent;
	@FXML
	private Label lblBranchLossesMW;
	@FXML
	private Label lblBranchResistance;
	@FXML
	private Label lblBranchReactance;
	@FXML
	private Label lblBranchStatus;
	@FXML
	private Button btnPreviousBranch;
	@FXML
	private Button btnNextBranch;
	

	@Override
	public void initializeController(Object... data) {
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.LOAD_SELECTED);
		this.listenToEvent(EventType.FEEDER_SELECTED);
		this.listenToEvent(EventType.BRANCH_SELECTED);
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
		lblLoadFeeder.setText("");
		lblLoadActivePower.setText("");
		lblLoadReactivePower.setText("");
		lblLoadPriority.setText("");
		lblLoadStatus.setText("");
		lblLoadCurrentVoltage.setText("");
		
		cbFeederNumber.setValue(null);
		lblFeederActivePower.setText("");
		lblFeederReactivePower.setText("");
		lblFeederEnergizedLoads.setText("");
		lblFeederUsedPower.setText("");
		lblFeederAvailablePower.setText("");
		
		cbBranchNumber.setValue(null);
		lblBranchDe.setText("");
		lblBranchPara.setText("");
		lblBranchMaxCurrent.setText("");
		lblBranchInstantCurrent.setText("");
		lblBranchLossesMW.setText("");
		lblBranchResistance.setText("");
		lblBranchReactance.setText("");
		lblBranchStatus.setText("");
		
		cbLoadNumber.setItems(FXCollections.observableArrayList());
		cbFeederNumber.setItems(FXCollections.observableArrayList());
		cbBranchNumber.setItems(FXCollections.observableArrayList());
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
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_3);
		Load load = getEnvironment().getLoad(selectedLoad);
		lblLoadFeeder.setText(load.getFeeder() != null ? load.getFeeder().getNodeNumber().toString() : "");
		lblLoadActivePower.setText(df.format(load.getActivePower()));
		lblLoadReactivePower.setText(df.format(load.getReactivePower()));
		lblLoadPriority.setText(String.valueOf(load.getPriority()));
		lblLoadStatus.setText(load.isOn() ? "On" : "Off");
		lblLoadCurrentVoltage.setText(df.format(load.getCurrentVoltagePU()));
		cbLoadNumber.valueProperty().setValue(selectedLoad);
	}
	
	/**
	 * Exibe na tela as informações do Feeder selecionado
	 * @param networkNodeStackPane
	 */
	private void updateFeederInformationBox(Integer selectedFeeder) {
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_3);
		Feeder feeder = getEnvironment().getFeeder(selectedFeeder);
		lblFeederActivePower.setText(df.format(feeder.getActivePower()));
		lblFeederReactivePower.setText(df.format(feeder.getReactivePower()));
		lblFeederUsedPower.setText(df.format(feeder.getUsedPower()));
		lblFeederEnergizedLoads.setText(String.valueOf(feeder.getEnergizedLoads()));
		lblFeederAvailablePower.setText(df.format(feeder.getAvailablePower()));
		cbFeederNumber.valueProperty().set(selectedFeeder);
	}
	
	/**
	 * Exibe na tela as informações do Branch selecionado
	 * @param branchNode
	 */
	private void updateBranchInformationBox(Integer selectedBranch) {
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_3);
		Branch branch = getEnvironment().getBranch(selectedBranch);
		lblBranchDe.setText(branch.getNode1().getNodeNumber().toString());
		lblBranchPara.setText(branch.getNode2().getNodeNumber().toString());
		lblBranchMaxCurrent.setText(df.format(branch.getMaxCurrent()));
		lblBranchInstantCurrent.setText(df.format(branch.getInstantCurrent()));
		lblBranchLossesMW.setText(df.format(branch.getLossesMW()));
		lblBranchResistance.setText(df.format(branch.getResistance()));
		lblBranchReactance.setText(df.format(branch.getReactance()));
		lblBranchStatus.setText(branch.isClosed() ? "Closed" : "Open");
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
