package br.com.guisi.simulador.rede.view.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;

public class SuppliedLoadsActivePowerPercentageChart extends GenericLineChart {

	private XYChart.Series<Number, Number> suppliedLoadsPercentageSeries;
	
	private Double minPercentage;
	private Double maxPercentage;
	
	public SuppliedLoadsActivePowerPercentageChart() {
		super();
		
		getYNumberAxis().setAutoRanging(false);
		getYNumberAxis().setLowerBound(0);
		getYNumberAxis().setUpperBound(100);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Loads Supplied Active Power MW x Priority");
		
		suppliedLoadsPercentageSeries = new XYChart.Series<>();
        getData().add(suppliedLoadsPercentageSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("% Loads Supplied x Priority");
		if (minPercentage != null) {
			BigDecimal value = new BigDecimal(minPercentage).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxPercentage != null) {
			BigDecimal value = new BigDecimal(maxPercentage).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		suppliedLoadsPercentageSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "% Loads Supplied Active Power MW x Priority";
	}
	
	@Override
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		//calcula percentual de loads atendidos considerando a prioridade
		Double suppliedLoads = agentStepStatus.getInformation(AgentInformationType.SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, Double.class);
		Double notSuppliedLoads = agentStepStatus.getInformation(AgentInformationType.NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY, Double.class);
		
		if (suppliedLoads != null && notSuppliedLoads != null) {
			Double total = suppliedLoads + notSuppliedLoads;
			BigDecimal value = total > 0 ? new BigDecimal(suppliedLoads / total * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			suppliedLoadsPercentageSeries.getData().add(chartData);
			minPercentage = minPercentage != null ? Math.min(minPercentage, value.doubleValue()) : value.doubleValue();
			maxPercentage = maxPercentage != null ? Math.max(maxPercentage, value.doubleValue()) : value.doubleValue();
			
			getYNumberAxis().setLowerBound(minPercentage < 5 ? 0 : minPercentage - 5);
	        getYNumberAxis().setUpperBound(maxPercentage > 95 ? 100 : maxPercentage + 5);
		}
		
		this.updateSeriesName();
	}
}
