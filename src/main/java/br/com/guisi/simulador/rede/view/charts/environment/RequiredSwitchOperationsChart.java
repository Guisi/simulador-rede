package br.com.guisi.simulador.rede.view.charts.environment;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class RequiredSwitchOperationsChart extends GenericLineChart {

	private XYChart.Series<Number, Number> requiredSwitchOperationsSeries;
	
	private Integer minValue;
	private Integer maxValue;
	
	public RequiredSwitchOperationsChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Required Switch Operations");
		
		requiredSwitchOperationsSeries = new XYChart.Series<>();
        getData().add(requiredSwitchOperationsSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Required Switch Operations");
		if (minValue != null) {
			sb.append("\nMin Value: ").append(minValue.toString());
		}
		if (maxValue != null) {
			sb.append("\nMax Value: ").append(maxValue.toString());
		}
		requiredSwitchOperationsSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Required Switch Operations";
	}

	@Override
	public void processAgentStepData(AgentStepData agentStepData) {
		getXNumberAxis().setUpperBound(agentStepData.getStep());
		
		Integer requiredSwitchOperations = agentStepData.getData(AgentDataType.REQUIRED_SWITCH_OPERATIONS, Integer.class);
		
		if (requiredSwitchOperations != null) {
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepData.getStep(), requiredSwitchOperations);
			requiredSwitchOperationsSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, requiredSwitchOperations) : requiredSwitchOperations;
			maxValue = maxValue != null ? Math.max(maxValue, requiredSwitchOperations) : requiredSwitchOperations;
		}
		
		this.updateSeriesName();
	}
}
