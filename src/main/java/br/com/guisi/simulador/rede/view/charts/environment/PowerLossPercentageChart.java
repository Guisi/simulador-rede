package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.NumberAxis;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class PowerLossPercentageChart extends GenericLineChart {

	private LineChartSeries activePowerLossSeries;
	private LineChartSeries reactivePowerLossSeries;
	
	public PowerLossPercentageChart() {
		super();
		
		NumberAxis yAxis = (NumberAxis) this.getYAxis();
        yAxis.setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Losses");
		
		activePowerLossSeries = new LineChartSeries("Active Power Lost %", Double.class);
		addLineChartSeries(activePowerLossSeries);
        
        reactivePowerLossSeries = new LineChartSeries("Reactive Power Lost (MVar)", Double.class);
        addLineChartSeries(reactivePowerLossSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "% Power Loss";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		//calcula percentual da perda de potência ativa
		Double activePowerLost = agentStepData.getData(AgentDataType.ACTIVE_POWER_LOST, Double.class);
		Double activePowerDemand = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER, Double.class);
		
		if (activePowerLost != null && activePowerDemand != null) {
			BigDecimal value = activePowerDemand.doubleValue() > 0 ? new BigDecimal(activePowerLost / activePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			activePowerLossSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}

		//calcula percentual da perda de potência reativa
		Double reactivePowerLost = agentStepData.getData(AgentDataType.REACTIVE_POWER_LOST, Double.class);
		Double reactivePowerDemand = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_REACTIVE_POWER, Double.class);
		if (reactivePowerLost != null && reactivePowerDemand != null) {
			BigDecimal value = reactivePowerDemand.doubleValue() > 0 ? new BigDecimal(reactivePowerLost / reactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			reactivePowerLossSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
	}
}
