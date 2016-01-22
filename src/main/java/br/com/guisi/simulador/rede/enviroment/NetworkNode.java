package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando um n� da rede (Load ou Feeder)
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
	 * Retorna se este n� � um {@link Feeder}
	 * @return boolean
	 */
	public boolean isFeeder() {
		return this instanceof Feeder;
	}
	
	/**
	 * Retorna se este n� � um {@link Load}
	 * @return boolean
	 */
	public boolean isLoad() {
		return this instanceof Load;
	}
	
	/**
	 * Retorna a lista de n�s conectados a este
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
	 * Retorna a branch que conecta este node ao node passado como par�metro
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
	 * Retorna se este n� est� ligado
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
	 * Retorna a posi��o X deste n� no ambiente
	 * @return {@link Integer}
	 */
	public Integer getX() {
		return x;
	}

	/**
	 * Retorna a posi��o Y deste n� no ambiente
	 * @return {@link Integer}
	 */
	public Integer getY() {
		return y;
	}

	/**
	 * Retorna um {@link Set<Branch>} com os branches ligados neste n�
	 * @return {@link Set<Branch>}
	 */
	public Set<Branch> getBranches() {
		return branches;
	}
	
	/**
	 * Retorna o n�mero deste n�
	 * @return {@link Integer}
	 */
	public Integer getNodeNumber() {
		return nodeNumber;
	}

	/**
	 * Retorna a pot�ncia ativa deste n�
	 * @return double
	 */
	public double getActivePower() {
		return activePower;
	}
	
	/**
	 * Retorna a pot�ncia reativa deste n�
	 * @return double
	 */
	public double getReactivePower() {
		return reactivePower;
	}

	/**
	 * Retorna o {@link Status} deste n�, on ou off
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
