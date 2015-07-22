package br.com.guisi.simulador.rede.view;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;

public class SimuladorRedeViewController {

	private Stage mainStage;

	@FXML
	private NetworkPane networkPane;
	@FXML
	private Button btnImportNetwork;

	public void initialize() {
		networkPane.initialize();
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
				Environment environment = EnvironmentUtils.getEnvironmentFromFile(csvFile);
				networkPane.drawNetworkFromEnvironment(environment);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Stage getMainStage() {
		return mainStage;
	}

	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
}
