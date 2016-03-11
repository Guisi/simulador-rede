package br.com.guisi.simulador.rede.view.charts;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;

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
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		Integer requiredSwitchOperations = agentStepStatus.getInformation(AgentInformationType.REQUIRED_SWITCH_OPERATIONS, Integer.class);
		
		if (requiredSwitchOperations != null) {
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), requiredSwitchOperations);
			requiredSwitchOperationsSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, requiredSwitchOperations) : requiredSwitchOperations;
			maxValue = maxValue != null ? Math.max(maxValue, requiredSwitchOperations) : requiredSwitchOperations;
		}
		
		this.updateSeriesName();
	}
}
