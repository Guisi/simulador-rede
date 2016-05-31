package br.com.guisi.simulador.rede.agent.qlearning.v1;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

public class CandidateSwitch implements Comparable<CandidateSwitch> {

	private Integer distance;
	private Integer switchNumber;
	private SwitchStatus switchStatus;
	
	public CandidateSwitch(Integer distance, Integer switchNumber, SwitchStatus switchStatus) {
		super();
		this.distance = distance;
		this.switchNumber = switchNumber;
		this.switchStatus = switchStatus;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public Integer getSwitchNumber() {
		return switchNumber;
	}

	public void setSwitchNumber(Integer switchNumber) {
		this.switchNumber = switchNumber;
	}

	public SwitchStatus getSwitchStatus() {
		return switchStatus;
	}

	public void setSwitchStatus(SwitchStatus switchStatus) {
		this.switchStatus = switchStatus;
	}

	@Override
	public int compareTo(CandidateSwitch o) {
		return distance.compareTo(o.getDistance());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((switchNumber == null) ? 0 : switchNumber.hashCode());
		result = prime * result + ((switchStatus == null) ? 0 : switchStatus.hashCode());
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
		CandidateSwitch other = (CandidateSwitch) obj;
		if (switchNumber == null) {
			if (other.switchNumber != null)
				return false;
		} else if (!switchNumber.equals(other.switchNumber))
			return false;
		if (switchStatus != other.switchStatus)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
