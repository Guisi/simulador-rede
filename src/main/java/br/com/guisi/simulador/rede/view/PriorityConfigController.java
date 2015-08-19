package br.com.guisi.simulador.rede.view;

import java.util.Map;
import java.util.prefs.BackingStoreException;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.PreferenceKey;
import br.com.guisi.simulador.rede.util.DoubleTextFieldListener;
import br.com.guisi.simulador.rede.util.PreferencesUtils;

public class PriorityConfigController extends Controller {

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
	
	public void initialize() {
		Map<PreferenceKey, String> preferences = SimuladorRede.getPreferences();
		
		tfPriority1.setText(preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_1));
		tfPriority2.setText(preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_2));
		tfPriority3.setText(preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_3));
		tfPriority4.setText(preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_4));
		
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
		
		Map<PreferenceKey, String> preferences = SimuladorRede.getPreferences();
		
		preferences.put(PreferenceKey.PREFERENCE_KEY_PRIORITY_1, tfPriority1.getText());
		preferences.put(PreferenceKey.PREFERENCE_KEY_PRIORITY_2, tfPriority2.getText());
		preferences.put(PreferenceKey.PREFERENCE_KEY_PRIORITY_3, tfPriority3.getText());
		preferences.put(PreferenceKey.PREFERENCE_KEY_PRIORITY_4, tfPriority4.getText());
		
		try {
			PreferencesUtils.savePreferences(preferences);
		} catch (BackingStoreException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setContentText(e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
		
		SimuladorRede.closeModalScene();
	}
	
	public void cancel() {
		SimuladorRede.closeModalScene();
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
