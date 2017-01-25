package br.com.guisi.simulador.rede.controller.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import br.com.guisi.simulador.rede.view.charts.environment.InstantCurrentBarChart;
import br.com.guisi.simulador.rede.view.charts.environment.LoadsPowerPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.MinLoadVoltagePUChart;
import br.com.guisi.simulador.rede.view.charts.environment.PowerLossChart;
import br.com.guisi.simulador.rede.view.charts.environment.PowerLossPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.RequiredSwitchOperationsChart;
import br.com.guisi.simulador.rede.view.charts.environment.SuppliedLoadsActivePowerPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.SuppliedLoadsPercentageChart;
import br.com.guisi.simulador.rede.view.charts.environment.VoltageBarChart;
import br.com.guisi.simulador.rede.view.charts.environment.VoltageLineChart;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

@Named
@Scope("prototype")
public class EnvironmentChartsPaneController extends AbstractEnvironmentPaneController {

	private VBox root;
	private TabPane tabPaneCharts;
	
	private List<GenericLineChart> lineCharts;
	private int stepProcessed;
	
	private VoltageBarChart voltageBarChart;
	private VoltageLineChart voltageLineChart;
	private InstantCurrentBarChart instantCurrentBarChart;
	
	public EnvironmentChartsPaneController(EnvironmentKeyType environmentKeyType) {
		super(environmentKeyType);
	}
	
	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
						   EventType.ENVIRONMENT_LOADED,
						   EventType.POWER_FLOW_COMPLETED,
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
			case POWER_FLOW_COMPLETED: this.processPowerflowCompleted(); break;
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
			chart.updateSeriesInfo();
			Tab tab = new Tab(chart.getChartTitle());
			tab.setContent(chart);
			tabPaneCharts.getTabs().add(tab);
		});
		
		this.createVoltageBarChart();
		
		this.createVoltageLineChart();
		
		this.createInstantCurrentBarChart();
	}
	
	private void createVoltageLineChart() {
		//voltage magnitude in p.u.
		voltageLineChart = new VoltageLineChart();
		
		Button button = new Button("Save current series");
		button.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Save current series");
			dialog.setHeaderText("Save current series");
			dialog.setContentText("Give this series a name:");
			Optional<String> result = dialog.showAndWait();
			result.ifPresent(name -> voltageLineChart.saveCurrentSeries(name));
		});
		
		Tab tab = new Tab(voltageLineChart.getChartTitle());
		VBox vBox = new VBox();
		tab.setContent(vBox);
		voltageLineChart.prefHeightProperty().bind(tabPaneCharts.heightProperty());
		vBox.setAlignment(Pos.CENTER_RIGHT);
		vBox.getChildren().add(button);
		vBox.getChildren().add(voltageLineChart);
		
		tabPaneCharts.getTabs().add(tab);
		voltageLineChart.updateChart(getEnvironment());
	}
	
	private void createVoltageBarChart() {
		//voltage magnitude in p.u.
		voltageBarChart = new VoltageBarChart();
		Tab tab = new Tab(voltageBarChart.getChartTitle());
		tabPaneCharts.getTabs().add(tab);
		
		Button button = new Button("Save current series");
		button.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Save current series");
			dialog.setHeaderText("Save current series");
			dialog.setContentText("Give this series a name:");
			Optional<String> result = dialog.showAndWait();
			result.ifPresent(name -> voltageBarChart.saveCurrentSeries(name));
		});
		
		VBox vBox = new VBox();
		voltageBarChart.prefHeightProperty().bind(tabPaneCharts.heightProperty());
		vBox.setAlignment(Pos.CENTER_RIGHT);
		vBox.getChildren().add(button);
		vBox.getChildren().add(voltageBarChart);
		tab.setContent(vBox);
		voltageBarChart.updateChart(getEnvironment());
	}
	
	private void createInstantCurrentBarChart() {
		//instant current in A.
		instantCurrentBarChart = new InstantCurrentBarChart();
		Tab tab = new Tab(instantCurrentBarChart.getChartTitle());
		tabPaneCharts.getTabs().add(tab);
		instantCurrentBarChart.updateChart(getEnvironment());
		
		Button button = new Button("Save current series");
		button.setOnAction(event -> {
			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Save current series");
			dialog.setHeaderText("Save current series");
			dialog.setContentText("Give this series a name:");
			Optional<String> result = dialog.showAndWait();
			result.ifPresent(name -> instantCurrentBarChart.saveCurrentSeries(name));
		});
		
		VBox vBox = new VBox();
		instantCurrentBarChart.prefHeightProperty().bind(tabPaneCharts.heightProperty());
		vBox.setAlignment(Pos.CENTER_RIGHT);
		vBox.getChildren().add(button);
		vBox.getChildren().add(instantCurrentBarChart);
		tab.setContent(vBox);
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
				chart.getXNumberAxis().setUpperBound(agentData.getSteps());
				chart.getXNumberAxis().setTickUnit(agentData.getSteps() / 50 + 1);
				chart.processAgentData(agentData);
				chart.updateSeriesInfo();
			});
			
			//para os casos onde o chart espera somente o step atual
			List<AgentStepData> environmentStepData = agentData.getEnvironmentStepData(getEnvironmentKeyType());
			for (int i = stepProcessed; i < environmentStepData.size(); i++) {
				AgentStepData agentStepData = environmentStepData.get(i);
				
				lineCharts.forEach((chart) -> {
					chart.getXNumberAxis().setUpperBound(agentStepData.getStep());
					chart.getXNumberAxis().setTickUnit(agentStepData.getStep() / 50 + 1);
					chart.processAgentStepData(agentStepData);
					chart.updateSeriesInfo();
				});
			}
			stepProcessed = environmentStepData.size();
		}
	}
	
	private void processPowerflowCompleted() {
		voltageBarChart.updateChart(getEnvironment());
		voltageLineChart.updateChart(getEnvironment());
		instantCurrentBarChart.updateChart(getEnvironment());
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
	@Override
	public String getControllerKey() {
		return super.getControllerKey() + "_" + getEnvironmentKeyType().name();
	}

}
