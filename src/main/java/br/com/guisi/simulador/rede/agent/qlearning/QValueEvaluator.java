package br.com.guisi.simulador.rede.agent.qlearning;

public class QValueEvaluator {

	private QValue qValue;
	private Integer distance;

	public QValue getQValue() {
		return qValue;
	}
	public void setQValue(QValue qValue) {
		this.qValue = qValue;
	}
	public Integer getDistance() {
		return distance;
	}
	public void setDistance(Integer distance) {
		this.distance = distance;
	}
	
	public double getReward() {
		return qValue.getReward(); //TODO verificar peso da distância
	}
}
