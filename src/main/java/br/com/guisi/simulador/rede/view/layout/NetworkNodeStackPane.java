package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public class NetworkNodeStackPane extends StackPane {
	
	private final Integer networkNodeNumber;
	
	public NetworkNodeStackPane(Integer networkNodeNumber) {
		this.networkNodeNumber = networkNodeNumber;
	}

	public Integer getNetworkNodeNumber() {
		return networkNodeNumber;
	}
	
	public Shape getNetworkNodeShape() {
		for (Node node : getChildren()) {
			if (node instanceof Circle || node instanceof Rectangle) {
				return (Shape) node;
			}
		}
		throw new IllegalStateException("No way! A NetworkNodeStackPane always have a Circle or a Rectangle!");
	}
	
	public Text getNetworkNodeText() {
		for (Node node : getChildren()) {
			if (node instanceof Text) {
				return (Text) node;
			}
		}
		throw new IllegalStateException("No way! A NetworkNodeStackPane always have a Text!");
	}
}
