package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class LoadStackPane extends StackPane {
	
	private final Integer loadNum;
	
	public LoadStackPane(Integer loadNum) {
		this.loadNum = loadNum;
	}

	public Integer getLoadNum() {
		return loadNum;
	}
	
	public Circle getLoadCircle() {
		for (Node node : getChildren()) {
			if (node instanceof Circle) {
				return (Circle) node;
			}
		}
		throw new IllegalStateException("No way! A LoadStackPane always have a Circle!");
	}
}
