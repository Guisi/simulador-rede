package br.com.guisi.simulador.rede.view.charts;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;

public abstract class GenericLineChart extends LineChart<Number, Number> {
	
	public GenericLineChart() {
		super(new NumberAxis(), new NumberAxis());
		
        this.setPrefHeight(300);
        this.setLegendVisible(true);
        this.setAnimated(false);
        this.setCreateSymbols(false);
        this.getStyleClass().add("thick-chart");
        
        getXNumberAxis().setAutoRanging(false);
        getXNumberAxis().setLowerBound(0);
        getXNumberAxis().setTickUnit(1);
        getXNumberAxis().setUpperBound(1);
	}
	
	public NumberAxis getXNumberAxis() {
		return (NumberAxis) this.getXAxis();
	}
	
	public NumberAxis getYNumberAxis() {
		return (NumberAxis) this.getYAxis();
	}
	
	public abstract String getChartTitle();

	public void processAgentStepData(AgentStepData agentStepData) {}
	
	public void processAgentData(AgentData agentStatus) {}
}
