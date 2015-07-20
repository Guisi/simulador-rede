package br.com.guisi.simulador.rede.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.enviroment.Environment;

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
				List<String> lines = Files.readAllLines(Paths.get(csvFile.getAbsolutePath()), Charset.forName("ISO-8859-1"));
				networkPane.drawNetworkFromEnvironment(Environment.getInstanceFromFile(lines));
			} catch (IOException e) {
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
