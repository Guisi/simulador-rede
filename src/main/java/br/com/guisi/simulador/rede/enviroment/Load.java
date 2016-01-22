package br.com.guisi.simulador.rede.enviroment;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando um Load da rede
 * 
 * @author douglas.guisi
 */
public class Load extends NetworkNode {

	private Feeder feeder;
	private int priority;
	
	public Load(Integer nodeNumber, Integer x, Integer y, double activePower, double reactivePower, Status status, int priority) {
		super(nodeNumber, x, y, activePower, reactivePower, status);
		this.priority = priority;
	}

	/**
	 * Retorna o {@link Feeder} ao qual este {@link Load} está ligado
	 * @return {@link Feeder}
	 */
	public Feeder getFeeder() {
		return feeder;
	}

	public void setFeeder(Feeder feeder) {
		this.feeder = feeder;
	}

	/**
	 * Retorna o indicador de prioridade deste {@link Load}
	 * @return int
	 */
	public int getPriority() {
		return priority;
	}
	
	public String getColor() {
		return feeder != null ? feeder.getLoadColor() : "#FFFFFF";
	}
	
	/**
	 * Retorna se o load está ligado e atendido normalmente
	 * @return boolean
	 */
	public boolean isSupplied() {
		return isOn() && feeder != null && !hasBrokenConstraint();
	}
	
	public boolean hasBrokenConstraint() {
		return isCurrentVoltageAboveLimit() || isCurrentVoltageBelowLimit();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	
}
