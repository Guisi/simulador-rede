package br.com.guisi.simulador.rede.enviroment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PriorityUtils;

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
	private final List<Load> loads;
	private final List<Feeder> feeders;
	private final List<Branch> branches;
	private final List<Branch> switches;
	private final List<Branch> faults;
	private Map<BranchKey, Branch> branchFromToMap;
	
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
	public Branch getRandomSwitch() {
		return getRandomSwitch(null);
	}
	
	public Branch getRandomSwitch(SwitchStatus switchStatus) {
		List<Branch> lst = switchStatus == null ? switches : switches.stream().filter((sw) -> sw.getSwitchState() == switchStatus).collect(Collectors.toList());
		return lst.isEmpty() ? null : lst.get(RANDOM.nextInt(lst.size()));
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
	public Branch getBranch(NetworkNode nodeFrom, NetworkNode nodeTo) {
		return branchFromToMap.get(new BranchKey(nodeFrom, nodeTo));
	}
	
	/**
	 * Retorna o switch mais próximo com o estado passado
	 * @param switchStatus
	 * @return
	 */
	public List<SwitchDistance> getSwitchesDistances(Branch currentSwitch, SwitchStatus switchStatus) {
		return EnvironmentUtils.getSwitchesDistances(currentSwitch, switchStatus);
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
	
	public Map<BranchKey, Branch> getBranchFromToMap() {
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
	 * Retorna o percentual de perda de potência ativa com base na potência ativa em uso 
	 * @return
	 */
	public double getActivePowerLostPercentage() {
		double activePowerLost = getActivePowerLostMW();
		double suppliedActivePower = getSuppliedActivePowerDemandMW();
		BigDecimal value = suppliedActivePower > 0 ? new BigDecimal(activePowerLost / suppliedActivePower * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		return value.doubleValue();
	}
	
	/**
	 * Retorna a soma da demanda de potência ativa de todos os loads atendidos da rede
	 * @return
	 */
	public double getSuppliedActivePowerDemandMW() {
		return loads.stream().filter((load) -> load.isSupplied()).mapToDouble((load) -> load.getActivePowerMW()).sum();
	}

	/**
	 * Retorna o percentual de potência atendida em relação ao total de demanda da rede
	 * @return
	 */
	public double getSuppliedActivePowerPercentage() {
		double suppliedActivePower = getSuppliedActivePowerDemandMW();
		Double totalActivePowerDemand = getTotalActivePowerDemandMW();
		BigDecimal value = totalActivePowerDemand.doubleValue() > 0 ? new BigDecimal(suppliedActivePower / totalActivePowerDemand * 100).setScale(5, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		return value.doubleValue();
	}
	
	/**
	 * Retorna a soma da demanda de potência ativa de todos os loads não atendidos da rede
	 * @return
	 */
	public double getNotSuppliedActivePowerDemandMW() {
		return loads.stream().filter((load) -> load.isOn() && !load.isSupplied()).mapToDouble((load) -> load.getActivePowerMW()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência ativa de todos os loads desligados da rede
	 * @return
	 */
	public double getOutOfServiceActivePowerDemandMW() {
		return loads.stream().filter((load) -> !load.isOn()).mapToDouble((load) -> load.getActivePowerMW()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência ativa de todos os loads da rede
	 * @return
	 */
	public double getTotalActivePowerDemandMW() {
		return loads.stream().mapToDouble((load) -> load.getActivePowerMW()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência reativa de todos os loads atendidos da rede
	 * @return
	 */
	public double getSuppliedReactivePowerDemandMVar() {
		return loads.stream().filter((load) -> load.isSupplied()).mapToDouble((load) -> load.getReactivePowerMVar()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência reativa de todos os loads não atendidos da rede
	 * @return
	 */
	public double getNotSuppliedReactivePowerDemandMVar() {
		return loads.stream().filter((load) -> load.isOn() && !load.isSupplied()).mapToDouble((load) -> load.getReactivePowerMVar()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência reativa de todos os loads desligados da rede
	 * @return
	 */
	public double getOutOfServiceReactivePowerDemandMVar() {
		return loads.stream().filter((load) -> !load.isOn()).mapToDouble((load) -> load.getReactivePowerMVar()).sum();
	}
	
	/**
	 * Retorna a soma da demanda de potência reativa de todos os loads da rede
	 * @return
	 */
	public double getTotalReactivePowerDemandMVar() {
		return loads.stream().mapToDouble((load) -> load.getReactivePowerMVar()).sum();
	}
	
	/**
	 * Retorna a soma das prioridades dos loads atendidos
	 * @return
	 */
	public double getSuppliedLoadsVsPriority() {
		return loads.stream().filter((load) -> load.isSupplied()).mapToDouble((load) -> PriorityUtils.getPriorityValue(load.getPriority()) ).sum();
	}
	
	/**
	 * Retorna a soma das prioridades dos loads não atendidos
	 * @return
	 */
	public double getNotSuppliedLoadsVsPriority() {
		return loads.stream().filter((load) -> !load.isSupplied()).mapToDouble((load) -> PriorityUtils.getPriorityValue(load.getPriority()) ).sum();
	}
	
	/**
	 * Retorna a soma das prioridades dos loads atendidos x potência ativa MW
	 * @return
	 */
	public double getSuppliedLoadsActivePowerMWVsPriority() {
		return loads.stream().filter((load) -> load.isSupplied()).mapToDouble((load) -> load.getActivePowerMW() * PriorityUtils.getPriorityValue(load.getPriority()) ).sum();
	}
	
	/**
	 * Retorna a soma das prioridades dos loads não atendidos x potência ativa MW
	 * @return
	 */
	public double getNotSuppliedLoadsActivePowerMWVsPriority() {
		return loads.stream().filter((load) -> !load.isSupplied()).mapToDouble((load) -> load.getActivePowerMW() * PriorityUtils.getPriorityValue(load.getPriority()) ).sum();
	}
	
	/**
	 * Retorna o menor valor de corrente em PU entre os loads atendidos
	 * @return
	 */
	public double getMinLoadCurrentVoltagePU() {
		List<Load> suppliedLoads = loads.stream().filter((load) -> load.isOn() && load.getFeeder() != null).collect(Collectors.toList());
		return suppliedLoads.isEmpty() ? 0d : suppliedLoads.stream().min(Comparator.comparing(load -> load.getCurrentVoltagePU())).get().getCurrentVoltagePU();
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