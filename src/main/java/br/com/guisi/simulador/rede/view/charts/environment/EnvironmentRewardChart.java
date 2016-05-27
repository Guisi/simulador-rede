package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class EnvironmentRewardChart extends GenericLineChart {

	private XYChart.Series<Number, Number> environmentConfigurationRateSeries;
	
	private Double minValue;
	private Double maxValue;
	private Double average;
	private double cumulative;
	private int betterThanInitial;
	
	public EnvironmentRewardChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Reward (% Loads Supplied)");
		
		environmentConfigurationRateSeries = new XYChart.Series<>();
        getData().add(environmentConfigurationRateSeries);
        
        this.cumulative = 0d;
        this.betterThanInitial = 0;
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Reward (% Loads Supplied)");
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
		sb.append("\nBetter than initial: ").append(betterThanInitial);
		environmentConfigurationRateSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Reward (% Loads Supplied)";
	}

	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		getXNumberAxis().setUpperBound(agentStepData.getStep());
		
		Double environmentConfigurationRate = agentStepData.getData(AgentDataType.ENVIRONMENT_REWARD, Double.class);
		
		if (environmentConfigurationRate != null) {
			BigDecimal value = new BigDecimal(environmentConfigurationRate).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			environmentConfigurationRateSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, value.doubleValue()) : value.doubleValue();
			maxValue = maxValue != null ? Math.max(maxValue, value.doubleValue()) : value.doubleValue();
			
			cumulative += value.doubleValue();
			average = cumulative / (double)agentStepData.getStep();
			
			if (value.doubleValue() > 0) {
				betterThanInitial++;
			}
			
			/*getYNumberAxis().setLowerBound(minValue < 5 ? 0 : minValue - 5);
	        getYNumberAxis().setUpperBound(maxValue > 95 ? 100 : maxValue + 5);*/
		}
		
		this.updateSeriesName();
	}
}
