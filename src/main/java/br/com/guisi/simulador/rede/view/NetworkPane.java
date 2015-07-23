package br.com.guisi.simulador.rede.view;

import java.text.DecimalFormat;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
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
		c.setFill(node.isLoad() ? Color.WHITE : Color.YELLOW);
		c.setStroke(Color.BLACK);
		c.setRadius(Constants.LOAD_RADIUS_PX);

		Text text = new Text(DecimalFormat.getNumberInstance().format(node.getLoadPower()));
		text.setBoundsType(TextBoundsType.VISUAL); 
		StackPane stack = new StackPane();
		stack.getChildren().addAll(c, text);
		
		int centerX = (node.getX()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		int centerY = (node.getY()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		stack.setLayoutX(centerX);
		stack.setLayoutY(centerY);
		
		getChildren().add(stack);
	}
	
	public void drawBranch(Branch branch) {
		Line l = new Line();
		l.setStartX((branch.getNode1().getX()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX);
		l.setStartY((branch.getNode1().getY()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX);
		l.setEndX((branch.getNode2().getX()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX);
		l.setEndY((branch.getNode2().getY()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX);
		l.setStroke(Color.BLACK);
		l.setStrokeType(StrokeType.CENTERED);
		l.getStrokeDashArray().clear();
		if (branch.getStatus().equals(BranchStatus.OFF)) {
			l.getStrokeDashArray().addAll(2d, 5d);
		}
		l.setStrokeWidth(1);
		getChildren().add(l);
		l.toBack();
		
		Text text = new Text("(" + DecimalFormat.getNumberInstance().format(branch.getBranchPower()) + ")");
		text.setFont(Font.font(11));
		text.setBoundsType(TextBoundsType.VISUAL);
		
		double x = (l.getEndX() - l.getStartX()) / 2 + l.getStartX();
		double y = (l.getEndY() - l.getStartY()) / 2 + l.getStartY();
		x += (l.getEndX() != l.getStartX() ? -10 : -22) + (l.getEndX() != l.getStartX() && l.getEndY() != l.getStartY() ? 10 : 0);
		y += (l.getEndY() != l.getStartY() ? 5 : -5) - (l.getEndX() != l.getStartX() && l.getEndY() != l.getStartY() ? 10 : 0);
		text.setLayoutX(x);
		text.setLayoutY(y);
		getChildren().add(text);
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
