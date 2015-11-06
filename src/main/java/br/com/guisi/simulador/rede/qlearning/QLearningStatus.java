package br.com.guisi.simulador.rede.qlearning;


public class QLearningStatus implements Cloneable {

	private Integer initialState;
	private Integer lastState;
	private Integer currentState;

	public Integer getInitialState() {
		return initialState;
	}
	public void setInitialState(Integer initialState) {
		this.initialState = initialState;
	}
	public Integer getLastState() {
		return lastState;
	}
	public void setLastState(Integer lastState) {
		this.lastState = lastState;
	}
	public Integer getCurrentState() {
		return currentState;
	}
	public void setCurrentState(Integer currentState) {
		this.currentState = currentState;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentState == null) ? 0 : currentState.hashCode());
		result = prime * result + ((lastState == null) ? 0 : lastState.hashCode());
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
		QLearningStatus other = (QLearningStatus) obj;
		if (currentState == null) {
			if (other.currentState != null)
				return false;
		} else if (!currentState.equals(other.currentState))
			return false;
		if (lastState == null) {
			if (other.lastState != null)
				return false;
		} else if (!lastState.equals(other.lastState))
			return false;
		return true;
	}
	
}
