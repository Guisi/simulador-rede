package br.com.guisi.simulador.rede.constants;

import br.com.guisi.simulador.rede.enviroment.Load;

/**
 * Enumeration representando os possíveis status de atendimento de um {@link Load}
 * 
 * @author douglas.guisi
 */
public enum LoadSupplyStatus {

	SUPPLIED("Atendido"),
	NO_FEEDER_CONNECTED("Not connected to a feeder"),
	CURRENT_VOLTAGE_BELOW_LIMIT("Current voltage below limit"),
	CURRENT_VOLTAGE_ABOVE_LIMIT("Current voltage avbove limit");
	
	private String description;

	private LoadSupplyStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
