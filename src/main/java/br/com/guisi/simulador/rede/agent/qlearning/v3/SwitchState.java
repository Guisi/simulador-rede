package br.com.guisi.simulador.rede.agent.qlearning.v3;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

public class SwitchState {
	private Integer number;
	private SwitchStatus status;

	public SwitchState(Integer number, SwitchStatus status) {
		super();
		this.number = number;
		this.status = status;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public SwitchStatus getStatus() {
		return status;
	}

	public void setStatus(SwitchStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		SwitchState other = (SwitchState) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (status != other.status)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + number + ", " + status + "]";
	}
}