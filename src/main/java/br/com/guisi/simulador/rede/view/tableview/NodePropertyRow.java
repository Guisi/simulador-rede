package br.com.guisi.simulador.rede.view.tableview;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NodePropertyRow implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private StringProperty propertyName;
	private StringProperty propertyValue;
	
	public NodePropertyRow(String propertyName, String propertyValue) {
		super();
		this.propertyName = new SimpleStringProperty();
		this.propertyName.setValue(propertyName);
		this.propertyValue = new SimpleStringProperty();
		this.propertyValue.setValue(propertyValue);
	}

	public StringProperty getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(StringProperty propertyName) {
		this.propertyName = propertyName;
	}

	public StringProperty getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(StringProperty propertyValue) {
		this.propertyValue = propertyValue;
	}

}
