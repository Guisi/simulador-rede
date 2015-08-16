package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;

public class Branch {

	private Integer branchNum;
	private NetworkNode load1;
	private NetworkNode load2;
	private double branchPower;
	private double distance;
	private Status status;
	private boolean switchBranch;
	
	public Branch(Integer branchNum, NetworkNode load1, NetworkNode load2, double branchPower, double distance, Status status, boolean switchBranch) {
		this.branchNum = branchNum;
		this.load1 = load1;
		this.load2 = load2;
		this.branchPower = branchPower;
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

	public double getBranchPower() {
		return branchPower;
	}
	public void setBranchPower(double branchPower) {
		this.branchPower = branchPower;
	}
	public Integer getBranchNum() {
		return branchNum;
	}
	public void setBranchNum(Integer branchNum) {
		this.branchNum = branchNum;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((branchNum == null) ? 0 : branchNum.hashCode());
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
		if (branchNum == null) {
			if (other.branchNum != null)
				return false;
		} else if (!branchNum.equals(other.branchNum))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Branch [branchNum=" + branchNum + ", load1=" + load1.getNodeNumber() + ", load2=" + load2.getNodeNumber() + ", branchPower=" + branchPower + ", distance=" + distance
				+ ", status=" + status + ", switchBranch=" + switchBranch + "]";
	}
	
}
