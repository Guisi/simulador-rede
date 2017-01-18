package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.Matlab;
import br.com.guisi.simulador.rede.util.PowerFlow;

import com.google.common.collect.Sets;

public class TesteExaustivo {

	public static void main(String[] args) throws Exception {
		Environment environment = loadEnvironment();
		
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(100);
        config.setMaxTotal(100);
        EnvironmentPool pool = new EnvironmentPool(new EnvironmentFactory(environment), config);
		
		List<Branch> switches = environment.getSwitches();
		
		/*switches = switches.stream().filter(sw -> sw.isOpen()  
				|| (Arrays.asList(15, 7, 13, 22, 23, 26, 27, 29, 33, 60, 66, 63, 62, 56, 54).contains(sw.getNumber())) ).collect(Collectors.toList());*/
		
		switches = switches.stream().filter(sw -> !(Arrays.asList(2, 39, 18, 53, 54, 58, 45, 20, 30).contains(sw.getNumber())) ).collect(Collectors.toList());
		
		List<Set<SwitchState>> listas = new ArrayList<>();
		for (Branch sw : switches) {
			Set<SwitchState> states = new LinkedHashSet<>();
			states.add(new SwitchState(sw.getNumber(), SwitchStatus.OPEN));
			states.add(new SwitchState(sw.getNumber(), SwitchStatus.CLOSED));
			listas.add(states);
		}
		
		Set<List<SwitchState>> result = Sets.cartesianProduct(listas);
		System.out.println("Total de combinações: " + result.size());
		System.out.println();
		
		BestConfiguration best = new BestConfiguration();
		Counter counter = new Counter();
		
		result.parallelStream().forEach(list -> {
		//for (List<SwitchState> list : result) {
			
			try {
				Environment env = pool.borrowObject();
			
				//Predicate<SwitchState> predicate = sw -> sw.getStatus() == SwitchStatus.OPEN;
				//long count = list.stream().filter(predicate).count();
				
				//if (count < 8) {
				
					for (SwitchState switchState : list) {
						Branch branch = env.getBranch(switchState.getNumber());
						branch.setSwitchStatus(switchState.getStatus());
					}
		
					boolean isRadial = executePowerFlow(env);
					
					if (isRadial) {
						double percentage = env.getSuppliedActivePowerPercentage();
						best.setBestConfiguration(percentage, list);
					}
				//}
				
				counter.increment();
				
				pool.returnObject(env);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		//}
		
		System.out.println();
		System.out.println("FINISHED!!!!!!!!!!!");
		System.out.println();
		System.out.println("Best % Supplied Active Power: " + best.getMaxPercentage());
		System.out.println("Best configuration: " + best.getBestConfiguration());
		
		
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
			try {
				PowerFlow.execute(environment);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.out.println();
			}
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

class Counter {
	private long ini = System.currentTimeMillis();
	private int cont;

	public synchronized void increment() {
		cont++;
		
		if (cont % 1000 == 0) {
			System.out.println("Processou: " + cont);
			long tempo = System.currentTimeMillis() - ini;
			System.out.println("Tempo: " + tempo + " ms - Media: " + ((double)tempo)/(double)cont);
			System.out.println();
		}
	}
	
	public long getTempo() {
		return System.currentTimeMillis() - ini;
	}
}

class BestConfiguration {
	
	private double maxPercentage = 0d;
	private List<SwitchState> bestConfiguration = null;

	public double getMaxPercentage() {
		return maxPercentage;
	}
	
	public List<SwitchState> getBestConfiguration() {
		return bestConfiguration;
	}

	public synchronized void setBestConfiguration(double percentage, List<SwitchState> bestConfiguration) {
		if (percentage > this.maxPercentage) {
			this.maxPercentage = percentage;
			this.bestConfiguration = bestConfiguration;
			
			System.out.println("Best % Supplied Active Power: " + maxPercentage);
			System.out.println("Best configuration: " + bestConfiguration);
			System.out.println();
		}
	}
}

class SwitchState {
	private Integer number;
	private SwitchStatus status;
	
	public SwitchState(Integer number, SwitchStatus status) {
		super();
		this.number = number;
		this.status = status;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public SwitchStatus getStatus() {
		return status;
	}
	public void setStatus(SwitchStatus status) {
		this.status = status;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		SwitchState other = (SwitchState) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (status != other.status)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "[" + number + ", " + status + "]";
	}
}