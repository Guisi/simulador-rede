package br.com.guisi.simulador.rede.view;

import java.text.DecimalFormat;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.constants.BranchStatus;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Node;

public class NetworkPane extends Pane {

	public void initialize() {
		this.setVisible(false);
	}
	
	public void drawNetworkFromEnvironment(Environment environment) {
		this.setVisible(true);
		
		this.setPrefWidth(environment.getSize() * Constants.NETWORK_GRID_SIZE_PX);
		this.setPrefHeight(environment.getSize() * Constants.NETWORK_GRID_SIZE_PX - 10);
		this.setMaxWidth(environment.getSize() * Constants.NETWORK_GRID_SIZE_PX);
		this.setMaxHeight(environment.getSize() * Constants.NETWORK_GRID_SIZE_PX - 10);
		
		this.getChildren().clear();

		for (Node node : environment.getNodeMap().values()) {
			this.drawNode(node);
		}
		
		for (Branch branch : environment.getBranchMap().values()) {
			this.drawBranch(branch);
		}
	}
	
	public void drawNode(Node node) {
		Circle c = new Circle();
		int centerX = node.getX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		int centerY = node.getY() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		c.setCenterX(centerX);
		c.setCenterY(centerY);
		c.setFill(Color.WHITE);
		c.setStroke(Color.BLACK);
		c.setRadius(Constants.LOAD_RADIUS_PX);
		getChildren().add(c);

		Label l = new Label();
		l.setText(DecimalFormat.getNumberInstance().format(node.getLoadPower()));
		l.setLayoutX(centerX - (Constants.LOAD_RADIUS_PX/2));
		l.setLayoutY(centerY - (Constants.LOAD_RADIUS_PX/2)-2);
		l.toFront();
		getChildren().add(l);
	}
	
	public void drawBranch(Branch branch) {
		Line l = new Line();
		l.setStartX(branch.getNode1().getX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		l.setStartY(branch.getNode1().getY() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		l.setEndX(branch.getNode2().getX() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		l.setEndY(branch.getNode2().getY() * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
		l.setStroke(Color.BLACK);
		l.setStrokeType(StrokeType.CENTERED);
		l.getStrokeDashArray().clear();
		if (branch.getStatus().equals(BranchStatus.OFF)) {
			l.getStrokeDashArray().addAll(2d, 5d);
		}
		l.setStrokeWidth(1);
		getChildren().add(l);
		l.toBack();
	}
	
	/*public void createRandomNetwork() {
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
	}*/
}
