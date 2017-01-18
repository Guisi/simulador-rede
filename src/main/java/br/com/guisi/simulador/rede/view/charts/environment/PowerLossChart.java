package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class PowerLossChart extends GenericLineChart {

	private LineChartSeries activePowerLossSeries;
	private LineChartSeries reactivePowerLossSeries;
	
	public PowerLossChart() {
		super();
		
        getYNumberAxis().setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Losses MW/MVar");
		
		activePowerLossSeries = new LineChartSeries("Active Power Lost (MW)", Double.class);
        addChartSeries(activePowerLossSeries);
        
        reactivePowerLossSeries = new LineChartSeries("Reactive Power Lost (MVar)", Double.class);
        addChartSeries(reactivePowerLossSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "Power Loss MW/MVar";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		//seta perda da potência ativa
		Double activePowerLost = agentStepData.getData(AgentDataType.ACTIVE_POWER_LOST, Double.class);
		if (activePowerLost != null) {
			BigDecimal value = new BigDecimal(activePowerLost).setScale(5, RoundingMode.HALF_UP);
			activePowerLossSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}

		//seta perda da potência reativa
		Double reactivePowerLost = agentStepData.getData(AgentDataType.REACTIVE_POWER_LOST, Double.class);
		if (reactivePowerLost != null) {
			BigDecimal value = new BigDecimal(reactivePowerLost).setScale(5, RoundingMode.HALF_UP);
			reactivePowerLossSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
	}
}
