package br.com.guisi.simulador.rede.util;

import java.util.Map;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.PreferenceKey;

public class PriorityUtils {

	public PriorityUtils() {}
	
	public static double getPriorityValue(int priority) {
		Map<PreferenceKey, String> preferences = SimuladorRede.getPreferences();
		
		String value = null;
		switch (priority) {
			case 1: value = preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_1); break;
			case 2: value = preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_2); break;
			case 3: value = preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_3); break;
			case 4: value = preferences.get(PreferenceKey.PREFERENCE_KEY_PRIORITY_4); break;
			default: 
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("Priority " + priority + " not defined!");
				alert.showAndWait();
				break;
		}

		return Double.parseDouble(value);
	}
}
