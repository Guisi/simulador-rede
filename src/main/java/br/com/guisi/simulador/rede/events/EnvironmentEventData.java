package br.com.guisi.simulador.rede.events;

import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;

public class EnvironmentEventData {

	private final EnvironmentKeyType environmentKeyType;
	private final Object data;

	public EnvironmentEventData(EnvironmentKeyType environmentKeyType, Object data) {
		super();
		this.environmentKeyType = environmentKeyType;
		this.data = data;
	}

	public EnvironmentKeyType getEnvironmentKeyType() {
		return environmentKeyType;
	}

	public Object getData() {
		return data;
	}
	
}
