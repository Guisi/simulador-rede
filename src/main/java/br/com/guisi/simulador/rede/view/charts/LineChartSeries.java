package br.com.guisi.simulador.rede.view.charts;

import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class LineChartSeries {

	private final String seriesTitle;
	private final Class<?> type;
	private XYChart.Series<Number, Number> series;
	private double minValue;
	private double maxValue;
	private int stepMaxValue;
	private double currentValue;

	public LineChartSeries(String seriesTitle, Class<?> type) {
		this.seriesTitle = seriesTitle;
		this.type = type;
		series = new XYChart.Series<>();
	}
	
	public void updateValues(int step, double value) {
		Data<Number, Number> chartData = new XYChart.Data<>(step, value);
		series.getData().add(chartData);
		currentValue = value;
		
		minValue = Math.min(minValue, value);
		
		stepMaxValue = value > maxValue ? step : stepMaxValue;
		maxValue = Math.max(maxValue, value);
	}

	public String getSeriesTitle() {
		return seriesTitle;
	}

	public XYChart.Series<Number, Number> getSeries() {
		return series;
	}

	public void setSeries(XYChart.Series<Number, Number> series) {
		this.series = series;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public int getStepMaxValue() {
		return stepMaxValue;
	}

	public void setStepMaxValue(int stepMaxValue) {
		this.stepMaxValue = stepMaxValue;
	}

	public double getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(double currentValue) {
		this.currentValue = currentValue;
	}

	public int getScale() {
		return type == Integer.class ? 0 : 5;
	}
}
