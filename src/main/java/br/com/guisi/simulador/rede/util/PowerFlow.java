package br.com.guisi.simulador.rede.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import br.com.guisi.simulador.rede.exception.NonRadialNetworkException;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.cornell.pserc.jpower.Djp_jpoption;
import edu.cornell.pserc.jpower.jpc.Bus;
import edu.cornell.pserc.jpower.jpc.Gen;
import edu.cornell.pserc.jpower.jpc.JPC;
import edu.cornell.pserc.jpower.pf.Djp_rundcpf;

public class PowerFlow {
	
	private static final Map<String, Double> options = Djp_jpoption.jpoption();
	
	static {
		options.put("VERBOSE", 0.0);
	}
	
	public static boolean execute(Environment environment) {
		//zera os valores do power flow anterior
		PowerFlow.resetPowerFlowValues(environment);
		
		//atualiza informa��es das conex�es dos elementos da rede
		EnvironmentUtils.updateEnvironmentConnections(environment);

		//executa power flow
		return executePowerFlow(environment);
	}
	
	public static void resetPowerFlowValues(Environment environment) {
		//zera valores dos loads
		environment.getNetworkNodes().forEach((node) -> node.setCurrentVoltagePU(0));
		
		//zera valores dos branches
		environment.getBranches().forEach((branch) -> {
			branch.setInstantCurrent(0);
			branch.setActiveLossMW(0);
			branch.setReactiveLossMVar(0);
		});
	}
	
	private static boolean executePowerFlow(Environment environment) {
		/*monta lista somente com os nodes que sejam feeders ou que estejam conectados a um feeder*/
		List<NetworkNode> activeNodes = new ArrayList<>();
		environment.getNetworkNodes().forEach((networkNode) -> {
			if (!networkNode.isIsolated() && (networkNode.isFeeder() || ((Load)networkNode).getFeeder() != null) ) {
				activeNodes.add(networkNode);
			}
		});
		
		/*se n�o existe nenhum load ativo, n�o executa powerflow*/
		long loads = activeNodes.stream().filter((node) -> node.isLoad() && ((Load)node).isOn()).count();
		if (loads == 0) {
			return false;
		}
		
		/*monta lista somente com os branches que estejam conectados a nodes ligados a algum feeder (mesmo que o load esteja desligado) */
		List<Branch> activeBranches = new ArrayList<>();
		environment.getBranches().forEach((branch) -> {
			if (branch.isClosed() && activeNodes.containsAll(branch.getConnectedNodes())) {
				activeBranches.add(branch);
			}
		});
		
		List<Feeder> activeFeeders = new ArrayList<>();
		activeFeeders.addAll(environment.getFeeders().stream().filter(feeder -> feeder.isOn()).collect(Collectors.toList()));

		//return executePowerFlowMatlab(environment, activeNodes, activeBranches);
		return executePowerFlowJPower(environment, activeFeeders, activeNodes, activeBranches);
	}
	
	@SuppressWarnings("unused")
	private static boolean executePowerFlowMatlab(Environment environment, List<NetworkNode> activeNodes, List<Branch> activeBranches) {
		double[][] mpcBus = mountMpcBus(activeNodes, Constants.TENSAO_REFERENCIA_PU);
		
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
			
			boolean success = processor.getNumericArray("mpc.success").getRealValue(0) == 1;
			
			if (success) {
				//recupera informacoes das cargas
				double[][] mpcBusRet = processor.getNumericArray("mpc.bus").getRealArray2D();
				
				for (double[] mpcBusRetLine : mpcBusRet) {
					Integer nodeNumber = (int) mpcBusRetLine[0];
					NetworkNode node = environment.getNetworkNode(nodeNumber);
					node.setCurrentVoltagePU(mpcBusRetLine[7]); //tens�o
				}
				
				double[][] mpcBranchRet = processor.getNumericArray("mpc.branch").getRealArray2D();
				
				for (double[] mpcBranchRetLine : mpcBranchRet) {
					double sAtual = new Complex(mpcBranchRetLine[13], mpcBranchRetLine[14]).abs();
					
					Integer nodeFrom = (int) mpcBranchRetLine[0];
					Integer nodeTo = (int) mpcBranchRetLine[1];
					
					Branch branch = environment.getBranch(environment.getNetworkNode(nodeFrom), environment.getNetworkNode(nodeTo));
					if (branch != null) {
						double actualCurrent = (sAtual / (branch.getNodeTo().getCurrentVoltagePU() * Constants.TENSAO_BASE)) * Constants.POTENCIA_BASE;
						branch.setInstantCurrent(actualCurrent);
						
						double lossMW = Math.abs(mpcBranchRetLine[13] + mpcBranchRetLine[15]);
						branch.setActiveLossMW(lossMW);
						
						double lossMVar = Math.abs(mpcBranchRetLine[14] + mpcBranchRetLine[16]);
						branch.setReactiveLossMVar(lossMVar);
					}
				}
			}
			
			return success;
		} catch (MatlabConnectionException | MatlabInvocationException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean executePowerFlowJPower(Environment environment, List<Feeder> feeders, List<NetworkNode> activeNodes, List<Branch> activeBranches) {
		JPC jpc = getJPowerCase(feeders, activeNodes, activeBranches);
		
		jpc = Djp_rundcpf.rundcpf(jpc, options);
		
		if (jpc.success) {
			//recupera informacoes das cargas
			DoubleMatrix2D mpcBus = jpc.bus.toMatrix();

			for (int i = 0; i < mpcBus.rows(); i++) {
				Integer nodeNumber = (int) mpcBus.get(i, 0);
				NetworkNode node = environment.getNetworkNode(nodeNumber);
				node.setCurrentVoltagePU(mpcBus.get(i, 7)); //tens�o
			}
			
			DoubleMatrix2D mpcBranch = jpc.branch.toMatrix();
			
			for (int i = 0; i < mpcBranch.rows(); i++) {
				double sAtual = new Complex(mpcBranch.get(i, 13), mpcBranch.get(i, 14)).abs();
				
				Integer nodeFrom = (int) mpcBranch.get(i, 0);
				Integer nodeTo = (int) mpcBranch.get(i, 1);
				
				Branch branch = environment.getBranch(environment.getNetworkNode(nodeFrom), environment.getNetworkNode(nodeTo));
				if (branch != null) {
					double actualCurrent = (sAtual / (branch.getNodeTo().getCurrentVoltagePU() * Constants.TENSAO_BASE)) * Constants.POTENCIA_BASE;
					branch.setInstantCurrent(actualCurrent);
					
					double lossMW = Math.abs(mpcBranch.get(i, 13) + mpcBranch.get(i, 15));
					branch.setActiveLossMW(lossMW);
					
					double lossMVar = Math.abs(mpcBranch.get(i, 14) + mpcBranch.get(i, 16));
					branch.setReactiveLossMVar(lossMVar);
				}
			}
		}
		
		return jpc.success;
	}
	
	private static JPC getJPowerCase(List<Feeder> feeders, List<NetworkNode> activeNodes, List<Branch> activeBranches) {

		JPC jpc = new JPC();

		/* JPOWER Case Format : Version 2 */
		jpc.version = "2";

		/* system MVA base */
		jpc.baseMVA = 1;

		/* bus data */
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		double[][] mpcBus = mountMpcBus(activeNodes, Constants.TENSAO_REFERENCIA_PU_JPOWER);
		jpc.bus = Bus.fromMatrix( DoubleFactory2D.dense.make(mpcBus));

		/* generator data */
		//	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_ag	ramp_10	ramp_30	ramp_q	apf
		double[][] mpcGen = mountMpcGen(feeders);
		jpc.gen = Gen.fromMatrix( DoubleFactory2D.dense.make(mpcGen) );

		/* branch data */
		//	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax
		double[][] mpcBranch = mountMpcBranch(activeBranches);
		jpc.branch = edu.cornell.pserc.jpower.jpc.Branch.fromMatrix( DoubleFactory2D.dense.make(mpcBranch) );

		return jpc;
	}
	
	private static double[][] mountMpcBus(List<NetworkNode> activeNodes, double tensaoReferencia) {
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
			
			//TODO multiplicar pelo percentual de uso do per�odo do dia
			if (node.isLoad() && node.isOn()) {
				//potencia ativa dos loads em megawatts (Pd) (se a carga estiver desligada, 0) 
				loadActivePowerMW[i] = node.getActivePowerMW();
				
				//potencia reativa dos loads em Mega Volt Amp�re (Qd) (se a carga estiver desligada, 0) 
				loadReactivePowerMVar[i] = node.getReactivePowerMVar();
			}
			
			//area sempre 1
			area[i] = 1;
			
			//voltage magnitude (Vm) (p.u.)
			voltageMagnitude[i] = tensaoReferencia;
			
			//Tens�o de Barra em kV
			double base = Constants.TENSAO_BASE / 1000;
			baseKV[i] = base;
			
			//zone sempre 1
			zone[i] = 1;
			
			//Restri��o m�xima (Vmax)
			restricaoMax[i] = Constants.TENSAO_MAX_PU;
			
			//Restri��o m�nima (Vmax)
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
			
			//Barras de gera��o de inje��o de pot�ncia (numeros dos feeders)
			busG[i] = feeder.getNodeNumber();
			
			//Pot�ncia Injetada em MW (Pg) (segundo o Fausto, nao faz diferenca o valor passado aqui)
			potenciaGeradaMW[i] = feeder.getActivePowerMW();
			
			//Pot�ncia Injetada em MVar (Qg)
			potenciaGeradaMVar[i] = feeder.getReactivePowerMVar();
			
			 //nao precisamos de valores maximos e minimos de capacidade do feeder
			//Pot�ncia m�xima de cada gera��o em MVar
			qMax[i] = potenciaGeradaMVar[i];
			//Pot�ncia m�nima de cada gera��o em MVar
			qMin[i] = potenciaGeradaMVar[i];
			//Pot�ncia m�xima de cada gera��o em MW
			pMax[i] = potenciaGeradaMW[i];
			//Pot�ncia m�nima de cada gera��o em MW
			pMin[i] = potenciaGeradaMW[i];

			//Voltage magnitude setpoint (vg) (p.u.) - para cada load/feeder
			vmSetpoint[i] = Constants.TENSAO_REFERENCIA_PU;
			
			//Pot�ncia de base em MVa
			double base = Constants.POTENCIA_BASE / 1000000;
			mBase[i] = base;
			
			//Status dos pontos de entrega de pot�ncia e de Gera��o distribu�da (DG)
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
		
		//Imped�ncia de base
		double zb = Math.pow(Constants.TENSAO_BASE, 2) / Constants.POTENCIA_BASE;
		
		for (int i = 0; i < branches.size(); i++) {
			Branch branch = branches.get(i);
			
			//branch De
			branchFrom[i] = branch.getNodeFrom().getNodeNumber();
			
			//branch Para
			branchTo[i] = branch.getNodeTo().getNodeNumber();

			//resist�ncia em pu (ohms / impedancia)
			resistenciaPu[i] = branch.getResistance() / zb;
			
			//reat�ncia em pu (ohms / impedancia)
			reatanciaPu[i] = branch.getReactance() / zb;
			
			//Capacidade m�xima em pot�ncia [MVA] (Smax)
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
		File f = new File("C:/Users/p9924018/Desktop/Pesquisa/modelo-zidan.xlsx");
		
		try {
			Environment environment = EnvironmentUtils.getEnvironmentFromFile(f);
			
			//primeiro valida se rede est� radial
			List<NonRadialNetworkException> exceptions = EnvironmentUtils.validateRadialState(environment);
			
			if (exceptions.isEmpty()) {
				//isola as faltas
				EnvironmentUtils.isolateFaultSwitches(environment);
				
				//marca switches que podem ser tie-sw
				EnvironmentUtils.validateTieSwitches(environment);
				
				//executa o fluxo de pot�ncia
				PowerFlow.execute(environment);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}