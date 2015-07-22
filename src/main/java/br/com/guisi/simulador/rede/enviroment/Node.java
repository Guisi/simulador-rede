package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.NodeType;


public class Node {

	private final NodeType nodeType;
	private final int loadNum;
	private final int x;
	private final int y;
	private final double loadPower;

	public Node(NodeType nodeType, int loadNum, int x, int y, double powerLoad) {
		this.nodeType = nodeType;
		this.loadNum = loadNum;
		this.x = x;
		this.y = y;
		this.loadPower = powerLoad;
	}

	public int getLoadNum() {
		return loadNum;
	}

	public double getLoadPower() {
		return loadPower;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public NodeType getNodeType() {
		return nodeType;
	}
	
	public boolean isFeeder() {
		return NodeType.FEEDER.equals(nodeType);
	}
	
	public boolean isLoad() {
		return NodeType.LOAD.equals(nodeType);
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
