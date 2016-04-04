package br.com.guisi.simulador.rede.controller.main;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.FunctionType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.functions.EvaluationObject;
import br.com.guisi.simulador.rede.functions.FunctionItem;
import br.com.guisi.simulador.rede.functions.FunctionItemPair;
import br.com.guisi.simulador.rede.util.EvaluatorUtils;
import br.com.guisi.simulador.rede.util.FunctionsUtils;

public class FunctionsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/FunctionsPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TabPane tabPaneFunctions;
	
	private Map<FunctionType, List<FunctionItem>> functions;

	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.FUNCTIONS_UPDATED,
				EventType.POWER_FLOW_COMPLETED,
				EventType.AGENT_NOTIFICATION);
		
		for (Tab tab: tabPaneFunctions.getTabs()) {
			@SuppressWarnings("unchecked")
			TableView<FunctionItem> tv = (TableView<FunctionItem>) tab.getContent();
			tv.getItems().clear();
		}
		
		this.createFunctionTables();
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case FUNCTIONS_UPDATED: this.updateFunctionsTables(); break;
			case POWER_FLOW_COMPLETED: this.processPowerflowCompleted(); break;
			case AGENT_NOTIFICATION : this.processAgentNotification(); break;
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
			TableView<FunctionItemPair> tv = new TableView<FunctionItemPair>();
			tv.widthProperty().addListener((source, oldWidth, newWidth) -> {
                Pane header = (Pane) tv.lookup("TableHeaderRow");
                if (header.isVisible()){
                    header.setMaxHeight(0);
                    header.setMinHeight(0);
                    header.setPrefHeight(0);
                    header.setVisible(false);
                }
			});
			tv.setPrefHeight(200);
			tv.setItems(FXCollections.observableArrayList());
			
			TableColumn<FunctionItemPair, String> tcFunctionName = new TableColumn<FunctionItemPair, String>();
			tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionItem1().getFunctionName());
			tcFunctionName.setStyle("-fx-font-weight: bold; -fx-alignment: center-right;");
			tcFunctionName.setPrefWidth(260);
			tv.getColumns().add(tcFunctionName);
			
			TableColumn<FunctionItemPair, String> tcFunctionResult = new TableColumn<FunctionItemPair, String>();
			tcFunctionResult.setPrefWidth(115);
			tcFunctionResult.setCellValueFactory(cellData -> cellData.getValue().getFunctionItem1().getFunctionResult());
			tv.getColumns().add(tcFunctionResult);
			
			tcFunctionName = new TableColumn<FunctionItemPair, String>();
			tcFunctionName.setCellValueFactory(cellData -> cellData.getValue().getFunctionItem2() != null ? cellData.getValue().getFunctionItem2().getFunctionName() : null);
			tcFunctionName.setStyle("-fx-font-weight: bold; -fx-alignment: center-right;");
			tcFunctionName.setPrefWidth(260);
			tv.getColumns().add(tcFunctionName);
			
			tcFunctionResult = new TableColumn<FunctionItemPair, String>();
			tcFunctionResult.setPrefWidth(115);
			tcFunctionResult.setCellValueFactory(cellData -> cellData.getValue().getFunctionItem2() != null ? cellData.getValue().getFunctionItem2().getFunctionResult() : null);
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
	@SuppressWarnings("unchecked")
	private void updateFunctionsTables() {
		if (SimuladorRede.getInteractionEnvironment() != null) {
			try {
				for (Tab tab: tabPaneFunctions.getTabs()) {
					TableView<FunctionItem> tv = (TableView<FunctionItem>) tab.getContent();
					tv.getItems().clear();
				}
				
				functions = FunctionsUtils.loadProperties();
				
				functions.forEach( (key, value) -> {
					TableView<FunctionItemPair> tv = (TableView<FunctionItemPair>) tabPaneFunctions
						.getTabs().get(key.ordinal()).getContent();
					
					for (Iterator<FunctionItem> iterator = value.iterator(); iterator.hasNext();) {
						FunctionItem functionItem = (FunctionItem) iterator.next();
						functionItem.getFunctionName().set(functionItem.getFunctionName().get() + ":");
	
						FunctionItemPair functionItemPair = new FunctionItemPair();
						functionItemPair.setFunctionItem1(functionItem);
						
						if (iterator.hasNext()) {
							functionItem = (FunctionItem) iterator.next();
							functionItem.getFunctionName().set(functionItem.getFunctionName().get() + ":");
							functionItemPair.setFunctionItem2(functionItem);
						}
						
						tv.getItems().add(functionItemPair);
					}
				});
				
				/*for (FunctionItem functionItem : functions) {
					FunctionType ft = FunctionType.getByDescription(functionItem.getFunctionType().get());
					functionItem.getFunctionName().set(functionItem.getFunctionName().get() + ":");

					@SuppressWarnings("unchecked")
					TableView<FunctionItem> tv = (TableView<FunctionItem>) tabPaneFunctions
						.getTabs().get(ft.ordinal()).getContent();
					tv.getItems().add(functionItem);
				}*/
				
				this.evaluateFunctionsExpressions(null);
			} catch (IOException e) {
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR, e.getMessage());
				alert.showAndWait();
			}
		}
	}
	
	/**
	 * Executa as expressões das funções
	 */
	private void evaluateFunctionsExpressions(String functionName) {
		EvaluationObject evaluationObject = new EvaluationObject();
		evaluationObject.setEnvironment(SimuladorRede.getInteractionEnvironment());
		
		functions.forEach( (key, value) -> {
			value.forEach(functionItem -> {
				if (functionName == null || functionName.equals(functionItem.getFunctionName().getValue())) {
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
			});
		});
	}
	
	private void processPowerflowCompleted() {
		this.evaluateFunctionsExpressions(null);
	}
	
	private void processAgentNotification() {
		this.evaluateFunctionsExpressions(null);
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
