package br.com.guisi.simulador.rede.view.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;

public class SuppliedLoadsChart extends GenericLineChart {

	private XYChart.Series<Number, Number> suppliedLoadsSeries;
	private Double minValue;
	private Double maxValue;
	
	public SuppliedLoadsChart() {
		super();
		
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Loads Supplied x Priority");
		
		suppliedLoadsSeries = new XYChart.Series<>();
        getData().add(suppliedLoadsSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Loads Supplied x Priority");
		if (minValue != null) {
			BigDecimal value = new BigDecimal(minValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());
		}
		if (maxValue != null) {
			BigDecimal value = new BigDecimal(maxValue).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString());
		}
		suppliedLoadsSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Loads Supplied x Priority";
	}
	
	@Override
	public void clearData() {
		suppliedLoadsSeries.getData().clear();
	}

	@Override
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		Double suppliedLoads = agentStepStatus.getInformation(AgentInformationType.SUPPLIED_LOADS_VS_PRIORITY, Double.class);
		
		if (suppliedLoads != null) {
			BigDecimal value = new BigDecimal(suppliedLoads).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			suppliedLoadsSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, value.doubleValue()) : value.doubleValue();
			maxValue = maxValue != null ? Math.max(maxValue, value.doubleValue()) : value.doubleValue();
		}

		this.updateSeriesName();
	}
}
