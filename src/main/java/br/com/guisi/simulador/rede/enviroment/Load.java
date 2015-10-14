package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;
import br.com.guisi.simulador.rede.constants.SupplyStatus;

/**
 * Classe representando um Load da rede
 * 
 * @author douglas.guisi
 */
public class Load extends NetworkNode {

	private Feeder feeder;
	private int priority;
	private SupplyStatus supplyStatus;
	
	public Load(Integer nodeNumber, Integer x, Integer y, double activePower, double reactivePower, Status status, int priority) {
		super(nodeNumber, x, y, activePower, reactivePower, status);
		this.priority = priority;
	}

	/**
	 * Retorna se o load está ligado e sendo totalmente energizado
	 * @return boolean
	 */
	public boolean isSupplied() {
		return isOn() && SupplyStatus.SUPPLIED.equals(supplyStatus);
	}
	
	/**
	 * Retorna se o load está ligado e parcialmente energizado
	 * @return boolean
	 */
	public boolean isPartiallySupplied() {
		return isOn() && (SupplyStatus.PARTIALLY_SUPPLIED_BRANCH_EXCEEDED.equals(supplyStatus)
				|| SupplyStatus.PARTIALLY_SUPPLIED_FEEDER_EXCEEDED.equals(supplyStatus));
	}
	
	/**
	 * Retorna se o load está ligado mas sem energia
	 * @return boolean
	 */
	public boolean isNotSupplied() {
		return isOn() && (SupplyStatus.NOT_SUPPLIED_BRANCH_EXCEEDED.equals(supplyStatus)
				|| SupplyStatus.NOT_SUPPLIED_FEEDER_EXCEEDED.equals(supplyStatus)
				|| SupplyStatus.NOT_SUPPLIED_NO_FEEDER_CONNECTED.equals(supplyStatus));
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
	 * Retorna o status de atendimento deste {@link Load}
	 * @return {@link SupplyStatus}
	 */
	public SupplyStatus getSupplyStatus() {
		return supplyStatus;
	}

	public void setSupplyStatus(SupplyStatus supplyStatus) {
		this.supplyStatus = supplyStatus;
	}
	
	@Override
	public String toString() {
		return "Load [nodeNumber=" + nodeNumber + ", x=" + x + ", y=" + y + ", activePower=" + activePower + ", reactivePower=" + reactivePower + ", status=" + status + ", feeder="
				+ (feeder != null ? feeder.getNodeNumber() : null) + ", priority=" + priority + ", supplyStatus=" + supplyStatus + "]";
	}
	
}
