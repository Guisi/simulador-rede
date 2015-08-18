package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Environment {

	private final int sizeX;
	private final int sizeY;
	private final Map<Integer, NetworkNode> networkNodeMap;
	private final Map<Integer, Branch> branchMap;
	private final List<Load> loads;
	private final List<Feeder> feeders;
	
	public Environment(int sizeX, int sizeY, Map<Integer, NetworkNode> networkNodeMap, Map<Integer, Branch> branchMap) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.networkNodeMap = networkNodeMap;
		this.branchMap = branchMap;
		
		loads = new ArrayList<Load>();
		feeders = new ArrayList<Feeder>();
		networkNodeMap.values().forEach((networkNode) -> {
			if (networkNode.isLoad()) {
				loads.add((Load) networkNode);
			} else {
				feeders.add((Feeder) networkNode);
			}
		});
	}
	
	public NetworkNode getNetworkNode(Integer networkNodeNumber) {
		return networkNodeMap.get(networkNodeNumber);
	}
	
	public Load getLoad(Integer loadNumber) {
		return (Load) networkNodeMap.get(loadNumber);
	}
	
	public Feeder getFeeder(Integer feederNumber) {
		return (Feeder) networkNodeMap.get(feederNumber);
	}
	
	public Branch getBranch(Integer branchNum) {
		return branchMap.get(branchNum);
	}
	
	public Map<Integer, NetworkNode> getNetworkNodeMap() {
		return networkNodeMap;
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
	
	public List<Feeder> getFeeders() {
		return feeders;
	}
	
	public List<Load> getLoads() {
		return loads;
	}

	public long getLoadsSupplied() {
		return loads.stream().filter((load) -> load.isSupplied()).count();
	}
	
	public long getLoadsPartiallySupplied() {
		return loads.stream().filter((load) -> load.isPartiallySupplied()).count();
	}
	
	public long getLoadsNotSupplied() {
		return loads.stream().filter((load) -> load.isNotSupplied()).count();
	}
	
	public double getLoadPowerSupplied() {
		return loads.stream().mapToDouble((load) -> load.getPowerSupplied()).sum();
	}
	
	public double getLoadPowerNotSupplied() {
		return loads.stream().mapToDouble((load) -> load.getPowerNotSupplied()).sum();
	}
	
	public double getFeederUsedPower() {
		return feeders.stream().mapToDouble((feeder) -> feeder.getUsedPower()).sum();
	}
	
	public double getFeederAvailablePower() {
		return feeders.stream().mapToDouble((feeder) -> feeder.getAvailablePower()).sum();
	}
}