package br.com.guisi.simulador.rede.view.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;

public abstract class GenericLineChart extends LineChart<Number, Number> {
	
	private List<LineChartSeries> seriesList;
	
	public GenericLineChart() {
		super(new NumberAxis(), new NumberAxis());
		
		this.seriesList = new ArrayList<>();
		
        this.setPrefHeight(300);
        this.setLegendVisible(true);
        this.setAnimated(false);
        this.setCreateSymbols(false);
        this.getStyleClass().add("thick-chart");
        
        getXAxis().setLabel("Iteraction");
		getYAxis().setLabel(getChartTitle());
        
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

	protected void addLineChartSeries(LineChartSeries lineChartSeries) {
		this.seriesList.add(lineChartSeries);
		getData().add(lineChartSeries.getSeries());
	}
	
	public void updateSeriesInfo() {
		for (LineChartSeries series : seriesList) {
			StringBuilder sb = new StringBuilder(series.getSeriesTitle());
			BigDecimal value = new BigDecimal(series.getMinValue()).setScale(series.getScale(), RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());

			value = new BigDecimal(series.getCurrentValue()).setScale(series.getScale(), RoundingMode.HALF_UP);
			sb.append("\nCurrent Value: ").append(value.toString());
			
			value = new BigDecimal(series.getMaxValue()).setScale(series.getScale(), RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" - Step: ").append(series.getStepMaxValue());
			
			series.getSeries().setName(sb.toString());
		}
	}
	
	public abstract String getChartTitle();

	public void processAgentStepData(AgentStepData agentStepData) {}
	
	public void processAgentData(AgentData agentStatus) {}
}
