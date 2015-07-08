package br.com.guisi.simulador.rede.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;

public class SimuladorRedeViewController {

	private static final int NETWORK_PANE_PADDING = 15;
	private static final int NETWORK_SIZE_X = 10;
	private static final int NETWORK_SIZE_Y = 10;
	private static final int NETWORK_GRID_SIZE_PX = 70;
	private static final int LOAD_RADIUS_PX = 10;
	
	@FXML
	private Pane networkPane;
	@FXML
	private Button btnCreateNetwork;
	
	public void initialize() {
		networkPane.setPrefWidth(NETWORK_SIZE_X * NETWORK_GRID_SIZE_PX + 4);
		networkPane.setPrefHeight(NETWORK_SIZE_Y * NETWORK_GRID_SIZE_PX + 4);
		networkPane.setMaxWidth(NETWORK_SIZE_X * NETWORK_GRID_SIZE_PX + 4);
		networkPane.setMaxHeight(NETWORK_SIZE_Y * NETWORK_GRID_SIZE_PX + 4);
	}

	public void createRandomNetwork() {
		
		for (int x = 0; x < NETWORK_SIZE_X; x++) {
			for (int y = 0; y < NETWORK_SIZE_Y; y++) {
				if (x < NETWORK_SIZE_X - 1) {
					Line l = new Line();
					l.setStartX(x * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setStartY(y * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setEndX((x+1) * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setEndY(y * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setStroke(Color.BLACK);
					l.setStrokeType(StrokeType.CENTERED);
					l.setStrokeWidth(1);
					networkPane.getChildren().add(l);
				}
				
				if (y < NETWORK_SIZE_Y - 1) {
					Line l = new Line();
					l.setStartX(x * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setStartY(y * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setEndX(x * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setEndY((y+1) * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
					l.setStroke(Color.BLACK);
					l.setStrokeType(StrokeType.CENTERED);
					l.setStrokeWidth(1);
					networkPane.getChildren().add(l);
				}
				
				Circle c = new Circle();
				c.setCenterX(x * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
				c.setCenterY(y * NETWORK_GRID_SIZE_PX + NETWORK_PANE_PADDING);
				c.setFill(Color.WHITE);
				c.setStroke(Color.BLACK);
				c.setRadius(LOAD_RADIUS_PX);
				networkPane.getChildren().add(c);
			}
		}
	}
}
