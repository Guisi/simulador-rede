package br.com.guisi.simulador.rede.view.charts.learning;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class PolicyChangeChart extends GenericLineChart {

	private XYChart.Series<Number, Number> policyChangeSeries;
	
	private int STEPS_GROUP_SIZE = 10;
	
	private Integer minValue;
	private Integer maxValue;
	private Double average;
	private int cumulative;
	private int stepProcessed;
	
	public PolicyChangeChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Policy Change (Every 10 steps)");
		
		getXNumberAxis().setTickUnit(STEPS_GROUP_SIZE);
		
		policyChangeSeries = new XYChart.Series<>();
		Data<Number, Number> chartData = new XYChart.Data<>(0, 0);
		policyChangeSeries.getData().add(chartData);
        getData().add(policyChangeSeries);
        
        this.cumulative = 0;
        this.stepProcessed = 0;
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		StringBuilder sb = new StringBuilder("Policy Change (Every 10 steps)");
		if (minValue != null) {
			sb.append("\nMin Value: ").append(minValue.toString());
		}
		if (maxValue != null) {
			sb.append("\nMax Value: ").append(maxValue.toString());
		}
		if (average != null) {
			BigDecimal value = new BigDecimal(average).setScale(2, RoundingMode.HALF_UP);
			sb.append("\nAverage: ").append(value.toString());
		}
		policyChangeSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "Policy Change";
	}
	
	@Override
	public void processAgentStatus(AgentStatus agentStatus) {
		int steps = agentStatus.getStepStatus().size();
		
		while ( (steps - stepProcessed) >= STEPS_GROUP_SIZE) {
			int changedPolicyCount = 0;

			for (int i = 0; i < STEPS_GROUP_SIZE; i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(stepProcessed++);
				
				Boolean changedPolicy = agentStepStatus.getInformation(AgentInformationType.CHANGED_POLICY, Boolean.class);
				if (changedPolicy) {
					changedPolicyCount++;
				}
			}
			
			Data<Number, Number> chartData = new XYChart.Data<>(stepProcessed, changedPolicyCount);
			policyChangeSeries.getData().add(chartData);
			minValue = minValue != null ? Math.min(minValue, changedPolicyCount) : changedPolicyCount;
			maxValue = maxValue != null ? Math.max(maxValue, changedPolicyCount) : changedPolicyCount;
			cumulative += changedPolicyCount;
			average = (double) cumulative / (double) (stepProcessed / STEPS_GROUP_SIZE);
			
			getXNumberAxis().setUpperBound(stepProcessed);
			
			this.updateSeriesName();
		}
	}
}
