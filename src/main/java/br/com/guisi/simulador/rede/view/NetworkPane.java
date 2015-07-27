package br.com.guisi.simulador.rede.view;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.layout.Pane;
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
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.view.layout.BranchText;
import br.com.guisi.simulador.rede.view.layout.LoadStackPane;

public class NetworkPane extends Pane {
	
	private Map<Integer, LoadStackPane> loadPaneMap = new HashMap<Integer, LoadStackPane>();

	public LoadStackPane drawNode(Load load) {
		Circle c = new Circle();
		c.setStroke(Color.BLACK);
		c.setRadius(Constants.LOAD_RADIUS_PX);

		Text text = new Text(DecimalFormat.getNumberInstance().format(load.getLoadPower()));
		text.setBoundsType(TextBoundsType.VISUAL); 
		LoadStackPane stack = new LoadStackPane(load.getLoadNum());
		stack.getChildren().addAll(c, text);
		
		int centerX = (load.getX()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		int centerY = (load.getY()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		stack.setLayoutX(centerX);
		stack.setLayoutY(centerY);
		getChildren().add(stack);
		
		loadPaneMap.put(load.getLoadNum(), stack);
		this.setLoadColor(load);
		
		return stack;
	}
	
	public void setLoadColor(Load load) {
		LoadStackPane loadPane = loadPaneMap.get(load.getLoadNum());
		
		Color c;
		if (load.isFeeder()) {
			c = Color.YELLOW;
		} else {
			if (load.getFeeder() == null) {
				c = Color.RED;
			} else {
				c = Color.WHITE;
			}
		}
		loadPane.getLoadCircle().setFill(c);
	}
	
	public BranchText drawBranch(Branch branch) {
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
		
		String power = "(" + DecimalFormat.getNumberInstance().format(branch.getBranchPower()) + ")";
		BranchText text = new BranchText(branch.getBranchNum(), power);
		text.setFont(Font.font(11));
		text.setBoundsType(TextBoundsType.VISUAL);
		
		double x = (l.getEndX() - l.getStartX()) / 2 + l.getStartX() + (l.getEndX() != l.getStartX() ? -10 : -22);
		double y = (l.getEndY() - l.getStartY()) / 2 + l.getStartY() + (l.getEndY() != l.getStartY() ? 5 : -5);
		
		//se esta na diagonal
		if (l.getEndX() != l.getStartX() && l.getEndY() != l.getStartY()) {
			x += l.getEndX() > l.getStartX() ? 12 : -12;
			y -= 5;
		}

		text.setLayoutX(x);
		text.setLayoutY(y);
		getChildren().add(text);
		
		return text;
	}
}
