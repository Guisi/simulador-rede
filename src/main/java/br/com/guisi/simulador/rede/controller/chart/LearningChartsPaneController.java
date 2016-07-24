package br.com.guisi.simulador.rede.controller.chart;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.learning.PolicyChangeChart;
import br.com.guisi.simulador.rede.view.charts.learning.QValuesAverageChart;

@Named
@Scope("prototype")
public class LearningChartsPaneController extends Controller {

	private VBox root;
	private TabPane tabPaneCharts;
	
	private List<GenericLineChart> lineCharts;
	private int stepProcessed;
	
	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
						   EventType.ENVIRONMENT_LOADED,
						   EventType.AGENT_NOTIFICATION);
		
		root = new VBox();
		tabPaneCharts = new TabPane();
		root.getChildren().add(tabPaneCharts);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	protected void onSetStage(Stage stage) {
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
			chart.updateSeriesInfo();
			Tab tab = new Tab(chart.getChartTitle());
			tab.setContent(chart);
			tabPaneCharts.getTabs().add(tab);
		});
	}
	
	private void resetScreen() {
		root.setVisible(false);
		this.stepProcessed = 0;
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
		this.createCharts();
	}
	
	private void processAgentNotification(Object data) {
		AgentData agentData = (AgentData) data;
		
		if (agentData != null) {
			//para os casos onde o chart processa o AgentStatus a sua forma
			lineCharts.forEach((chart) -> {
				chart.processAgentData(agentData);
				chart.updateSeriesInfo();
				chart.getXNumberAxis().setUpperBound(agentData.getSteps());
				chart.getXNumberAxis().setTickUnit(agentData.getSteps() / 50 + 1);
			});
			
			//para os casos onde o chart espera somente o step atual
			for (int i = stepProcessed; i < agentData.getAgentStepData().size(); i++) {
				AgentStepData agentStepStatus = agentData.getAgentStepData().get(i);
				
				lineCharts.forEach((chart) -> {
					chart.processAgentStepData(agentStepStatus);
					chart.updateSeriesInfo();
					chart.getXNumberAxis().setUpperBound(agentStepStatus.getStep());
					chart.getXNumberAxis().setTickUnit(agentStepStatus.getStep() / 50 + 1);
				});
			}
			stepProcessed = agentData.getAgentStepData().size();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
