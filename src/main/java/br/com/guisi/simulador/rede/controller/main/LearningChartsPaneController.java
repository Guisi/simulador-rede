package br.com.guisi.simulador.rede.controller.main;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.learning.PolicyChangeChart;
import br.com.guisi.simulador.rede.view.charts.learning.QValuesAverageChart;

public class LearningChartsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/LearningChartsPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TabPane tabPaneCharts;
	
	private List<GenericLineChart> lineCharts;
	private int stepUpdateReceived;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
						   EventType.ENVIRONMENT_LOADED,
						   EventType.AGENT_NOTIFICATION);
		
		this.root.prefWidthProperty().bind(SimuladorRede.getPrimaryStage().widthProperty());
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void setStage(Stage stage) {
		super.setStage(stage);
		this.tabPaneCharts.prefHeightProperty().bind(stage.heightProperty());
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.processEnvironmentLoaded(); break;
			case AGENT_NOTIFICATION: this.processAgentNotification(data); break;
			default: break;
		}
	}
	
	private void createCharts() {
		tabPaneCharts.getTabs().clear();
		lineCharts = new ArrayList<>();
		
		//policy change
		lineCharts.add(new PolicyChangeChart());
		//q-values average
		lineCharts.add(new QValuesAverageChart());
		
		lineCharts.forEach((chart) -> {
			Tab tab = new Tab(chart.getChartTitle());
			tab.setContent(chart);
			tabPaneCharts.getTabs().add(tab);
		});
	}
	
	private void resetScreen() {
		root.setVisible(false);
		this.stepUpdateReceived = 0;
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
		this.createCharts();
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		
		if (agentStatus != null) {
			//para os casos onde o chart processa o AgentStatus a sua forma
			lineCharts.forEach((chart) -> {
				chart.processAgentStatus(agentStatus);
			});
			
			//para os casos onde o chart espera somente o step atual
			for (int i = stepUpdateReceived; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				lineCharts.forEach((chart) -> {
					chart.processAgentStepStatus(agentStepStatus);
				});
			}
			stepUpdateReceived = agentStatus.getStepStatus().size();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
