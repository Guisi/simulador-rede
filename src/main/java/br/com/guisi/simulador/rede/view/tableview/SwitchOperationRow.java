package br.com.guisi.simulador.rede.view.tableview;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SwitchOperationRow implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private StringProperty message;
	
	public SwitchOperationRow() {
		this.message = new SimpleStringProperty();
	}
	
	public StringProperty getMessage() {
		return message;
	}

	public void setMessage(StringProperty message) {
		this.message = message;
	}

}
