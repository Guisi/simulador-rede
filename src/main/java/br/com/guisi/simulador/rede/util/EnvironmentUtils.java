package br.com.guisi.simulador.rede.util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.constants.NodeType;
import br.com.guisi.simulador.rede.constants.Status;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Load;



public class EnvironmentUtils {

	private EnvironmentUtils() {}
	
	public static Environment getEnvironmentFromFile(File csvFile) throws Exception {
		
		List<String> lines = Files.readAllLines(Paths.get(csvFile.getAbsolutePath()), Charset.forName("ISO-8859-1"));
		
		//carrega os loads e feeders
		Map<Integer, Load> nodeMap = new HashMap<>();
		List<String> loadLines = getLoadLines(lines);
		for (String line : loadLines) {
			String[] colunas = line.split(";");
			
			//tipo, feeder ou load
			NodeType nodeType = "F".equals(colunas[0]) ? NodeType.FEEDER : NodeType.LOAD;
			boolean isLoad = nodeType.equals(NodeType.LOAD);
			
			//numero da carga
			Integer loadNum = Integer.valueOf(colunas[1]);
			
			//posicao X
			Integer x = Integer.valueOf(colunas[2]);
			
			//posicao Y
			Integer y = Integer.valueOf(colunas[3]);
			
			//potencia
			double loadPower = Double.parseDouble(colunas[4]);
			
			double loadMinPower = StringUtils.isNotBlank(colunas[5]) ? Double.parseDouble(colunas[5]) : 0;
			
			double loadMaxPower = StringUtils.isNotBlank(colunas[6]) ? Double.parseDouble(colunas[6]) : 0;
			
			//prioridade
			int loadPriority = 0;
			if (isLoad) {
				 loadPriority = Integer.valueOf(colunas[7]);
			}
			
			String feederColor = StringUtils.isNotBlank(colunas[8]) ? colunas[8] : "#FFFFFF";
			String loadColor = StringUtils.isNotBlank(colunas[9]) ? colunas[9] : "#FFFFFF";
			
			boolean loadStatus = Integer.parseInt(colunas[10]) == 1;
			Status status = loadStatus ? Status.ON : Status.OFF;
			
			Load node = new Load(nodeType, loadNum, x, y, loadPower, loadMinPower, loadMaxPower, loadPriority, feederColor, loadColor, status);
			
			nodeMap.put(loadNum, node);
		}
		
		//carrega os branches
		Map<Integer, Branch> branchMap = new HashMap<>();
		List<String> branchLines = getBranchLines(lines);
		for (String line : branchLines) {
			String[] colunas = line.split(";");
			
			//numero do branch
			Integer branchNum = Integer.parseInt(colunas[0]);
			
			//numero da carga de
			Integer loadFrom = Integer.parseInt(colunas[1]);
			Load node1 = nodeMap.get(loadFrom);
			
			//numero da carga para
			Integer loadTo = Integer.parseInt(colunas[2]);
			Load node2 = nodeMap.get(loadTo);
			
			//potencia maxima
			double branchPower = Double.parseDouble(colunas[3]);
			
			//distancia
			double distance = Double.parseDouble(colunas[4]);
			
			//status do branch
			int branchStatus = Integer.parseInt(colunas[5]);
			Status status = branchStatus == 0 ? Status.OFF : Status.ON;
			
			boolean switchBranch = Integer.parseInt(colunas[6]) == 1;
			
			Branch branch = new Branch(branchNum, node1, node2, branchPower, distance, status, switchBranch);
			branchMap.put(branchNum, branch);
			
			//adiciona a branch nos dois loads os quais ela conecta
			node1.addBranch(branch);
			node2.addBranch(branch);
		}
		
		int sizeX = nodeMap.values().stream().max(Comparator.comparing(node -> node.getX())).get().getX();
		int sizeY = nodeMap.values().stream().max(Comparator.comparing(node -> node.getY())).get().getY();
		
		return new Environment(sizeX, sizeY, nodeMap, branchMap);
	}
	
	private static List<String> getLoadLines(List<String> lines) {
		return getLines("-- Loads/Feeders --", lines);
	}
	
	private static List<String> getBranchLines(List<String> lines) {
		return getLines("-- Branches --", lines);
	}
	
	private static List<String> getLines(String label, List<String> lines) {
		List<String> filteredLines = new ArrayList<String>();

		boolean found = false;
		for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
			String line = iterator.next();
			
			if (!found && line.contains(label)) {
				iterator.next();
				line = iterator.next();
				found = true;
			}
			
			if (found) {
				if (isEmptyLine(line)) {
				 break;	
				}
				filteredLines.add(line);
			}
		}
		
		return filteredLines;
	}
	
	private static boolean isEmptyLine(String line) {
		return StringUtils.replace(line, ";", "").isEmpty();
	}
	
	/**
	 * Valida se a rede esta configurada corretamente
	 * Por exemplo, se existe algum ciclo fechado ou isolamento
	 */
	public static String validateEnvironment(Environment environment) throws IllegalStateException {
		StringBuilder msgs = new StringBuilder();
		
		//primeiro valida se rede está radial
		msgs.append( validateRadialState(environment) );
		
		//depois, verifica se todos os loads estão conectados a algum feeder
		environment.getLoadMap().values().forEach((load) -> {
			if (load.isLoad()) {
				Load feeder = getFeeder(load);
				load.setFeeder(feeder);
				if (feeder == null) {
					msgs.append("O load " + load.getLoadNum() + " não está ligado a nenhum feeder.").append("\n");
				}
			}
		});
		
		return msgs.toString();
	}
	
	/**
	 * Valida se existe algum ciclo fechado na rede
	 * @param environment
	 * @throws IllegalStateException
	 */
	private static String validateRadialState(Environment environment) {
		StringBuilder msgs = new StringBuilder();
		
		environment.getLoadMap().values().forEach((load) -> {
			try {
				validateRadialStateRecursive(load, load, null, new ArrayList<>());
				
				if (load.isFeeder()) {
					checkIfExistsConnectedFeeders(load, load, null);
				}
			} catch (IllegalStateException e) {
				msgs.append(e.getMessage()).append("\n");
			}
		});
		
		return msgs.toString();
	}

	/**
	 * Navega recursivamente na rede validando ciclos fechados 
	 * @param observedLoad
	 * @param connectedLoad
	 * @param lastConnectedLoad
	 * @param checkedLoads
	 * @throws IllegalStateException
	 */
	private static void validateRadialStateRecursive(Load observedLoad, Load connectedLoad, Load lastConnectedLoad, List<Load> checkedLoads) {
		checkedLoads.add(connectedLoad);

		Set<Load> connectedLoads = connectedLoad.getConnectedLoads();
		connectedLoads.remove(lastConnectedLoad);
		
		for (Load load : connectedLoads) {
			if (checkedLoads.contains(load)) {
				throw new IllegalStateException("Existe algum ciclo fechado no qual o " + (observedLoad.isFeeder() ? "feeder " : "load ") + observedLoad.getLoadNum() + " está incluso.");
			}
			
			validateRadialStateRecursive(observedLoad, load, connectedLoad, checkedLoads);
		}
	}
	
	/**
	 * Verifica se existe algum feeder conectado a outro dentro da rede,
	 * ou seja, nesse caso alguns loads estariam sendo alimentados por mais de um feeder
	 * @param observedFeeder
	 * @param connectedLoad
	 * @param lastConnectedLoad
	 * @throws IllegalStateException
	 */
	private static void checkIfExistsConnectedFeeders(Load observedFeeder, Load connectedLoad, Load lastConnectedLoad) throws IllegalStateException {
		Set<Load> connectedLoads = connectedLoad.getConnectedLoads();
		connectedLoads.remove(lastConnectedLoad);

		connectedLoads.forEach((load) -> {
			if (load.isFeeder()) {
				throw new IllegalStateException("Os feeders " + observedFeeder.getLoadNum() + " e " + load.getLoadNum() + " estão conectados.");
			}
			checkIfExistsConnectedFeeders(observedFeeder, load, connectedLoad);
		});
	}
	
	/**
	 * Recupera o feeder do load
	 * @param environment
	 * @throws IllegalStateException
	 */
	public static Load getFeeder(Load load) throws IllegalStateException {
		return searchFeederRecursive(load, load, null);
	}
	
	/**
	 * Procura pelo feeder do load recursivamente
	 * @param observedLoad
	 * @param connectedLoad
	 * @param lastConnectedLoad
	 * @return
	 * @throws IllegalStateException
	 */
	private static Load searchFeederRecursive(Load observedLoad, Load connectedLoad, Load lastConnectedLoad) throws IllegalStateException {
		Set<Load> connectedLoads = connectedLoad.getConnectedLoads();
		connectedLoads.remove(lastConnectedLoad);

		for (Load load : connectedLoads) {
			if (load.isFeeder()) {
				return load;
			}

			Load feeder = searchFeederRecursive(observedLoad, load, connectedLoad);
			if (feeder != null) {
				return feeder;
			}
		}
		return null;
	}
}
