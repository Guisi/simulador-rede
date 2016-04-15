package br.com.guisi.simulador.rede.view.tableview;

import java.io.Serializable;

public class PropertyRowPair implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private PropertyRow propertyRow1;
	private PropertyRow propertyRow2;

	public PropertyRow getPropertyRow1() {
		return propertyRow1;
	}
	public void setPropertyRow1(PropertyRow propertyRow1) {
		this.propertyRow1 = propertyRow1;
	}
	public PropertyRow getPropertyRow2() {
		return propertyRow2;
	}
	public void setPropertyRow2(PropertyRow propertyRow2) {
		this.propertyRow2 = propertyRow2;
	}

}
