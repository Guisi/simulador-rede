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
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;

public class NetworkPane extends Pane {

	private Map<Integer, NetworkNodeStackPane> networkNodePaneMap = new HashMap<Integer, NetworkNodeStackPane>();
	private Map<Integer, BranchStackPane> branchPaneMap = new HashMap<Integer, BranchStackPane>();

	public NetworkNodeStackPane drawLoad(NetworkNode networkNode, Environment environment) {
		Text text = new Text(DecimalFormat.getNumberInstance().format(networkNode.getNodeNumber()));
		text.setBoundsType(TextBoundsType.VISUAL);
		NetworkNodeStackPane stack = new NetworkNodeStackPane(networkNode.getNodeNumber());
		
		if (networkNode.isLoad()) {
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

		int centerX = (networkNode.getX() - 1) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		int centerY = (environment.getSizeY() - networkNode.getY()) * Constants.NETWORK_GRID_SIZE_PX + Constants.NETWORK_PANE_PADDING;
		stack.setLayoutX(centerX);
		stack.setLayoutY(centerY);
		getChildren().add(stack);

		networkNodePaneMap.put(networkNode.getNodeNumber(), stack);
		this.setNetworkNodeColor(networkNode);

		return stack;
	}

	public void setNetworkNodeColor(NetworkNode networkNode) {
		NetworkNodeStackPane networkNodePane = networkNodePaneMap.get(networkNode.getNodeNumber());

		Color networkNodeColor;
		if (networkNode.isLoad()) {
			Load load = (Load) networkNode;
			networkNodeColor = load.isOn() ? Color.web(load.getColor()) : Color.BLACK;
			
			Text txt = networkNodePane.getNetworkNodeText();
			if (load.isSupplied()) {
				txt.setFont(Font.font("Verdana", FontWeight.NORMAL, 11));
				txt.setFill(Color.BLACK);
			} else {
				txt.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
				txt.setFill(Color.RED);
			}
		} else {
			Feeder feeder = (Feeder) networkNode;
			networkNodeColor = Color.web(feeder.getFeederColor());
		}
		networkNodePane.getNetworkNodeShape().setFill(networkNodeColor);
	}

	public void drawBranch(Branch branch, int sizeX, int sizeY, EventHandler<MouseEvent> mouseClicked) {
		BranchStackPane sp = new BranchStackPane(branch.getNumber());
		branchPaneMap.put(branch.getNumber(), sp);
		getChildren().add(sp);
		sp.toBack();

		int x1 = branch.getNode2().getX() - 1;
		int y1 = sizeY - branch.getNode2().getY();
		int x2 = branch.getNode1().getX() - 1;
		int y2 = sizeY - branch.getNode1().getY();

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
		String power = " (" + df.format(branch.getMaxCurrent()) + ")";
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

	public Map<Integer, NetworkNodeStackPane> getLoadPaneMap() {
		return networkNodePaneMap;
	}

	public Map<Integer, BranchStackPane> getBranchPaneMap() {
		return branchPaneMap;
	}
}
