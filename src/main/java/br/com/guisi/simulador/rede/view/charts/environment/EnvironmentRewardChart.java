package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class EnvironmentRewardChart extends GenericLineChart {

	private LineChartSeries environmentConfigurationRateSeries;
	
	public EnvironmentRewardChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);

		environmentConfigurationRateSeries = new LineChartSeries(getChartTitle(), Double.class);
		addChartSeries(environmentConfigurationRateSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "Reward (% Loads Supplied)";
	}

	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		Double environmentConfigurationRate = agentStepData.getData(AgentDataType.ENVIRONMENT_REWARD, Double.class);
		if (environmentConfigurationRate != null) {
			BigDecimal value = new BigDecimal(environmentConfigurationRate).setScale(5, RoundingMode.HALF_UP);
			environmentConfigurationRateSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
	}
}
