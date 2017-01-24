package br.com.guisi.simulador.rede.view.charts.environment;

import br.com.guisi.simulador.rede.enviroment.Environment;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class VoltageLineChart extends LineChart<String, Number> {

	private XYChart.Series<String, Number> series;
	private XYChart.Series<String, Number> series2;
	private XYChart.Series<String, Number> series3;
	
	public VoltageLineChart() {
		super(new CategoryAxis(), new NumberAxis());
		
		this.setPrefHeight(300);
        this.setLegendVisible(false);
        this.setAnimated(false);
        this.getStyleClass().add("thick-chart");
        this.setCreateSymbols(false);

		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Load Number");
		
		getYAxis().setLabel(getChartTitle());
		
		series = new XYChart.Series<>();
		getData().add(series);
		
		series2 = new XYChart.Series<>();
		getData().add(series2);
		
		series3 = new XYChart.Series<>();
		getData().add(series3);
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
		series2.getData().clear();
		series3.getData().clear();
		environment.getLoads().forEach(load -> {
			Data<String, Number> chartData = new XYChart.Data<>(load.getNodeNumber().toString(), load.getCurrentVoltagePU());
			series.getData().add(chartData);
			
			chartData = new XYChart.Data<>(load.getNodeNumber().toString(), (load.getCurrentVoltagePU() / 2));
			series2.getData().add(chartData);
			
			chartData = new XYChart.Data<>(load.getNodeNumber().toString(), (load.getCurrentVoltagePU() * 2));
			series3.getData().add(chartData);
		});
	}
}
