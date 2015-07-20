package br.com.guisi.simulador.rede.enviroment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.guisi.simulador.rede.constants.BranchStatus;

public class Environment {

	private final int size;
	private final Map<Integer, Node> nodeMap = new HashMap<>();
	private final Map<Integer, Branch> branchMap = new HashMap<>();
	
	private Environment(int size) {
		this.size = size;
	}
	
	public static Environment getInstanceFromFile(List<String> lines) {
		//na primeira linha vem o tamanho do ambiente
		int size = Integer.parseInt(lines.get(0).split(";")[2]);
		Environment environment = new Environment(size);
		
		//a partir da quarta linha vem os dados da rede
		
		//primeiro carrega os loads
		for (int i = 3; i < lines.size(); i++) {
			String line = lines.get(i);
			
			String[] colunas = line.split(";");
			
			if (colunas.length > 2) {
				//numero da carga
				Integer loadNum = Integer.parseInt(colunas[1]);
				
				if (!environment.getNodeMap().containsKey(loadNum)) {
					//potencia
					double loadPower = Double.parseDouble(colunas[2]);
					
					Node node = new Node(size, loadNum, loadPower);
					environment.getNodeMap().put(loadNum, node);
				}
			}
		}
		
		//depois carrega as branchs
		for (int i = 3; i < lines.size(); i++) {
			String line = lines.get(i);
			
			String[] colunas = line.split(";");
		
			if (colunas.length > 6){
				//numero do branch
				Integer branchNum = Integer.parseInt(colunas[3]);
				
				//numero da carga de
				Integer loadFrom = Integer.parseInt(colunas[4]);
				Node node1 = environment.getNodeMap().get(loadFrom);
				
				//numero da carga para
				Integer loadTo = Integer.parseInt(colunas[5]);
				Node node2 = environment.getNodeMap().get(loadTo);
				
				//status do branch
				int branchStatus = Integer.parseInt(colunas[6]);
				
				BranchStatus status = branchStatus == 0 ? BranchStatus.OFF : BranchStatus.ON;
				Branch branch = new Branch(branchNum, node1, node2, status);
				environment.getBranchMap().put(branchNum, branch);
			}
		}
		
		return environment;
	}

	public Map<Integer, Node> getNodeMap() {
		return nodeMap;
	}

	public int getSize() {
		return size;
	}

	public Map<Integer, Branch> getBranchMap() {
		return branchMap;
	}
	
}