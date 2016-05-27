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
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.controller.environment.AbstractEnvironmentPaneController;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.environment.EnvironmentRewardChart;
import br.com.guisi.simulador.rede.view.charts.environment.LoadsPowerPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.MinLoadVoltagePUChart;
import br.com.guisi.simulador.rede.view.charts.environment.PowerLossChart;
import br.com.guisi.simulador.rede.view.charts.environment.PowerLossPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.RequiredSwitchOperationsChart;
import br.com.guisi.simulador.rede.view.charts.environment.SuppliedLoadsActivePowerPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.SuppliedLoadsPercentageChart;

@Named
@Scope("prototype")
public class EnvironmentChartsPaneController extends AbstractEnvironmentPaneController {

	private VBox root;
	private TabPane tabPaneCharts;
	
	private List<GenericLineChart> lineCharts;
	private int stepProcessed;
	
	public EnvironmentChartsPaneController(EnvironmentKeyType environmentKeyType) {
		super(environmentKeyType);
	}
	
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
		
		//total power lost %
		lineCharts.add(new PowerLossPercentageChart());
		//total power lost MW/MVar
		lineCharts.add(new PowerLossChart());
		//% supplied loads x priority
		lineCharts.add(new SuppliedLoadsPercentageChart());
		//supplied loads x priority
		lineCharts.add(new SuppliedLoadsActivePowerPercentageChart());
		//out-of-service loads power %
		lineCharts.add(new LoadsPowerPercentageChart(getEnvironmentKeyType()));
		//min load voltage PU
		lineCharts.add(new MinLoadVoltagePUChart());
		//environment reward
		lineCharts.add(new EnvironmentRewardChart());
		//required switch operations
		lineCharts.add(new RequiredSwitchOperationsChart());
		
		lineCharts.forEach((chart) -> {
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
			});
			
			//para os casos onde o chart espera somente o step atual
			List<AgentStepData> environmentStepData = agentData.getEnvironmentStepData(getEnvironmentKeyType());
			for (int i = stepProcessed; i < environmentStepData.size(); i++) {
				AgentStepData agentStepData = environmentStepData.get(i);
				
				lineCharts.forEach((chart) -> {
					chart.processAgentStepData(agentStepData);
				});
			}
			stepProcessed = environmentStepData.size();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
