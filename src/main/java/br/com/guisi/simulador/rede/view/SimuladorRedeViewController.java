package br.com.guisi.simulador.rede.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SimuladorRedeViewController {

	@FXML
	private NetworkPane networkPane;
	@FXML
	private Button btnCreateNetwork;
	
	public void initialize() {
		networkPane.initialize();
	}

	public void createRandomNetwork() {
		networkPane.createRandomNetwork();
	}
}
