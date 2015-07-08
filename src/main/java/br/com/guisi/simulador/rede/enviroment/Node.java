package br.com.guisi.simulador.rede.enviroment;

public class Node {

	private final int x;
	private final int y;
	private final double powerLoad;

	public Node(int x, int y, double powerLoad) {
		this.x = x;
		this.y = y;
		this.powerLoad = powerLoad;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(x: " + x + ", y: " + y + ", powerLoad: " + powerLoad + ")";
	}
}
