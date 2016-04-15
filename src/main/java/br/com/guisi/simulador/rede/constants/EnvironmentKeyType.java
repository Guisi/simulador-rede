package br.com.guisi.simulador.rede.constants;

public enum EnvironmentKeyType {

	INITIAL_ENVIRONMENT("Initial Environment"),
	INTERACTION_ENVIRONMENT("Interaction Environment"),
	LEARNING_ENVIRONMENT("Learning Environment");
	
	private final String label;

	private EnvironmentKeyType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
}
