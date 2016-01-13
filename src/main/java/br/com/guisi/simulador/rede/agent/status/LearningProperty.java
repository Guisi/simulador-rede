package br.com.guisi.simulador.rede.agent.status;

public class LearningProperty {

	private String property;
	private String value;
	
	public LearningProperty(String property, String value) {
		super();
		this.property = property;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
