package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;

import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.Matlab;
import br.com.guisi.simulador.rede.util.PowerFlow;

import com.google.common.collect.Sets;

public class TesteExaustivoClusters {

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		long ini = System.currentTimeMillis();
		
		Environment environment = loadEnvironment();
		
		List<Cluster> clusters = environment.getClusters();
		
		List<Set<ClusterCombination>> listas = new ArrayList<>();
		
		for (Cluster cluster : clusters) {
			Set<ClusterCombination> clusterCombinations = new LinkedHashSet<>();
			
			//todos fechados
			final Map<Integer, SwitchStatus> closedSwitchMap = cluster.getSwitchesMap();
			//System.out.println("Cluster " + cluster.getNumber() + ": " + closedSwitchMap.keySet());
			closedSwitchMap.keySet().forEach(key -> closedSwitchMap.put(key, SwitchStatus.CLOSED));
			clusterCombinations.add(new ClusterCombination(cluster.getNumber(), closedSwitchMap));
			
			for (Branch branch : cluster.getSwitches()) {
				final Map<Integer, SwitchStatus> switchMap = cluster.getSwitchesMap();
				switchMap.keySet().forEach(key -> switchMap.put(key, SwitchStatus.CLOSED));
				switchMap.put(branch.getNumber(), SwitchStatus.OPEN);
				clusterCombinations.add(new ClusterCombination(cluster.getNumber(), switchMap));
			}
			
			listas.add(clusterCombinations);
			
			for (ClusterCombination clusterCombination : clusterCombinations) {
				System.out.println(clusterCombination);
			}
			System.out.println(clusterCombinations.size());
			System.out.println();
		}
		
		System.out.println();
		
		double maxPercentage = 0d;
		List<ClusterCombination> bestConfiguration = null;
		
		Set<List<ClusterCombination>> result = Sets.cartesianProduct(listas);
		System.out.println("Total de combinações: " + result.size());
		System.out.println();
		
		int i = 0;
		for (List<ClusterCombination> list : result) {
			list.forEach(combination -> {
				combination.getSwitches().entrySet().forEach(entry -> {
					Branch branch = environment.getBranch(entry.getKey());
					branch.setSwitchStatus(entry.getValue());
				});
			});
			
			boolean isRadial = executePowerFlow(environment);
			
			if (isRadial) {
				double percentage = environment.getSuppliedActivePowerPercentage();
				
				if (percentage > maxPercentage) {
					maxPercentage = percentage;
					bestConfiguration = list;
				}
			}
			System.out.println(++i);
		}
		
		System.out.println();
		System.out.println("Best % Supplied Active Power: " + maxPercentage);
		System.out.println("Best configuration: ");
		for (ClusterCombination clusterCombination : bestConfiguration) {
			System.out.println(clusterCombination);
		}
		
		System.out.println();
		System.out.println("Tempo: " + (System.currentTimeMillis() - ini) + " ms");
		
		Matlab.disconnectMatlabProxy();
	}
	
	private static boolean executePowerFlow(Environment environment) {
		//primeiro valida se rede está radial
		List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
		
		boolean isRadial = exceptions.isEmpty();
		
		if (isRadial) {
			//executa o fluxo de potência
			PowerFlow.execute(environment);
		}
		
		return isRadial;
	}
	
	private static Environment loadEnvironment() {
		File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.xlsx");
		//File f = new File("C:/Users/p9924018/Desktop/Pesquisa/modelo-zidan.xlsx");
		Environment environment = null;
		
		try {
			environment = EnvironmentUtils.getEnvironmentFromFile(f);
			
			//isola as faltas
			EnvironmentUtils.isolateFaultSwitches(environment);
			
			EnvironmentUtils.validateTieSwitches(environment);
			
			try {
				PowerFlow.execute(environment);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<Cluster> clusters = EnvironmentUtils.mountClusters(environment);
			environment.setClusters(clusters);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return environment;
	}
}

class ClusterCombination {
	
	private Integer clusterNumber;
	private Map<Integer, SwitchStatus> switches;

	public ClusterCombination(Integer clusterNumber, Map<Integer, SwitchStatus> switches) {
		super();
		this.clusterNumber = clusterNumber;
		this.switches = switches;
	}

	public Integer getClusterNumber() {
		return clusterNumber;
	}
	public void setClusterNumber(Integer clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	public Map<Integer, SwitchStatus> getSwitches() {
		return switches;
	}
	public void setSwitches(Map<Integer, SwitchStatus> switches) {
		this.switches = switches;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clusterNumber == null) ? 0 : clusterNumber.hashCode());
		result = prime * result + ((switches == null) ? 0 : switches.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusterCombination other = (ClusterCombination) obj;
		if (clusterNumber == null) {
			if (other.clusterNumber != null)
				return false;
		} else if (!clusterNumber.equals(other.clusterNumber))
			return false;
		if (switches == null) {
			if (other.switches != null)
				return false;
		} else if (!switches.equals(other.switches))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Cluster " + clusterNumber + ": " + switches;
	}
	
}