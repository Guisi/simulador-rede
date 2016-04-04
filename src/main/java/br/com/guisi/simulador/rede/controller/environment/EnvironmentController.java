package br.com.guisi.simulador.rede.controller.environment;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;

import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;

@Named
@Scope("prototype")
public class EnvironmentController extends Controller {

	private VBox root;
	private SplitPane splitPane;
	private ScrollPane scrollPaneRight;
	private ScrollPane scrollPaneLeft;
	private VBox networkBoxLeft;
	private VBox networkBoxRight;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN, EventType.ENVIRONMENT_LOADED);
		
		root = new VBox();
		splitPane = new SplitPane();
		splitPane.setDividerPositions(0.5);
		splitPane.getStyleClass().add("scrollPane");
		root.getChildren().add(splitPane);
		
		scrollPaneLeft = new ScrollPane();
		scrollPaneLeft.setHbarPolicy(ScrollBarPolicy.NEVER);
		scrollPaneLeft.setMaxWidth(755);
		scrollPaneLeft.setPrefWidth(755);
		scrollPaneLeft.getStyleClass().add("scrollPane");
		splitPane.getItems().add(scrollPaneLeft);
		
		scrollPaneRight = new ScrollPane();
		scrollPaneRight.getStyleClass().add("scrollPane");
		splitPane.getItems().add(scrollPaneRight);
		
		networkBoxLeft = new VBox();
		scrollPaneLeft.setContent(networkBoxLeft);
		
		networkBoxRight = new VBox();
		scrollPaneRight.setContent(networkBoxRight);
		
		scrollPaneRight.prefHeightProperty().bind(SimuladorRede.getPrimaryStage().heightProperty());
		scrollPaneLeft.prefHeightProperty().bind(SimuladorRede.getPrimaryStage().heightProperty());
		
		//NetworkPane
		NetworkPaneController networkPaneController = getController(NetworkPaneController.class);
		networkBoxRight.getChildren().add(networkPaneController.getView());
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case ENVIRONMENT_LOADED: processEnvironmentLoaded(); break;
			case RESET_SCREEN: processResetScreen(); break; 
			default: break;
		}
	}
	
	private void processEnvironmentLoaded() {
	}
	
	private void processResetScreen() {
	}
	
	@Override
	public Node getView() {
		return root;
	}
}