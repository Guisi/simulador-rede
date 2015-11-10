package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Classe representando um ambiente simulado de uma rede de energia el�trica
 * 
 * @author douglas.guisi
 */
public class Environment {

	private final int sizeX;
	private final int sizeY;
	private final Map<Integer, NetworkNode> networkNodeMap;
	private final Map<Integer, Branch> branchMap;
	private final Map<BranchId, Branch> branchFromToMap;
	private final List<Load> loads;
	private final List<Feeder> feeders;
	private final List<Branch> branches;
	private final List<Branch> switches;
	
	private final Random RANDOM = new Random(System.currentTimeMillis());
	
	public Environment(int sizeX, int sizeY, Map<Integer, NetworkNode> networkNodeMap, Map<Integer, Branch> branchMap) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.networkNodeMap = networkNodeMap;
		this.branchMap = branchMap;
		this.branchFromToMap = new HashMap<>();
		
		loads = new ArrayList<Load>();
		feeders = new ArrayList<Feeder>();
		networkNodeMap.values().forEach((networkNode) -> {
			if (networkNode.isLoad()) {
				loads.add((Load) networkNode);
			} else {
				feeders.add((Feeder) networkNode);
			}
		});
		
		branches = new ArrayList<Branch>();
		switches = new ArrayList<Branch>();
		branchMap.values().forEach((branch) -> {
			this.branchFromToMap.put(branch.getBranchId(), branch);
			
			branches.add(branch);
			if (branch.isSwitchBranch()) {
				switches.add(branch);
			}
		});
	}
	
	/**
	 * Retorna um switch aleat�rio
	 * @return
	 */
	public Branch getRandomSwitch() {
		return switches.get(RANDOM.nextInt(switches.size()));
	}
	
	/**
	 * Indica a mudan�a do estado de um switch
	 * 
	 * @param switchNumber
	 * @param switchState
	 * @return verdadeiro se mudou o estado, falso se manteve o mesmo
	 */
	public boolean changeSwitchState(Integer switchNumber, SwitchState switchState) {
		Branch switchBranch = getBranch(switchNumber);
		boolean changed = switchBranch.getSwitchState().equals(switchState);
		switchBranch.setSwitchState(switchState);
		return changed;
	}
	
	/**
	 * Retorna o {@link NetworkNode} pelo n�mero
	 * @param networkNodeNumber
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNetworkNode(Integer networkNodeNumber) {
		return networkNodeMap.get(networkNodeNumber);
	}
	
	/**
	 * Retorna o {@link Load} pelo n�mero
	 * @param loadNumber
	 * @return {@link Load}
	 */
	public Load getLoad(Integer loadNumber) {
		return (Load) networkNodeMap.get(loadNumber);
	}
	
	/**
	 * Retorna o {@link Feeder} pelo n�mero
	 * @param feederNumber
	 * @return {@link Feeder}
	 */
	public Feeder getFeeder(Integer feederNumber) {
		return (Feeder) networkNodeMap.get(feederNumber);
	}
	
	/**
	 * Retorna o {@link Branch} pelo n�mero
	 * @param branchNum
	 * @return {@link Branch}
	 */
	public Branch getBranch(Integer branchNum) {
		return branchMap.get(branchNum);
	}
	
	/**
	 * Retorna o {@link Branch} pelos n�meros dos nodes de e para
	 * @param nodeFrom
	 * @param nodeTo
	 * @return
	 */
	public Branch getBranch(Integer nodeFrom, Integer nodeTo) {
		return branchFromToMap.get(new BranchId(nodeFrom, nodeTo));
	}
	
	public Branch getClosestSwitch() {
		//(Math.abs(x - goal.x) + Math.abs(y - goal.y))
		return null;
	}
	
	/**
	 * Returns a {@link Map<Integer, NetworkNode>} with all nodes
	 * @return {@link Map<Integer, NetworkNode>}
	 */
	public Map<Integer, NetworkNode> getNetworkNodeMap() {
		return networkNodeMap;
	}
	
	public List<NetworkNode> getNetworkNodes() {
		return new ArrayList<>(networkNodeMap.values());
	}

	/**
	 * Retorna o tamanho X do ambiente
	 * @return sizeX
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * Retorna o tamanho Y do ambiente
	 * @return sizeY
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * Retorna um {@link Map<Integer, Branch>} com todas as branches
	 * @return {@link Map<Integer, Branch>}
	 */
	public Map<Integer, Branch> getBranchMap() {
		return branchMap;
	}
	
	public Map<BranchId, Branch> getBranchFromToMap() {
		return branchFromToMap;
	}

	/**
	 * Retorna um {@link List<Feeder>} com todos os feeders
	 * @return {@link List<Feeder>}
	 */
	public List<Feeder> getFeeders() {
		return feeders;
	}
	
	/**
	 * Retorna um {@link List<Load>} com todos os loads
	 * @return {@link List<Load>}
	 */
	public List<Load> getLoads() {
		return loads;
	}

	/**
	 * Retorna um {@link List<Branch>} com todas as branches
	 * @return {@link List<Branch>}
	 */
	public List<Branch> getBranches() {
		return branches;
	}

	/**
	 * Retorna um {@link List<Branch>} com todas as branches que s�o switches
	 * @return {@link List<Branch>}
	 */
	public List<Branch> getSwitches() {
		return switches;
	}
}