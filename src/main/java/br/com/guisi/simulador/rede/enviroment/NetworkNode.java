package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.guisi.simulador.rede.constants.Status;

public abstract class NetworkNode {

	protected Integer nodeNumber;
	protected Integer x;
	protected Integer y;
	protected double power;
	protected Status status;
	protected Set<Branch> branches = new HashSet<>();
	
	public NetworkNode(Integer nodeNumber, Integer x, Integer y, double power, Status status) {
		super();
		this.nodeNumber = nodeNumber;
		this.x = x;
		this.y = y;
		this.power = power;
		this.status = status;
	}

	public void addBranch(Branch branch) {
		branches.add(branch);
	}
	
	public boolean isFeeder() {
		return this instanceof Feeder;
	}
	
	public boolean isLoad() {
		return this instanceof Load;
	}
	
	/**
	 * Retorna os nodes conectados a este node
	 * @return
	 */
	public List<NetworkNode> getConnectedNodes() {
		List<NetworkNode> networkNodes = new ArrayList<>();
		branches.forEach((branch) -> {
			if (branch.isOn()) {
				networkNodes.add(branch.getConnectedLoad(this));
			}
		});
		return networkNodes;
	}
	
	/**
	 * Retorna a branch que conecta este node ao node passado como parâmetro
	 * @return
	 */
	public Branch getBranch(NetworkNode node) {
		for (Branch branch : branches) {
			if (branch.isOn() && branch.getConnectedLoad(this).equals(node)) {
				return branch;
			}
		}
		return null;
	}
	
	public boolean isOn() {
		return Status.ON.equals(status);
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	public Set<Branch> getBranches() {
		return branches;
	}
	
	public Integer getNodeNumber() {
		return nodeNumber;
	}

	public double getPower() {
		return power;
	}

	public Status getStatus() {
		return status;
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
