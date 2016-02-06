package br.com.guisi.simulador.rede.agent.control.impl;

import br.com.guisi.simulador.rede.agent.control.StoppingCriteria;

public class StepNumberStoppingCriteria implements StoppingCriteria {

	private Integer value;
	
	@Override
	public void setValue(Object value) {
		this.value = Integer.valueOf(value.toString());
	}
	
	@Override
	public boolean wasReached(Object... params) {
		Integer step = (Integer) params[0];
		return step >= value;
	}
	
	@Override
	public String toString() {
		return "Step number";
	}
}
