package br.com.guisi.simulador.rede.constants;

public enum NetworkRestrictionsTreatmentType {

	LOAD_SHEDDING("Load shedding"),
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
