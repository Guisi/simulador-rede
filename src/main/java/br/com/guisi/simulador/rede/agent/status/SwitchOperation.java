package br.com.guisi.simulador.rede.agent.status;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

public class SwitchOperation {

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
