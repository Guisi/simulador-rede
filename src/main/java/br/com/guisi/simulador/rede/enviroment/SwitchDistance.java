package br.com.guisi.simulador.rede.enviroment;

public class SwitchDistance implements Comparable<SwitchDistance> {

	private Integer distance;
	private Branch theSwitch;
	
	public SwitchDistance(Integer distance, Branch theSwitch) {
		super();
		this.distance = distance;
		this.theSwitch = theSwitch;
	}

	public Integer getDistance() {
		return distance;
	}
	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	public Branch getTheSwitch() {
		return theSwitch;
	}
	public void setTheSwitch(Branch theSwitch) {
		this.theSwitch = theSwitch;
	}
	
	@Override
	public int compareTo(SwitchDistance o) {
		return distance.compareTo(o.getDistance());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((theSwitch == null) ? 0 : theSwitch.hashCode());
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
		SwitchDistance other = (SwitchDistance) obj;
		if (theSwitch == null) {
			if (other.theSwitch != null)
				return false;
		} else if (!theSwitch.equals(other.theSwitch))
			return false;
		return true;
	}
}
