package br.com.guisi.simulador.rede.view.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;

public class EnvironmentConfigurationRateChart extends GenericLineChart {

	private XYChart.Series<Number, Number> environmentConfigurationRateSeries;
	
	private Double minValue;
	private Double maxValue;
	private Double average;
	private double cumulative;
	
	public EnvironmentConfigurationRateChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Environment Configuration Rate");
		
		environmentConfigurationRateSeries = new XYChart.Series<>();
        getData().add(environmentConfigurationRateSeries);
        
        this.cumulative = 0d;
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Environment Configuration Rate");
		if (minValue != null) {
			BigDecimal value = new BigDecimal(minValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());
		}
		if (maxValue != null) {
			BigDecimal value = new BigDecimal(maxValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString());
		}
		if (average != null) {
			BigDecimal value = new BigDecimal(average).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nAverage: ").append(value.toString());
		}
		environmentConfigurationRateSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Environment Configuration Rate";
	}

	@Override
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		Double environmentConfigurationRate = agentStepStatus.getInformation(AgentInformationType.ENVIRONMENT_CONFIGURATION_RATE, Double.class);
		
		if (environmentConfigurationRate != null) {
			BigDecimal value = new BigDecimal(environmentConfigurationRate).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			environmentConfigurationRateSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, value.doubleValue()) : value.doubleValue();
			maxValue = maxValue != null ? Math.max(maxValue, value.doubleValue()) : value.doubleValue();
			
			cumulative += value.doubleValue();
			average = cumulative / (double)agentStepStatus.getStep();
			
			/*getYNumberAxis().setLowerBound(minValue < 5 ? 0 : minValue - 5);
	        getYNumberAxis().setUpperBound(maxValue > 95 ? 100 : maxValue + 5);*/
		}
		
		this.updateSeriesName();
	}
}
