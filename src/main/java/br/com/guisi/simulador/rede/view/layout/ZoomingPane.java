package br.com.guisi.simulador.rede.view.layout;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

public class ZoomingPane extends Pane {
	
	private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);
	private double contentWidth;
	private double contentHeight;
	
	public ZoomingPane(NetworkPane content) {
		getChildren().add(content);
		Scale scale = new Scale(1, 1);
		content.getTransforms().add(scale);

		zoomFactor.addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				scale.setX(newValue.doubleValue());
				scale.setY(newValue.doubleValue());
				setPrefWidth(contentWidth * newValue.doubleValue());
				setPrefHeight(contentHeight * newValue.doubleValue());
				requestLayout();
			}
		});
	}

	public double getContentWidth() {
		return contentWidth;
	}

	public void setContentWidth(double contentWidth) {
		this.contentWidth = contentWidth;
	}

	public double getContentHeight() {
		return contentHeight;
	}

	public void setContentHeight(double contentHeight) {
		this.contentHeight = contentHeight;
	}

	public final Double getZoomFactor() {
		return zoomFactor.get();
	}

	public final void setZoomFactor(Double zoomFactor) {
		this.zoomFactor.set(zoomFactor);
	}

	public final DoubleProperty zoomFactorProperty() {
		return zoomFactor;
	}
}