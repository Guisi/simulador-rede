package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.NodeType;


public class Node {

	private final NodeType nodeType;
	private final Integer loadNum;
	private Integer feeder; 
	private final Integer x;
	private final Integer y;
	private final double loadPower;

	public Node(NodeType nodeType, Integer loadNum, Integer feeder, Integer x, Integer y, double powerLoad) {
		this.nodeType = nodeType;
		this.loadNum = loadNum;
		this.feeder = feeder;
		this.x = x;
		this.y = y;
		this.loadPower = powerLoad;
	}

	public boolean isFeeder() {
		return NodeType.FEEDER.equals(nodeType);
	}
	
	public boolean isLoad() {
		return NodeType.LOAD.equals(nodeType);
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

	public double getLoadPower() {
		return loadPower;
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
		Node other = (Node) obj;
		if (loadNum != other.loadNum)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(loadNum: " + loadNum + ", x: " + getX() + ", y: " + getY() + ", loadPower: " + loadPower + ")";
	}
}
