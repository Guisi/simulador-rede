package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class LoadsPowerPercentageChart extends GenericLineChart {

	private LineChartSeries suppliedActivePowerSeries;
	private LineChartSeries suppliedReactivePowerSeries;
	private LineChartSeries notSuppliedActivePowerSeries;
	private LineChartSeries notSuppliedReactivePowerSeries;
	private LineChartSeries outOfServiceActivePowerSeries;
	private LineChartSeries outOfServiceReactivePowerSeries;
	
	private EnvironmentKeyType environmentKeyType;
	
	public LoadsPowerPercentageChart(EnvironmentKeyType environmentKeyType) {
		super();
		
		this.environmentKeyType = environmentKeyType;
		
        getYNumberAxis().setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Loads Power");
		
		suppliedActivePowerSeries = new LineChartSeries("Supplied Loads Active Power %", Double.class);
		addLineChartSeries(suppliedActivePowerSeries);
        suppliedReactivePowerSeries = new LineChartSeries("Supplied Loads Reactive Power %", Double.class);
        addLineChartSeries(suppliedReactivePowerSeries);
		
		notSuppliedActivePowerSeries = new LineChartSeries("Not Supplied Loads Active Power %", Double.class);
		addLineChartSeries(notSuppliedActivePowerSeries);
        notSuppliedReactivePowerSeries = new LineChartSeries("Not Supplied Loads Reactive Power %", Double.class);
        addLineChartSeries(notSuppliedReactivePowerSeries);
		
		outOfServiceActivePowerSeries = new LineChartSeries("Out-of-service Loads Active Power %", Double.class);
		addLineChartSeries(outOfServiceActivePowerSeries);
        outOfServiceReactivePowerSeries = new LineChartSeries("Out-of-service Loads Reactive Power %", Double.class);
        addLineChartSeries(outOfServiceReactivePowerSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "% Loads Power";
	}
	
	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		Environment environment = SimuladorRede.getEnvironment(environmentKeyType);
		
		Double totalActivePowerDemand = environment.getTotalActivePowerDemandMW();
		Double totalReactivePowerDemand = environment.getTotalReactivePowerDemandMVar();

		//Supplied
		Double suppliedActivePower = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_ACTIVE_POWER, Double.class);
		if (suppliedActivePower != null && totalActivePowerDemand != null) {
			BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(suppliedActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			suppliedActivePowerSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}

		Double suppliedReactivePower = agentStepData.getData(AgentDataType.SUPPLIED_LOADS_REACTIVE_POWER, Double.class);
		if (suppliedReactivePower != null && totalReactivePowerDemand != null) {
			BigDecimal value = totalReactivePowerDemand.doubleValue() > 0 ? new BigDecimal(suppliedReactivePower / totalReactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			suppliedReactivePowerSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
		
		//Not Supplied
		Double notSuppliedActivePower = agentStepData.getData(AgentDataType.NOT_SUPPLIED_LOADS_ACTIVE_POWER, Double.class);
		if (notSuppliedActivePower != null && totalActivePowerDemand != null) {
			BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(notSuppliedActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			notSuppliedActivePowerSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}

		Double notSuppliedReactivePower = agentStepData.getData(AgentDataType.NOT_SUPPLIED_LOADS_REACTIVE_POWER, Double.class);
		if (notSuppliedReactivePower != null && totalReactivePowerDemand != null) {
			BigDecimal value = totalReactivePowerDemand.doubleValue() > 0 ? new BigDecimal(notSuppliedReactivePower / totalReactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			notSuppliedReactivePowerSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
		
		//Out of Service
		Double outOfServiceActivePower = agentStepData.getData(AgentDataType.OUT_OF_SERVICE_LOADS_ACTIVE_POWER, Double.class);
		if (outOfServiceActivePower != null && totalActivePowerDemand != null) {
			BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(outOfServiceActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			outOfServiceActivePowerSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}

		Double outOfServiceReactivePower = agentStepData.getData(AgentDataType.OUT_OF_SERVICE_LOADS_REACTIVE_POWER, Double.class);
		if (outOfServiceReactivePower != null && totalReactivePowerDemand != null) {
			BigDecimal value = totalReactivePowerDemand.doubleValue() > 0 ? new BigDecimal(outOfServiceReactivePower / totalReactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			outOfServiceReactivePowerSeries.updateValues(agentStepData.getStep(), value.doubleValue());
		}
	}
}
