package br.com.guisi.simulador.rede.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import br.com.guisi.simulador.rede.Constants;

public class NetworkPane extends Pane {

	public void initialize() {
		this.setPrefWidth(Constants.NETWORK_SIZE_X * Constants.NETWORK_GRID_SIZE_PX + 4);
		this.setPrefHeight(Constants.NETWORK_SIZE_Y * Constants.NETWORK_GRID_SIZE_PX + 4);
		this.setMaxWidth(Constants.NETWORK_SIZE_X * Constants.NETWORK_GRID_SIZE_PX + 4);
		this.setMaxHeight(Constants.NETWORK_SIZE_Y * Constants.NETWORK_GRID_SIZE_PX + 4);
	}
	
	public void createRandomNetwork() {
		this.getChildren().clear();

		for (int x = 0; x < Constants.NETWORK_SIZE_X; x++) {
			for (int y = 0; y < Constants.NETWORK_SIZE_Y; y++) {
				if (x < Constants.NETWORK_SIZE_X - 1) {
					Line l = new Line();
					l.setStartX(x * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setStartY(y * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setEndX((x+1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setEndY(y * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setStroke(Color.BLACK);
					l.setStrokeType(StrokeType.CENTERED);
					l.setStrokeWidth(1);
					getChildren().add(l);
				}
				
				if (y < Constants.NETWORK_SIZE_Y - 1) {
					Line l = new Line();
					l.setStartX(x * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setStartY(y * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setEndX(x * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setEndY((y+1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
					l.setStroke(Color.BLACK);
					l.setStrokeType(StrokeType.CENTERED);
					l.setStrokeWidth(1);
					getChildren().add(l);
				}
				
				Circle c = new Circle();
				c.setCenterX(x * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
				c.setCenterY(y * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
				c.setFill(Color.WHITE);
				c.setStroke(Color.BLACK);
				c.setRadius(Constants.LOAD_RADIUS_PX);
				getChildren().add(c);
			}
		}
	}
}
