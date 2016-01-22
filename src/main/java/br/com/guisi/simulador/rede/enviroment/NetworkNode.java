package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando um nó da rede (Load ou Feeder)
 * 
 * @author douglas.guisi
 */
public abstract class NetworkNode {

	protected Integer nodeNumber;
	protected Integer x;
	protected Integer y;
	protected double activePower;
	protected double reactivePower;
	protected double currentVoltagePU;
	protected Status status;
	protected Set<Branch> branches = new HashSet<>();
	
	public NetworkNode(Integer nodeNumber, Integer x, Integer y, double activePower, double reactivePower, Status status) {
		super();
		this.nodeNumber = nodeNumber;
		this.x = x;
		this.y = y;
		this.activePower = activePower;
		this.reactivePower = reactivePower;
		this.status = status;
	}

	public void addBranch(Branch branch) {
		branches.add(branch);
	}
	
	/**
	 * Retorna se este nó é um {@link Feeder}
	 * @return boolean
	 */
	public boolean isFeeder() {
		return this instanceof Feeder;
	}
	
	/**
	 * Retorna se este nó é um {@link Load}
	 * @return boolean
	 */
	public boolean isLoad() {
		return this instanceof Load;
	}
	
	/**
	 * Retorna a lista de nós conectados a este
	 * @return {@link List<NetworkNode>}
	 */
	public List<NetworkNode> getConnectedNodes() {
		List<NetworkNode> networkNodes = new ArrayList<>();
		branches.forEach((branch) -> {
			if (branch.isClosed()) {
				networkNodes.add(branch.getConnectedLoad(this));
			}
		});
		return networkNodes;
	}
	
	/**
	 * Retorna a branch que conecta este node ao node passado como parâmetro
	 * @return {@link Branch}
	 */
	public Branch getBranch(NetworkNode node) {
		for (Branch branch : branches) {
			if (branch.isClosed() && branch.getConnectedLoad(this).equals(node)) {
				return branch;
			}
		}
		return null;
	}
	
	/**
	 * Retorna se este nó está ligado
	 * @return boolean
	 */
	public boolean isOn() {
		return Status.ON.equals(status);
	}
	
	/**
	 * Desliga o load
	 */
	public void turnOff() {
		this.status = Status.OFF;
	}
	
	/**
	 * Liga o load
	 */
	public void turnOn() {
		this.status = Status.ON;
	}

	/**
	 * Retorna a posição X deste nó no ambiente
	 * @return {@link Integer}
	 */
	public Integer getX() {
		return x;
	}

	/**
	 * Retorna a posição Y deste nó no ambiente
	 * @return {@link Integer}
	 */
	public Integer getY() {
		return y;
	}

	/**
	 * Retorna um {@link Set<Branch>} com os branches ligados neste nó
	 * @return {@link Set<Branch>}
	 */
	public Set<Branch> getBranches() {
		return branches;
	}
	
	/**
	 * Retorna o número deste nó
	 * @return {@link Integer}
	 */
	public Integer getNodeNumber() {
		return nodeNumber;
	}

	/**
	 * Retorna a potência ativa deste nó
	 * @return double
	 */
	public double getActivePower() {
		return activePower;
	}
	
	/**
	 * Retorna a potência reativa deste nó
	 * @return double
	 */
	public double getReactivePower() {
		return reactivePower;
	}

	/**
	 * Retorna o {@link Status} deste nó, on ou off
	 * @return {@link Status}
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Voltagem atual em PU
	 * @return
	 */
	public double getCurrentVoltagePU() {
		return currentVoltagePU;
	}

	public void setCurrentVoltagePU(double currentVoltagePU) {
		this.currentVoltagePU = currentVoltagePU;
	}
	
	public boolean isCurrentVoltageBelowLimit() {
		return currentVoltagePU < Constants.TENSAO_MIN_PU; 
	}
	
	public boolean isCurrentVoltageAboveLimit() {
		return currentVoltagePU > Constants.TENSAO_MAX_PU; 
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodeNumber;
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
		NetworkNode other = (NetworkNode) obj;
		if (nodeNumber != other.nodeNumber)
			return false;
		return true;
	}
}
