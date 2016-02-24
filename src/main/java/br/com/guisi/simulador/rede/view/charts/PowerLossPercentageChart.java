package br.com.guisi.simulador.rede.view.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;

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
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		//calcula percentual da perda de potência ativa
		Double activePowerLost = agentStepStatus.getInformation(AgentInformationType.ACTIVE_POWER_LOST, Double.class);
		Double activePowerDemand = agentStepStatus.getInformation(AgentInformationType.ACTIVE_POWER_DEMAND, Double.class);
		
		if (activePowerLost != null && activePowerDemand != null) {
			BigDecimal value = activePowerDemand.doubleValue() > 0 ? new BigDecimal(activePowerLost / activePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			activePowerLossSeries.getData().add(chartData);
			minActivePowerLoss = minActivePowerLoss != null ? Math.min(minActivePowerLoss, value.doubleValue()) : value.doubleValue();
			maxActivePowerLoss = maxActivePowerLoss != null ? Math.max(maxActivePowerLoss, value.doubleValue()) : value.doubleValue();
		}

		//calcula percentual da perda de potência reativa
		Double reactivePowerLost = agentStepStatus.getInformation(AgentInformationType.REACTIVE_POWER_LOST, Double.class);
		Double reactivePowerDemand = agentStepStatus.getInformation(AgentInformationType.REACTIVE_POWER_DEMAND, Double.class);
		if (reactivePowerLost != null && reactivePowerDemand != null) {
			BigDecimal value = reactivePowerDemand.doubleValue() > 0 ? new BigDecimal(reactivePowerLost / reactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			reactivePowerLossSeries.getData().add(chartData);
			minReactivePowerLoss = minReactivePowerLoss != null ? Math.min(minReactivePowerLoss, value.doubleValue()) : value.doubleValue();
			maxReactivePowerLoss = maxReactivePowerLoss != null ? Math.max(maxReactivePowerLoss, value.doubleValue()) : value.doubleValue();
		}
		
		this.updateSeriesName();
	}
}
