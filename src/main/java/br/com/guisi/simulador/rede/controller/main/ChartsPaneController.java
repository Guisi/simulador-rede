package br.com.guisi.simulador.rede.controller.main;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import br.com.guisi.simulador.rede.agent.status.AgentStatus;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.view.charts.GenericLineChart;
import br.com.guisi.simulador.rede.view.charts.MinLoadCurrentVoltagePUChart;
import br.com.guisi.simulador.rede.view.charts.PowerLossChart;
import br.com.guisi.simulador.rede.view.charts.PowerLossPercentageChart;
import br.com.guisi.simulador.rede.view.charts.SuppliedLoadsActivePowerPercentageChart;
import br.com.guisi.simulador.rede.view.charts.SuppliedLoadsPercentageChart;

public class ChartsPaneController extends Controller {

	public static final String FXML_FILE = "/fxml/main/ChartsPane.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TabPane tabPaneCharts;
	
	private List<GenericLineChart> lineCharts;
	private int stepUpdateReceived;
	
	@Override
	public void initializeController() {
		this.listenToEvent(EventType.RESET_SCREEN,
						   EventType.ENVIRONMENT_LOADED,
						   EventType.AGENT_NOTIFICATION);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
	}
	
	@Override
	public void onEvent(EventType eventType, Object data) {
		switch (eventType) {
			case RESET_SCREEN: this.resetScreen(); break;
			case ENVIRONMENT_LOADED: this.processEnvironmentLoaded(); break;
			case AGENT_NOTIFICATION: this.processAgentNotification(data); break;
			default: break;
		}
	}
	
	private void createCharts() {
		tabPaneCharts.getTabs().clear();
		lineCharts = new ArrayList<>();
		
		//total power lost %
		lineCharts.add(new PowerLossPercentageChart());
		//total power lost MW/MVar
		lineCharts.add(new PowerLossChart());
		//% supplied loads x priority
		lineCharts.add(new SuppliedLoadsPercentageChart());
		//supplied loads x priority
		lineCharts.add(new SuppliedLoadsActivePowerPercentageChart());
		//min load current voltage PU
		lineCharts.add(new MinLoadCurrentVoltagePUChart());

		lineCharts.forEach((chart) -> {
			Tab tab = new Tab(chart.getChartTitle());
			tab.setContent(chart);
			tabPaneCharts.getTabs().add(tab);
		});
	}
	
	private void resetScreen() {
		root.setVisible(false);
		this.stepUpdateReceived = 0;
	}
	
	private void processEnvironmentLoaded() {
		root.setVisible(true);
		this.createCharts();
	}
	
	private void processAgentNotification(Object data) {
		AgentStatus agentStatus = (AgentStatus) data;
		if (agentStatus != null) {
			for (int i = stepUpdateReceived; i < agentStatus.getStepStatus().size(); i++) {
				AgentStepStatus agentStepStatus = agentStatus.getStepStatus().get(i);
				
				lineCharts.forEach((chart) -> {
					chart.processAgentStepStatus(agentStepStatus);
				});
			}
			stepUpdateReceived = agentStatus.getStepStatus().size();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}

}
