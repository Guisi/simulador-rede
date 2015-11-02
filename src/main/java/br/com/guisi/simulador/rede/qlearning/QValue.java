package br.com.guisi.simulador.rede.qlearning;


/**
 * Classe representando o valor de recompensa para um estado/ação do agente
 * 
 * @author Guisi
 *
 */
public class QValue {

	private final QKey qKey;
	private double reward;
	
	public QValue(QKey qKey) {
		this.qKey = qKey;
	}

	public QKey getQKey() {
		return qKey;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((qKey == null) ? 0 : qKey.hashCode());
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
		QValue other = (QValue) obj;
		if (qKey == null) {
			if (other.qKey != null)
				return false;
		} else if (!qKey.equals(other.qKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QValue [qKey=" + qKey + ", reward=" + reward + "]";
	}
	
}
