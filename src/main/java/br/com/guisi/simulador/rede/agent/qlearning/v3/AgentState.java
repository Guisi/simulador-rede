package br.com.guisi.simulador.rede.agent.qlearning.v3;

import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

/**
 * Classe representando a chave da QTable
 * 
 * @author Guisi
 */
public class AgentState {

	private final Integer clusterNumber;
	private final Map<Integer, SwitchStatus> switches;

	public AgentState(Integer clusterNumber, Map<Integer, SwitchStatus> switches) {
		super();
		this.clusterNumber = clusterNumber;
		this.switches = switches;
	}

	public Map<Integer, SwitchStatus> getSwitches() {
		return switches;
	}

	public Integer getClusterNumber() {
		return clusterNumber;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clusterNumber == null) ? 0 : clusterNumber.hashCode());
		result = prime * result + ((switches == null) ? 0 : switches.hashCode());
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
		AgentState other = (AgentState) obj;
		if (clusterNumber == null) {
			if (other.clusterNumber != null)
				return false;
		} else if (!clusterNumber.equals(other.clusterNumber))
			return false;
		if (switches == null) {
			if (other.switches != null)
				return false;
		} else if (!switches.equals(other.switches))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
