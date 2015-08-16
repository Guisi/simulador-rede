package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;
import br.com.guisi.simulador.rede.constants.SupplyStatus;

public class Load extends NetworkNode {

	private Feeder feeder;
	private int priority;
	private SupplyStatus supplyStatus;
	private double receivedPower;
	
	public Load(Integer nodeNumber, Integer x, Integer y, double power, Status status, int priority) {
		super(nodeNumber, x, y, power, status);
		this.priority = priority;
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
	
	public boolean isSupplied() {
		return SupplyStatus.SUPPLIED.equals(supplyStatus);
	}

	public double getReceivedPower() {
		return receivedPower;
	}

	public void setReceivedPower(double receivedPower) {
		this.receivedPower = receivedPower;
	}

	@Override
	public String toString() {
		return "Load [nodeNumber=" + nodeNumber + ", x=" + x + ", y=" + y + ", power=" + power + ", status=" + status + ", feeder="
				+ (feeder != null ? feeder.getNodeNumber() : null) + ", priority=" + priority + ", supplyStatus=" + supplyStatus + ", receivedPower=" + receivedPower + "]";
	}
	
}
