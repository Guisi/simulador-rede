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
        this.setLegendVisible(true);
        this.setAnimated(false);
        this.getStyleClass().add("bar-chart");

		getYNumberAxis().setAutoRanging(true);
		getXAxis().setLabel("Load Number");
		setBarGap(0);
		
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
		series.setName("Current");
		environment.getBranches().forEach(branch -> {
			Data<String, Number> chartData = new XYChart.Data<>(branch.getNumber().toString(), branch.getInstantCurrent());
			series.getData().add(chartData);
		});
	}
	
	public void saveCurrentSeries(String title) {
		XYChart.Series<String, Number> seriesSave = new XYChart.Series<>();
		seriesSave.setName(title);
		series.getData().forEach(data -> seriesSave.getData().add( new XYChart.Data<>(data.getXValue(), data.getYValue()) ));
		getData().add(getData().size()-1, seriesSave);
		
		series.setName("");
	}
}
