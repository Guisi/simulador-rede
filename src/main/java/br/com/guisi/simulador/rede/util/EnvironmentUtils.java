package br.com.guisi.simulador.rede.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import br.com.guisi.simulador.rede.constants.Status;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.BranchKey;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;
import br.com.guisi.simulador.rede.enviroment.SwitchDistance;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;


public class EnvironmentUtils {

	private EnvironmentUtils() {}
	
	public static Environment getEnvironmentFromFile(File csvFile) throws Exception {
		try (XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(csvFile))) {
			XSSFSheet ws = wb.getSheetAt(0);

			//carrega os loads e feeders
			Map<Integer, NetworkNode> nodeMap = new HashMap<>();
			List<XSSFRow> loadLines = getLoadLines(ws);
			for (XSSFRow line : loadLines) {
				int col = 1;
				
				//tipo, feeder ou load
				boolean isLoad = "L".equalsIgnoreCase(getStringCellValue(line.getCell(col++)));
				
				//numero da carga
				Integer nodeNum = getIntegerCellValue(line.getCell(col++));
				
				//posicao X
				Integer x = getIntegerCellValue(line.getCell(col++));
				
				//posicao Y
				Integer y = getIntegerCellValue(line.getCell(col++));
				
				//potencia ativa
				double activePower = getDoubleCellValue(line.getCell(col++));
				
				//potencia reativa
				double reactivePower = getDoubleCellValue(line.getCell(col++));
				
				//prioridade
				int priority = 0;
				if (isLoad) {
					priority = getIntegerCellValue(line.getCell(col));
				}
				col++;
				
				String color = getStringCellValue(line.getCell(col++));
				String feederColor = StringUtils.isNotBlank(color) ? color : "#FFFFFF";
				
				color = getStringCellValue(line.getCell(col++));
				String loadColor = StringUtils.isNotBlank(color) ? color : "#FFFFFF";
				
				boolean statusValue = getIntegerCellValue(line.getCell(col++)) == 1;
				Status status = statusValue ? Status.ON : Status.OFF;
				
				NetworkNode node;
				if (isLoad) {
					node = new Load(nodeNum, x, y, activePower, reactivePower, status, priority);
				} else {
					node = new Feeder(nodeNum, x, y, activePower, reactivePower, feederColor, loadColor, status);
				}
				nodeMap.put(nodeNum, node);

			}
			
			//carrega os branches
			Map<Integer, Branch> branchMap = new HashMap<>();
			List<XSSFRow> branchLines = getBranchLines(ws);
			for (XSSFRow line : branchLines) {
				int col = 1;
				
				//numero do branch
				Integer branchNum = getIntegerCellValue(line.getCell(col++));
				
				//numero da carga de
				Integer loadFrom = getIntegerCellValue(line.getCell(col++));
				NetworkNode node1 = nodeMap.get(loadFrom);
				
				//numero da carga para
				Integer loadTo = getIntegerCellValue(line.getCell(col++));
				NetworkNode node2 = nodeMap.get(loadTo);
				
				//corrent maxima
				double maxCurrent = getDoubleCellValue(line.getCell(col++));
				
				//resistencia
				double resistance = getDoubleCellValue(line.getCell(col++));
				
				//reatancia
				double reactance = getDoubleCellValue(line.getCell(col++));
				
				//status do branch
				int branchStatus = getIntegerCellValue(line.getCell(col++));

				boolean switchBranch = getIntegerCellValue(line.getCell(col++)) == 1;
				
				boolean fault = getIntegerCellValue(line.getCell(col++)) == 1;
				
				SwitchStatus switchStatus = fault ? SwitchStatus.FAULT
						: branchStatus == 0 ? SwitchStatus.OPEN : SwitchStatus.CLOSED;
				
				Branch branch = new Branch(branchNum, node1, node2, maxCurrent, resistance, reactance, switchStatus, switchBranch, fault);
				branchMap.put(branchNum, branch);
				
				//adiciona a branch nos dois loads os quais ela conecta
				node1.addBranch(branch);
				node2.addBranch(branch);
			}
			
			int sizeX = nodeMap.values().stream().max(Comparator.comparing(node -> node.getX())).get().getX();
			int sizeY = nodeMap.values().stream().max(Comparator.comparing(node -> node.getY())).get().getY();

			return new Environment(sizeX, sizeY, nodeMap, branchMap);
		}
	}
	
	private static List<XSSFRow> getLoadLines(XSSFSheet ws) {
		return getLines("-- Loads/Feeders --", ws);
	}
	
	private static List<XSSFRow> getBranchLines(XSSFSheet ws) {
		return getLines("-- Branches --", ws);
	}
	
	private static List<XSSFRow> getLines(String label, XSSFSheet ws) {
		List<XSSFRow> filteredLines = new ArrayList<XSSFRow>();

		int loadsStartRow = 0;
		int rowNum = ws.getLastRowNum();
		for (int i = 0; i <= rowNum; i++) {
			XSSFRow row = ws.getRow(i);
	        
			if (row != null) {
				XSSFCell cell = row.getCell(1);
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING
						&& label.equals(cell.getStringCellValue())) {
					loadsStartRow = i + 2; //pula cabeçalho;
					break;
				}
			}
		}
		
		if (loadsStartRow > 0) {
			XSSFRow row = ws.getRow(loadsStartRow);
			while (row != null && row.getCell(1).getCellType() != Cell.CELL_TYPE_BLANK) {
				filteredLines.add(row);
				row = ws.getRow(++loadsStartRow);
			}
		}
		
		return filteredLines;
	}
	
	private static String getStringCellValue(Cell cell) {
		return String.valueOf(getCellValue(cell));
	}
	
	private static Double getDoubleCellValue(Cell cell) {
		Object cellValue = getCellValue(cell);
		if (cellValue instanceof Double) {
			return (Double) cellValue;
		} else {
			return Double.valueOf((String) cellValue);
		}
	}
	
	private static Integer getIntegerCellValue(Cell cell) {
		return getDoubleCellValue(cell).intValue();
	}
	
	private static Object getCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}
		
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING: return cell.getStringCellValue();
			case Cell.CELL_TYPE_NUMERIC: return cell.getNumericCellValue();
			default: return null;
		}
	}
	
	/**
	 * Atualiza informações das conexões entre os elementos da rede
	 * @param environment
	 */
	public static void updateEnvironmentConnections(Environment environment) {
		//zera valores consolidados do feeder
		environment.getFeeders().forEach((feeder) -> {
			feeder.getServedLoads().clear();
		});
		
		environment.getLoads().forEach(load -> load.setFeeder(null));
		
		environment.getBranchFromToMap().clear();
		
		//atualiza feeders dos loads, e nodes from/to dos branches
		environment.getFeeders().forEach(feeder -> {
			feeder.getBranches().forEach(branch -> {
				updateEnvironmentConnectionsRecursive(environment, feeder, branch, feeder, 0);
			});
		});
	}
	
	private static void updateEnvironmentConnectionsRecursive(Environment environment, Feeder feeder, Branch branch, NetworkNode lastNetworkNode, int switchIndex) {
		if (branch.isClosed()) {
			
			if (branch.isSwitchBranch()) {
				branch.setSwitchIndex(++switchIndex);
			}
			
			for (NetworkNode networkNode : branch.getConnectedNodes()) {
				if (!lastNetworkNode.equals(networkNode)) {
					
					BranchKey branchKey = new BranchKey(lastNetworkNode, networkNode);
					branch.setBranchKey(branchKey);
					environment.getBranchFromToMap().put(branchKey, branch);
					
					if (networkNode.isLoad()) {
						Load load = (Load) networkNode;
						load.setFeeder(feeder);
						if (!feeder.getServedLoads().contains(load)) {
							feeder.getServedLoads().add(load);
						}
					}
		
					for (Branch connectedBranch : networkNode.getBranches()) {
						if (!connectedBranch.equals(branch)) {
							updateEnvironmentConnectionsRecursive(environment, feeder, connectedBranch, networkNode, switchIndex);
						}
					};
				}
			};
		}
	}
	
	/**
	 * Valida se existe algum ciclo fechado na rede
	 * @param environment
	 */
	public static List<NonRadialNetworkException> validateRadialState(Environment environment) {
		List<NonRadialNetworkException> exceptions = new ArrayList<>();
		
		environment.getNetworkNodeMap().values().forEach((node) -> {
			try {
				node.getBranches().forEach(branch -> {
					validateRadialStateRecursive(node, branch, node, new ArrayList<>());
				});
				
				if (node.isFeeder()) {
					Feeder feeder = (Feeder) node;
					feeder.getBranches().forEach(branch -> {
						checkIfExistsConnectedFeedersRecursive(feeder, branch, feeder);
					});
				}
			} catch (NonRadialNetworkException e) {
				exceptions.add(e);
			}
		});
		
		return exceptions;
	}
	
	/**
	 * Navega recursivamente na rede validando ciclos fechados
	 * @param observedNode
	 * @param branch
	 * @param lastNetworkNode
	 * @param checkedNodes
	 */
	private static void validateRadialStateRecursive(NetworkNode observedNode, Branch branch, NetworkNode lastNetworkNode, List<NetworkNode> checkedNodes) {
		checkedNodes.add(lastNetworkNode);
		if (branch.isClosed()) {
			branch.getConnectedNodes().forEach(networkNode -> {
				if (!lastNetworkNode.equals(networkNode)) {
					
					if (checkedNodes.contains(networkNode)) {
						String msg = "Existe algum ciclo fechado no qual o " + (observedNode.isFeeder() ? "feeder " : "load ") + observedNode.getNodeNumber() + " está incluso.";
						throw new NonRadialNetworkException(msg, observedNode);
					}
					
					networkNode.getBranches().forEach(connectedBranch -> {
						if (!connectedBranch.equals(branch)) {
							validateRadialStateRecursive(observedNode, connectedBranch, networkNode, checkedNodes);
						}
					});
				}
			});
		}
	}
	
	/**
	 * Verifica se existe algum feeder conectado a outro dentro da rede,
	 * ou seja, nesse caso alguns loads estariam sendo alimentados por mais de um feeder
	 * @param observedFeeder
	 * @param branch
	 * @param lastNetworkNode
	 * @throws IllegalStateException
	 */
	private static void checkIfExistsConnectedFeedersRecursive(Feeder observedFeeder, Branch branch, NetworkNode lastNetworkNode) throws IllegalStateException {
		if (branch.isClosed()) {
			branch.getConnectedNodes().forEach(networkNode -> {
				if (!lastNetworkNode.equals(networkNode)) {
					if (networkNode.isFeeder()) {
						throw new NonRadialNetworkException("Os feeders " + observedFeeder.getNodeNumber() + " e " + networkNode.getNodeNumber() + " estão conectados.", observedFeeder);
					}
					
					networkNode.getBranches().forEach(connectedBranch -> {
						if (!connectedBranch.equals(branch)) {
							checkIfExistsConnectedFeedersRecursive(observedFeeder, connectedBranch, networkNode);
						}
					});
				}
			});
		}
	}
	
	public static void isolateFaultSwitches(Environment environment) {
		environment.getFaults().forEach((branch) -> isolateNextSwitchesRecursive(branch, null));
	}
	
	private static void isolateNextSwitchesRecursive(Branch branch, NetworkNode lastNetworkNode) {
		branch.getConnectedNodes().forEach((networkNode) -> {
			if (lastNetworkNode == null || !lastNetworkNode.equals(networkNode)) {
				networkNode.getBranches().forEach((connectedBranch) -> {
					if (!connectedBranch.equals(branch)) {
						if (connectedBranch.isSwitchBranch()) {
							connectedBranch.isolateSwitch();
						} else {
							isolateNextSwitchesRecursive(connectedBranch, networkNode);
						}
					}
				});
			}
		});
	}

	/**
	 * Monta uma lista com as distâncias entre o switch passado como parâmetro e os demais switches com o estado passado
	 * @param branch
	 * @param switchStatus
	 * @return
	 */
	public static List<SwitchDistance> getSwitchesDistances(Branch branch, SwitchStatus switchStatus) {
		List<Branch> visitedBranches = new ArrayList<>();
		visitedBranches.add(branch);
		
		List<SwitchDistance> switchesDistances = getSwitchesDistancesRecursive(branch, 0, visitedBranches, new ArrayList<NetworkNode>(), switchStatus);
		Collections.sort(switchesDistances);
		
		return switchesDistances;
	}

	/**
	 * @param branch
	 * @param distance
	 * @param visitedBranches
	 * @param visitedNetworkNodes
	 * @param switchStatus
	 * @return
	 */
	private static List<SwitchDistance> getSwitchesDistancesRecursive(Branch branch, int distance, 
			List<Branch> visitedBranches, List<NetworkNode> visitedNetworkNodes, SwitchStatus switchStatus) {

		distance++;
		List<SwitchDistance> closestSwitches = new ArrayList<>();
		for (NetworkNode networkNode : branch.getConnectedNodes()) {
			
			//se ainda não visitou este node
			if (!visitedNetworkNodes.contains(networkNode)) {
				visitedNetworkNodes.add(networkNode);
				
				for (Branch connectedBranch : networkNode.getBranches()) {

					//verifica se ainda não visitou este branch
					if (!visitedBranches.contains(connectedBranch)) {
						visitedBranches.add(connectedBranch);

						//se encontrou o switch conforme o estado, adiciona na lista
						if (connectedBranch.isSwitchBranch() && connectedBranch.getSwitchStatus() == switchStatus) {
							closestSwitches.add(new SwitchDistance(distance, connectedBranch));
						}
						
						//continua navegação até que chega ao final do ramo da rede
						List<SwitchDistance> lst = getSwitchesDistancesRecursive(connectedBranch, distance, 
								new ArrayList<Branch>(visitedBranches), new ArrayList<NetworkNode>(visitedNetworkNodes), switchStatus);
						
						//para cada sw retornado, verifica se já não existe um switch distance com distância menor
						for (SwitchDistance switchDistance : lst) {
							int index = closestSwitches.indexOf(switchDistance);
							if (index >= 0) {
								SwitchDistance closestSw = closestSwitches.get(index);
								if (switchDistance.getDistance() < closestSw.getDistance()) {
									closestSwitches.remove(index);
									closestSwitches.add(switchDistance);
								}
							} else {
								closestSwitches.add(switchDistance);
							}
						}
					}
				}
			}
		}
		return closestSwitches;
	}
	
	public static int countDifferentSwitchStates(Environment environment1, Environment environment2) {
		int count = 0;
		for (Branch branch : environment1.getBranches()) {
			Branch otherBranch = environment2.getBranch(branch.getNumber());
			if (!branch.getSwitchStatus().equals(otherBranch.getSwitchStatus())) {
				count++;
			}
		};
		return count;
	}
	
	public static void main(String[] args) {
		File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.xlsx");
		Environment environment = null;
		
		try {
			environment = EnvironmentUtils.getEnvironmentFromFile(f);
			
			EnvironmentUtils.updateEnvironmentConnections(environment);
			
			Branch branch = environment.getBranch(18);
			
			System.out.println(branch.getSwitchIndex());
			
			/*long ini = System.currentTimeMillis();
			List<SwitchDistance> switchesDistances = getSwitchesDistances(branch, SwitchStatus.OPEN);
			long fim = System.currentTimeMillis();
			System.out.println("Tempo: " + (fim-ini));
			
			for (SwitchDistance switchDistance : switchesDistances) {
				System.out.println("Switch: " + switchDistance.getTheSwitch().getNumber() + " - Distance: " + switchDistance.getDistance());
			}*/
			
			//EnvironmentUtils.validateEnvironment(environment);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
