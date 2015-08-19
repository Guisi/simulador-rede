package br.com.guisi.simulador.rede.util;

import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class DoubleTextFieldListener implements ChangeListener<String> {

	private TextField textField;
	
	public DoubleTextFieldListener(TextField textField) {
		this.textField = textField;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		final Pattern pattern = Pattern.compile("^\\d*\\.?\\d*$");
    	
    	if (!pattern.matcher(newValue).matches()) {
    		textField.setText(oldValue);
    	}
	}

}
