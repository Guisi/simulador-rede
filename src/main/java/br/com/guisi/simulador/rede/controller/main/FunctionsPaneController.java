package br.com.guisi.simulador.rede.controller.main;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.constants.FunctionType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.functions.EvaluationObject;
import br.com.guisi.simulador.rede.functions.FunctionItem;
import br.com.guisi.simulador.rede.util.EvaluatorUtils;
import br.com.guisi.simulador.rede.util.FunctionsUtils;

public class FunctionsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/FunctionsPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TabPane tabPaneFunctions;
	
	private List<FunctionItem> functions;

	@Override
	public void initializeController(Object... data) {
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.FUNCTIONS_UPDATED);
		this.listenToEvent(EventType.POWER_FLOW_COMPLETED);
		
		for (Tab tab: tabPaneFunctions.getTabs()) {
			@SuppressWarnings("unchecked")
			TableView<FunctionItem> tv = (TableView<FunctionItem>) tab.getContent();
			tv.getItems().clear();
		}
		
		this.createFunctionTables();
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case FUNCTIONS_UPDATED: this.updateFunctionsTables(); break;
			case POWER_FLOW_COMPLETED: this.evaluateFunctionsExpressions(); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
	}
	
	private void onEnvironmentLoaded() {
		root.setVisible(true);
		this.updateFunctionsTables();
	}
	
	/**
	 * Cria os tabs e tabelas para cada tipo de função
	 */
	private void createFunctionTables() {
		tabPaneFunctions.getTabs().clear();
		for (FunctionType type : FunctionType.values()) {
			TableView<FunctionItem> tv = new TableView<FunctionItem>();
			tv.widthProperty().addListener((source, oldWidth, newWidth) -> {
                Pane header = (Pane) tv.lookup("TableHeaderRow");
                if (header.isVisible()){
                    header.setMaxHeight(0);
                    header.setMinHeight(0);
                    header.setPrefHeight(0);
                    header.setVisible(false);
                }
			});
			tv.setPrefHeight(170);
			tv.setItems(FXCollections.observableArrayList());
			
			TableColumn<FunctionItem, String> tcFunctionName = new TableColumn<FunctionItem, String>();
			tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionName());
			tcFunctionName.setStyle("-fx-font-weight: bold; -fx-alignment: center-right;");
			tcFunctionName.setPrefWidth(292);
			tv.getColumns().add(tcFunctionName);
			
			TableColumn<FunctionItem, String> tcFunctionResult = new TableColumn<FunctionItem, String>();
			tcFunctionResult.setPrefWidth(292);
			tcFunctionResult.setCellValueFactory(cellData -> cellData.getValue().getFunctionResult());
			tv.getColumns().add(tcFunctionResult);
			
			Tab tab = new Tab(type.getDescription());
			tab.setContent(tv);
			tabPaneFunctions.getTabs().add(tab);
		}
	}
	
	/**
	 * Com base nas funções cadastradas, executa as respectivas expressôes
	 * e inclui nas tabelas
	 */
	private void updateFunctionsTables() {
		if (getEnvironment() != null) {
			try {
				for (Tab tab: tabPaneFunctions.getTabs()) {
					@SuppressWarnings("unchecked")
					TableView<FunctionItem> tv = (TableView<FunctionItem>) tab.getContent();
					tv.getItems().clear();
				}
				
				functions = FunctionsUtils.loadProperties();
				for (FunctionItem functionItem : functions) {
					FunctionType ft = FunctionType.getByDescription(functionItem.getFunctionType().get());
					functionItem.getFunctionName().set(functionItem.getFunctionName().get() + ":");

					@SuppressWarnings("unchecked")
					TableView<FunctionItem> tv = (TableView<FunctionItem>) tabPaneFunctions
						.getTabs().get(ft.ordinal()).getContent();
					tv.getItems().add(functionItem);
				}
				
				this.evaluateFunctionsExpressions();
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			}
		}
	}
	
	/**
	 * Executa as expressões das funções
	 */
	private void evaluateFunctionsExpressions() {
		EvaluationObject evaluationObject = new EvaluationObject();
		evaluationObject.setEnvironment(getEnvironment());
		
		for (FunctionItem functionItem : functions) {
			String expression = functionItem.getFunctionExpression();
			
			String functionResult;
			try {
				Object result = EvaluatorUtils.evaluateExpression(evaluationObject, expression);
				functionResult = String.valueOf(result);
			} catch (Exception e) {
				functionResult = "Error in expression evaluation!";
			}
			functionItem.getFunctionResult().set(functionResult);
		}
	}

	@Override
	public Node getView() {
		return root;
	}

}
