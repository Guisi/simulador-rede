package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class SuppliedLoadsActivePowerPercentageChart extends GenericLineChart {

	private XYChart.Series<Number, Number> suppliedLoadsPercentageSeries;
	
	private Double minPercentage;
	private Double maxPercentage;
	private Double average;
	private double cumulativePercentage;
	
	public SuppliedLoadsActivePowerPercentageChart() {
		super();
		
		getYNumberAxis().setAutoRanging(false);
		getYNumberAxis().setLowerBound(0);
		getYNumberAxis().setUpperBound(100);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Active Power MW of Supplied Loads x Priority");
		
		suppliedLoadsPercentageSeries = new XYChart.Series<>();
        getData().add(suppliedLoadsPercentageSeries);
        
        this.cumulativePercentage = 0d;
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("% Supplied Loads x Priority");
		if (minPercentage != null) {
			BigDecimal value = new BigDecimal(minPercentage).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxPercentage != null) {
			BigDecimal value = new BigDecimal(maxPercentage).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		if (average != null) {
			BigDecimal value = new BigDecimal(average).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nAverage: ").append(value.toString()).append(" %");
		}
		suppliedLoadsPercentageSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "% Active Power MW of Supplied Loads x Priority";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		getXNumberAxis().setUpperBound(agentStepData.getStep());
		
		//calcula percentual de loads atendidos considerando a prioridade
		Double suppliedLoads = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, Double.class);
		Double notSuppliedLoads = agentStepData.getData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, Double.class);
		
		if (suppliedLoads != null && notSuppliedLoads != null) {
			Double total = suppliedLoads + notSuppliedLoads;
			BigDecimal value = total > 0 ? new BigDecimal(suppliedLoads / total * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			suppliedLoadsPercentageSeries.getData().add(chartData);
			minPercentage = minPercentage != null ? Math.min(minPercentage, value.doubleValue()) : value.doubleValue();
			maxPercentage = maxPercentage != null ? Math.max(maxPercentage, value.doubleValue()) : value.doubleValue();
			
			cumulativePercentage += value.doubleValue();
			average = cumulativePercentage / (double)agentStepData.getStep();
			
			getYNumberAxis().setLowerBound(minPercentage < 5 ? 0 : minPercentage - 5);
	        getYNumberAxis().setUpperBound(maxPercentage > 95 ? 100 : maxPercentage + 5);
		}
		
		this.updateSeriesName();
	}
}
