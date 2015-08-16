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

import org.apache.commons.lang3.StringUtils;

import br.com.guisi.simulador.rede.constants.Status;
import br.com.guisi.simulador.rede.constants.SupplyStatus;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;



public class EnvironmentUtils {

	private EnvironmentUtils() {}
	
	public static Environment getEnvironmentFromFile(File csvFile) throws Exception {
		
		List<String> lines = Files.readAllLines(Paths.get(csvFile.getAbsolutePath()), Charset.forName("ISO-8859-1"));
		
		//carrega os loads e feeders
		Map<Integer, NetworkNode> nodeMap = new HashMap<>();
		List<String> loadLines = getLoadLines(lines);
		for (String line : loadLines) {
			String[] colunas = line.split(";");
			
			//tipo, feeder ou load
			boolean isLoad = "L".equalsIgnoreCase(colunas[0]);
			
			//numero da carga
			Integer nodeNum = Integer.valueOf(colunas[1]);
			
			//posicao X
			Integer x = Integer.valueOf(colunas[2]);
			
			//posicao Y
			Integer y = Integer.valueOf(colunas[3]);
			
			//potencia
			double power = Double.parseDouble(colunas[4]);
			
			double minPower = StringUtils.isNotBlank(colunas[5]) ? Double.parseDouble(colunas[5]) : 0;
			
			double maxPower = StringUtils.isNotBlank(colunas[6]) ? Double.parseDouble(colunas[6]) : 0;
			
			//prioridade
			int priority = 0;
			if (isLoad) {
				 priority = Integer.valueOf(colunas[7]);
			}
			
			String feederColor = StringUtils.isNotBlank(colunas[8]) ? colunas[8] : "#FFFFFF";
			String loadColor = StringUtils.isNotBlank(colunas[9]) ? colunas[9] : "#FFFFFF";
			
			boolean statusValue = Integer.parseInt(colunas[10]) == 1;
			Status status = statusValue ? Status.ON : Status.OFF;
			
			NetworkNode node;
			if (isLoad) {
				node = new Load(nodeNum, x, y, power, status, priority);
			} else {
				node = new Feeder(nodeNum, x, y, power, minPower, maxPower, feederColor, loadColor, status);
			}
			nodeMap.put(nodeNum, node);
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
			NetworkNode node1 = nodeMap.get(loadFrom);
			
			//numero da carga para
			Integer loadTo = Integer.parseInt(colunas[2]);
			NetworkNode node2 = nodeMap.get(loadTo);
			
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
		environment.getNetworkNodeMap().values().forEach((node) -> {
			if (node.isLoad()) {
				Load load = (Load) node;
				Feeder feeder = getFeeder(load);
				load.setFeeder(feeder);
				if (feeder == null) {
					load.setSupplyStatus(SupplyStatus.NOT_SUPPLIED_NO_FEEDER_CONNECTED);
					//msgs.append("O load " + load.getLoadNum() + " não está ligado a nenhum feeder.").append("\n");
				}
			}
		});
		
		if (msgs.length() == 0) {
			validatePowerSupply(environment);
		}
		
		return msgs.toString();
	}
	
	/**
	 * Valida se existe algum ciclo fechado na rede
	 * @param environment
	 * @throws IllegalStateException
	 */
	private static String validateRadialState(Environment environment) {
		StringBuilder msgs = new StringBuilder();
		
		environment.getNetworkNodeMap().values().forEach((node) -> {
			try {
				validateRadialStateRecursive(node, node, null, new ArrayList<>());
				
				if (node.isFeeder()) {
					Feeder feeder = (Feeder) node;
					checkIfExistsConnectedFeeders(feeder, node, null);
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
	private static void validateRadialStateRecursive(NetworkNode observedLoad, NetworkNode connectedLoad, NetworkNode lastConnectedLoad, List<NetworkNode> checkedLoads) {
		checkedLoads.add(connectedLoad);

		List<NetworkNode> connectedNodes = connectedLoad.getConnectedNodes();
		connectedNodes.remove(lastConnectedLoad);
		
		for (NetworkNode networkNode : connectedNodes) {
			if (checkedLoads.contains(networkNode)) {
				throw new IllegalStateException("Existe algum ciclo fechado no qual o " + (observedLoad.isFeeder() ? "feeder " : "load ") + observedLoad.getNodeNumber() + " está incluso.");
			}
			
			validateRadialStateRecursive(observedLoad, networkNode, connectedLoad, checkedLoads);
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
	private static void checkIfExistsConnectedFeeders(Feeder observedFeeder, NetworkNode connectedLoad, NetworkNode lastConnectedLoad) throws IllegalStateException {
		List<NetworkNode> connectedNodes = connectedLoad.getConnectedNodes();
		connectedNodes.remove(lastConnectedLoad);

		connectedNodes.forEach((node) -> {
			if (node.isFeeder()) {
				throw new IllegalStateException("Os feeders " + observedFeeder.getNodeNumber() + " e " + node.getNodeNumber() + " estão conectados.");
			}
			checkIfExistsConnectedFeeders(observedFeeder, node, connectedLoad);
		});
	}
	
	/**
	 * Recupera o feeder do load
	 * @param environment
	 * @throws IllegalStateException
	 */
	public static Feeder getFeeder(NetworkNode networkNode) throws IllegalStateException {
		return (Feeder) searchFeederRecursive(networkNode, networkNode, null);
	}
	
	/**
	 * Procura pelo feeder do load recursivamente
	 * @param observedLoad
	 * @param connectedLoad
	 * @param lastConnectedLoad
	 * @return
	 * @throws IllegalStateException
	 */
	private static NetworkNode searchFeederRecursive(NetworkNode observedLoad, NetworkNode connectedLoad, NetworkNode lastConnectedLoad) throws IllegalStateException {
		List<NetworkNode> connectedNodes = connectedLoad.getConnectedNodes();
		connectedNodes.remove(lastConnectedLoad);

		for (NetworkNode networkNode : connectedNodes) {
			if (networkNode.isFeeder()) {
				return networkNode;
			}

			NetworkNode feeder = searchFeederRecursive(observedLoad, networkNode, connectedLoad);
			if (feeder != null) {
				return feeder;
			}
		}
		return null;
	}
	
	/**
	 * Retorna uma lista onde o primeiro item é o load para o qual está sendo retornado a rota
	 * e o último item é o feeder
	 * @param networkNode
	 * @return
	 * @throws IllegalStateException
	 */
	public static List<NetworkNode> getRouteToFeeder(NetworkNode networkNode) {
		return searchRouteToFeederRecursive(networkNode, networkNode, null);
	}
	
	/**
	 * Com base na rota entre o load e o feeder, calcula a potência que pode ser atendida
	 * levando em conta a capacidade máxima dos branches e a potência dos loads existentes
	 * no meio desta rota
	 * @param networkNode
	 * @return
	 */
	public static double getSupportedPower(NetworkNode networkNode) {
		List<NetworkNode> route = getRouteToFeeder(networkNode);

		double loadsPower = 0;
		double discountedPower = 0;
		for (int i = 0; i < route.size(); i++) {
			NetworkNode node = route.get(i);
			
			if (node.isLoad()) {
				loadsPower += node.getPower();
			}
		
			if (i < route.size() - 1) {
				NetworkNode nextNode = route.get(i+1);
				Branch branch = node.getBranch(nextNode);
				double branchPower = branch.getBranchPower();
				if (branchPower < loadsPower) {
					discountedPower = Math.max(discountedPower, loadsPower - branchPower);
				}
			}
		}
		return Math.max(0, networkNode.getPower() - discountedPower);
	}
	
	/**
	 * Monta a rota até o feeder recursivamente
	 * @param observedLoad
	 * @param connectedLoad
	 * @param lastConnectedLoad
	 * @return
	 * @throws IllegalStateException
	 */
	private static List<NetworkNode> searchRouteToFeederRecursive(NetworkNode observedLoad, NetworkNode connectedLoad, NetworkNode lastConnectedLoad) {
		List<NetworkNode> route = new ArrayList<>();
		route.add(connectedLoad);
		
		List<NetworkNode> connectedNodes = connectedLoad.getConnectedNodes();
		connectedNodes.remove(lastConnectedLoad);

		for (NetworkNode networkNode : connectedNodes) {
			if (networkNode.isFeeder()) {
				route.add(networkNode);
				return route;
			}

			List<NetworkNode> childrenRoute = searchRouteToFeederRecursive(observedLoad, networkNode, connectedLoad);
			if (childrenRoute != null) {
				route.addAll(childrenRoute);
				return route;
			}
		}

		return null;
	}
	
	/**
	 * Para cada feeder, navega pelas árvores de loads atualizando o status de atendimento
	 * @param environment
	 */
	public static void validatePowerSupply(Environment environment) {
		List<Feeder> feeders = environment.getFeeders();
		
		//para cada feeder
		feeders.forEach((feeder) -> {
			
			//monta lista de loads de acordo com a ordem de atendimento
			List<NetworkNode> allConnectedNodes = new ArrayList<>();
			List<NetworkNode> observedNodes = new ArrayList<>();
			observedNodes.add(feeder);
			do {
				allConnectedNodes.addAll(observedNodes);
	
				List<NetworkNode> connectedNodes = new ArrayList<>();
				observedNodes.forEach((observedNode) -> {
					List<NetworkNode> nodes = observedNode.getConnectedNodes();
					nodes.removeAll(allConnectedNodes);
					allConnectedNodes.addAll(nodes);
					connectedNodes.addAll(nodes);
				});
				
				observedNodes = new ArrayList<>();
				for (NetworkNode connectedNode : connectedNodes) {
					observedNodes.addAll(connectedNode.getConnectedNodes());
				}
				observedNodes.removeAll(allConnectedNodes);
			} while (!observedNodes.isEmpty());
			
			//remove o feeder da lista
			allConnectedNodes.remove(feeder);
			
			//distribui potência do feeder nos loads
			double feederPower = feeder.getPower();
			for (NetworkNode node : allConnectedNodes) {
				Load load = (Load) node;
				
				//só calcula para loads on
				if (load.isOn()) {
					//se ainda existe capacidade disponível no feeder
					if (feederPower > 0) {
						double receivedPower;
						double loadPower = load.getPower();
						
						//se o feeder ainda tem potência para atender o load totalmente
						if (loadPower <= feederPower) {
							load.setSupplyStatus(SupplyStatus.SUPPLIED);
							receivedPower = loadPower;
						} else {
							//senão atribui o que resta de potência no feeder para atendimento parcial do load
							load.setSupplyStatus(SupplyStatus.PARTIALLY_SUPPLIED_FEEDER_EXCEEDED);
							receivedPower = feederPower;
						}
						
						//mesmo que o feeder consiga atender o load, é preciso validar se os branches suportam atender
						double supportedPower = getSupportedPower(load);
						if (supportedPower < receivedPower) {
							load.setSupplyStatus(supportedPower > 0 ? SupplyStatus.PARTIALLY_SUPPLIED_BRANCH_EXCEEDED : SupplyStatus.NOT_SUPPLIED_BRANCH_EXCEEDED);
						}
						supportedPower = Math.min(receivedPower, supportedPower);
						load.setReceivedPower(supportedPower);
						
						feederPower -= supportedPower;
					} else {
						load.setSupplyStatus(SupplyStatus.NOT_SUPPLIED_FEEDER_EXCEEDED);
					}
				}
			}
		});
	}
	
	public static void main(String[] args) {
		File f = new File("C:/Users/Guisi/Desktop/modelo.csv");
		Environment environment = null;
		
		try {
			environment = EnvironmentUtils.getEnvironmentFromFile(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (environment != null) {
			EnvironmentUtils.validateEnvironment(environment);
		}
	}
}
