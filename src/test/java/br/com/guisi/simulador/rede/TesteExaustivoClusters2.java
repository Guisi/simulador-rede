package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

public class TesteExaustivoClusters2 {

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		long ini = System.currentTimeMillis();
		
		Environment environment = loadEnvironment();
		
		List<Cluster> clusters = environment.getClusters();
		
		List<Set<ClusterCombination>> listas = new ArrayList<>();
		
		for (Cluster cluster : clusters) {
			Set<ClusterCombination> clusterCombinations = new LinkedHashSet<>();
			
			List<Set<SwitchState>> list = new ArrayList<>();
			
			List<Branch> switches = cluster.getSwitches();
			for (Branch sw : switches) {
				Set<SwitchState> states = new LinkedHashSet<>();
				states.add(new SwitchState(sw.getNumber(), SwitchStatus.OPEN));
				states.add(new SwitchState(sw.getNumber(), SwitchStatus.CLOSED));
				list.add(states);
			}
			
			Set<List<SwitchState>> result = Sets.cartesianProduct(list);
			result = result.stream().filter(lst -> lst.stream().filter(st -> st.getStatus() == SwitchStatus.OPEN).count() <= 1).collect(Collectors.toSet());
			
			for (List<SwitchState> res : result) {
				Map<Integer, SwitchStatus> switchMap = new HashMap<>();
				res.forEach(r -> switchMap.put(r.getNumber(), r.getStatus()));
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
					
					System.out.println("Best % Supplied Active Power: " + maxPercentage);
					System.out.println("Best configuration: " + bestConfiguration);
					System.out.println();
				}
			}
			
			if (++i % 500 == 0) {
				System.out.println("Processou: " + i);
				System.out.println("Tempo: " + (System.currentTimeMillis() - ini) + " ms");
				System.out.println();
			}
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