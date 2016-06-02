package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class MinLoadVoltagePUChart extends GenericLineChart {

	private XYChart.Series<Number, Number> minLoadCurrentVoltageSeries;
	private Double minValue;
	private Double maxValue;
	
	public MinLoadVoltagePUChart() {
		super();
		
        getYNumberAxis().setAutoRanging(false);
        getYNumberAxis().setLowerBound(Constants.TENSAO_MIN_PU);
        getYNumberAxis().setUpperBound(Constants.TENSAO_MAX_PU);
        getYNumberAxis().setTickUnit(0.01);
		
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Min Load Current Voltage (PU)");
		
		minLoadCurrentVoltageSeries = new XYChart.Series<>();
        getData().add(minLoadCurrentVoltageSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Min Load Current Voltage (PU)");
		if (minValue != null) {
			BigDecimal value = new BigDecimal(minValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());
		}
		if (maxValue != null) {
			BigDecimal value = new BigDecimal(maxValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString());
		}
		minLoadCurrentVoltageSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Min Load Current Voltage (PU)";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		getXNumberAxis().setUpperBound(agentStepData.getStep());
		Double minVoltage = agentStepData.getData(AgentDataType.MIN_LOAD_VOLTAGE_PU, Double.class);
		
		if (minVoltage != null && minVoltage > 0) {
			BigDecimal value = new BigDecimal(minVoltage).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			minLoadCurrentVoltageSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, value.doubleValue()) : value.doubleValue();
			maxValue = maxValue != null ? Math.max(maxValue, value.doubleValue()) : value.doubleValue();
			
			getYNumberAxis().setLowerBound(minValue - 0.05);
	        getYNumberAxis().setUpperBound(maxValue + 0.05);
		}

		this.updateSeriesName();
	}
}