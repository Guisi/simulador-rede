package br.com.guisi.simulador.rede.agent.status;

import br.com.guisi.simulador.rede.enviroment.SwitchState;

public class SwitchOperation {

	private final Integer switchNumber;
	private final SwitchState switchState;
	
	public SwitchOperation(Integer switchNumber, SwitchState switchState) {
		super();
		this.switchNumber = switchNumber;
		this.switchState = switchState;
	}

	public Integer getSwitchNumber() {
		return switchNumber;
	}
	
	public SwitchState getSwitchState() {
		return switchState;
	}
	
}
