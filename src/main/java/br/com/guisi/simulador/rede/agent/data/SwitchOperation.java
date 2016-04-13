package br.com.guisi.simulador.rede.agent.data;

import java.io.Serializable;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

public class SwitchOperation implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Integer switchNumber;
	private final SwitchStatus switchStatus;
	
	public SwitchOperation(Integer switchNumber, SwitchStatus switchStatus) {
		super();
		this.switchNumber = switchNumber;
		this.switchStatus = switchStatus;
	}

	public Integer getSwitchNumber() {
		return switchNumber;
	}
	
	public SwitchStatus getSwitchState() {
		return switchStatus;
	}
	
}
