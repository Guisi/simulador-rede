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
}
