package br.com.guisi.simulador.rede.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;
import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.extensions.MatlabNumericArray;
import org.n52.matlab.control.extensions.MatlabTypeConverter;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Feeder;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.enviroment.NetworkNode;

public class PowerFlow {
	
	public static void execute(Environment environment) throws Exception {
		//zera o valor de potencia usado dos feeders
		environment.getFeeders().forEach((feeder) -> {
			feeder.setUsedPower(0);
		});
		
		//atualiza informações das conexões dos feeders e loads
		EnvironmentUtils.updateFeedersConnections(environment);

		//executa power flow
		//TODO remover executePowerFlow(environment);
		
		//atribui o valor de potencia usado dos feeders de acordo com o retorno do fluxo de potência
		environment.getFeeders().forEach((feeder) -> {
			feeder.getBranches().forEach((branch) -> feeder.addUsedPower(branch.getInstantCurrent()));
		});
	}
	
	private static void executePowerFlow(Environment environment) throws Exception {
		/*monta lista somente com os nodes que sejam feeders ou que estejam
		conectados a um feeder*/
		List<NetworkNode> nodes = environment.getNetworkNodes();
		nodes.forEach((node) -> node.setCurrentVoltagePU(0));
		List<NetworkNode> activeNodes = new ArrayList<>();
		for (NetworkNode networkNode : nodes) {
			if (networkNode.isFeeder() || ((Load)networkNode).getFeeder() != null) {
				activeNodes.add(networkNode);
			}
		}
		
		/*se não existe nenhum load ativo, não executa powerflow*/
		long loads = activeNodes.stream().filter((node) -> node.isLoad()).count();
		if (loads == 0) {
			//throw new Exception("No active load found, power flow can't be executed!");
			return;
		}
		
		/*monta lista somente com os branches que estejam conectados a nodes ativos*/
		List<Branch> branches = environment.getBranches();
		branches.forEach((branch) -> {
			branch.setInstantCurrent(0);
			branch.setActiveLossMW(0);
			branch.setReactiveLossMVar(0);
		});
		List<Branch> activeBranches = new ArrayList<>();
		for (Branch branch : branches) {
			if (activeNodes.contains(branch.getNode1()) && activeNodes.contains(branch.getNode2())) {
				activeBranches.add(branch);
			}
		}
		
		double[][] mpcBus = mountMpcBus(activeNodes);
		
		double[][] mpcGen = mountMpcGen(environment.getFeeders());
		
		double[][] mpcBranch = mountMpcBranch(activeBranches);
		
		try {
			MatlabProxy proxy = Matlab.getMatlabProxy();
			
			MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
			
			processor.setNumericArray("mpcBus", new MatlabNumericArray(mpcBus, null));
			processor.setNumericArray("mpcGen", new MatlabNumericArray(mpcGen, null));
			processor.setNumericArray("mpcBranch", new MatlabNumericArray(mpcBranch, null));
			
			proxy.setVariable("potenciaBase", Constants.POTENCIA_BASE);
			
			//long ini = System.currentTimeMillis();
			proxy.eval("mpc = runpf(case_simulador(mpcBus, mpcGen, mpcBranch, potenciaBase), mpoption('OUT_ALL', 0));");
			//System.out.println("Tempo: " + (System.currentTimeMillis() - ini));
			
			double success = processor.getNumericArray("mpc.success").getRealValue(0);
			
			if (success == 1) {
				//recupera informacoes das cargas
				double[][] mpcBusRet = processor.getNumericArray("mpc.bus").getRealArray2D();
				
				for (double[] mpcBusRetLine : mpcBusRet) {
					Integer nodeNumber = (int) mpcBusRetLine[0];
					NetworkNode node = environment.getNetworkNode(nodeNumber);
					
					node.setCurrentVoltagePU(mpcBusRetLine[7]); //tensão
				}
				
				double[][] mpcBranchRet = processor.getNumericArray("mpc.branch").getRealArray2D();
				
				for (double[] mpcBranchRetLine : mpcBranchRet) {
					double sAtual = new Complex(mpcBranchRetLine[13], mpcBranchRetLine[14]).abs();
					
					Integer nodeFrom = (int) mpcBranchRetLine[0];
					Integer nodeTo = (int) mpcBranchRetLine[1];
					
					Branch branch = environment.getBranch(nodeFrom, nodeTo);
					if (branch != null) {
						double actualCurrent = (sAtual / (branch.getNode2().getCurrentVoltagePU() * Constants.TENSAO_BASE)) * Constants.POTENCIA_BASE;
						branch.setInstantCurrent(actualCurrent);
						
						double lossMW = Math.abs(mpcBranchRetLine[13] + mpcBranchRetLine[15]);
						branch.setActiveLossMW(lossMW);
						
						double lossMVar = Math.abs(mpcBranchRetLine[14] + mpcBranchRetLine[16]);
						branch.setReactiveLossMVar(lossMVar);
					}
				}
			} else {
				throw new Exception("Newton's method power flow did not converge");
			}
		} catch (MatlabConnectionException | MatlabInvocationException e) {
			throw new Exception(e);
		}
	}
	
	private static double[][] mountMpcBus(List<NetworkNode> activeNodes) {
		double[] nodeNums = new double[activeNodes.size()];
		double[] nodeTypes = new double[activeNodes.size()];
		double[] loadActivePowerMW = new double[activeNodes.size()];
		double[] loadReactivePowerMVar = new double[activeNodes.size()];
		double[] area = new double[nodeNums.length];
		double[] voltageMagnitude = new double[nodeNums.length];
		double[] baseKV = new double[nodeNums.length];
		double[] zone = new double[nodeNums.length];
		double[] restricaoMax = new double[nodeNums.length];
		double[] restricaoMin = new double[nodeNums.length];

		for (int i = 0; i < activeNodes.size(); i++) {
			NetworkNode node = activeNodes.get(i);
			
			//numeros dos loads/feeders
			nodeNums[i] = node.getNodeNumber();
			
			//tipos dos loads/feeders
			nodeTypes[i] = node.isFeeder() ? 3 : 1; //feeders serao marcados com 3, loads com 1
			
			//TODO multiplicar pelo percentual de uso do período do dia
			if (node.isLoad() && node.isOn()) {
				//potencia ativa dos loads em megawatts (Pd) (se a carga estiver desligada, 0) 
				//carga da planilha deve estar em kW, pois dividimos por 1000 para transformar em mW
				loadActivePowerMW[i] = node.getActivePowerKW() / 1000 * 0.9;
				
				//potencia reativa dos loads em Mega Volt Ampère (Qd) (se a carga estiver desligada, 0) 
				//carga da planilha deve estar em kVar, pois dividimos por 1000 para transformar em mVar
				loadReactivePowerMVar[i] = node.getReactivePowerKVar() / 1000 * 0.75;
			}
			
			//area sempre 1
			area[i] = 1;
			
			//voltage magnitude (Vm) (p.u.)
			voltageMagnitude[i] = Constants.TENSAO_REFERENCIA_PU;
			
			//Tensão de Barra em kV
			double base = Constants.TENSAO_BASE / 1000;
			baseKV[i] = base;
			
			//zone sempre 1
			zone[i] = 1;
			
			//Restrição máxima (Vmax)
			restricaoMax[i] = Constants.TENSAO_MAX_PU;
			
			//Restrição mínima (Vmax)
			restricaoMin[i] = Constants.TENSAO_MIN_PU;
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
	
	private static double[][] mountMpcGen(List<Feeder> feeders) {
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
			//carga da planilha deve estar em kW, pois dividimos por 1000 para transformar em mW
			potenciaGeradaMW[i] = feeder.getActivePowerKW() / 1000;
			
			//Potência Injetada em MVar (Qg)
			//carga da planilha deve estar em kVar, pois dividimos por 1000 para transformar em mVar
			potenciaGeradaMVar[i] = feeder.getReactivePowerKVar() / 1000;
			
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
			vmSetpoint[i] = Constants.TENSAO_REFERENCIA_PU;
			
			//Potência de base em MVa
			double base = Constants.POTENCIA_BASE / 1000000;
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
	
	private static double[][] mountMpcBranch(List<Branch> branches) {
		double[] branchFrom = new double[branches.size()];
		double[] branchTo = new double[branches.size()];
		double[] resistenciaPu = new double[branches.size()];
		double[] reatanciaPu = new double[branches.size()];
		double[] correnteMaxBranchMVA = new double[branches.size()];
		double[] statusBranch = new double[branches.size()];
		double[] anguloMin = new double[branchFrom.length];
		double[] anguloMax = new double[branchFrom.length];
		
		//Impedância de base
		double zb = Math.pow(Constants.TENSAO_BASE, 2) / Constants.POTENCIA_BASE;
		
		for (int i = 0; i < branches.size(); i++) {
			Branch branch = branches.get(i);
			
			//branch De
			branchFrom[i] = branch.getNode1().getNodeNumber();
			
			//branch Para
			branchTo[i] = branch.getNode2().getNodeNumber();

			//resistência em pu (ohms / impedancia)
			resistenciaPu[i] = branch.getResistance() / zb;
			
			//reatância em pu (ohms / impedancia)
			reatanciaPu[i] = branch.getReactance() / zb;
			
			//Capacidade máxima em potência [MVA] (Smax)
			correnteMaxBranchMVA[i] = branch.getMaxCurrent() * Constants.TENSAO_BASE / Constants.POTENCIA_BASE;
			
			//Status Branch
			statusBranch[i] = branch.isClosed() ? 1 : 0;
			
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
		File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.csv");
		
		try {
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(f);
			
			executePowerFlow(environment);
			
			Matlab.disconnectMatlabProxy();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}