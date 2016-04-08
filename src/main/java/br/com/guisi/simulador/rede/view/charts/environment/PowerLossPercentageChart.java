package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class PowerLossPercentageChart extends GenericLineChart {

	private XYChart.Series<Number, Number> activePowerLossSeries;
	private XYChart.Series<Number, Number> reactivePowerLossSeries;
	
	private Double minActivePowerLoss;
	private Double maxActivePowerLoss;
	
	private Double minReactivePowerLoss;
	private Double maxReactivePowerLoss;
	
	public PowerLossPercentageChart() {
		super();
		
		NumberAxis yAxis = (NumberAxis) this.getYAxis();
        yAxis.setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Losses");
		
		activePowerLossSeries = new XYChart.Series<>();
        getData().add(activePowerLossSeries);
        
        reactivePowerLossSeries = new XYChart.Series<>();
        getData().add(reactivePowerLossSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Active Power Lost (MW)");
		if (minActivePowerLoss != null) {
			BigDecimal value = new BigDecimal(minActivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxActivePowerLoss != null) {
			BigDecimal value = new BigDecimal(maxActivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		activePowerLossSeries.setName(sb.toString());
		
		sb = new StringBuilder("Reactive Power Lost (MVar)");
		if (minReactivePowerLoss != null) {
			BigDecimal value = new BigDecimal(minReactivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxReactivePowerLoss != null) {
			BigDecimal value = new BigDecimal(maxReactivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		reactivePowerLossSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "% Power Loss";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		getXNumberAxis().setUpperBound(agentStepData.getStep());
		
		//calcula percentual da perda de potência ativa
		Double activePowerLost = agentStepData.getData(AgentDataType.ACTIVE_POWER_LOST, Double.class);
		Double activePowerDemand = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER, Double.class);
		
		if (activePowerLost != null && activePowerDemand != null) {
			BigDecimal value = activePowerDemand.doubleValue() > 0 ? new BigDecimal(activePowerLost / activePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			activePowerLossSeries.getData().add(chartData);
			minActivePowerLoss = minActivePowerLoss != null ? Math.min(minActivePowerLoss, value.doubleValue()) : value.doubleValue();
			maxActivePowerLoss = maxActivePowerLoss != null ? Math.max(maxActivePowerLoss, value.doubleValue()) : value.doubleValue();
		}

		//calcula percentual da perda de potência reativa
		Double reactivePowerLost = agentStepData.getData(AgentDataType.REACTIVE_POWER_LOST, Double.class);
		Double reactivePowerDemand = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_REACTIVE_POWER, Double.class);
		if (reactivePowerLost != null && reactivePowerDemand != null) {
			BigDecimal value = reactivePowerDemand.doubleValue() > 0 ? new BigDecimal(reactivePowerLost / reactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			reactivePowerLossSeries.getData().add(chartData);
			minReactivePowerLoss = minReactivePowerLoss != null ? Math.min(minReactivePowerLoss, value.doubleValue()) : value.doubleValue();
			maxReactivePowerLoss = maxReactivePowerLoss != null ? Math.max(maxReactivePowerLoss, value.doubleValue()) : value.doubleValue();
		}
		
		this.updateSeriesName();
	}
}
