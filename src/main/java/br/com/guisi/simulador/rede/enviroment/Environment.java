package br.com.guisi.simulador.rede.enviroment;

import java.util.Map;
import java.util.Set;

public class Environment {

	private final int sizeX;
	private final int sizeY;
	private final Map<Integer, Load> loadMap;
	private final Map<Integer, Branch> branchMap;
	
	public Environment(int sizeX, int sizeY, Map<Integer, Load> nodeMap, Map<Integer, Branch> branchMap) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.loadMap = nodeMap;
		this.branchMap = branchMap;
	}
	
	/**
	 * Valida se a rede esta configurada corretamente
	 * Por exemplo, se existe algum ciclo fechado ou isolamento
	 */
	private void validateEnvironment() {
		loadMap.values().forEach((entry) -> {
			
		});
		
		for (Load load : loadMap.values()) {
			if (load.isLoad()) {
				
			}
		}
	}
	
	private void checkFeederConnection(Load load) {
		Load feeder = null;
		
		Set<Load> connectedLoads = load.getConnectedLoads();
		for (Load connectedLoad : connectedLoads) {
			if (connectedLoad.isFeeder()) {
				feeder = connectedLoad;
				break;
			}
		}
		
		Load settedFeeder = loadMap.get(load.getFeeder());
		if (feeder == null || !feeder.equals(settedFeeder)) {
			throw new IllegalStateException("O feeder do load " + load.getLoadNum() + " é inválido.");
		}
	}
	
	public Load getLoad(Integer loadNum) {
		return loadMap.get(loadNum);
	}
	
	public Branch getBranch(Integer branchNum) {
		return branchMap.get(branchNum);
	}
	
	public Map<Integer, Load> getLoadMap() {
		return loadMap;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public Map<Integer, Branch> getBranchMap() {
		return branchMap;
	}
	
}