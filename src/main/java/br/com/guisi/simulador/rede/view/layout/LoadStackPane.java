package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class LoadStackPane extends StackPane {
	
	private final Integer loadNum;
	
	public LoadStackPane(Integer loadNum) {
		this.loadNum = loadNum;
	}

	public Integer getLoadNum() {
		return loadNum;
	}
	
	public Shape getLoadShape() {
		for (Node node : getChildren()) {
			if (node instanceof Circle || node instanceof Rectangle) {
				return (Shape) node;
			}
		}
		throw new IllegalStateException("No way! A LoadStackPane always have a Circle!");
	}
}
