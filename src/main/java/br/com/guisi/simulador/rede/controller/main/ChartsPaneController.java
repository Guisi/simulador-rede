package br.com.guisi.simulador.rede.controller.main;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;

public class ChartsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/ChartsPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TabPane tabPaneCharts;
	
	private XYChart.Series<Number, Number> activePowerLostPercentageSeries;
	private XYChart.Series<Number, Number> activePowerLostSeries;
	private Map<Node, Tooltip> totalActivePowerLostTooltips;
	
	private XYChart.Series<Number, Number> reactivePowerLostPercentageSeries;
	private XYChart.Series<Number, Number> reactivePowerLostSeries;
	private Map<Node, Tooltip> totalReactivePowerLostTooltips;
	
	private int stepUpdateReceived;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
						   EventType.ENVIRONMENT_LOADED,
						   EventType.AGENT_NOTIFICATION,
						   EventType.AGENT_RUNNING,
						   EventType.AGENT_STOPPED);
	
		totalActivePowerLostTooltips = new HashMap<>();
		totalReactivePowerLostTooltips = new HashMap<>();
		this.createCharts();
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.processEnvironmentLoaded(); break;
			case AGENT_NOTIFICATION: this.processAgentNotification(data); break;
			case AGENT_RUNNING: this.processAgentRunning(); break;
			case AGENT_STOPPED: this.processAgentStopped(); break;
			default: break;
		}
	}
	
	private void createCharts() {
		tabPaneCharts.getTabs().clear();

		//total power lost %
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Iteraction");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setUpperBound(0.1);
        yAxis.setLabel("Losses %");
        
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(300);
        lineChart.setLegendVisible(true);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.getStyleClass().add("thick-chart");
        
        activePowerLostPercentageSeries = new XYChart.Series<>();
        activePowerLostPercentageSeries.setName("Active Power Lost (MW)\nMin Value");
        lineChart.getData().add(activePowerLostPercentageSeries);
        
        reactivePowerLostPercentageSeries = new XYChart.Series<>();
        reactivePowerLostPercentageSeries.setName("Reactive Power Lost (MVar)");
        lineChart.getData().add(reactivePowerLostPercentageSeries);
        
        Tab tab = new Tab("Power Lost %");
        tab.setContent(lineChart);
		tabPaneCharts.getTabs().add(tab);
		
		//total power lost MW/MVar
        xAxis = new NumberAxis();
        xAxis.setLabel("Iteraction");
        yAxis = new NumberAxis();
        yAxis.setUpperBound(0.1);
        yAxis.setLabel("Losses MW/MVar");
        
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(300);
        lineChart.setLegendVisible(true);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(false);
        lineChart.getStyleClass().add("thick-chart");
        
        activePowerLostSeries = new XYChart.Series<>();
        activePowerLostSeries.setName("Active Power Lost (MW)");
        lineChart.getData().add(activePowerLostSeries);
        
        reactivePowerLostSeries = new XYChart.Series<>();
        reactivePowerLostSeries.setName("Reactive Power Lost (MVar)");
        lineChart.getData().add(reactivePowerLostSeries);
		
		tab = new Tab("Power Lost MW/MVar");
        tab.setContent(lineChart);
		tabPaneCharts.getTabs().add(tab);
	}
	
	private void resetScreen() {
		root.setVisible(false);
		activePowerLostPercentageSeries.getData().clear();
		reactivePowerLostPercentageSeries.getData().clear();
		activePowerLostSeries.getData().clear();
		reactivePowerLostSeries.getData().clear();
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		
		if (agentStatus != null) {
			for (int i = stepUpdateReceived; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				Double activePowerLost = agentStepStatus.getInformation(AgentInformationType.ACTIVE_POWER_LOST, Double.class);
				Double activePowerDemand = agentStepStatus.getInformation(AgentInformationType.ACTIVE_POWER_DEMAND, Double.class);
				if (activePowerLost != null) {
					BigDecimal value = new BigDecimal(activePowerLost).setScale(5, RoundingMode.HALF_UP);
					
					Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
					activePowerLostSeries.getData().add(chartData);
					
					if (activePowerDemand != null) {
						value = new BigDecimal(activePowerLost / activePowerDemand * 100).setScale(5, RoundingMode.HALF_UP);
					}
					
					chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
					activePowerLostPercentageSeries.getData().add(chartData);
		            //totalActivePowerLostTooltips.put(chartData.getNode(), TooltipUtils.hackTooltipStartTiming(new Tooltip(value.toString())));
				}

				Double reactivePowerLost = agentStepStatus.getInformation(AgentInformationType.REACTIVE_POWER_LOST, Double.class);
				Double reactivePowerDemand = agentStepStatus.getInformation(AgentInformationType.REACTIVE_POWER_DEMAND, Double.class);
				if (reactivePowerLost != null) {
					BigDecimal value = new BigDecimal(reactivePowerLost).setScale(5, RoundingMode.HALF_UP);
					
					Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
					reactivePowerLostSeries.getData().add(chartData);
					
					if (reactivePowerDemand != null) {
						value = new BigDecimal(reactivePowerLost / reactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP);
					}
					
					chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
					reactivePowerLostPercentageSeries.getData().add(chartData);
		            //totalReactivePowerLostTooltips.put(chartData.getNode(), TooltipUtils.hackTooltipStartTiming(new Tooltip(value.toString())));
				}
			}
			stepUpdateReceived = agentStatus.getStepStatus().size();
		}
	}
	
	private void processAgentStopped() {
		for (Entry<Node, Tooltip> entry : totalActivePowerLostTooltips.entrySet()) {
			Tooltip.install(entry.getKey(), entry.getValue());
		}
		for (Entry<Node, Tooltip> entry : totalReactivePowerLostTooltips.entrySet()) {
			Tooltip.install(entry.getKey(), entry.getValue());
		}
	}
	
	private void processAgentRunning() {
		for (Entry<Node, Tooltip> entry : totalActivePowerLostTooltips.entrySet()) {
			Tooltip.uninstall(entry.getKey(), entry.getValue());
		}
		for (Entry<Node, Tooltip> entry : totalReactivePowerLostTooltips.entrySet()) {
			Tooltip.uninstall(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
