package br.com.guisi.simulador.rede.controller.main;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;
import br.com.guisi.simulador.rede.agent.control.impl.StepNumberStoppingCriteria;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.data.AgentDataType;
import br.com.guisi.simulador.rede.agent.data.AgentStepData;
import br.com.guisi.simulador.rede.agent.data.SwitchOperation;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.NetworkRestrictionsTreatmentType;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.constants.RandomActionType;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

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
	@FXML
	private Label lblTimer;
	@FXML
	private ComboBox<StoppingCriteria> cbAgentStoppingCriteria;
	@FXML
	private TextField tfStoppingCriteria;
	@FXML
	private Label lblCurrentSwitch;
	@FXML
	private ComboBox<RandomActionType> cbRandomAction;
	@FXML
	private ComboBox<NetworkRestrictionsTreatmentType> cbNetworkRestrictions;

	private LocalTime localTime;
	private Timeline timeline;

	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN, EventType.ENVIRONMENT_LOADED, EventType.AGENT_RUNNING, EventType.AGENT_STOPPED, EventType.AGENT_NOTIFICATION);

		Image imageCheck = new Image(getClass().getResourceAsStream("/img/check.png"));
		btnRunAgent.setGraphic(new ImageView(imageCheck));

		Image imageDelete = new Image(getClass().getResourceAsStream("/img/delete.png"));
		btnStopAgent.setGraphic(new ImageView(imageDelete));

		Image imageReset = new Image(getClass().getResourceAsStream("/img/reset.png"));
		btnResetAgent.setGraphic(new ImageView(imageReset));

		cbTaskExecutionType.setItems(FXCollections.observableArrayList(Arrays.asList(TaskExecutionType.values())));
		cbTaskExecutionType.setValue(TaskExecutionType.STEP_BY_STEP);

		StepNumberStoppingCriteria stepNumberStoppingCriteria = new StepNumberStoppingCriteria();
		cbAgentStoppingCriteria.setItems(FXCollections.observableArrayList(stepNumberStoppingCriteria));
		cbAgentStoppingCriteria.setValue(stepNumberStoppingCriteria);

		tfStoppingCriteria.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue && StringUtils.isBlank(tfStoppingCriteria.getText())) {
				tfStoppingCriteria.setText(Constants.STOPPING_CRITERIA_STEP_NUMBER);
			}
		});

		tfStoppingCriteria.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.length() > 10 || !newValue.matches("\\d*")) {
				tfStoppingCriteria.setText(oldValue);
			}
		});

		timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				localTime = localTime.plusMinutes(1);
				lblTimer.setText(localTime.toString());
				if (cbTaskExecutionType.getValue() == TaskExecutionType.CONTINUOUS_UPDATE_END_ONLY) {
					lblSteps.setText(String.valueOf(agentControl.getAgent().getStep()));
				}
			}
		});
		timeline.getKeyFrames().add(keyFrame);
		
		cbRandomAction.setItems(FXCollections.observableArrayList(RandomActionType.values()));
		cbRandomAction.setValue(RandomActionType.valueOf(PropertiesUtils.getProperty(PropertyKey.RANDOM_ACTION)));
		
		cbNetworkRestrictions.setItems(FXCollections.observableArrayList(NetworkRestrictionsTreatmentType.values()));
		cbNetworkRestrictions.setValue(NetworkRestrictionsTreatmentType.valueOf(PropertiesUtils.getProperty(PropertyKey.NETWORK_RESTRICTIONS_TREATMENT)));
	}

	@Override
	public void initializeControllerData(Object... data) {
	}

	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
		case RESET_SCREEN:
			this.resetScreen();
			break;
		case ENVIRONMENT_LOADED:
			this.processEnvironmentLoaded();
			break;
		case AGENT_RUNNING:
			this.processAgentRunning();
			break;
		case AGENT_STOPPED:
			this.processAgentStopped(data);
			break;
		case AGENT_NOTIFICATION:
			this.processAgentNotification(data);
			break;
		default:
			break;
		}
	}

	private void resetScreen() {
		root.setVisible(false);
		lblSteps.setText("0");
		localTime = LocalTime.MIN;
		lblTimer.setText(localTime.toString());
		timeline.stop();
		tfStoppingCriteria.setText(Constants.STOPPING_CRITERIA_STEP_NUMBER);
	}

	private void processEnvironmentLoaded() {
		root.setVisible(true);

		Branch currentState = agentControl.getAgent().getCurrentState();
		if (currentState != null) {
			lblCurrentSwitch.setText(currentState.getNumber().toString() + " (" + currentState.getSwitchStatus().getDescription() + ")");
		}
	}

	private void processAgentNotification(Object data) {
		AgentData agentData = (AgentData) data;

		if (agentData != null) {
			lblSteps.setText(String.valueOf(agentData.getSteps()));

			AgentStepData agentStepStatus = agentData.getAgentStepData().get(agentData.getAgentStepData().size() - 1);
			SwitchOperation switchOperation = agentStepStatus.getData(AgentDataType.SWITCH_OPERATION, SwitchOperation.class);
			if (switchOperation != null) {
				lblCurrentSwitch.setText(switchOperation.getSwitchNumber() + " (" + switchOperation.getSwitchState().getDescription() + ")");
			}
		}
	}

	private void enableDisableScreen(boolean disable) {
		btnRunAgent.setDisable(disable);
		btnStopAgent.setDisable(!disable);
		btnResetAgent.setDisable(disable);
		cbTaskExecutionType.setDisable(disable);
		cbAgentStoppingCriteria.setDisable(disable);
		tfStoppingCriteria.setDisable(disable);
		cbRandomAction.setDisable(disable);
		cbNetworkRestrictions.setDisable(disable);
	}

	private void processAgentRunning() {
		this.enableDisableScreen(true);
		timeline.playFromStart();
	}

	private void processAgentStopped(Object data) {
		this.enableDisableScreen(false);
		timeline.stop();

		Boolean stopRequested = (Boolean) data;
		if (cbTaskExecutionType.getValue() == TaskExecutionType.CONTINUOUS_UPDATE_END_ONLY && !stopRequested) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setContentText("Iteration finished!");
			alert.show();
		}
	}

	public void onCbRandomActionChange() {
		PropertiesUtils.saveProperty(PropertyKey.RANDOM_ACTION, cbRandomAction.getValue().name());
	}
	
	public void onCbNetworkRestrictionsChange() {
		PropertiesUtils.saveProperty(PropertyKey.NETWORK_RESTRICTIONS_TREATMENT, cbNetworkRestrictions.getValue().name());
	}

	/*********************************
	 *********************************
	 * Controle da interação do agente
	 *********************************
	 *********************************/
	public void runAgent() {
		StoppingCriteria stoppingCriteria = cbAgentStoppingCriteria.getValue();
		stoppingCriteria.setValue(tfStoppingCriteria.getText());
		agentControl.run(cbTaskExecutionType.getValue(), stoppingCriteria);
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
