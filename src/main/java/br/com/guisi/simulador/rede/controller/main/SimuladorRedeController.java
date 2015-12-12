package br.com.guisi.simulador.rede.controller.main;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;

public class SimuladorRedeController extends Controller {

	public static final String FXML_FILE = "/fxml/main/SimuladorRede.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private VBox networkBoxLeft;
	@FXML
	private VBox networkBoxRight;
	
	
	@Override
	public void initializeController(Object... data) {
		this.listenToEvent(EventType.AGENT_RUNNING);
		this.listenToEvent(EventType.AGENT_STOPPED);
		
		//menu
		root.getChildren().add(0, getController(MenuPaneController.class).getView());
		
		//controls
		networkBoxLeft.getChildren().add(getController(ControlsPaneController.class).getView());
		
		//Painel dos detalhes dos elementos da rede
		networkBoxLeft.getChildren().add(getController(ElementsDetailsPaneController.class).getView());
		
		//Painel das funções
		networkBoxLeft.getChildren().add(getController(FunctionsPaneController.class).getView());
		
		//Painel de labels e messages
		networkBoxLeft.getChildren().add(getController(LabelAndMessagesPaneController.class).getView());
		
		//NetworkPane
		networkBoxRight.getChildren().add(getController(NetworkPaneController.class).getView());
		
		this.fireEvent(EventType.RESET_SCREEN);
		
		/*File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.csv");
		this.loadEnvironmentFromFile(f);*/
	}
	
	/*private void updateAgentStatus(QLearningStatus qLearningStatus) {
		qLearningStatus.setHandled(true);
		networkPane.setAgentCirclePosition(qLearningStatus.getCurrentState());
		
		List<Integer> switchesChanged = qLearningStatus.getSwitchesChanged();
		if (!switchesChanged.isEmpty()) {
			Environment environment = getEnvironment();
			
			//atualiza informações das conexões dos feeders e loads
			EnvironmentUtils.updateFeedersConnections(environment);
			
			//atualiza status dos switches na tela
			switchesChanged.forEach((swNum) -> {
				Branch sw = environment.getBranch(swNum);
				networkPane.updateBranch(sw);
			});
			
			//atualiza status dos nós na tela
			environment.getNetworkNodes().forEach((node) -> networkPane.updateNetworkNode(node));
		}
	}*/
	
	@Override
	public Node getView() {
		return root;
	}
}
