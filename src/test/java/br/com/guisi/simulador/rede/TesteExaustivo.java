package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;

public class TesteExaustivo {

	public static void main(String[] args) {
		Environment environment = loadEnvironment();
		
		List<Cluster> clusters = environment.getClusters();
		
		Map<Integer, List<Map<Integer, SwitchStatus>>> combinationsMap = new HashMap<>();
		
		int i = 0;
		for (Cluster cluster : clusters) {
			i++;

			if (i != 2 && i != 3) continue;
			
			List<Map<Integer, SwitchStatus>> list = new ArrayList<>();
			//todos fechados
			final Map<Integer, SwitchStatus> closedSwitchMap = cluster.getSwitchesMap();
			closedSwitchMap.keySet().forEach(key -> closedSwitchMap.put(key, SwitchStatus.CLOSED));
			list.add(closedSwitchMap);
			
			for (Branch branch : cluster.getSwitches()) {
				final Map<Integer, SwitchStatus> switchMap = cluster.getSwitchesMap();
				switchMap.keySet().forEach(key -> switchMap.put(key, SwitchStatus.CLOSED));
				switchMap.put(branch.getNumber(), SwitchStatus.OPEN);
				list.add(switchMap);
			}
			
			combinationsMap.put(cluster.getNumber(), list);
		}
		
		Set<Entry<Integer, List<Map<Integer, SwitchStatus>>>> entrySet = combinationsMap.entrySet();
		
		for (Entry<Integer, List<Map<Integer, SwitchStatus>>> entry : entrySet) {
			List<Map<Integer, SwitchStatus>> lst = entry.getValue();
			for (Map<Integer, SwitchStatus> map : lst) {
				System.out.println(entry.getKey() + ": " + map);
			}
			System.out.println();
		}
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
