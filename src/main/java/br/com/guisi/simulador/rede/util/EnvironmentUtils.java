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

import br.com.guisi.simulador.rede.constants.BranchStatus;
import br.com.guisi.simulador.rede.constants.NodeType;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Node;

public class EnvironmentUtils {

	private EnvironmentUtils() {}
	
	public static Environment getEnvironmentFromFile(File csvFile) throws Exception {
		
		List<String> lines = Files.readAllLines(Paths.get(csvFile.getAbsolutePath()), Charset.forName("ISO-8859-1"));
		
		//carrega os loads e feeders
		Map<Integer, Node> nodeMap = new HashMap<>();
		List<String> loadLines = getLoadLines(lines);
		for (String line : loadLines) {
			String[] colunas = line.split(";");
			
			//tipo, feeder ou load
			NodeType nodeType = "F".equals(colunas[0]) ? NodeType.FEEDER : NodeType.LOAD;
			
			//feeder do load
			Integer feeder = null;
			if (nodeType.equals(NodeType.LOAD)) {
				feeder = Integer.valueOf(colunas[1]);
			}
			
			//numero da carga
			Integer loadNum = Integer.valueOf(colunas[2]);
			
			//posicao X
			Integer x = Integer.valueOf(colunas[3]);
			
			//posicao Y
			Integer y = Integer.valueOf(colunas[4]);
			
			//potencia
			double loadPower = Double.parseDouble(colunas[5]);
			
			Node node = new Node(nodeType, loadNum, feeder, x, y, loadPower);
			
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
			Node node1 = nodeMap.get(loadFrom);
			
			//numero da carga para
			Integer loadTo = Integer.parseInt(colunas[2]);
			Node node2 = nodeMap.get(loadTo);
			
			//potencia maxima
			double branchPower = Double.parseDouble(colunas[3]);
			
			//status do branch
			int branchStatus = Integer.parseInt(colunas[4]);
			
			BranchStatus status = branchStatus == 0 ? BranchStatus.OFF : BranchStatus.ON;
			Branch branch = new Branch(branchNum, node1, node2, branchPower, status);
			branchMap.put(branchNum, branch);
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
}
