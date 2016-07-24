package br.com.guisi.simulador.rede.view.charts.environment;

import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class RequiredSwitchOperationsChart extends GenericLineChart {

	private LineChartSeries requiredSwitchOperationsSeries;
	
	public RequiredSwitchOperationsChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Required Switch Operations");
		
		requiredSwitchOperationsSeries = new LineChartSeries("Required Switch Operations", Integer.class);
		addLineChartSeries(requiredSwitchOperationsSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "Required Switch Operations";
	}

	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		Integer requiredSwitchOperations = agentStepData.getData(AgentDataType.REQUIRED_SWITCH_OPERATIONS, Integer.class);
		
		if (requiredSwitchOperations != null) {
			requiredSwitchOperationsSeries.updateValues(agentStepData.getStep(), requiredSwitchOperations);
		}
	}
}
