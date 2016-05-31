package br.com.guisi.simulador.rede.agent.qlearning.v2;

import java.util.List;

import br.com.guisi.simulador.rede.enviroment.Branch;

public class Cluster {

	private Integer tieSwitchNumber;
	private List<Branch> switches;
	
	public Integer getTieSwitchNumber() {
		return tieSwitchNumber;
	}
	
	public void setTieSwitchNumber(Integer tieSwitchNumber) {
		this.tieSwitchNumber = tieSwitchNumber;
	}
	
	public List<Branch> getSwitches() {
		return switches;
	}
	
	public void setSwitches(List<Branch> switches) {
		this.switches = switches;
	}
}
