package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class MinLoadVoltagePUChart extends GenericLineChart {

	private LineChartSeries minLoadCurrentVoltageSeries;
	
	public MinLoadVoltagePUChart() {
		super();
		
        getYNumberAxis().setAutoRanging(false);
        getYNumberAxis().setLowerBound(Constants.TENSAO_MIN_PU);
        getYNumberAxis().setUpperBound(Constants.TENSAO_MAX_PU);
        getYNumberAxis().setTickUnit(0.01);
		
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Min Load Current Voltage (PU)");
		
		minLoadCurrentVoltageSeries = new LineChartSeries("Min Load Current Voltage (PU)", Double.class);
        addChartSeries(minLoadCurrentVoltageSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "Min Load Current Voltage (PU)";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		Double minVoltage = agentStepData.getData(AgentDataType.MIN_LOAD_VOLTAGE_PU, Double.class);
		if (minVoltage != null && minVoltage > 0) {
			BigDecimal value = new BigDecimal(minVoltage).setScale(5, RoundingMode.HALF_UP);
			minLoadCurrentVoltageSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
	}
}
