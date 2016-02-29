package br.com.guisi.simulador.rede.view.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.enviroment.Environment;

public class OutOfServicePowerPercentageChart extends GenericLineChart {

	private XYChart.Series<Number, Number> activePowerLossSeries;
	private XYChart.Series<Number, Number> reactivePowerLossSeries;
	
	private Double minActivePower;
	private Double maxActivePower;
	
	private Double minReactivePower;
	private Double maxReactivePower;
	
	public OutOfServicePowerPercentageChart() {
		super();
		
		NumberAxis yAxis = (NumberAxis) this.getYAxis();
        yAxis.setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Out-of-service Loads Power");
		
		activePowerLossSeries = new XYChart.Series<>();
        getData().add(activePowerLossSeries);
        
        reactivePowerLossSeries = new XYChart.Series<>();
        getData().add(reactivePowerLossSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Out-of-service Loads Active Power (MW)");
		if (minActivePower != null) {
			BigDecimal value = new BigDecimal(minActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxActivePower != null) {
			BigDecimal value = new BigDecimal(maxActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		activePowerLossSeries.setName(sb.toString());
		
		sb = new StringBuilder("Out-of-service Loads Reactive Power (MVar)");
		if (minReactivePower != null) {
			BigDecimal value = new BigDecimal(minReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxReactivePower != null) {
			BigDecimal value = new BigDecimal(maxReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		reactivePowerLossSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "% Out-of-service Loads Power";
	}
	
	@Override
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		Environment environment = SimuladorRede.getEnvironment();
		
		Double activePower = agentStepStatus.getInformation(AgentInformationType.OUT_OF_SERVICE_ACTIVE_POWER, Double.class);
		Double activePowerDemand = environment.getTotalActivePowerDemandMW();
		
		if (activePower != null && activePowerDemand != null) {
			BigDecimal value = activePowerDemand.doubleValue() > 0 ? new BigDecimal(activePower / activePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			activePowerLossSeries.getData().add(chartData);
			minActivePower = minActivePower != null ? Math.min(minActivePower, value.doubleValue()) : value.doubleValue();
			maxActivePower = maxActivePower != null ? Math.max(maxActivePower, value.doubleValue()) : value.doubleValue();
		}

		Double reactivePower = agentStepStatus.getInformation(AgentInformationType.OUT_OF_SERVICE_REACTIVE_POWER, Double.class);
		Double reactivePowerDemand = environment.getTotalReactivePowerDemandMVar();
		if (reactivePower != null && reactivePowerDemand != null) {
			BigDecimal value = reactivePowerDemand.doubleValue() > 0 ? new BigDecimal(reactivePower / reactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			reactivePowerLossSeries.getData().add(chartData);
			minReactivePower = minReactivePower != null ? Math.min(minReactivePower, value.doubleValue()) : value.doubleValue();
			maxReactivePower = maxReactivePower != null ? Math.max(maxReactivePower, value.doubleValue()) : value.doubleValue();
		}
		
		this.updateSeriesName();
	}
}
