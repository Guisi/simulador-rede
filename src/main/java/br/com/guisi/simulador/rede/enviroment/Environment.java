package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Environment {

	private final int sizeX;
	private final int sizeY;
	private final Map<Integer, NetworkNode> networkNodeMap;
	private final Map<Integer, Branch> branchMap;
	
	public Environment(int sizeX, int sizeY, Map<Integer, NetworkNode> networkNodeMap, Map<Integer, Branch> branchMap) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.networkNodeMap = networkNodeMap;
		this.branchMap = branchMap;
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
		List<Feeder> feeders = new ArrayList<Feeder>();
		networkNodeMap.values().forEach((networkNode) -> {
			if (networkNode.isFeeder()) feeders.add((Feeder) networkNode);
		});
		return feeders;
	}
	
}