package br.com.guisi.simulador.rede.view.charts.environment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;

public class LoadsPowerPercentageChart extends GenericLineChart {

	private XYChart.Series<Number, Number> suppliedActivePowerSeries;
	private XYChart.Series<Number, Number> suppliedReactivePowerSeries;
	private XYChart.Series<Number, Number> notSuppliedActivePowerSeries;
	private XYChart.Series<Number, Number> notSuppliedReactivePowerSeries;
	private XYChart.Series<Number, Number> outOfServiceActivePowerSeries;
	private XYChart.Series<Number, Number> outOfServiceReactivePowerSeries;
	
	private Double minSuppliedActivePower;
	private Double maxSuppliedActivePower;
	private Double minSuppliedReactivePower;
	private Double maxSuppliedReactivePower;
	
	private Double minNotSuppliedActivePower;
	private Double maxNotSuppliedActivePower;
	private Double minNotSuppliedReactivePower;
	private Double maxNotSuppliedReactivePower;
	
	private Double minOutOfServiceActivePower;
	private Double maxOutOfServiceActivePower;
	private Double minOutOfServiceReactivePower;
	private Double maxOutOfServiceReactivePower;
	
	public LoadsPowerPercentageChart() {
		super();
		
		NumberAxis yAxis = (NumberAxis) this.getYAxis();
        yAxis.setUpperBound(0.1);
		getXAxis().setLabel("Iteraction");
		getYAxis().setLabel("% Loads Power");
		
		suppliedActivePowerSeries = new XYChart.Series<>();
        getData().add(suppliedActivePowerSeries);
        suppliedReactivePowerSeries = new XYChart.Series<>();
        getData().add(suppliedReactivePowerSeries);
		
		notSuppliedActivePowerSeries = new XYChart.Series<>();
        getData().add(notSuppliedActivePowerSeries);
        notSuppliedReactivePowerSeries = new XYChart.Series<>();
        getData().add(notSuppliedReactivePowerSeries);
		
		outOfServiceActivePowerSeries = new XYChart.Series<>();
        getData().add(outOfServiceActivePowerSeries);
        outOfServiceReactivePowerSeries = new XYChart.Series<>();
        getData().add(outOfServiceReactivePowerSeries);
        
        this.updateSeriesName();
	}
	
	private void updateSeriesName() {
		//Supplied
		StringBuilder sb = new StringBuilder("Supplied Loads Active Power (MW)");
		if (minSuppliedActivePower != null) {
			BigDecimal value = new BigDecimal(minSuppliedActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxSuppliedActivePower != null) {
			BigDecimal value = new BigDecimal(maxSuppliedActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		suppliedActivePowerSeries.setName(sb.toString());
		
		sb = new StringBuilder("Supplied Loads Reactive Power (MVar)");
		if (minSuppliedReactivePower != null) {
			BigDecimal value = new BigDecimal(minSuppliedReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxSuppliedReactivePower != null) {
			BigDecimal value = new BigDecimal(maxSuppliedReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		suppliedReactivePowerSeries.setName(sb.toString());
		
		//Not Supplied
		sb = new StringBuilder("Not Supplied Loads Active Power (MW)");
		if (minNotSuppliedActivePower != null) {
			BigDecimal value = new BigDecimal(minNotSuppliedActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxNotSuppliedActivePower != null) {
			BigDecimal value = new BigDecimal(maxNotSuppliedActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		notSuppliedActivePowerSeries.setName(sb.toString());
		
		sb = new StringBuilder("Not Supplied Loads Reactive Power (MVar)");
		if (minNotSuppliedReactivePower != null) {
			BigDecimal value = new BigDecimal(minNotSuppliedReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxNotSuppliedReactivePower != null) {
			BigDecimal value = new BigDecimal(maxNotSuppliedReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		notSuppliedReactivePowerSeries.setName(sb.toString());
		
		//Out of Service
		sb = new StringBuilder("Out-of-service Loads Active Power (MW)");
		if (minOutOfServiceActivePower != null) {
			BigDecimal value = new BigDecimal(minOutOfServiceActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxOutOfServiceActivePower != null) {
			BigDecimal value = new BigDecimal(maxOutOfServiceActivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		outOfServiceActivePowerSeries.setName(sb.toString());
		
		sb = new StringBuilder("Out-of-service Loads Reactive Power (MVar)");
		if (minOutOfServiceReactivePower != null) {
			BigDecimal value = new BigDecimal(minOutOfServiceReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMin Value: ").append(value.toString()).append(" %");
		}
		if (maxOutOfServiceReactivePower != null) {
			BigDecimal value = new BigDecimal(maxOutOfServiceReactivePower).setScale(5, RoundingMode.HALF_UP);
			sb.append("\nMax Value: ").append(value.toString()).append(" %");
		}
		outOfServiceReactivePowerSeries.setName(sb.toString());
	}
	
	@Override
	public String getChartTitle() {
		return "% Loads Power";
	}
	
	@Override
	public void processAgentStepStatus(AgentStepStatus agentStepStatus) {
		getXNumberAxis().setUpperBound(agentStepStatus.getStep());
		
		Environment environment = SimuladorRede.getInteractionEnvironment();
		
		Double totalActivePowerDemand = environment.getTotalActivePowerDemandMW();
		Double totalReactivePowerDemand = environment.getTotalReactivePowerDemandMVar();

		//Supplied
		Double suppliedActivePower = agentStepStatus.getInformation(AgentInformationType.SUPPLIED_LOADS_ACTIVE_POWER, Double.class);
		if (suppliedActivePower != null && totalActivePowerDemand != null) {
			BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(suppliedActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			suppliedActivePowerSeries.getData().add(chartData);
			minSuppliedActivePower = minSuppliedActivePower != null ? Math.min(minSuppliedActivePower, value.doubleValue()) : value.doubleValue();
			maxSuppliedActivePower = maxSuppliedActivePower != null ? Math.max(maxSuppliedActivePower, value.doubleValue()) : value.doubleValue();
		}

		Double suppliedReactivePower = agentStepStatus.getInformation(AgentInformationType.SUPPLIED_LOADS_REACTIVE_POWER, Double.class);
		if (suppliedReactivePower != null && totalReactivePowerDemand != null) {
			BigDecimal value = totalReactivePowerDemand.doubleValue() > 0 ? new BigDecimal(suppliedReactivePower / totalReactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			suppliedReactivePowerSeries.getData().add(chartData);
			minSuppliedReactivePower = minSuppliedReactivePower != null ? Math.min(minSuppliedReactivePower, value.doubleValue()) : value.doubleValue();
			maxSuppliedReactivePower = maxSuppliedReactivePower != null ? Math.max(maxSuppliedReactivePower, value.doubleValue()) : value.doubleValue();
		}
		
		//Not Supplied
		Double notSuppliedActivePower = agentStepStatus.getInformation(AgentInformationType.NOT_SUPPLIED_LOADS_ACTIVE_POWER, Double.class);
		if (notSuppliedActivePower != null && totalActivePowerDemand != null) {
			BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(notSuppliedActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			notSuppliedActivePowerSeries.getData().add(chartData);
			minNotSuppliedActivePower = minNotSuppliedActivePower != null ? Math.min(minNotSuppliedActivePower, value.doubleValue()) : value.doubleValue();
			maxNotSuppliedActivePower = maxNotSuppliedActivePower != null ? Math.max(maxNotSuppliedActivePower, value.doubleValue()) : value.doubleValue();
		}

		Double notSuppliedReactivePower = agentStepStatus.getInformation(AgentInformationType.NOT_SUPPLIED_LOADS_REACTIVE_POWER, Double.class);
		if (notSuppliedReactivePower != null && totalReactivePowerDemand != null) {
			BigDecimal value = totalReactivePowerDemand.doubleValue() > 0 ? new BigDecimal(notSuppliedReactivePower / totalReactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			notSuppliedReactivePowerSeries.getData().add(chartData);
			minNotSuppliedReactivePower = minNotSuppliedReactivePower != null ? Math.min(minNotSuppliedReactivePower, value.doubleValue()) : value.doubleValue();
			maxNotSuppliedReactivePower = maxNotSuppliedReactivePower != null ? Math.max(maxNotSuppliedReactivePower, value.doubleValue()) : value.doubleValue();
		}
		
		//Out of Service
		Double outOfServiceActivePower = agentStepStatus.getInformation(AgentInformationType.OUT_OF_SERVICE_LOADS_ACTIVE_POWER, Double.class);
		if (outOfServiceActivePower != null && totalActivePowerDemand != null) {
			BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(outOfServiceActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			outOfServiceActivePowerSeries.getData().add(chartData);
			minOutOfServiceActivePower = minOutOfServiceActivePower != null ? Math.min(minOutOfServiceActivePower, value.doubleValue()) : value.doubleValue();
			maxOutOfServiceActivePower = maxOutOfServiceActivePower != null ? Math.max(maxOutOfServiceActivePower, value.doubleValue()) : value.doubleValue();
		}

		Double outOfServiceReactivePower = agentStepStatus.getInformation(AgentInformationType.OUT_OF_SERVICE_LOADS_REACTIVE_POWER, Double.class);
		if (outOfServiceReactivePower != null && totalReactivePowerDemand != null) {
			BigDecimal value = totalReactivePowerDemand.doubleValue() > 0 ? new BigDecimal(outOfServiceReactivePower / totalReactivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
			Data<Number, Number> chartData = new XYChart.Data<>(agentStepStatus.getStep(), value.doubleValue());
			outOfServiceReactivePowerSeries.getData().add(chartData);
			minOutOfServiceReactivePower = minOutOfServiceReactivePower != null ? Math.min(minOutOfServiceReactivePower, value.doubleValue()) : value.doubleValue();
			maxOutOfServiceReactivePower = maxOutOfServiceReactivePower != null ? Math.max(maxOutOfServiceReactivePower, value.doubleValue()) : value.doubleValue();
		}
		
		this.updateSeriesName();
	}
}
