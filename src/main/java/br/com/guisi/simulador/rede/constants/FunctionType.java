package br.com.guisi.simulador.rede.constants;

/**
 * @author Guisi
 *
 */
public enum FunctionType {

	DISTRIBUITION_SYSTEM("Distribuition System"),
	POWER_FLOW("Power Flow"),
	OBJETIVE_FUNCTIONS("Objective Function");
	
	private FunctionType(String description) {
		this.description = description;
	}

	private final String description;

	public String getDescription() {
		return description;
	}
	
}
