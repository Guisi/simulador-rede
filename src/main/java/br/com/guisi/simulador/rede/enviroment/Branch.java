package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;

public class Branch {

	private Integer number;
	private NetworkNode load1;
	private NetworkNode load2;
	private double power;
	private double distance;
	private Status status;
	private boolean switchBranch;
	private double usedPower;
	
	public Branch(Integer number, NetworkNode load1, NetworkNode load2, double power, double distance, Status status, boolean switchBranch) {
		this.number = number;
		this.load1 = load1;
		this.load2 = load2;
		this.power = power;
		this.distance = distance;
		this.status = status;
		this.switchBranch = switchBranch;
	}
	
	public boolean isOn() {
		return Status.ON.equals(status);
	}
	
	/**
	 * Retorna o load conectado por esta branch
	 * ao load passado como parametro
	 * @param networkNode
	 * @return
	 */
	public NetworkNode getConnectedLoad(NetworkNode networkNode) {
		return load1.equals(networkNode) ? load2 : load1;
	}
	
	public double getAvailablePower() {
		return power - usedPower;
	}
	
	public void addUsedPower(double usedPower) {
		this.usedPower += usedPower;
	}

	public double getPower() {
		return power;
	}
	public Integer getNumber() {
		return number;
	}
	public NetworkNode getLoad1() {
		return load1;
	}
	public void setLoad1(NetworkNode load1) {
		this.load1 = load1;
	}
	public NetworkNode getLoad2() {
		return load2;
	}
	public void setLoad2(NetworkNode load2) {
		this.load2 = load2;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public boolean isSwitchBranch() {
		return switchBranch;
	}
	public void setSwitchBranch(boolean switchBranch) {
		this.switchBranch = switchBranch;
	}

	public double getUsedPower() {
		return usedPower;
	}

	public void setUsedPower(double usedPower) {
		this.usedPower = usedPower;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Branch other = (Branch) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Branch [branchNum=" + number + ", load1=" + load1.getNodeNumber() + ", load2=" + load2.getNodeNumber() + ", branchPower=" + power + ", distance=" + distance
				+ ", status=" + status + ", switchBranch=" + switchBranch + "]";
	}
	
}
