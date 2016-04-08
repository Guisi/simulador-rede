package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class PowerLossChart extends GenericLineChart {

	private XYChart.Series<Number, Number> activePowerLossSeries;
	private XYChart.Series<Number, Number> reactivePowerLossSeries;
	
	private Double minActivePowerLoss;
	private Double maxActivePowerLoss;
	
	private Double minReactivePowerLoss;
	private Double maxReactivePowerLoss;
	
	public PowerLossChart() {
		super();
		
        getYNumberAxis().setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Losses MW/MVar");
		
		activePowerLossSeries = new XYChart.Series<>();
        activePowerLossSeries.setName("Active Power Lost (MW)");
        getData().add(activePowerLossSeries);
        
        reactivePowerLossSeries = new XYChart.Series<>();
        reactivePowerLossSeries.setName("Reactive Power Lost (MVar)");
        getData().add(reactivePowerLossSeries);
        
        this.updateSeriesName();
	}
	
	@Override
	public String getChartTitle() {
		return "Power Loss MW/MVar";
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Active Power Lost (MW)");
		if (minActivePowerLoss != null) {
			BigDecimal value = new BigDecimal(minActivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());
		}
		if (maxActivePowerLoss != null) {
			BigDecimal value = new BigDecimal(maxActivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString());
		}
		activePowerLossSeries.setName(sb.toString());
		
		sb = new StringBuilder("Reactive Power Lost (MVar)");
		if (minReactivePowerLoss != null) {
			BigDecimal value = new BigDecimal(minReactivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString());
		}
		if (maxReactivePowerLoss != null) {
			BigDecimal value = new BigDecimal(maxReactivePowerLoss).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString());
		}
		reactivePowerLossSeries.setName(sb.toString());
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		getXNumberAxis().setUpperBound(agentStepData.getStep());
		
		//seta perda da potência ativa
		Double activePowerLost = agentStepData.getData(AgentDataType.ACTIVE_POWER_LOST, Double.class);
		if (activePowerLost != null) {
			BigDecimal value = new BigDecimal(activePowerLost).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			activePowerLossSeries.getData().add(chartData);
			minActivePowerLoss = minActivePowerLoss != null ? Math.min(minActivePowerLoss, activePowerLost) : activePowerLost;
			maxActivePowerLoss = maxActivePowerLoss != null ? Math.max(maxActivePowerLoss, activePowerLost) : activePowerLost;
		}

		//seta perda da potência reativa
		Double reactivePowerLost = agentStepData.getData(AgentDataType.REACTIVE_POWER_LOST, Double.class);
		if (reactivePowerLost != null) {
			BigDecimal value = new BigDecimal(reactivePowerLost).setScale(5, RoundingMode.HALF_UP);
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), value.doubleValue());
			reactivePowerLossSeries.getData().add(chartData);
			minReactivePowerLoss = minReactivePowerLoss != null ? Math.min(minReactivePowerLoss, reactivePowerLost) : reactivePowerLost;
			maxReactivePowerLoss = maxReactivePowerLoss != null ? Math.max(maxReactivePowerLoss, reactivePowerLost) : reactivePowerLost;
		}
		
		this.updateSeriesName();
	}
}
