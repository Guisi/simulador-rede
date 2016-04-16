package br.com.guisi.simulador.rede.constants;

public enum NetworkRestrictionsTreatmentType {

	TURN_OFF_LOADS_TO_AVOID_RESTRICTIONS("Turn off loads to avoid restrictions"),
	CONSIDER_AS_NOT_SUPPLIED("Consider as not supplied");
	
	private final String label;

	private NetworkRestrictionsTreatmentType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
}
