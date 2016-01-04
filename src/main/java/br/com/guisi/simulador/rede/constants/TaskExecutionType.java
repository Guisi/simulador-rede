package br.com.guisi.simulador.rede.constants;

public enum TaskExecutionType {

	CONTINUOUS_UPDATE_EVERY_STEP("Continuous - Update every step", true),
	CONTINUOUS_UPDATE_END_ONLY("Continuous - Update only at the end", false),
	STEP_BY_STEP("Step by step", true);
	
	private final String label;
	private final boolean notifyEveryStep;

	private TaskExecutionType(String label, boolean notifyEveryStep) {
		this.label = label;
		this.notifyEveryStep = notifyEveryStep;
	}

	public String getLabel() {
		return label;
	}
	
	public boolean isNotifyEveryStep() {
		return notifyEveryStep;
	}

	public String toString() {
		return label;
	}
}
