package br.com.guisi.simulador.rede.agent.qlearning.v2;

import java.io.Serializable;
import java.util.List;

import br.com.guisi.simulador.rede.enviroment.Branch;

public class Cluster implements Serializable {

	private static final long serialVersionUID = 1L;

	private Branch tieSwitch;
	private List<Branch> closedSwitches;
	
	public Branch getTieSwitch() {
		return tieSwitch;
	}

	public void setTieSwitch(Branch tieSwitch) {
		this.tieSwitch = tieSwitch;
	}

	public List<Branch> getClosedSwitches() {
		return closedSwitches;
	}

	public void setClosedSwitches(List<Branch> closedSwitches) {
		this.closedSwitches = closedSwitches;
	}

}
