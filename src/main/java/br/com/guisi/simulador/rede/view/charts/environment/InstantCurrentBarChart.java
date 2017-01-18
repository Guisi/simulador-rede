package br.com.guisi.simulador.rede.view.charts.environment;

import br.com.guisi.simulador.rede.enviroment.Environment;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class InstantCurrentBarChart extends BarChart<String, Number> {

	private XYChart.Series<String, Number> series;
	
	public InstantCurrentBarChart() {
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
		return "Instant Current in A";
	}
	
	public void updateChart(Environment environment) {
		series.getData().clear();
		environment.getBranches().forEach(branch -> {
			Data<String, Number> chartData = new XYChart.Data<>(branch.getNumber().toString(), branch.getInstantCurrent());
			series.getData().add(chartData);
		});
	}
}
