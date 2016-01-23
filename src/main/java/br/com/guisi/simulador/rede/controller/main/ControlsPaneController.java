package br.com.guisi.simulador.rede.controller.main;

import java.util.Arrays;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
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
	private Button btnResetAgent;
	@FXML
	private ComboBox<TaskExecutionType> cbTaskExecutionType;
	@FXML
	private Label lblSteps;

	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
				EventType.ENVIRONMENT_LOADED,
				EventType.AGENT_RUNNING,
				EventType.AGENT_STOPPED, 
				EventType.AGENT_NOTIFICATION);

		Image imageCheck = new Image(getClass().getResourceAsStream("/img/check.png"));
		btnRunAgent.setGraphic(new ImageView(imageCheck));
		
		Image imageDelete = new Image(getClass().getResourceAsStream("/img/delete.png"));
		btnStopAgent.setGraphic(new ImageView(imageDelete));
		
		Image imageReset = new Image(getClass().getResourceAsStream("/img/reset.png"));
		btnResetAgent.setGraphic(new ImageView(imageReset));
		
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
			case ENVIRONMENT_LOADED: this.processEnvironmentLoaded(); break;
			case AGENT_RUNNING: this.enableDisableScreen(true); break;
			case AGENT_STOPPED: this.enableDisableScreen(false); break;
			case AGENT_NOTIFICATION: this.processAgentNotification(data); break;
			default: break;
		}
	}
	
	private void resetScreen() {
		root.setVisible(false);
		lblSteps.setText("");
		agentControl.reset();
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		
		if (agentStatus != null) {
			lblSteps.setText(String.valueOf(agentStatus.getSteps()));
		}
	}
	
	private void enableDisableScreen(boolean disable) {
		btnRunAgent.setDisable(disable);
		btnStopAgent.setDisable(!disable);
		btnResetAgent.setDisable(disable);
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
	
	public void resetAgent() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setContentText("Confirma o reset do aprendizado?");
		Optional<ButtonType> result = alert.showAndWait();
		if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
			this.fireEvent(EventType.RELOAD_ENVIRONMENT);
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
