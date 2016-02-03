package br.com.guisi.simulador.rede.enviroment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.guisi.simulador.rede.util.EnvironmentUtils;

/**
 * Classe representando um ambiente simulado de uma rede de energia elétrica
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
	private final List<Branch> faults;
	
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
		faults = new ArrayList<>();
		branchMap.values().forEach((branch) -> {
			this.branchFromToMap.put(branch.getBranchId(), branch);
			
			branches.add(branch);
			if (branch.isSwitchBranch()) {
				switches.add(branch);
			}
			
			if (branch.hasFault()) {
				faults.add(branch);
			}
		});
		
	}
	
	/**
	 * Retorna um switch aleatório
	 * @return
	 */
	public Branch getRandomSwitch(SwitchState switchState) {
		List<Branch> openSwitches = switches.stream().filter((sw) -> sw.getSwitchState() == switchState).collect(Collectors.toList());
		return openSwitches.isEmpty() ? null : openSwitches.get(RANDOM.nextInt(openSwitches.size()));
	}
	
	/**
	 * Retorna um branch com falta aleatoriamente
	 * @return
	 */
	public Branch getRandomFault() {
		return faults.isEmpty() ? null : faults.get(RANDOM.nextInt(faults.size()));
	}
	
	/**
	 * Inverte estado do switch
	 * 
	 * @param switchNumber
	 */
	public void reverseSwitch(Integer switchNumber) {
		Branch switchBranch = getBranch(switchNumber);
		switchBranch.reverse();
	}
	
	/**
	 * Retorna o {@link NetworkNode} pelo número
	 * @param networkNodeNumber
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNetworkNode(Integer networkNodeNumber) {
		return networkNodeMap.get(networkNodeNumber);
	}
	
	/**
	 * Retorna o {@link Load} pelo número
	 * @param loadNumber
	 * @return {@link Load}
	 */
	public Load getLoad(Integer loadNumber) {
		return (Load) networkNodeMap.get(loadNumber);
	}
	
	/**
	 * Retorna o {@link Feeder} pelo número
	 * @param feederNumber
	 * @return {@link Feeder}
	 */
	public Feeder getFeeder(Integer feederNumber) {
		return (Feeder) networkNodeMap.get(feederNumber);
	}
	
	/**
	 * Retorna o {@link Branch} pelo número
	 * @param branchNum
	 * @return {@link Branch}
	 */
	public Branch getBranch(Integer branchNum) {
		return branchMap.get(branchNum);
	}
	
	/**
	 * Retorna o {@link Branch} pelos números dos nodes de e para
	 * @param nodeFrom
	 * @param nodeTo
	 * @return
	 */
	public Branch getBranch(Integer nodeFrom, Integer nodeTo) {
		return branchFromToMap.get(new BranchId(nodeFrom, nodeTo));
	}
	
	/**
	 * Retorna o switch mais próximo com o estado passado
	 * @param switchState
	 * @return
	 */
	public List<SwitchDistance> getClosestSwitches(Branch currentSwitch, SwitchState switchState) {
		List<SwitchDistance> closestSwitches = EnvironmentUtils.getClosestSwitches(currentSwitch, switchState);
		//ordena lista para que os switches mais próximos fiquem no início da lista
		Collections.sort(closestSwitches);
		return closestSwitches;
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
	 * Retorna um {@link List<Branch>} com todas as branches que são switches
	 * @return {@link List<Branch>}
	 */
	public List<Branch> getSwitches() {
		return switches;
	}
	
	/**
	 * Retorna um {@link List<Branch>} com todas as branches que contém falta
	 * @return {@link List<Branch>}
	 */
	public List<Branch> getFaults() {
		return faults;
	}
	
	/**
	 * Retorna a soma da perda de potência ativa de todos os branches fechados
	 * @return
	 */
	public double getActivePowerLostMW() {
		return branches.stream().filter((branch) -> branch.isClosed()).mapToDouble((branch) -> branch.getActiveLossMW()).sum();
	}
	
	/**
	 * Retorna a soma da perda de potência reativa de todos os branches fechados
	 * @return
	 */
	public double getReactivePowerLostMVar() {
		return branches.stream().filter((branch) -> branch.isClosed()).mapToDouble((branch) -> branch.getReactiveLossMVar()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência ativa de todos os loads da rede
	 * @return
	 */
	public double getActivePowerDemandMW() {
		return loads.stream().filter((load) -> load.isOn()).mapToDouble((load) -> load.getActivePowerKW()).sum() / 1000;
	}
	
	/**
	 * Retorna a soma da demanda de potência reativa de todos os loads da rede
	 * @return
	 */
	public double getReactivePowerDemandMVar() {
		return loads.stream().filter((load) -> load.isOn()).mapToDouble((load) -> load.getReactivePowerKVar()).sum() / 1000;
	}

	/**
	 * Retorna verdadeiro se ambiente é válido para reconfiguração
	 * Passo 1: valida se existem switches abertos
	 * @return
	 */
	public boolean isValidForReconfiguration() {
		//valida se existem switches abertos, pois se todos os switches estiverem fechados, não há como reconfigurar
		boolean hasOpenSwitch = false;
		for (Branch switc : switches) {
			if (switc.isOpen()) {
				hasOpenSwitch = true;
				break;
			}
		}
		
		return hasOpenSwitch;
	}
}