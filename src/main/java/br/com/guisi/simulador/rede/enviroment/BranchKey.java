package br.com.guisi.simulador.rede.enviroment;

public class BranchKey {
	
	private final NetworkNode nodeFrom;
	private final NetworkNode nodeTo;
	
	public BranchKey(NetworkNode nodeFrom, NetworkNode nodeTo) {
		super();
		this.nodeFrom = nodeFrom;
		this.nodeTo = nodeTo;
	}
	
	public NetworkNode getNodeFrom() {
		return nodeFrom;
	}

	public NetworkNode getNodeTo() {
		return nodeTo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeFrom == null) ? 0 : nodeFrom.hashCode());
		result = prime * result + ((nodeTo == null) ? 0 : nodeTo.hashCode());
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
		BranchKey other = (BranchKey) obj;
		if (nodeFrom == null) {
			if (other.nodeFrom != null)
				return false;
		} else if (!nodeFrom.equals(other.nodeFrom))
			return false;
		if (nodeTo == null) {
			if (other.nodeTo != null)
				return false;
		} else if (!nodeTo.equals(other.nodeTo))
			return false;
		return true;
	}
}