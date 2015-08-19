package br.com.guisi.simulador.rede.constants;

public enum PreferenceKey {

	/** Prioridades */
	PREFERENCE_KEY_PRIORITY(Integer.valueOf(0));
	
	private final Object defaultValue;
	
	private PreferenceKey(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}
	
}
