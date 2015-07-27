package br.com.guisi.simulador.rede.enviroment;

import java.util.Map;

public class Environment {

	private final int sizeX;
	private final int sizeY;
	private final Map<Integer, Load> nodeMap;
	private final Map<Integer, Branch> branchMap;
	
	public Environment(int sizeX, int sizeY, Map<Integer, Load> nodeMap, Map<Integer, Branch> branchMap) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.nodeMap = nodeMap;
		this.branchMap = branchMap;
	}
	
	public Load getNode(Integer loadNum) {
		return nodeMap.get(loadNum);
	}
	
	public Branch getBranch(Integer branchNum) {
		return branchMap.get(branchNum);
	}
	
	public Map<Integer, Load> getNodeMap() {
		return nodeMap;
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