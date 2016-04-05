package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando um Feeder
 * 
 * @author douglas.guisi
 */
public class Feeder extends NetworkNode {

	private static final long serialVersionUID = 1L;

	private String feederColor;
	private String loadColor;
	private List<Load> servedLoads;
	
	public Feeder(Integer nodeNumber, Integer x, Integer y, double activePower, double reactivePower, String feederColor, String loadColor, Status status) {
		super(nodeNumber, x, y, activePower, reactivePower, status);
		this.feederColor = feederColor;
		this.loadColor = loadColor;
		this.servedLoads = new ArrayList<>();
	}
	
	public String getFeederColor() {
		return feederColor;
	}

	public String getLoadColor() {
		return loadColor;
	}
	
	public boolean isPowerOverflow() {
		return getActivePowerMW() < getUsedActivePowerMW();
	}

	/**
	 * Retorna o total de potência em uso deste feeder
	 * @return double
	 */
	public double getUsedActivePowerMW() {
		double sum = servedLoads.stream().filter(load -> load.isOn() && load.isSupplied()).mapToDouble(load -> load.getActivePowerKW()).sum() / 1000;
		Set<Branch> branches = new HashSet<>();
		servedLoads.forEach(load -> branches.addAll(load.getBranches()));
		sum += branches.stream().filter(branch -> branch.isClosed()).mapToDouble(branch -> branch.getActiveLossMW()).sum();
		return sum;
	}
	
	public double getAvailableActivePowerMW() {
		return getActivePowerMW() - getUsedActivePowerMW();
	}

	/**
	 * Retorna a lista dos loads conectados a este feeder
	 * @return
	 */
	public List<Load> getServedLoads() {
		return servedLoads;
	}

	@Override
	public String toString() {
		return "Feeder [nodeNumber=" + nodeNumber + ", x=" + x + ", y=" + y + ", activePower=" + activePowerKW + ", reactivePower=" + reactivePowerKVar + ", status=" + status 
				+ ", feederColor=" + feederColor + ", loadColor=" + loadColor + "]";
	}
}
