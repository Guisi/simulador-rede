package br.com.guisi.simulador.rede.agent.qlearning.v1;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

/**
 * Classe representando a chave da QTable
 * � representado pelo par: n�mero da branch switch / status do switch (aberto ou fechado)
 * 
 * @author Guisi
 */
public class AgentState {

	private final Integer switchNumber;
	private final SwitchStatus switchStatus;
	
	public AgentState(Integer switchNumber, SwitchStatus switchStatus) {
		super();
		this.switchNumber = switchNumber;
		this.switchStatus = switchStatus;
	}

	public Integer getSwitchNumber() {
		return switchNumber;
	}

	public SwitchStatus getSwitchStatus() {
		return switchStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((switchNumber == null) ? 0 : switchNumber.hashCode());
		result = prime * result + ((switchStatus == null) ? 0 : switchStatus.hashCode());
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
		if (switchNumber == null) {
			if (other.switchNumber != null)
				return false;
		} else if (!switchNumber.equals(other.switchNumber))
			return false;
		if (switchStatus != other.switchStatus)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
