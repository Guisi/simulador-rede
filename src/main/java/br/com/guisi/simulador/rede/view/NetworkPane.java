package br.com.guisi.simulador.rede.view;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.view.layout.BranchRectangle;
import br.com.guisi.simulador.rede.view.layout.BranchText;
import br.com.guisi.simulador.rede.view.layout.LoadStackPane;

public class NetworkPane extends Pane {
	
	private Map<Integer, LoadStackPane> loadPaneMap = new HashMap<Integer, LoadStackPane>();

	public LoadStackPane drawNode(Load load, int sizeY) {
		Circle c = new Circle();
		c.setStroke(Color.BLACK);
		c.setRadius(Constants.LOAD_RADIUS_PX);

		Text text = new Text(DecimalFormat.getNumberInstance().format(load.getLoadPower()));
		text.setBoundsType(TextBoundsType.VISUAL); 
		LoadStackPane stack = new LoadStackPane(load.getLoadNum());
		stack.getChildren().addAll(c, text);
		
		int centerX = (load.getX()-1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		int centerY = (sizeY - load.getY()) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
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
	
	public void drawBranch(Branch branch, int sizeX, int sizeY, EventHandler<MouseEvent> mouseClicked) {
		StackPane sp = new StackPane();
		getChildren().add(sp);
		sp.toBack();

		int x1 = branch.getLoad2().getX() - 1;
		int y1 = sizeY - branch.getLoad2().getY();
		int x2 = branch.getLoad1().getX() - 1;
		int y2 = sizeY - branch.getLoad1().getY();
		
		int startX = x1 * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX;
		int startY = y1 * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX;
		int endX = x2 * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX;
		int endY = y2 * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX;
		
		if (y1 == y2) {
			startY -= Constants.BRANCH_TYPE_PX * 1.5;
			endY -= Constants.BRANCH_TYPE_PX * 1.5;
		} else if (x1 == x2) {
			startX -= Constants.BRANCH_TYPE_PX * 2.8;
			endX -= Constants.BRANCH_TYPE_PX * 2.8;
		}
		
		/** Linha branch */
		Line l = new Line();
		l.setStartX(startX);
		l.setStartY(startY);
		l.setEndX(endX);
		l.setEndY(endY);
		l.setStroke(Color.BLACK);
		l.setStrokeType(StrokeType.CENTERED);
		l.getStrokeDashArray().clear();
		/*if (!branch.isOn()) {
			l.getStrokeDashArray().addAll(2d, 5d);
		}*/
		l.setStrokeWidth(1.3);
		sp.getChildren().add(l);
		l.toBack();
		sp.setLayoutX(Math.min(l.getEndX(), l.getStartX()));
		sp.setLayoutY(Math.min(l.getEndY(), l.getStartY()));

		/** Label branch */
		DecimalFormat df = new DecimalFormat("00.0");
		String power = " (" + df.format(branch.getBranchPower()) + ")";
		BranchText text = new BranchText(branch.getBranchNum(), power);
		text.setFont(Font.font(10));
		text.setBoundsType(TextBoundsType.VISUAL);
		text.setOnMouseClicked(mouseClicked);
		
		/** Tipo branch */
		BranchRectangle r = new BranchRectangle(branch.getBranchNum());
		r.setWidth(Constants.BRANCH_TYPE_PX);
		r.setHeight(Constants.BRANCH_TYPE_PX);
		r.setOnMouseClicked(mouseClicked);
		r.setFill(branch.isOn() ? Color.BLACK : Color.WHITE);
		if (!branch.isOn()) {
			r.setStroke(Color.LIGHTGRAY);
			r.setStrokeWidth(1);
		}
		/*r.setVisible(branch.isSwitchBranch() && branch.isOn());*/
		r.setVisible(branch.isSwitchBranch());
		
		/** agrupa rectangle e text */
		if (x1 == x2) {
			HBox box = new HBox();
			box.setAlignment(Pos.CENTER);
			if (x1 < sizeX / 2) {
				box.getChildren().add(text);
				box.getChildren().add(r);
				box.setPadding(new Insets(0, Constants.BRANCH_TYPE_PX * 2.3, 0, 0));
			} else {
				box.getChildren().add(r);
				box.getChildren().add(text);
				box.setPadding(new Insets(0, 0, 0, Constants.BRANCH_TYPE_PX * 2.3));
			}
			sp.getChildren().add(box);
			box.toFront();
		} else {
			VBox box = new VBox();
			box.setPadding(new Insets(0, 0, Constants.BRANCH_TYPE_PX, 0));
			box.setAlignment(Pos.CENTER);
			box.getChildren().add(text);
			box.getChildren().add(r);
			sp.getChildren().add(box);
			box.toFront();
		}
	}
	
	/**
	 * Desenha um grid para facilitar a visualização do plano cartesiano
	 */
	public void drawGrid(int sizeX, int sizeY) {
		
		for (int x = 0; x < sizeX; x++) {
			Line l = new Line();
			int posX = x * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX;
			l.setStartX(posX);
			l.setStartY(0);
			l.setEndX(posX);
			l.setEndY(sizeY * Constants.NETWORK_GRID_SIZE_PX - 10 + Constants.NETWORK_PANE_PADDING);
			l.setStroke(Color.LIGHTGRAY);
			l.setStrokeType(StrokeType.CENTERED);
			l.setStrokeWidth(0.8);
			getChildren().add(l);
			l.toBack();
			
			Text text = new Text(String.valueOf(x + 1));
			text.setFont(Font.font(10));
			text.setFill(Color.GRAY);
			text.setBoundsType(TextBoundsType.VISUAL);
			text.setLayoutX(posX + 3);
			text.setLayoutY(sizeY * Constants.NETWORK_GRID_SIZE_PX - 15 + Constants.NETWORK_PANE_PADDING);
			getChildren().add(text);
			text.toBack();
		}
		
		for (int y = 0; y < sizeY; y++) {
			Line l = new Line();
			l.setStartX(0);
			int posY = y * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING + Constants.LOAD_RADIUS_PX;
			l.setStartY(posY);
			l.setEndX(sizeX * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING);
			l.setEndY(posY);
			l.setStroke(Color.LIGHTGRAY);
			l.setStrokeType(StrokeType.CENTERED);
			l.setStrokeWidth(0.8);
			getChildren().add(l);
			l.toBack();
			
			Text text = new Text(String.valueOf(sizeY - y));
			text.setFont(Font.font(10));
			text.setFill(Color.GRAY);
			text.setBoundsType(TextBoundsType.VISUAL);
			text.setLayoutX(3);
			text.setLayoutY(posY - 3);
			getChildren().add(text);
			text.toBack();
		}
	}
}
