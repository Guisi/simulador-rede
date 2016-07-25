package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
			result = result.stream().filter(lst -> lst.stream().filter(st -> st.getStatus() == SwitchStatus.OPEN).count() <= 2).collect(Collectors.toSet());
			
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
		
		BestClusterConfiguration bestConfiguration = new BestClusterConfiguration();
		
		Set<List<ClusterCombination>> result = Sets.cartesianProduct(listas);
		System.out.println("Total de combinações: " + result.size());
		System.out.println();
		
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(1000);
        config.setMaxTotal(1000);
        EnvironmentPool pool = new EnvironmentPool(new EnvironmentFactory(environment), config);
		
        Counter counter = new Counter();
        
		result.parallelStream().forEach(list -> {
		//for (List<ClusterCombination> list : result) {
			//Environment env = SerializationUtils.clone(environment);
			try {
				Environment env = pool.borrowObject();
			
				list.forEach(combination -> {
					combination.getSwitches().entrySet().forEach(entry -> {
						Branch branch = env.getBranch(entry.getKey());
						branch.setSwitchStatus(entry.getValue());
					});
				});
				
				boolean isRadial = executePowerFlow(env);
				
				if (isRadial) {
					double percentage = env.getSuppliedActivePowerPercentage();
					bestConfiguration.setBestConfiguration(percentage, list);
				}
				
				counter.increment();
				
				pool.returnObject(env);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		System.out.println();
		System.out.println("FINISHED!!!!!!!!!!!");
		System.out.println();
		System.out.println("Best % Supplied Active Power: " + bestConfiguration.getMaxPercentage());
		System.out.println("Best configuration: " + bestConfiguration.getBestConfiguration());
		
		System.out.println();
		System.out.println("Tempo: " + counter.getTempo() + " ms");
		
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
		//File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.xlsx");
		File f = new File("C:/Users/p9924018/Desktop/Pesquisa/modelo-zidan.xlsx");
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

class BestClusterConfiguration {
	
	private double maxPercentage = 0d;
	private List<ClusterCombination> bestConfiguration = null;

	public double getMaxPercentage() {
		return maxPercentage;
	}

	public List<ClusterCombination> getBestConfiguration() {
		return bestConfiguration;
	}

	public synchronized void setBestConfiguration(double percentage, List<ClusterCombination> bestConfiguration) {
		if (percentage > this.maxPercentage) {
			this.maxPercentage = percentage;
			this.bestConfiguration = bestConfiguration;
			
			System.out.println("Best % Supplied Active Power: " + maxPercentage);
			System.out.println("Best configuration: " + bestConfiguration);
			System.out.println();
		}
	}
}