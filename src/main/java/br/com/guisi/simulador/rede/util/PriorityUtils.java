package br.com.guisi.simulador.rede.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import br.com.guisi.simulador.rede.constants.PropertyKey;

public class PriorityUtils {

	public PriorityUtils() {}
	
	public static double getPriorityValue(int priority) {
		String value = null;
		switch (priority) {
			case 1: value = PropertiesUtils.getProperty(PropertyKey.PRIORITY_1); break;
			case 2: value = PropertiesUtils.getProperty(PropertyKey.PRIORITY_2); break;
			case 3: value = PropertiesUtils.getProperty(PropertyKey.PRIORITY_3); break;
			case 4: value = PropertiesUtils.getProperty(PropertyKey.PRIORITY_4); break;
			default: 
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("Priority " + priority + " not defined!");
				alert.showAndWait();
				break;
		}

		return Double.parseDouble(value);
	}
}
