package br.com.guisi.simulador.rede.constants;

public enum TaskExecutionType {

	CONTINUOUS_UPDATE_EVERY_STEP("Continuous - Update every step", true),
	CONTINUOUS_UPDATE_END_ONLY("Continuous - Update only at the end", false),
	STEP_BY_STEP("Step by step", true);
	
	private final String label;
	private final boolean notifyObservers;

	private TaskExecutionType(String label, boolean notifyObservers) {
		this.label = label;
		this.notifyObservers = notifyObservers;
	}

	public String getLabel() {
		return label;
	}
	
	public boolean isNotifyObservers() {
		return notifyObservers;
	}

	public String toString() {
		return label;
	}
}
