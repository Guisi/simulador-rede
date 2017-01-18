package br.com.guisi.simulador.rede.view.charts.learning;

import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.LineChartSeries;

public class PolicyChangeChart extends GenericLineChart {

	private LineChartSeries policyChangeSeries;
	
	private int STEPS_GROUP_SIZE = 10;
	private int stepProcessed;
	
	public PolicyChangeChart() {
		super();
		
		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("Policy Change (Every 10 steps)");
		
		getXNumberAxis().setTickUnit(STEPS_GROUP_SIZE);
		
		policyChangeSeries = new LineChartSeries("Policy Change (Every 10 steps)", Double.class);
		policyChangeSeries.getSeries().getData().add(new XYChart.Data<>(0, 0));
		addChartSeries(policyChangeSeries);
	}
	
	@Override
	public String getChartTitle() {
		return "Policy Change";
	}
	
	@Override
	public void processAgentData(AgentData agentData) {
		int steps = agentData.getAgentStepData().size();
		
		while ( (steps - stepProcessed) >= STEPS_GROUP_SIZE) {
			int changedPolicyCount = 0;

			for (int i = 0; i < STEPS_GROUP_SIZE; i++) {
				AgentStepData agentStepStatus = agentData.getAgentStepData().get(stepProcessed++);
				
				Boolean changedPolicy = agentStepStatus.getData(AgentDataType.CHANGED_POLICY, Boolean.class);
				if (changedPolicy) {
					changedPolicyCount++;
				}
			}
			
			policyChangeSeries.updateValues(stepProcessed, changedPolicyCount);
			
			getXNumberAxis().setUpperBound(stepProcessed);
		}
	}
}
