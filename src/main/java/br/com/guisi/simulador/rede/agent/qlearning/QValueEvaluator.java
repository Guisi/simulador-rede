package br.com.guisi.simulador.rede.agent.qlearning;

public class QValueEvaluator {

	private QValue qValue;
	private Integer distance;
	private double rewardAdjustment;

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
	public double getRewardAdjustment() {
		return rewardAdjustment;
	}
	public void setRewardAdjustment(double rewardAdjustment) {
		this.rewardAdjustment = rewardAdjustment;
	}

	public double getReward() {
		return qValue.getReward() + rewardAdjustment; //TODO verificar peso da dist�ncia
	}
}
