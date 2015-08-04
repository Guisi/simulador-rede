package br.com.guisi.simulador.rede.enviroment;

import java.util.HashSet;
import java.util.Set;

import br.com.guisi.simulador.rede.constants.NodeType;

public class Load {

	private final NodeType nodeType;
	private final Integer loadNum;
	private Integer feeder; 
	private final Integer x;
	private final Integer y;
	private final double loadPower;
	private final int loadPriority;
	private final String feederColor;
	private final String loadColor;
	private Set<Branch> branches = new HashSet<>();

	public Load(NodeType nodeType, Integer loadNum, Integer feeder, Integer x, Integer y, double powerLoad, int loadPriority, String feederColor, String loadColor) {
		this.nodeType = nodeType;
		this.loadNum = loadNum;
		this.feeder = feeder;
		this.x = x;
		this.y = y;
		this.loadPower = powerLoad;
		this.loadPriority = loadPriority;
		this.feederColor = feederColor;
		this.loadColor = loadColor;
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

	public Integer getFeeder() {
		return feeder;
	}

	public void setFeeder(Integer feeder) {
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

	public Set<Branch> getBranches() {
		return branches;
	}
	
	public String getFeederColor() {
		return feederColor;
	}

	public String getLoadColor() {
		return loadColor;
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
