package br.com.guisi.simulador.rede.constants;

public enum PreferenceKey {

	/** Prioridades */
	PREFERENCE_KEY_PRIORITY_1("0.125"),
	PREFERENCE_KEY_PRIORITY_2("0.25"),
	PREFERENCE_KEY_PRIORITY_3("0.5"),
	PREFERENCE_KEY_PRIORITY_4("1");
	
	private final String defaultValue;
	
	private PreferenceKey(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
