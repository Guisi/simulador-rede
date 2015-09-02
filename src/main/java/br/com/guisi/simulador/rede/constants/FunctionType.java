package br.com.guisi.simulador.rede.constants;

/**
 * Enumeration representando os tipos de funções que podem ser cadastrados no simulador
 * 
 * @author douglas.guisi
 */
public enum FunctionType {

	DISTRIBUITION_SYSTEM("Distribuition System"),
	POWER_FLOW("Power Flow"),
	OBJETIVE_FUNCTIONS("Objective Functions");
	
	private final String description;

	private FunctionType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public static FunctionType getByDescription(String description) {
		for (FunctionType ft : FunctionType.values()) {
			if (ft.getDescription().equals(description)) {
				return ft;
			}
		}
		
		throw new IllegalArgumentException("You see man, there's two kind of parameters in this world: "
				+ "those who are valid and those who are not. " + description + " it's not valid!");
	}
	
}
