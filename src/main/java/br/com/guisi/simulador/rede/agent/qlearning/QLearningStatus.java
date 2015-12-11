package br.com.guisi.simulador.rede.agent.qlearning;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;


public class QLearningStatus {

	private Integer initialState;
	private Integer lastState;
	private Integer currentState;
	private List<Integer> switchesChanged = new ArrayList<>();
	private boolean handled;
	
	public QLearningStatus() {
	}
	
	public QLearningStatus clone() {
		try {
			return (QLearningStatus) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
			return null;
		}
	}
	
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
	
	public List<Integer> getSwitchesChanged() {
		return switchesChanged;
	}
	public void setSwitchesChanged(List<Integer> switchesChanged) {
		this.switchesChanged = switchesChanged;
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
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
