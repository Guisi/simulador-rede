package br.com.guisi.simulador.rede.enviroment;

import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando um Feeder
 * 
 * @author douglas.guisi
 */
public class Feeder extends NetworkNode {

	private String feederColor;
	private String loadColor;
	private double minPower;
	private double maxPower;
	private double usedPower;
	
	public Feeder(Integer nodeNumber, Integer x, Integer y, double power, double minPower, double maxPower, String feederColor, String loadColor, Status status) {
		super(nodeNumber, x, y, power, status);
		this.feederColor = feederColor;
		this.loadColor = loadColor;
		this.minPower = minPower;
		this.maxPower = maxPower;
	}
	
	/**
	 * Retorna a pot�ncia dispon�vel deste feeder
	 * @return double
	 */
	public double getAvailablePower() {
		return power - usedPower;
	}
	
	/**
	 * Adiciona um valor de pot�ncia ao total em uso deste feeder
	 * @param usedPower
	 */
	public void addUsedPower(double usedPower) {
		this.usedPower += usedPower;
	}
	
	/**
	 * Retorna o valor de pot�ncia m�nimo deste feeder
	 * @return double
	 */
	public double getMinPower() {
		return minPower;
	}

	/**
	 * Retorna o valor de pot�ncia m�ximo deste feeder
	 * @return double
	 */
	public double getMaxPower() {
		return maxPower;
	}

	public String getFeederColor() {
		return feederColor;
	}

	public String getLoadColor() {
		return loadColor;
	}

	/**
	 * Retorna o total de pot�ncia em uso deste feeder
	 * @return double
	 */
	public double getUsedPower() {
		return usedPower;
	}

	public void setUsedPower(double usedPower) {
		this.usedPower = usedPower;
	}

	@Override
	public String toString() {
		return "Feeder [nodeNumber=" + nodeNumber + ", x=" + x + ", y=" + y + ", power=" + power + ", status=" + status 
				+ ", feederColor=" + feederColor + ", loadColor=" + loadColor + ", minPower=" + minPower + ", maxPower=" + maxPower + "]";
	}
}
