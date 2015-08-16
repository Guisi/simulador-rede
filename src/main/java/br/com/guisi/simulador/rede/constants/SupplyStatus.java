package br.com.guisi.simulador.rede.constants;

public enum SupplyStatus {

	SUPPLIED("Atendido"),
	PARTIALLY_SUPPLIED_FEEDER_EXCEEDED("Parcialmente atendido, excedeu capacidade do Feeder"),
	PARTIALLY_SUPPLIED_BRANCH_EXCEEDED("Parcialmente atendido, excedeu capacidade de algum Branch"),
	NOT_SUPPLIED_NO_FEEDER_CONNECTED("Não está conectado a nenhum Feeder"),
	NOT_SUPPLIED_BRANCH_EXCEEDED("Carga excedeu capacidade de algum Branch"),
	NOT_SUPPLIED_FEEDER_EXCEEDED("Carga excedeu capacidade do Feeder");
	
	private String description;

	private SupplyStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
}
