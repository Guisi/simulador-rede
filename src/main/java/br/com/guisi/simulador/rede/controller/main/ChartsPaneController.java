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
import br.com.guisi.simulador.rede.util.TooltipUtils;

public class ChartsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/ChartsPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TabPane tabPaneCharts;
	
	private XYChart.Series<Number, Number> totalPowerLostSeries;
	private Map<Node, Tooltip> totalPowerLostTooltips;
	
	private int stepUpdateReceived;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
						   EventType.ENVIRONMENT_LOADED,
						   EventType.AGENT_NOTIFICATION,
						   EventType.AGENT_RUNNING,
						   EventType.AGENT_STOPPED);
	
		totalPowerLostTooltips = new HashMap<>();
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

		//total power lost
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Steps");
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setUpperBound(0.1);
        
        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(300);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        
        totalPowerLostSeries = new XYChart.Series<>();
        lineChart.getData().add(totalPowerLostSeries);
        
        Tab tab = new Tab("Total Power Lost");
        tab.setContent(lineChart);
		tabPaneCharts.getTabs().add(tab);
	}
	
	private void resetScreen() {
		root.setVisible(false);
		totalPowerLostSeries.getData().clear();
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		
		if (agentStatus != null) {
			for (int i = stepUpdateReceived; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				Double totalPowerLost = agentStepStatus.getInformation(AgentInformationType.TOTAL_POWER_LOST, Double.class);
				if (totalPowerLost != null) {
					BigDecimal value = new BigDecimal(totalPowerLost);
				    value = value.setScale(10, RoundingMode.HALF_UP);
					
					Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
					totalPowerLostSeries.getData().add(chartData);
		            totalPowerLostTooltips.put(chartData.getNode(), TooltipUtils.hackTooltipStartTiming(new Tooltip(value.toString())));
				}
			}
			stepUpdateReceived = agentStatus.getStepStatus().size();
		}
	}
	
	private void processAgentStopped() {
		for (Entry<Node, Tooltip> entry : totalPowerLostTooltips.entrySet()) {
			Tooltip.install(entry.getKey(), entry.getValue());
		}
	}
	
	private void processAgentRunning() {
		for (Entry<Node, Tooltip> entry : totalPowerLostTooltips.entrySet()) {
			Tooltip.uninstall(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
