package br.com.guisi.simulador.rede.agent.control;

public interface StoppingCriteria {

	void setValue(Object value);
	
	boolean wasReached(Object... params);
}
