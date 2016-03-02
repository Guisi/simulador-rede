package br.com.guisi.simulador.rede.agent.status;

public enum AgentInformationType {

	SWITCH_OPERATION,
	ACTIVE_POWER_LOST,
	REACTIVE_POWER_LOST,
	SUPPLIED_LOADS_ACTIVE_POWER,
	SUPPLIED_LOADS_REACTIVE_POWER,
	NOT_SUPPLIED_LOADS_ACTIVE_POWER,
	NOT_SUPPLIED_LOADS_REACTIVE_POWER,
	OUT_OF_SERVICE_LOADS_ACTIVE_POWER,
	OUT_OF_SERVICE_LOADS_REACTIVE_POWER,
	SUPPLIED_LOADS_VS_PRIORITY,
	NOT_SUPPLIED_LOADS_VS_PRIORITY,
	SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY,
	NOT_SUPPLIED_LOADS_ACTIVE_POWER_VS_PRIORITY,
	MIN_LOAD_CURRENT_VOLTAGE_PU;
}
