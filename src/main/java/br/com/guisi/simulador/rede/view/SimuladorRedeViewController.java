package br.com.guisi.simulador.rede.view;

import java.io.File;
import java.text.DecimalFormat;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import br.com.guisi.simulador.rede.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.view.layout.BranchText;
import br.com.guisi.simulador.rede.view.layout.LoadStackPane;

public class SimuladorRedeViewController {

	private Stage mainStage;

	@FXML
	private NetworkPane networkPane;
	@FXML
	private Button btnImportNetwork;

	@FXML
	private VBox boxLoadInfo;
	@FXML
	private Label lblLoadNumber;
	@FXML
	private Label lblLoadFeeder;
	@FXML
	private Label lblLoadPower;
	
	@FXML
	private VBox boxFeederInfo;
	@FXML
	private Label lblFeederNumber;
	@FXML
	private Label lblFeederPower;
	
	@FXML
	private VBox boxBranchInfo;
	@FXML
	private Label lblBranchNumber;
	@FXML
	private Label lblBranchDe;
	@FXML
	private Label lblBranchPara;
	@FXML
	private Label lblBranchPower;
	@FXML
	private Label lblBranchStatus;
	
	private Environment environment;

	public void initialize() {
		this.resetScreen();
	}
	
	public void resetScreen() {
		networkPane.setVisible(false);
		
		boxLoadInfo.setVisible(false);
		lblLoadNumber.setText("");
		lblLoadFeeder.setText("");
		lblLoadPower.setText("");
		
		boxFeederInfo.setVisible(false);
		lblFeederNumber.setText("");
		lblFeederPower.setText("");
		
		boxBranchInfo.setVisible(false);
		lblBranchNumber.setText("");
		lblBranchDe.setText("");
		lblBranchPara.setText("");
		lblBranchPower.setText("");
		lblBranchStatus.setText("");
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
			this.initialize();

			try {
				environment = EnvironmentUtils.getEnvironmentFromFile(csvFile);
				this.drawNetworkFromEnvironment();
			} catch (Exception e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(e.getMessage());
				e.printStackTrace();
				alert.showAndWait();
			}
		}
	}
	
	private void drawNetworkFromEnvironment() {
		networkPane.setVisible(true);
		networkPane.setPrefWidth(environment.getSizeX() * Constants.NETWORK_GRID_SIZE_PX);
		networkPane.setPrefHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10);
		networkPane.setMaxWidth(environment.getSizeX() * Constants.NETWORK_GRID_SIZE_PX);
		networkPane.setMaxHeight(environment.getSizeY() * Constants.NETWORK_GRID_SIZE_PX - 10);
		boxLoadInfo.setVisible(true);
		boxFeederInfo.setVisible(true);
		boxBranchInfo.setVisible(true);

		networkPane.getChildren().clear();

		for (Load node : environment.getNodeMap().values()) {
			LoadStackPane loadStack = networkPane.drawNode(node);
			loadStack.setOnMouseClicked((event) -> {
				if (node.isLoad()) { 
					updateLoadInformationBox((LoadStackPane)event.getSource()); 
				} else {
					updateFeederInformationBox((LoadStackPane)event.getSource());	
				}
			});
		}
		
		for (Branch branch : environment.getBranchMap().values()) {
			Text text = networkPane.drawBranch(branch);
			text.setOnMouseClicked((event) -> updateBranchInformationBox((BranchText)event.getSource()));
		}
	}
	
	private void updateLoadInformationBox(LoadStackPane loadStackPane) {
		Load node = environment.getNode(loadStackPane.getLoadNum());
		lblLoadNumber.setText(node.getLoadNum().toString());
		lblLoadFeeder.setText(node.getFeeder().toString());
		lblLoadPower.setText(DecimalFormat.getNumberInstance().format(node.getLoadPower()));
	}
	
	private void updateFeederInformationBox(LoadStackPane loadStackPane) {
		Load node = environment.getNode(loadStackPane.getLoadNum());
		lblFeederNumber.setText(node.getLoadNum().toString());
		lblFeederPower.setText(DecimalFormat.getNumberInstance().format(node.getLoadPower()));
	}
	
	private void updateBranchInformationBox(BranchText branchText) {
		Branch branch = environment.getBranch(branchText.getBranchNum());
		lblBranchNumber.setText(branch.getBranchNum().toString());
		lblBranchDe.setText(branch.getNode1().getLoadNum().toString());
		lblBranchPara.setText(branch.getNode2().getLoadNum().toString());
		lblBranchPower.setText(DecimalFormat.getNumberInstance().format(branch.getBranchPower()));
		lblBranchStatus.setText(branch.isOn() ? "Ligado" : "Desligado");
	}

	public Stage getMainStage() {
		return mainStage;
	}

	public void setMainStage(Stage mainStage) {
		this.mainStage = mainStage;
	}
}
