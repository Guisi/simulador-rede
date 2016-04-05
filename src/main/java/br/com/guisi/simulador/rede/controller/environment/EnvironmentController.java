package br.com.guisi.simulador.rede.controller.environment;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;

@Named
@Scope("prototype")
public class EnvironmentController extends AbstractEnvironmentPaneController {

	private VBox root;
	private SplitPane splitPane;
	private ScrollPane scrollPaneRight;
	private ScrollPane scrollPaneLeft;
	private VBox networkBoxLeft;
	private VBox networkBoxRight;
	
	private NetworkPaneController networkPaneController;
	
	public EnvironmentController(EnvironmentKeyType environmentKeyType) {
		super(environmentKeyType);
	}
	
	@PostConstruct
	public void initializeController() {
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
		networkPaneController = getController(NetworkPaneController.class, getEnvironmentKeyType());
		networkBoxRight.getChildren().add(networkPaneController.getView());
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	protected void onSetStage(Stage stage) {
		networkPaneController.setStage(stage);
	}
	
	@Override
	public Node getView() {
		return root;
	}
}