package br.com.guisi.simulador.rede.enviroment;

import java.util.HashSet;
import java.util.Set;

import br.com.guisi.simulador.rede.constants.NodeType;
import br.com.guisi.simulador.rede.constants.Status;

public class Load {

	private final NodeType nodeType;
	private final Integer loadNum;
	private Load feeder; 
	private final Integer x;
	private final Integer y;
	private final double loadPower;
	private final double loadMinPower;
	private final double loadMaxPower;
	private final int loadPriority;
	private final String feederColor;
	private final String loadColor;
	private Status loadStatus;
	private Set<Branch> branches = new HashSet<>();

	public Load(NodeType nodeType, Integer loadNum, Integer x, Integer y, double loadPower, double loadMinPower, double loadMaxPower,
			int loadPriority, String feederColor, String loadColor, Status loadStatus) {
		this.nodeType = nodeType;
		this.loadNum = loadNum;
		this.x = x;
		this.y = y;
		this.loadPower = loadPower;
		this.loadMinPower = loadMinPower;
		this.loadMaxPower = loadMaxPower;
		this.loadPriority = loadPriority;
		this.feederColor = feederColor;
		this.loadColor = loadColor;
		this.loadStatus = loadStatus;
	}
	
	public void addBranch(Branch branch) {
		branches.add(branch);
	}
	
	public boolean isFeeder() {
		return NodeType.FEEDER.equals(nodeType);
	}
	
	public boolean isLoad() {
		return NodeType.LOAD.equals(nodeType);
	}
	
	public Set<Load> getConnectedLoads() {
		Set<Load> loads = new HashSet<>();
		branches.forEach((branch) -> {
			if (branch.isOn()) {
				loads.add(branch.getConnectedLoad(this));
			}
		});
		return loads;
	}
	
	public boolean isOn() {
		return Status.ON.equals(loadStatus);
	}

	public Load getFeeder() {
		return feeder;
	}

	public void setFeeder(Load feeder) {
		this.feeder = feeder;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public Integer getLoadNum() {
		return loadNum;
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	public int getLoadPriority() {
		return loadPriority;
	}

	public double getLoadPower() {
		return loadPower;
	}

	public double getLoadMinPower() {
		return loadMinPower;
	}

	public double getLoadMaxPower() {
		return loadMaxPower;
	}

	public Set<Branch> getBranches() {
		return branches;
	}
	
	public String getFeederColor() {
		return feederColor;
	}

	public String getLoadColor() {
		return loadColor;
	}

	public Status getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(Status loadStatus) {
		this.loadStatus = loadStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + loadNum;
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
		Load other = (Load) obj;
		if (loadNum != other.loadNum)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(loadNum: " + loadNum + ", x: " + getX() + ", y: " + getY() + ", loadPower: " + loadPower + ")";
	}
}
