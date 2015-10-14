package br.com.guisi.simulador.rede.util;

import java.io.File;
import java.util.List;

import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;

public class PowerFlow {
	
	//Tensão de referencia em pu (VRef) (Fausto usou 1.02, vamos usar 1.0)
	private static final double TENSAO_REFERENCIA_PU = 1.0;
	
	//Potência de base (Sbase)
	private static final double POTENCIA_BASE = 1000000;
	
	//Tensão de base (VBase) 
	private static final double TENSAO_BASE = 11000;
	
	//Restrição máxima de tensão em pu (Vmax_pu)
	private static final double TENSAO_MAX_PU = 1.05;
	
	//Restrição mínima de tensão em pu (Vmin_pu)
	private static final double TENSAO_MIN_PU = 0.9;

	public static void executePowerFlow(Environment environment) {
		double[][] mpcBus = mountMpcBus(environment);
		
		double[][] mpcGen = mountMpcGen(environment);
		
		double[][] mpcBranch = mountMpcBranch(environment);
		
		for (double[] ds : mpcBranch) {
			for (double d : ds) {
				System.out.print(d + "	");
			}
			System.out.println();
		}
	}
	
	private static double[][] mountMpcBus(Environment environment) {
		List<NetworkNode> nodes = environment.getNetworkNodes();
		
		double[] nodeNums = new double[nodes.size()];
		double[] nodeTypes = new double[nodes.size()];
		double[] loadActivePowerMW = new double[nodes.size()];
		double[] loadReactivePowerMVar = new double[nodes.size()];
		double[] area = new double[nodeNums.length];
		double[] voltageMagnitude = new double[nodeNums.length];
		double[] baseKV = new double[nodeNums.length];
		double[] zone = new double[nodeNums.length];
		double[] restricaoMax = new double[nodeNums.length];
		double[] restricaoMin = new double[nodeNums.length];

		for (int i = 0; i < nodes.size(); i++) {
			NetworkNode node = nodes.get(i);
			
			//numeros dos loads/feeders
			nodeNums[i] = node.getNodeNumber();
			
			//tipos dos loads/feeders
			nodeTypes[i] = node.isFeeder() ? 3 : 1; //feeders serao marcados com 3, loads com 1
			
			//TODO multiplicar pelo percentual de uso do período do dia
			if (node.isLoad() && node.isOn()) {
				//potencia ativa dos loads em megawatts (Pd) (se a carga estiver desligada, 0) 
				//TODO carga da planilha deve estar em kW, pois dividimos por 1000 para transformar em mW
				loadActivePowerMW[i] = node.getPower() / 1000;
				
				//potencia reativa dos loads em Mega Volt Ampère (Qd) (se a carga estiver desligada, 0) 
				//TODO carga da planilha deve estar em kVar, pois dividimos por 1000 para transformar em mVar
				//TODO incluir informação de potência reativa na planilha
				loadReactivePowerMVar[i] = node.getPower() / 1000;
			}
			
			//area sempre 1
			area[i] = 1;
			
			//voltage magnitude (Vm) (p.u.)
			voltageMagnitude[i] = TENSAO_REFERENCIA_PU;
			
			//Tensão de Barra em kV
			double base = TENSAO_BASE / 1000;
			baseKV[i] = base;
			
			//zone sempre 1
			zone[i] = 1;
			
			//Restrição máxima (Vmax)
			restricaoMax[i] = TENSAO_MAX_PU;
			
			//Restrição mínima (Vmax)
			restricaoMin[i] = TENSAO_MIN_PU;
		}
		
		//Gs e bs zerado
		double[] gs = new double[nodeNums.length];
		double[] bs = new double[nodeNums.length];
		
		//angulo barras (Va) tudo com 0, nao estamos considerando
		double[] anguloBarras = new double[nodeNums.length];
		
		//variavel mpc.bus do matpower
		double[][] mpcBus = new double[nodeNums.length][13];
		for (int i = 0; i < nodeNums.length; i++) {
			mpcBus[i][0] = nodeNums[i];
			mpcBus[i][1] = nodeTypes[i];
			mpcBus[i][2] = loadActivePowerMW[i];
			mpcBus[i][3] = loadReactivePowerMVar[i];
			mpcBus[i][4] = gs[i];
			mpcBus[i][5] = bs[i];
			mpcBus[i][6] = area[i];
			mpcBus[i][7] = voltageMagnitude[i];
			mpcBus[i][8] = anguloBarras[i];
			mpcBus[i][9] = baseKV[i];
			mpcBus[i][10] = zone[i];
			mpcBus[i][11] = restricaoMax[i];
			mpcBus[i][12] = restricaoMin[i];
		}
		
		return mpcBus;
	}
	
	private static double[][] mountMpcGen(Environment environment) {
		List<Feeder> feeders = environment.getFeeders();
		
		double[] busG = new double[feeders.size()];
		double[] potenciaGeradaMW = new double[feeders.size()];
		double[] potenciaGeradaMVar = new double[feeders.size()];
		double[] qMax = new double[feeders.size()];
		double[] qMin = new double[feeders.size()];
		double[] pMax = new double[feeders.size()];
		double[] pMin = new double[feeders.size()];
		double[] vmSetpoint = new double[feeders.size()];
		double[] mBase = new double[feeders.size()];
		double[] statusG = new double[feeders.size()];

		for (int i = 0; i < feeders.size(); i++) {
			Feeder feeder = feeders.get(i);
			
			//Barras de geração de injeção de potência (numeros dos feeders)
			busG[i] = feeder.getNodeNumber();
			
			//Potência Injetada em MW (Pg) (segundo o Fausto, nao faz diferenca o valor passado aqui)
			//TODO carga da planilha deve estar em kW, pois dividimos por 1000 para transformar em mW
			potenciaGeradaMW[i] = feeder.getPower() / 1000;
			
			//Potência Injetada em MVar (Qg)
			//TODO carga da planilha deve estar em kVar, pois dividimos por 1000 para transformar em mVar
			potenciaGeradaMVar[i] = feeder.getPower() / 1000;
			
			 //nao precisamos de valores maximos e minimos de capacidade do feeder
			//Potência máxima de cada geração em MVar
			qMax[i] = potenciaGeradaMVar[i];
			//Potência mínima de cada geração em MVar
			qMin[i] = potenciaGeradaMVar[i];
			//Potência máxima de cada geração em MW
			pMax[i] = potenciaGeradaMW[i];
			//Potência mínima de cada geração em MW
			pMin[i] = potenciaGeradaMW[i];

			//Voltage magnitude setpoint (vg) (p.u.) - para cada load/feeder
			vmSetpoint[i] = TENSAO_REFERENCIA_PU;
			
			//Potência de base em MVa
			double base = POTENCIA_BASE / 1000000;
			mBase[i] = base;
			
			//Status dos pontos de entrega de potência e de Geração distribuída (DG)
			statusG[i] = feeder.isOn() ? 1 : 0;
		}
		
		//variavel mpc.gen do matpower
		double[][] mpcGen = new double[busG.length][21];
		for (int i = 0; i < busG.length; i++) {
			mpcGen[i][0] = busG[i];
			mpcGen[i][1] = potenciaGeradaMW[i];
			mpcGen[i][2] = potenciaGeradaMVar[i];
			mpcGen[i][3] = qMax[i];
			mpcGen[i][4] = qMin[i];
			mpcGen[i][5] = vmSetpoint[i];
			mpcGen[i][6] = mBase[i];
			mpcGen[i][7] = statusG[i];
			mpcGen[i][8] = pMax[i];
			mpcGen[i][9] = pMin[i];
		}
		return mpcGen;
	}
	
	private static double[][] mountMpcBranch(Environment environment) {
		List<Branch> branches = environment.getBranches();
		
		double[] branchFrom = new double[branches.size()];
		double[] branchTo = new double[branches.size()];
		double[] resistenciaPu = new double[branches.size()];
		double[] reatanciaPu = new double[branches.size()];
		double[] correnteMaxBranchMVA = new double[branches.size()];
		double[] statusBranch = new double[branches.size()];
		double[] anguloMin = new double[branchFrom.length];
		double[] anguloMax = new double[branchFrom.length];
		
		//Impedância de base
		double zb = Math.pow(TENSAO_BASE, 2)/POTENCIA_BASE;
		
		for (int i = 0; i < branches.size(); i++) {
			Branch branch = branches.get(i);
			
			//branch De
			branchFrom[i] = branch.getLoad1().getNodeNumber();
			
			//branch Para
			branchTo[i] = branch.getLoad2().getNodeNumber();

			//resistência em pu (ohms / impedancia)
			resistenciaPu[i] = 0 / zb; //TODO incluir na planilha (em Ohms)
			
			//reatância em pu (ohms / impedancia)
			reatanciaPu[i] = 0 / zb; //TODO incluir na planilha (em Ohms)
			
			//Capacidade máxima em potência [MVA] (Smax)
			correnteMaxBranchMVA[i] = branch.getMaxCurrent() * TENSAO_BASE / POTENCIA_BASE;
			
			//Status Branch
			statusBranch[i] = branch.isOn() ? 1 : 0;
			
			//angulo minimo
			anguloMin[i] = -360;
			
			//angulo maximo
			anguloMax[i] = 360;
		}
		
		//Vetor bshant
		double[] bsh = new double[branchFrom.length];
		
		//Ratio
		double[] ratio = new double[branchFrom.length];
		
		//Angle
		double[] angle = new double[branchFrom.length];
		
		//variavel mpc.branch do matpower
		double[][] mpcBranch = new double[branchFrom.length][13];
		for (int i = 0; i < branchFrom.length; i++) {
			mpcBranch[i][0] = branchFrom[i];
			mpcBranch[i][1] = branchTo[i];
			mpcBranch[i][2] = resistenciaPu[i];
			mpcBranch[i][3] = reatanciaPu[i];
			mpcBranch[i][4] = bsh[i];
			mpcBranch[i][5] = correnteMaxBranchMVA[i];
			mpcBranch[i][6] = correnteMaxBranchMVA[i];
			mpcBranch[i][7] = correnteMaxBranchMVA[i];
			mpcBranch[i][8] = ratio[i];
			mpcBranch[i][9] = angle[i];
			mpcBranch[i][10] = statusBranch[i];
			mpcBranch[i][11] = anguloMin[i];
			mpcBranch[i][12] = anguloMax[i];
		}
		return mpcBranch;
	}
	
	public static void main(String[] args) {
		File f = new File("C:/Users/Guisi/Desktop/modelo.csv");
		
		try {
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(f);
			
			executePowerFlow(environment);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
