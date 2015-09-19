package br.com.guisi.simulador.rede.constants;

public enum TaskExecutionType {

	CONTINUOUS_UPDATE_EVERY_STEP("Continuous - Update every step"),
	CONTINUOUS_UPDATE_END_ONLY("Continuous - Update only at the end"),
	STEP_BY_STEP("Step by step");
	
	private final String label;

	private TaskExecutionType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public String toString() {
		return label;
	}
}
