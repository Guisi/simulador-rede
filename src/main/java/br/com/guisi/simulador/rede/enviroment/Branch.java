package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando uma branch
 * 
 * @author douglas.guisi
 */
public class Branch {

	private Integer number;
	private BranchId branchId;
	private NetworkNode node1;
	private NetworkNode node2;
	private double maxCurrent;
	private double resistance;
	private double reactance;
	private Status status;
	private boolean switchBranch;
	private int switchOperations;

	private double instantCurrent;
	private double lossesMW;
	
	public Branch(Integer number, NetworkNode node1, NetworkNode node2, double maxCurrent, double resistance, double reactance, Status status, boolean switchBranch) {
		this.number = number;
		this.node1 = node1;
		this.node2 = node2;
		this.maxCurrent = maxCurrent;
		this.resistance = resistance;
		this.reactance = reactance;
		this.status = status;
		this.switchBranch = switchBranch;
		this.branchId = new BranchId(node1.getNodeNumber(), node2.getNodeNumber());
	}
	
	/**
	 * Retorna se a branch está ligada
	 * @return boolean
	 */
	public boolean isOn() {
		return Status.ON.equals(status);
	}
	
	/**
	 * Retorna o load conectado por esta branch
	 * ao load passado como parametro
	 * @param networkNode
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getConnectedLoad(NetworkNode networkNode) {
		return node1.equals(networkNode) ? node2 : node1;
	}
	
	/**
	 * Retorna a quantidade de potência disponível com relação à capacidade máxima
	 * @return double
	 */
	public double getAvailablePower() {
		return maxCurrent - instantCurrent;
	}
	
	/**
	 * Incrementa o número de operações desta branch em 1
	 */
	public void incrementSwitchOperation() {
		this.switchOperations++;
	}

	/**
	 * Retorna a capacidade máxima desta branch (em ampères)
	 * @return double
	 */
	public double getMaxCurrent() {
		return maxCurrent;
	}

	/**
	 * Retorna o número desta branch
	 * @return {@link Integer}
	 */
	public Integer getNumber() {
		return number;
	}
	
	/**
	 * Retorna o {@link NetworkNode} de uma das pontas da branch
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNode1() {
		return node1;
	}
	
	public void setNode1(NetworkNode node1) {
		this.node1 = node1;
	}

	/**
	 * Retorna o {@link NetworkNode} de uma das pontas da branch
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNode2() {
		return node2;
	}
	public void setNode2(NetworkNode node2) {
		this.node2 = node2;
	}
	
	/**
	 * Retorna o {@link Status} desta branch, on ou off
	 * @return {@link Status}
	 */
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Retorna a resistencia do branch
	 * @return
	 */
	public double getResistance() {
		return resistance;
	}

	public void setResistence(double resistence) {
		this.resistance = resistence;
	}

	/**
	 * Retorna a reatancia do branch
	 * @return
	 */
	public double getReactance() {
		return reactance;
	}

	public void setReactance(double reactance) {
		this.reactance = reactance;
	}

	/**
	 * Retorna se esta branch é um switch
	 * @return boolean
	 */
	public boolean isSwitchBranch() {
		return switchBranch;
	}
	public void setSwitchBranch(boolean switchBranch) {
		this.switchBranch = switchBranch;
	}

	/**
	 * Retorna a corrente atual passando por esta branch
	 * @return
	 */
	public double getInstantCurrent() {
		return instantCurrent;
	}

	public void setInstantCurrent(double instantCurrent) {
		this.instantCurrent = instantCurrent;
	}

	/**
	 * Retorna o número de operações de switch desta branch
	 * @return int
	 */
	public int getSwitchOperations() {
		return switchOperations;
	}

	public BranchId getBranchId() {
		return branchId;
	}

	/**
	 * Perda em megawatts
	 * @return
	 */
	public double getLossesMW() {
		return lossesMW;
	}

	public void setLossesMW(double lossesMW) {
		this.lossesMW = lossesMW;
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
		return "Branch [branchNum=" + number + ", node1=" + node1.getNodeNumber() + ", node2=" + node2.getNodeNumber() + ", branchPower=" + maxCurrent + ", resistance=" + resistance
				+ ", reactance=" + reactance + ", status=" + status + ", switchBranch=" + switchBranch + ", instantCurrent=" + instantCurrent + ", lossesMW=" + lossesMW + "]";
	}
	
}
