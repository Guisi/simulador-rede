package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class SuppliedLoadsActivePowerPercentageChart extends GenericLineChart {

	private LineChartSeries suppliedLoadsPercentageSeries;
	
	public SuppliedLoadsActivePowerPercentageChart() {
		super();
		
		getYNumberAxis().setAutoRanging(false);
		getYNumberAxis().setLowerBound(0);
		getYNumberAxis().setUpperBound(100);
		
		suppliedLoadsPercentageSeries = new LineChartSeries(getChartTitle(), Double.class);
		addLineChartSeries(suppliedLoadsPercentageSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "% Supplied Loads Active Power x Priority";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		//calcula percentual de loads atendidos considerando a prioridade
		Double suppliedLoads = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, Double.class);
		Double notSuppliedLoads = agentStepData.getData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, Double.class);
		
		if (suppliedLoads != null && notSuppliedLoads != null) {
			Double total = suppliedLoads + notSuppliedLoads;
			BigDecimal value = total > 0 ? new BigDecimal(suppliedLoads / total * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			suppliedLoadsPercentageSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
	}
}
