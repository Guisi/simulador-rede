package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.BranchStatus;

public class Branch {

	private Integer branchNum;
	private Load node1;
	private Load node2;
	private double branchPower;
	private BranchStatus status;
	
	public Branch(Integer branchNum, Load node1, Load node2, double branchPower, BranchStatus status) {
		this.branchNum = branchNum;
		this.node1 = node1;
		this.node2 = node2;
		this.branchPower = branchPower;
		this.status = status;
	}
	
	public boolean isOn() {
		return BranchStatus.ON.equals(status);
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
	public Load getNode1() {
		return node1;
	}
	public void setNode1(Load node1) {
		this.node1 = node1;
	}
	public Load getNode2() {
		return node2;
	}
	public void setNode2(Load node2) {
		this.node2 = node2;
	}
	public BranchStatus getStatus() {
		return status;
	}
	public void setStatus(BranchStatus status) {
		this.status = status;
	}
	
}
