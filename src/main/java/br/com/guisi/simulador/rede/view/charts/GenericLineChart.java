package br.com.guisi.simulador.rede.view.charts;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class GenericLineChart extends LineChart<Number, Number> {
	
	public GenericLineChart() {
		super(new NumberAxis(), new NumberAxis());
        
        NumberAxis yAxis = (NumberAxis) this.getYAxis();
        yAxis.setUpperBound(0.1);
        
        this.setPrefHeight(300);
        this.setLegendVisible(true);
        this.setAnimated(false);
        this.setCreateSymbols(false);
        this.getStyleClass().add("thick-chart");
	}
	
}
