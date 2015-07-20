package br.com.guisi.simulador.rede.enviroment;


public class Node {

	private final int environmentSize;
	private final int loadNum;
	private final double loadPower;

	public Node(int environmentSize, int loadNum, double powerLoad) {
		this.environmentSize = environmentSize;
		this.loadNum = loadNum;
		this.loadPower = powerLoad;
	}

	public int getLoadNum() {
		return loadNum;
	}

	public double getLoadPower() {
		return loadPower;
	}

	public int getX() {
		return Math.floorMod(loadNum-1, environmentSize);
	}

	public int getY() {
		return Math.floorDiv(loadNum-1, environmentSize);
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
