package br.com.guisi.simulador.rede.constants;

import br.com.guisi.simulador.rede.enviroment.Load;

/**
 * Enumeration representando os possíveis status de atendimento de um {@link Load}
 * 
 * @author douglas.guisi
 */
public enum SupplyStatus {

	SUPPLIED("Atendido"),
	PARTIALLY_SUPPLIED_FEEDER_EXCEEDED("Partially supplied, exceeded feeder capacity"),
	PARTIALLY_SUPPLIED_BRANCH_EXCEEDED("Partially supplied, exceeded branch capacity"),
	NOT_SUPPLIED_NO_FEEDER_CONNECTED("Not connected to a feeder"),
	NOT_SUPPLIED_BRANCH_EXCEEDED("Exceeded branch capacity"),
	NOT_SUPPLIED_FEEDER_EXCEEDED("Exceeded feeder capacity");
	
	private String description;

	private SupplyStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
