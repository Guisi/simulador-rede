package br.com.guisi.simulador.rede.agent.qlearning;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

public class Cluster implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer number;
	private Branch initialTieSwitch;
	private List<Branch> switches;
	
	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Branch getInitialTieSwitch() {
		return initialTieSwitch;
	}

	public void setInitialTieSwitch(Branch initialTieSwitch) {
		this.initialTieSwitch = initialTieSwitch;
	}

	public List<Branch> getSwitches() {
		return switches;
	}

	public void setSwitches(List<Branch> closedSwitches) {
		this.switches = closedSwitches;
	}
	
	public Map<Integer, SwitchStatus> getSwitchesMap() {
		Map<Integer, SwitchStatus> switchMap = new HashMap<>();
		switches.forEach(sw -> switchMap.put(sw.getNumber(), sw.getSwitchStatus()));
		return switchMap;
	}

}
