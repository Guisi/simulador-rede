package br.com.guisi.simulador.rede.constants;

public enum PropertyKey {

	/** Prioridades */
	PRIORITY_1("0.125"),
	PRIORITY_2("0.25"),
	PRIORITY_3("0.5"),
	PRIORITY_4("1"),
	
	/**  */
	RANDOM_ACTION(RandomActionType.PSEUDO_RANDOM_PROPORTIONAL.name());
	
	private final String defaultValue;
	
	private PropertyKey(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
