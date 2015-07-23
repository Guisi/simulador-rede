package br.com.guisi.simulador.rede.view;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;

public class SimuladorRedeViewController {

	private Stage mainStage;

	@FXML
	private NetworkPane networkPane;
	@FXML
	private Button btnImportNetwork;
	@FXML
	private VBox boxInfo;
	@FXML
	private Label lblNumber;
	
	private Environment environment;

	public void initialize() {
		networkPane.initialize();
		boxInfo.setVisible(false);
	}

	public void createRandomNetwork() {
	}

	public void openNetworkFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open CSV File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
		File csvFile = fileChooser.showOpenDialog(null);
		
		if (csvFile != null) {
			try {
				environment = EnvironmentUtils.getEnvironmentFromFile(csvFile);
				networkPane.drawNetworkFromEnvironment(environment);
				this.updateLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateLayout() {
		networkPane.setVisible(true);
		networkPane.setPrefWidth(environment.getSizeX() * Constants.NETWORK_GRID_SIZE_PX);
		networkPane.setPrefHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10);
		networkPane.setMaxWidth(environment.getSizeX() * Constants.NETWORK_GRID_SIZE_PX);
		networkPane.setMaxHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10);

		boxInfo.setVisible(true);
		boxInfo.setPrefHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10);
		boxInfo.setMaxHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10);
	}

	public Stage getMainStage() {
		return mainStage;
	}

	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
}
