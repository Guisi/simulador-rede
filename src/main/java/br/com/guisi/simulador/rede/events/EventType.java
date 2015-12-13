package br.com.guisi.simulador.rede.events;

/**
 * Tipos de eventos
 * 
 * @author Guisi
 */
public enum EventType {

	RESET_SCREEN,
	
	ENVIRONMENT_LOADED,
	
	FUNCTIONS_UPDATED,
	
	AGENT_RUNNING,
	AGENT_STOPPED,
	AGENT_NOTIFICATION,
	
	POWER_FLOW_COMPLETED,
	
	LOAD_SELECTED,
	FEEDER_SELECTED,
	BRANCH_SELECTED
}
