package br.com.guisi.simulador.rede.view.charts.learning;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class QValuesAverageChart extends GenericLineChart {

	private XYChart.Series<Number, Number> qValuesAverageSeries;
	
	private Double minValue;
	private Double maxValue;
	
	public QValuesAverageChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Q-Values Average");
		
		qValuesAverageSeries = new XYChart.Series<>();
        getData().add(qValuesAverageSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Q-Values Average");
		if (minValue != null) {
			BigDecimal value = new BigDecimal(minValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());
		}
		if (maxValue != null) {
			BigDecimal value = new BigDecimal(maxValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString());
		}
		qValuesAverageSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Q-Values Average";
	}

	@Override
	public void processAgentStepData(AgentStepData agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		Double qValuesAverage = agentStepStatus.getData(AgentDataType.QVALUES_AVERAGE, Double.class);
		
		if (qValuesAverage != null) {
			BigDecimal value = new BigDecimal(qValuesAverage).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			qValuesAverageSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, value.doubleValue()) : value.doubleValue();
			maxValue = maxValue != null ? Math.max(maxValue, value.doubleValue()) : value.doubleValue();
		}
		
		this.updateSeriesName();
	}
}
