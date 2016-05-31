package br.com.guisi.simulador.rede.agent.qlearning.v2;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.enviroment.SwitchStatus;

/**
 * Classe representando uma ação do agente
 * É representado pelo par: número da branch switch para onde o agente vai / status do switch após a interação do agente (aberto ou fechado)
 * 
 * @author Guisi
 */
public class AgentAction {

	private final Integer switchNumber;
	private final SwitchStatus switchStatus;
	
	public AgentAction(Integer switchNumber, SwitchStatus switchStatus) {
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
		AgentAction other = (AgentAction) obj;
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
