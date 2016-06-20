package br.com.guisi.simulador.rede.controller.environment;

import javafx.beans.property.DoubleProperty;
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
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

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
	private ElementsDetailsPaneController elementsDetailsPaneController;
	private FunctionsPaneController functionsPaneController;
	private LabelAndMessagesPaneController labelAndMessagesPaneController;
	
	public EnvironmentController(EnvironmentKeyType environmentKeyType) {
		super(environmentKeyType);
	}
	
	@PostConstruct
	public void initializeController() {
		root = new VBox();
		splitPane = new SplitPane();
		splitPane.getStyleClass().add("scrollPane");
		root.getChildren().add(splitPane);
		
		splitPane.getDividers();
		
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
		
		//Elements Details
		elementsDetailsPaneController = getController(ElementsDetailsPaneController.class, getEnvironmentKeyType());
		networkBoxLeft.getChildren().add(elementsDetailsPaneController.getView());
		
		//Functions
		functionsPaneController = getController(FunctionsPaneController.class, getEnvironmentKeyType());
		networkBoxLeft.getChildren().add(functionsPaneController.getView());
		
		//Labels and messages
		labelAndMessagesPaneController = getController(LabelAndMessagesPaneController.class, getEnvironmentKeyType());
		networkBoxLeft.getChildren().add(labelAndMessagesPaneController.getView());
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	protected void onSetStage(Stage stage) {
		networkPaneController.setStage(stage);
		elementsDetailsPaneController.setStage(stage);
		functionsPaneController.setStage(stage);
		labelAndMessagesPaneController.setStage(stage);
		splitPane.prefHeightProperty().bind(stage.getScene().heightProperty());
		stage.setHeight(800);
		
		DoubleProperty dividerPositionProperty = splitPane.getDividers().get(0).positionProperty();
		dividerPositionProperty.setValue(PropertiesUtils.getDoubleProperty(PropertyKey.SPLIT_PANE_DIVIDER, getEnvironmentKeyType().name()));
		dividerPositionProperty.addListener((observable, oldValue, newValue) -> { 
			PropertiesUtils.saveProperty(PropertyKey.SPLIT_PANE_DIVIDER, getEnvironmentKeyType().name(), String.valueOf(newValue));
		});
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
	@Override
	public String getControllerKey() {
		return super.getControllerKey() + "_" + getEnvironmentKeyType().name();
	}
}