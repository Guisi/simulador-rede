package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando uma branch
 * 
 * @author douglas.guisi
 */
public class Branch {

	private Integer number;
	private NetworkNode load1;
	private NetworkNode load2;
	private double maxCurrent;
	private double resistance;
	private double reactance;
	private Status status;
	private boolean switchBranch;
	private double usedPower;
	private int switchOperations;
	
	public Branch(Integer number, NetworkNode load1, NetworkNode load2, double maxCurrent, double resistance, double reactance, Status status, boolean switchBranch) {
		this.number = number;
		this.load1 = load1;
		this.load2 = load2;
		this.maxCurrent = maxCurrent;
		this.resistance = resistance;
		this.reactance = reactance;
		this.status = status;
		this.switchBranch = switchBranch;
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
		return load1.equals(networkNode) ? load2 : load1;
	}
	
	/**
	 * Retorna a quantidade de potência disponível com relação à capacidade máxima
	 * @return double
	 */
	public double getAvailablePower() {
		return maxCurrent - usedPower;
	}
	
	/**
	 * Adiciona o valor de potência usado com relação à capacidade máxima
	 * @param usedPower
	 */
	public void addUsedPower(double usedPower) {
		this.usedPower += usedPower;
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
	 * Retorna o {@link Load} de uma das pontas da branch
	 * @return {@link Load}
	 */
	public NetworkNode getLoad1() {
		return load1;
	}
	
	public void setLoad1(NetworkNode load1) {
		this.load1 = load1;
	}

	/**
	 * Retorna o {@link Load} de uma das pontas da branch
	 * @return {@link Load}
	 */
	public NetworkNode getLoad2() {
		return load2;
	}
	public void setLoad2(NetworkNode load2) {
		this.load2 = load2;
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
	 * Retorna a potência em uso nesta branch
	 * @return double
	 */
	public double getUsedPower() {
		return usedPower;
	}

	public void setUsedPower(double usedPower) {
		this.usedPower = usedPower;
	}

	/**
	 * Retorna o número de operações de switch desta branch
	 * @return int
	 */
	public int getSwitchOperations() {
		return switchOperations;
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
		return "Branch [branchNum=" + number + ", load1=" + load1.getNodeNumber() + ", load2=" + load2.getNodeNumber() + ", branchPower=" + maxCurrent + ", resistance=" + resistance
				+ ", reactance=" + reactance + ", status=" + status + ", switchBranch=" + switchBranch + "]";
	}
	
}
