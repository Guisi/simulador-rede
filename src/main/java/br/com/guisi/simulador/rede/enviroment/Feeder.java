package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;


public class Feeder extends NetworkNode {

	private String feederColor;
	private String loadColor;
	private double minPower;
	private double maxPower;
	
	public Feeder(Integer nodeNumber, Integer x, Integer y, double power, double minPower, double maxPower, String feederColor, String loadColor, Status status) {
		super(nodeNumber, x, y, power, status);
		this.feederColor = feederColor;
		this.loadColor = loadColor;
		this.minPower = minPower;
		this.maxPower = maxPower;
	}

	public double getMinPower() {
		return minPower;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public String getFeederColor() {
		return feederColor;
	}

	public String getLoadColor() {
		return loadColor;
	}

	@Override
	public String toString() {
		return "Feeder [nodeNumber=" + nodeNumber + ", x=" + x + ", y=" + y + ", power=" + power + ", status=" + status 
				+ ", feederColor=" + feederColor + ", loadColor=" + loadColor + ", minPower=" + minPower + ", maxPower=" + maxPower + "]";
	}
}
