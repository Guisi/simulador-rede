package br.com.guisi.simulador.rede.constants;

public enum RandomActionType {

	PSEUDO_RANDOM("Pseudo-Random"), PSEUDO_RANDOM_PROPORTIONAL("Pseudo-Random-Proportional");
	
	private final String label;

	private RandomActionType(String label) {
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
