package br.com.guisi.simulador.rede.controller.options;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.PropertyKey;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.util.DoubleTextFieldListener;
import br.com.guisi.simulador.rede.util.PropertiesUtils;

public class PriorityConfigController extends Controller {
	
	public static final String FXML_FILE = "/fxml/options/PriorityConfig.fxml";

	@FXML
	private VBox root;
	
	@FXML
	private TextField tfPriority1;
	@FXML
	private TextField tfPriority2;
	@FXML
	private TextField tfPriority3;
	@FXML
	private TextField tfPriority4;
	
	@Override
	public void initializeControllerData(Object... data) {
		tfPriority1.setText(PropertiesUtils.getProperty(PropertyKey.PRIORITY_1));
		tfPriority2.setText(PropertiesUtils.getProperty(PropertyKey.PRIORITY_2));
		tfPriority3.setText(PropertiesUtils.getProperty(PropertyKey.PRIORITY_3));
		tfPriority4.setText(PropertiesUtils.getProperty(PropertyKey.PRIORITY_4));
		
		tfPriority1.textProperty().addListener(new DoubleTextFieldListener(tfPriority1));
		tfPriority2.textProperty().addListener(new DoubleTextFieldListener(tfPriority2));
		tfPriority3.textProperty().addListener(new DoubleTextFieldListener(tfPriority3));
		tfPriority4.textProperty().addListener(new DoubleTextFieldListener(tfPriority4));
	}
	
	public void save() {
		if (StringUtils.isBlank(tfPriority1.getText())
				|| StringUtils.isBlank(tfPriority2.getText())
				|| StringUtils.isBlank(tfPriority3.getText())
				|| StringUtils.isBlank(tfPriority4.getText())) {
			Alert alert = new Alert(AlertType.ERROR, "Priority values are mandatory!");
			alert.showAndWait();
			return;
		}
		
		PropertiesUtils.saveProperty(PropertyKey.PRIORITY_1, tfPriority1.getText());
		PropertiesUtils.saveProperty(PropertyKey.PRIORITY_2, tfPriority2.getText());
		PropertiesUtils.saveProperty(PropertyKey.PRIORITY_3, tfPriority3.getText());
		PropertiesUtils.saveProperty(PropertyKey.PRIORITY_4, tfPriority4.getText());
		
		SimuladorRede.closeScene(this);
	}
	
	public void cancel() {
		SimuladorRede.closeScene(this);
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
