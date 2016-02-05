package br.com.guisi.simulador.rede.constants;

public enum StoppingCriteriaType {

	STEP_NUMBER("Step Number");
	
	private final String label;

	private StoppingCriteriaType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public String toString() {
		return label;
	}
}
