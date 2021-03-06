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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.control.AgentControl;
import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;
import br.com.guisi.simulador.rede.agent.control.impl.StepNumberStoppingCriteria;
import br.com.guisi.simulador.rede.agent.data.AgentData;
import br.com.guisi.simulador.rede.agent.qlearning.v3.AgentState;
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
	private Label lblCurrentState;
	@FXML
	private ComboBox<RandomActionType> cbRandomAction;
	@FXML
	private ComboBox<NetworkRestrictionsTreatmentType> cbNetworkRestrictions;
	@FXML
	private CheckBox cbUndoRandomAction;
	@FXML
	private ComboBox<Integer> cbClusterMaxSize;
	
	@FXML
	private TextField tfLearningConstant;
	@FXML
	private TextField tfDiscountFactor;
	@FXML
	private TextField tfEGreedy;

	private LocalTime localTime;
	private Timeline timeline;

	@PostConstruct
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN, 
						   EventType.ENVIRONMENT_LOADED,
						   EventType.AGENT_RUNNING,
						   EventType.AGENT_STOPPED,
						   EventType.AGENT_NOTIFICATION,
						   EventType.CLUSTERS_UPDATED);

		Image imageCheck = new Image(getClass().getResourceAsStream("/img/check.png"));
		btnRunAgent.setGraphic(new ImageView(imageCheck));

		Image imageDelete = new Image(getClass().getResourceAsStream("/img/delete.png"));
		btnStopAgent.setGraphic(new ImageView(imageDelete));

		Image imageReset = new Image(getClass().getResourceAsStream("/img/reset.png"));
		btnResetAgent.setGraphic(new ImageView(imageReset));

		StepNumberStoppingCriteria stepNumberStoppingCriteria = new StepNumberStoppingCriteria();
		cbAgentStoppingCriteria.setItems(FXCollections.observableArrayList(stepNumberStoppingCriteria));
		cbAgentStoppingCriteria.setValue(stepNumberStoppingCriteria);

		// Stopping Criteria
		this.initializeTextField(PropertyKey.STOPPING_CRITERIA_STEP_NUMBER, tfStoppingCriteria, false);
		
		//Learning Constant
		this.initializeTextField(PropertyKey.LEARNING_CONSTANT, tfLearningConstant, true);
		
		//Discount Factor
		this.initializeTextField(PropertyKey.DISCOUNT_FACTOR, tfDiscountFactor, true);
		
		//E-Greedy
		this.initializeTextField(PropertyKey.E_GREEDY, tfEGreedy, true);

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
		
		cbTaskExecutionType.setItems(FXCollections.observableArrayList(Arrays.asList(TaskExecutionType.values())));
		cbTaskExecutionType.setValue(TaskExecutionType.valueOf(PropertiesUtils.getProperty(PropertyKey.TASK_EXECUTION_TYPE)));
		
		cbRandomAction.setItems(FXCollections.observableArrayList(RandomActionType.values()));
		cbRandomAction.setValue(RandomActionType.valueOf(PropertiesUtils.getProperty(PropertyKey.RANDOM_ACTION)));
		
		cbNetworkRestrictions.setItems(FXCollections.observableArrayList(NetworkRestrictionsTreatmentType.values()));
		cbNetworkRestrictions.setValue(NetworkRestrictionsTreatmentType.valueOf(PropertiesUtils.getProperty(PropertyKey.NETWORK_RESTRICTIONS_TREATMENT)));
		
		cbUndoRandomAction.setSelected(Boolean.valueOf(PropertiesUtils.getProperty(PropertyKey.UNDO_RANDOM_ACTION)));
		cbUndoRandomAction.selectedProperty().addListener((observable, oldValue, newValue) -> PropertiesUtils.saveProperty(PropertyKey.UNDO_RANDOM_ACTION, String.valueOf(newValue)));
		
		cbClusterMaxSize.setItems(FXCollections.observableArrayList(3, 5, 7, 9, 11));
		cbClusterMaxSize.setValue(Integer.valueOf(PropertiesUtils.getProperty(PropertyKey.CLUSTER_MAX_SIZE)));
		cbClusterMaxSize.valueProperty().addListener((observable, oldValue, newValue) -> {
			PropertiesUtils.saveProperty(PropertyKey.CLUSTER_MAX_SIZE, String.valueOf(newValue));
			SimuladorRede.updateClusters();
			this.fireEvent(EventType.CLUSTERS_UPDATED);
		});
			
	}
	
	private void initializeTextField(PropertyKey propertyKey, TextField textField, boolean decimal) {
		textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				try {
					if (decimal) {
						Double.valueOf(textField.getText());
					} else {
						Integer.valueOf(textField.getText());
					}
				} catch (NumberFormatException e) {
					textField.setText(PropertiesUtils.getProperty(propertyKey));
				}
				PropertiesUtils.saveProperty(propertyKey, textField.getText());
			}
		});
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
			case CLUSTERS_UPDATED:
				this.processClustersUpdated();
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
		tfStoppingCriteria.setText(PropertiesUtils.getProperty(PropertyKey.STOPPING_CRITERIA_STEP_NUMBER));
		tfLearningConstant.setText(String.valueOf(PropertiesUtils.getLearningConstant()));
		tfDiscountFactor.setText(String.valueOf(PropertiesUtils.getDiscountFactor()));
		tfEGreedy.setText(String.valueOf(PropertiesUtils.getEGreedy()));
		cbClusterMaxSize.setDisable(false);
	}
	
	private void processClustersUpdated() {
		agentControl.reset();
		setCurrentStateLabel();
	}

	private void processEnvironmentLoaded() {
		root.setVisible(true);

		setCurrentStateLabel();		
	}
	
	private void setCurrentStateLabel() {
		Object currentState = agentControl.getAgent().getCurrentState();
		if (currentState != null) {
			if (currentState instanceof Branch) {
				Branch branch = (Branch) currentState;
				lblCurrentState.setText(branch.getNumber().toString() + " (" + branch.getSwitchStatus().getDescription() + ")");
			} else if (currentState instanceof AgentState) {
				AgentState agentState = (AgentState) currentState;
				lblCurrentState.setText( "ClusterNumber=" + agentState.getClusterNumber() + ", " + agentState.getSwitches() );
			}
		}
	}

	private void processAgentNotification(Object data) {
		AgentData agentData = (AgentData) data;

		if (agentData != null) {
			lblSteps.setText(String.valueOf(agentData.getSteps()));

			setCurrentStateLabel();
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
		tfDiscountFactor.setDisable(disable);
		tfEGreedy.setDisable(disable);
		tfLearningConstant.setDisable(disable);
		tfStoppingCriteria.setDisable(disable);
		cbUndoRandomAction.setDisable(disable);
	}

	private void processAgentRunning() {
		this.enableDisableScreen(true);
		cbClusterMaxSize.setDisable(true);
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
	
	public void onCbTaskExecutionTypeChange() {
		PropertiesUtils.saveProperty(PropertyKey.TASK_EXECUTION_TYPE, cbTaskExecutionType.getValue().name());
	}

	/*********************************
	 *********************************
	 * Controle da intera��o do agente
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
