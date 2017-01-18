package br.com.guisi.simulador.rede.view.charts.environment;

import br.com.guisi.simulador.rede.enviroment.Environment;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class VoltageBarChart extends BarChart<String, Number> {

	private XYChart.Series<String, Number> series;
	
	public VoltageBarChart() {
		super(new CategoryAxis(), new NumberAxis());
		
		this.setPrefHeight(300);
        this.setLegendVisible(false);
        this.setAnimated(false);
        this.getStyleClass().add("bar-chart");

		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Load Number");
		
		getYAxis().setLabel(getChartTitle());
		
		series = new XYChart.Series<>();
		getData().add(series);
	}
	
	public CategoryAxis getXNumberAxis() {
		return (CategoryAxis) this.getXAxis();
	}
	
	public NumberAxis getYNumberAxis() {
		return (NumberAxis) this.getYAxis();
	}
	
	public String getChartTitle() {
		return "Voltage Magnitude in p.u.";
	}
	
	public void updateChart(Environment environment) {
		series.getData().clear();
		environment.getLoads().forEach(load -> {
			Data<String, Number> chartData = new XYChart.Data<>(load.getNodeNumber().toString(), load.getCurrentVoltagePU());
			series.getData().add(chartData);
		});
	}
}
