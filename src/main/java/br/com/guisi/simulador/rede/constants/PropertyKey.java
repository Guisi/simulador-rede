package br.com.guisi.simulador.rede.constants;

public enum PropertyKey {

	/** Prioridades */
	PRIORITY_1("0.125"),
	PRIORITY_2("0.25"),
	PRIORITY_3("0.5"),
	PRIORITY_4("1"),
	
	/** Agent Options */
	RANDOM_ACTION(RandomActionType.PSEUDO_RANDOM_PROPORTIONAL.name()),
	NETWORK_RESTRICTIONS_TREATMENT(NetworkRestrictionsTreatmentType.LOAD_SHEDDING.name()),
	STOPPING_CRITERIA_STEP_NUMBER("1000"),
	
	/** Q-Learning */
	/* E-greedy de 90% */
	E_GREEDY("0.9"),
	
	/* Constante de aprendizagem (alpha) */
	LEARNING_CONSTANT("0.1"),
	
	/* Fator de desconto (gamma) */
	DISCOUNT_FACTOR("0.9"),
	
	/** Environment */
	LAST_ENVIRONMENT_FILE(null);
	
	private final String defaultValue;
	
	private PropertyKey(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
