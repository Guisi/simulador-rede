package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;
import br.com.guisi.simulador.rede.constants.SupplyStatus;

public class Load extends NetworkNode {

	private Feeder feeder;
	private int priority;
	private SupplyStatus supplyStatus;
	private double powerSupplied;
	
	public Load(Integer nodeNumber, Integer x, Integer y, double power, Status status, int priority) {
		super(nodeNumber, x, y, power, status);
		this.priority = priority;
	}

	public boolean isSupplied() {
		return isOn() && SupplyStatus.SUPPLIED.equals(supplyStatus);
	}
	
	public boolean isPartiallySupplied() {
		return isOn() && (SupplyStatus.PARTIALLY_SUPPLIED_BRANCH_EXCEEDED.equals(supplyStatus)
				|| SupplyStatus.PARTIALLY_SUPPLIED_FEEDER_EXCEEDED.equals(supplyStatus));
	}
	
	public boolean isNotSupplied() {
		return isOn() && (SupplyStatus.NOT_SUPPLIED_BRANCH_EXCEEDED.equals(supplyStatus)
				|| SupplyStatus.NOT_SUPPLIED_FEEDER_EXCEEDED.equals(supplyStatus)
				|| SupplyStatus.NOT_SUPPLIED_NO_FEEDER_CONNECTED.equals(supplyStatus));
	}
	
	public double getPowerNotSupplied() {
		return power - powerSupplied;
	}
	
	public Feeder getFeeder() {
		return feeder;
	}

	public void setFeeder(Feeder feeder) {
		this.feeder = feeder;
	}

	public int getPriority() {
		return priority;
	}
	
	public String getColor() {
		return feeder != null ? feeder.getLoadColor() : "#FFFFFF";
	}

	public SupplyStatus getSupplyStatus() {
		return supplyStatus;
	}

	public void setSupplyStatus(SupplyStatus supplyStatus) {
		this.supplyStatus = supplyStatus;
	}
	
	public double getPowerSupplied() {
		return powerSupplied;
	}

	public void setPowerSupplied(double powerSupplied) {
		this.powerSupplied = powerSupplied;
	}

	@Override
	public String toString() {
		return "Load [nodeNumber=" + nodeNumber + ", x=" + x + ", y=" + y + ", power=" + power + ", status=" + status + ", feeder="
				+ (feeder != null ? feeder.getNodeNumber() : null) + ", priority=" + priority + ", supplyStatus=" + supplyStatus + ", receivedPower=" + powerSupplied + "]";
	}
	
}
