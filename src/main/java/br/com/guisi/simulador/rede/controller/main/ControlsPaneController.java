package br.com.guisi.simulador.rede.controller.main;

import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;

public class ControlsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/ControlsPane.fxml";

	@Inject
	private AgentControl agentControl;
	
	@FXML
	private VBox root;
	
	@FXML
	private VBox controlsVBox;
	@FXML
	private Button btnRunAgent;
	@FXML
	private Button btnStopAgent;
	@FXML
	private ComboBox<TaskExecutionType> cbTaskExecutionType;

	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN);
		this.listenToEvent(EventType.ENVIRONMENT_LOADED);
		this.listenToEvent(EventType.AGENT_RUNNING);
		this.listenToEvent(EventType.AGENT_STOPPED);
		this.listenToEvent(EventType.AGENT_NOTIFICATION);

		Image imageCheck = new Image(getClass().getResourceAsStream("/img/check.png"));
		btnRunAgent.setGraphic(new ImageView(imageCheck));
		
		Image imageDelete = new Image(getClass().getResourceAsStream("/img/delete.png"));
		btnStopAgent.setGraphic(new ImageView(imageDelete));
		
		cbTaskExecutionType.setItems(FXCollections.observableArrayList(Arrays.asList(TaskExecutionType.values())));
		cbTaskExecutionType.setValue(TaskExecutionType.STEP_BY_STEP);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.onEnvironmentLoaded(); break;
			case AGENT_RUNNING: this.enableDisableScreen(true); break;
			case AGENT_STOPPED: this.enableDisableScreen(false); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
	}
	
	private void onEnvironmentLoaded() {
		root.setVisible(true);
	}
	
	private void enableDisableScreen(boolean disable) {
		btnRunAgent.setDisable(disable);
		btnStopAgent.setDisable(!disable);
		cbTaskExecutionType.setDisable(disable);
	}
	
	/*********************************
	 *********************************
	 * Controle da interação do agente
	 *********************************
	 *********************************/
	public void runAgent() {
		agentControl.run(cbTaskExecutionType.getValue());
	}
	
	public void stopAgent() {
		agentControl.stop();
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
