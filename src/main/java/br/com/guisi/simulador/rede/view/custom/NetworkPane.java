package br.com.guisi.simulador.rede.view.custom;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;
import br.com.guisi.simulador.rede.events.EnvironmentEventData;
import br.com.guisi.simulador.rede.events.EventType;

public class NetworkPane extends Pane {

	private EnvironmentKeyType environmentKeyType;
	
	private Map<Integer, NetworkNodeStackPane> networkNodePaneMap = new HashMap<Integer, NetworkNodeStackPane>();
	private Map<Integer, BranchStackPane> branchPaneMap = new HashMap<Integer, BranchStackPane>();
	
	private Integer selectedLoad;
	private Integer selectedFeeder;
	private Integer selectedBranch;
	private Integer agentPosition;
	
	public NetworkPane(EnvironmentKeyType environmentKeyType) {
		this.environmentKeyType = environmentKeyType;
	}
	
	private Environment getEnvironment() {
		return SimuladorRede.getEnvironment(environmentKeyType);
	}
	
	/**
	 * Guarda o load selecionado e atualiza estilo
	 * @param selected
	 */
	public void selectLoad(Integer selected) {
		Integer lastSelectedLoad = selectedLoad;
		selectedLoad = selected;
		if (lastSelectedLoad != null) {
			this.updateLoadDrawing(getEnvironment().getLoad(lastSelectedLoad));
		}
		this.updateLoadDrawing(getEnvironment().getLoad(selectedLoad));
	}
	
	/**
	 * Atualiza o estilo do load passado
	 * @param load
	 */
	public void updateLoadDrawing(Load load) {
		boolean selected = selectedLoad != null && selectedLoad.equals(load.getNodeNumber());
		
		NetworkNodeStackPane networkNodePane = networkNodePaneMap.get(load.getNodeNumber());

		Shape shape = networkNodePane.getNetworkNodeShape();
		shape.setStroke(selected ? Color.DARKORANGE : Color.BLACK);
		shape.setStrokeWidth(selected ? 2 : 1);
		
		Color networkNodeColor = load.isOn() ? Color.web(load.getColor()) : load.isIsolated() ? Color.WHITE : Color.BLACK;
		Text txt = networkNodePane.getNetworkNodeText();
		if (load.isSupplied()) {
			txt.setFont(Font.font("Verdana", FontWeight.NORMAL, 11));
			txt.setFill(Color.BLACK);
		} else {
			txt.setFont(Font.font("Verdana", FontWeight.BOLD, 11));
			txt.setFill(Color.RED);
		}
		shape.setFill(networkNodeColor);
	}
	
	/**
	 * Guarda o feeder selecionado e atualiza estilo
	 * @param selected
	 */
	public void selectFeeder(Integer selected) {
		Integer lastSelectedFeeder = selectedFeeder;
		selectedFeeder = selected;
		if (lastSelectedFeeder != null) {
			this.updateFeederDrawing(getEnvironment().getFeeder(lastSelectedFeeder));
		}
		this.updateFeederDrawing(getEnvironment().getFeeder(selectedFeeder));
	}
	
	/**
	 * Atualiza o estilo do feeder passado
	 * @param feeder
	 */
	public void updateFeederDrawing(Feeder feeder) {
		boolean selected = selectedFeeder != null && selectedFeeder.equals(feeder.getNodeNumber());
		
		NetworkNodeStackPane networkNodePane = networkNodePaneMap.get(feeder.getNodeNumber());

		Shape shape = networkNodePane.getNetworkNodeShape();
		shape.setStroke(selected ? Color.DARKORANGE : Color.BLACK);
		shape.setStrokeWidth(selected ? 2 : 1);
		
		Color networkNodeColor = feeder.isOn() ? Color.web(feeder.getFeederColor()) : Color.WHITE;
		Text txt = networkNodePane.getNetworkNodeText();
		txt.setFill(feeder.isPowerOverflow() ? Color.RED : Color.BLACK);
		txt.setFont(Font.font("Verdana", feeder.isPowerOverflow() ? FontWeight.BOLD : FontWeight.NORMAL, 11));
		shape.setFill(networkNodeColor);
	}
	
	/**
	 * Guarda o branch selecionado e atualiza estilo
	 * @param selected
	 */
	public void selectBranch(Integer selected) {
		Integer lastSelectedBranch = selectedBranch;
		selectedBranch = selected;
		if (lastSelectedBranch != null) {
			this.updateBranchDrawing(getEnvironment().getBranch(lastSelectedBranch));
		}
		this.updateBranchDrawing(getEnvironment().getBranch(selectedBranch));
	}
	
	/**
	 * Atualiza o estilo do branch passado
	 * @param branch
	 */
	public void updateBranchDrawing(Branch branch) {
		boolean selected = selectedBranch != null && selectedBranch.equals(branch.getNumber());
		
		BranchStackPane branchPane = branchPaneMap.get(branch.getNumber());

		Text txt = branchPane.getBranchText();
		if (txt != null) {
			txt.setFill(branch.isMaxCurrentOverflow() ? Color.RED : Color.BLACK);
		}
		
		Line line = branchPane.getBranchLine();
		line.setStroke(selected ? Color.DARKORANGE : Color.BLACK);
		line.setStrokeWidth(selected ? 4 : 1.3);
		if (branch.isClosed()) {
			line.getStrokeDashArray().clear();
		} else {
			line.getStrokeDashArray().addAll(2d, 5d);
		}
		
		Rectangle rect = branchPane.getSwitchRectangle();
		rect.setVisible(branch.isSwitchBranch() || branch.hasFault());
		VBox box = (VBox) rect.getParent();
		if (!branch.hasFault()) {
			rect.setFill(branch.isClosed() ? Color.BLACK : Color.WHITE);
			rect.setStrokeWidth(1);
			if (branch.isClosed()) {
				rect.setStroke(Color.BLACK);
			} else {
				rect.setStroke(Color.GRAY);
			}
			rect.setWidth(Constants.BRANCH_TYPE_PX * 2);
			rect.setHeight(Constants.BRANCH_TYPE_PX);
			box.setPadding(new Insets(0, 0, Constants.BRANCH_TYPE_PX, 0));
			if (!box.getChildren().contains(txt)) {
				box.getChildren().add(0, txt);
			}
		} else {
			rect.setWidth(Constants.BRANCH_TYPE_PX * 4);
			rect.setHeight(Constants.BRANCH_TYPE_PX * 3);
			Image img = new Image(getClass().getResourceAsStream("/img/fault-bolt.png"));
			ImagePattern imagePattern = new ImagePattern(img);
			rect.setFill(imagePattern);
			rect.setStrokeWidth(0);
			box.setPadding(new Insets(0, 0, 0, 0));
			box.getChildren().remove(txt);
		}
		
		branchPane.getItemCreateFault().setVisible(!branch.hasFault());
		branchPane.getItemRemoveFault().setVisible(branch.hasFault());
	}
	
	public void changeAgentCirclePosition(Integer newBranchNumber) {
		Circle agentCircle;

		if (agentPosition == null) {
			agentCircle = new Circle();
			agentCircle.setRadius(Constants.AGENT_RADIUS_PX);
			agentCircle.setFill(Color.LIGHTGREEN);
			/*agentCircle.setStroke(Color.GREEN);
			agentCircle.setStrokeWidth(2);*/
		} else {
			BranchStackPane branchPane = branchPaneMap.get(agentPosition);
			agentCircle = branchPane.removeAgentCircle();
		}
		
		BranchStackPane branchPane = branchPaneMap.get(newBranchNumber);
		branchPane.addAgentCircle(agentCircle);
		this.agentPosition = newBranchNumber;
	}
	
	public NetworkNodeStackPane drawNetworkNode(NetworkNode networkNode, Environment environment) {
		Text text = new Text(DecimalFormat.getNumberInstance().format(networkNode.getNodeNumber()));
		text.setBoundsType(TextBoundsType.VISUAL);
		NetworkNodeStackPane stack = new NetworkNodeStackPane(networkNode.getNodeNumber());
		
		if (networkNode.isLoad()) {
			Circle c = new Circle();
			c.setRadius(Constants.LOAD_RADIUS_PX);
			stack.getChildren().addAll(c, text);
		} else {
			Rectangle r = new Rectangle();
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
		if (networkNode.isLoad()) {
			this.updateLoadDrawing((Load) networkNode);
		} else {
			this.updateFeederDrawing((Feeder) networkNode);
		}

		return stack;
	}
		
	public void drawBranch(Branch branch, int sizeX, int sizeY, Controller controller, EnvironmentKeyType environmentKeyType) {
		BranchStackPane sp = new BranchStackPane(branch.getNumber());
		branchPaneMap.put(branch.getNumber(), sp);
		getChildren().add(sp);
		sp.toBack();
		
		final ContextMenu contextMenu = new ContextMenu();
		MenuItem itemCreateFault = new MenuItem("Create Fault");
		itemCreateFault.setVisible(!branch.hasFault());
		itemCreateFault.setOnAction(event -> {
			getEnvironment().addFault(branch.getNumber());
	        controller.fireEvent(EventType.FAULT_CREATED, branch);
		});
		contextMenu.getItems().addAll(itemCreateFault);
		sp.setItemCreateFault(itemCreateFault);

		MenuItem itemRemoveFault = new MenuItem("Remove Fault");
		itemRemoveFault.setVisible(branch.hasFault());
		itemRemoveFault.setOnAction(event -> {
			getEnvironment().removeFault(branch.getNumber());
	        controller.fireEvent(EventType.FAULT_CREATED, branch);
		});
		contextMenu.getItems().addAll(itemRemoveFault);
		sp.setItemRemoveFault(itemRemoveFault);
		
		EventHandler<MouseEvent> mouseClicked = (event) -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				Node node = (Node) event.getSource();
				while (!(node instanceof BranchStackPane)) {
					node = node.getParent();
				}
				controller.fireEvent(EventType.BRANCH_SELECTED, new EnvironmentEventData(environmentKeyType, ((BranchStackPane) node).getBranchNum()));
			} else if (event.getButton() == MouseButton.SECONDARY) {
				contextMenu.show(sp, event.getScreenX(), event.getScreenY());
			}
		};

		int x1 = branch.getNodeTo().getX() - 1;
		int y1 = sizeY - branch.getNodeTo().getY();
		int x2 = branch.getNodeFrom().getX() - 1;
		int y2 = sizeY - branch.getNodeFrom().getY();

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
		
		/** agrupa rectangle e text */
		VBox box = new VBox();
		box.setAlignment(Pos.CENTER);
		box.setSpacing(2);
		sp.getChildren().add(box);
		
		/** Linha branch */
		Line l = new Line();
		l.setStartX(startX);
		l.setStartY(startY);
		l.setEndX(endX);
		l.setEndY(endY);
		l.setStroke(Color.BLACK);
		l.setStrokeType(StrokeType.CENTERED);
		l.getStrokeDashArray().clear();
		l.setOnMouseClicked(mouseClicked);
		sp.getChildren().add(l);
		l.toBack();
		sp.setLayoutX(Math.min(l.getEndX(), l.getStartX()));
		sp.setLayoutY(Math.min(l.getEndY(), l.getStartY()));

		/** Label branch */
		DecimalFormat df = new DecimalFormat(Constants.DECIMAL_FORMAT_2);
		String power = " (" + df.format(branch.getMaxCurrent()) + ")";
		Text text = new Text(power);
		text.setFont(Font.font(10));
		text.setBoundsType(TextBoundsType.VISUAL);
		text.setOnMouseClicked(mouseClicked);
		sp.setBranchText(text);
		if (!branch.hasFault()) {
			box.getChildren().add(text);
		}

		/** Tipo branch */
		Rectangle r = new Rectangle();
		r.setOnMouseClicked(mouseClicked);
		sp.setSwitchRectangle(r);
		box.getChildren().add(r);
		
		if (branch.hasFault()) {
			r.setWidth(Constants.BRANCH_TYPE_PX * 4);
			r.setHeight(Constants.BRANCH_TYPE_PX * 3);
			Image img = new Image(getClass().getResourceAsStream("/img/fault-bolt.png"));
			ImagePattern imagePattern = new ImagePattern(img);
			r.setFill(imagePattern);
			box.setPadding(new Insets(0, 0, 0, 0));
		} else {
			r.setWidth(Constants.BRANCH_TYPE_PX * 2);
			r.setHeight(Constants.BRANCH_TYPE_PX);
			box.setPadding(new Insets(0, 0, Constants.BRANCH_TYPE_PX, 0));
		}

		if (y1 != y2) {
			double xDiff = x2 - x1;
			double yDiff = y2 - y1;
			double angle = Math.atan2(yDiff, xDiff) * (180 / Math.PI);
			
			// se inclinou mais que 90 graus, ou � linha na vertical do lado
			// esquerdo, inverte
			if (angle > 90) {
				angle -= 180;
			} else if (angle < -90) {
				angle += 180;
			}
			if (branch.hasFault()) {
				sp.setLayoutX(sp.getLayoutX() - Constants.BRANCH_TYPE_PX / 2);
			}
			box.setRotate(angle);
		}
		
		box.toFront();
		
		this.updateBranchDrawing(branch);
	}
	
	/**
	 * Desenha um grid para facilitar a visualiza��o do plano cartesiano
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

	public void reset() {
		getChildren().clear();
		this.selectedBranch = null;
		this.selectedFeeder = null;
		this.selectedLoad = null;
		this.agentPosition = null;
	}

}
