package br.com.guisi.simulador.rede.view.layout;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Load;

public class NetworkPane extends Pane {

	private Map<Integer, LoadStackPane> loadPaneMap = new HashMap<Integer, LoadStackPane>();
	private Map<Integer, BranchStackPane> branchPaneMap = new HashMap<Integer, BranchStackPane>();

	public LoadStackPane drawLoad(Load load, Environment environment) {
		Text text = new Text(DecimalFormat.getNumberInstance().format(load.getLoadPower()));
		text.setBoundsType(TextBoundsType.VISUAL);
		LoadStackPane stack = new LoadStackPane(load.getLoadNum());
		
		if (load.isLoad()) {
			Circle c = new Circle();
			c.setStroke(Color.BLACK);
			c.setRadius(Constants.LOAD_RADIUS_PX);
			stack.getChildren().addAll(c, text);
		} else {
			Rectangle r = new Rectangle();
			r.setStroke(Color.BLACK);
			r.setWidth(Constants.LOAD_RADIUS_PX * 2);
			r.setHeight(Constants.LOAD_RADIUS_PX * 2);
			r.setArcHeight(5);
			r.setArcWidth(5);
			stack.getChildren().addAll(r, text);
		}

		int centerX = (load.getX() - 1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		int centerY = (environment.getSizeY() - load.getY()) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		stack.setLayoutX(centerX);
		stack.setLayoutY(centerY);
		getChildren().add(stack);

		loadPaneMap.put(load.getLoadNum(), stack);
		this.setLoadColor(load, environment);

		return stack;
	}

	public void setLoadColor(Load load, Environment environment) {
		LoadStackPane loadPane = loadPaneMap.get(load.getLoadNum());

		Color loadColor;
		if (load.isLoad()) {
			if (load.isOn()) {
				Load feeder = load.getFeeder();
				loadColor = (feeder != null) ? Color.web(feeder.getLoadColor()) : Color.WHITE;
			} else {
				loadColor = Color.BLACK;
			}
			
			Text txt = loadPane.getLoadText();
			if (load.getFeeder() == null) {
				txt.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
				txt.setFill(Color.RED);
			} else {
				txt.setFont(Font.font("Verdana", FontWeight.NORMAL, 11));
				txt.setFill(Color.BLACK);
			}
		} else {
			loadColor = Color.web(load.getFeederColor());
		}
		loadPane.getLoadShape().setFill(loadColor);
	}

	public void drawBranch(Branch branch, int sizeX, int sizeY, EventHandler<MouseEvent> mouseClicked) {
		BranchStackPane sp = new BranchStackPane(branch.getBranchNum());
		branchPaneMap.put(branch.getBranchNum(), sp);
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
			startX -= Constants.BRANCH_TYPE_PX * 1.5;
			endX -= Constants.BRANCH_TYPE_PX * 1.5;
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
		if (!branch.isOn()) {
			l.getStrokeDashArray().addAll(2d, 5d);
		}
		l.setStrokeWidth(1.3);
		l.setOnMouseClicked(mouseClicked);
		sp.getChildren().add(l);
		l.toBack();
		sp.setLayoutX(Math.min(l.getEndX(), l.getStartX()));
		sp.setLayoutY(Math.min(l.getEndY(), l.getStartY()));

		/** Label branch */
		DecimalFormat df = new DecimalFormat(Constants.POWER_DECIMAL_FORMAT);
		String power = " (" + df.format(branch.getBranchPower()) + ")";
		Text text = new Text(power);
		text.setFont(Font.font(10));
		text.setBoundsType(TextBoundsType.VISUAL);
		text.setOnMouseClicked(mouseClicked);

		/** Tipo branch */
		Rectangle r = new Rectangle();
		r.setWidth(Constants.BRANCH_TYPE_PX * 2);
		r.setHeight(Constants.BRANCH_TYPE_PX);
		r.setOnMouseClicked(mouseClicked);
		r.setFill(branch.isOn() ? Color.BLACK : Color.WHITE);
		if (!branch.isOn()) {
			r.setStroke(Color.GRAY);
			r.setStrokeWidth(1);
		}
		/* r.setVisible(branch.isSwitchBranch() && branch.isOn()); */
		r.setVisible(branch.isSwitchBranch());

		/** agrupa rectangle e text */
		VBox box = new VBox();
		box.setPadding(new Insets(0, 0, Constants.BRANCH_TYPE_PX, 0));
		box.setAlignment(Pos.CENTER);
		box.setSpacing(2);
		box.getChildren().add(text);
		box.getChildren().add(r);
		sp.getChildren().add(box);

		if (y1 != y2) {
			double xDiff = x2 - x1;
			double yDiff = y2 - y1;
			double angle = Math.atan2(yDiff, xDiff) * (180 / Math.PI);
			
			// se inclinou mais que 90 graus, ou é linha na vertical do lado
			// esquerdo, inverte
			if (angle > 90) {
				angle -= 180;
			} else if (angle < -90) {
				angle += 180;
			}
			box.setRotate(angle);
		}
		
		box.toFront();
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

	public Map<Integer, LoadStackPane> getLoadPaneMap() {
		return loadPaneMap;
	}

	public Map<Integer, BranchStackPane> getBranchPaneMap() {
		return branchPaneMap;
	}
}
