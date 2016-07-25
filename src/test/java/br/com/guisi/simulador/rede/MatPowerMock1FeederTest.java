package br.com.guisi.simulador.rede;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;
import org.n52.matlab.control.extensions.MatlabNumericArray;
import org.n52.matlab.control.extensions.MatlabTypeConverter;

public class MatPowerMock1FeederTest {
	
	//Tensão de referencia em pu (VRef) (perguntar pro Fausto)
	private static final double TENSAO_REFERENCIA_PU = 1.02;
	
	//Potência de base (Sbase)
	private static final double POTENCIA_BASE = 1000000;
	
	//Tensão de base (VBase) (perguntar pro Fausto)
	private static final double TENSAO_BASE = 11000;
	
	//Restrição máxima de tensão em pu (Vmax_pu)
	private static final double TENSAO_MAX_PU = 1.05;
	
	//Restrição mínima de tensão em pu (Vmin_pu)
	private static final double TENSAO_MIN_PU = 0.9;
	
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		runMatPower();
	}
	
	public static void runMatPower() throws MatlabConnectionException, MatlabInvocationException {
		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setHidden(true)
				.setUseSingleComputationalThread(true)
				.setUsePreviouslyControlledSession(true).build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		MatlabProxy proxy = factory.getProxy();
		proxy.eval("addpath('matpower5.1')");

		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		
		double[][] mpcBus = getMpcBus();
		processor.setNumericArray("mpcBus", new MatlabNumericArray(mpcBus, null));
		
		double[][] mpcGen = getMpcGen();
		processor.setNumericArray("mpcGen", new MatlabNumericArray(mpcGen, null));
		
		double[][] mpcBranch = getMpcBranch();
		processor.setNumericArray("mpcBranch", new MatlabNumericArray(mpcBranch, null));
		
		proxy.setVariable("potenciaBase", POTENCIA_BASE);

		//proxy.eval("ret = case_simulador_fluxo(mpcBus, mpcGen, mpcBranch, potenciaBase);");
		proxy.eval("ret = runpf(case_simulador(mpcBus, mpcGen, mpcBranch, potenciaBase), mpoption('OUT_ALL', 0));");
		
		//recupera informacoes das cargas
		Map<Integer, Double> actualVoltageMap = new LinkedHashMap<>();
		double[][] mpsBus = processor.getNumericArray("ret.bus").getRealArray2D();
		
		for (int i = 0; i < mpsBus.length; i++) {
			Integer nodeNumber = (int) mpcBus[i][0];
			actualVoltageMap.put(nodeNumber, mpsBus[i][7]);
			
			System.out.println(nodeNumber + ": " + new BigDecimal(mpsBus[i][7]).setScale(10, RoundingMode.HALF_UP).doubleValue());
		}
		
		MatlabNumericArray array = processor.getNumericArray("ret.branch");
		double[][] d = array.getRealArray2D();
		
		double losses = 0;
		for (double[] es : d) {
			double sAtual = new Complex(es[13], es[14]).abs();
			
			Integer loadTo = (int) es[1];
			
			Double voltagePu = actualVoltageMap.get(loadTo);
			if (voltagePu != null) {
				//System.out.println( (sAtual / (voltagePu * TENSAO_BASE)) * POTENCIA_BASE);
			}
			
			double loss = Math.abs(es[13] + es[15]);
			//System.out.println(loss);
			losses += loss;
		}
		
		//System.out.println("Losses: " + losses);
		
		proxy.eval("rmpath('matpower5.1')");

		// Disconnect the proxy from MATLAB
		proxy.disconnect();
	}
	
	/**
	 * Dados dos loads
	 * @return
	 */
	private static double[][] getMpcBus() {
		//numeros dos loads/feeders
		double[] loadNums = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,68,69};
		
		//tipos dos loads/feeders
		double[] loadTypes = new double[loadNums.length];
		Arrays.fill(loadTypes, 1); //loads
		loadTypes[0] = 3; //feeders serao marcados com 3
		
		//cargas em megawatts (Pd) (se a carga estiver desligada, zerar)
		double[] loadsPowerMW = {0, 0.108, 0.0648, 0.162, 0.081, 0.01944, 0.01944, 0.01404, 0.0171, 0.0216, 0.01728, 0.054, 0.1134, 0.027, 0.0432, 0.108, 0.0432};
	
		//cargas em MVar (Qd) (se a carga estiver desligada, zerar) (perguntar pro Fausto de onde tirou)
		double[] loadsPowerMVar = {0, 0.081, 0.036, 0.117, 0.045, 0.00975, 0.01275, 0.009, 0.00975, 0.009, 0.00825, 0.036, 0.081, 0.0135, 0.0225, 0.054, 0.027};
		
		//Gs e bs zerado
		double[] gs = new double[loadNums.length];
		double[] bs = new double[loadNums.length];
		
		//area tudo com 1
		double[] area = new double[loadNums.length];
		Arrays.fill(area, 1);
		
		//voltage magnitude (Vm) (p.u.) - para cada load/feeder
		double[] voltageMagnitude = new double[loadNums.length];
		Arrays.fill(voltageMagnitude, TENSAO_REFERENCIA_PU);
		
		//angulo barras (Va) tudo com 0, nao estamos considerando
		double[] anguloBarras = new double[loadNums.length];
		
		//Tensão de Barra em KV
		double[] baseKV = new double[loadNums.length];
		Arrays.fill(baseKV, TENSAO_BASE / 1000);
		
		//zone tudo com 1
		double[] zone = new double[loadNums.length];
		Arrays.fill(zone, 1);
		
		//Restrição máxima (Vmax)
		double[] restricaoMax = new double[loadNums.length];
		Arrays.fill(restricaoMax, TENSAO_MAX_PU);
		
		//Restrição mínima (Vmax)
		double[] restricaoMin = new double[loadNums.length];
		Arrays.fill(restricaoMin, TENSAO_MIN_PU);
		
		//variavel mpc.bus do matpower
		double[][] mpcBus = new double[loadNums.length][13];
		for (int i = 0; i < loadNums.length; i++) {
			mpcBus[i][0] = loadNums[i];
			mpcBus[i][1] = loadTypes[i];
			mpcBus[i][2] = loadsPowerMW[i];
			mpcBus[i][3] = loadsPowerMVar[i];
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
	
	/**
	 * Dados dos feeders
	 * @return
	 */
	private static double[][] getMpcGen() {
		
		//Barras de geração de injeção de potência (serao os numeros dos feeders)
		double[] busG = {1};
		
		//Potência Injetada em MW (Pg)
		double[] potenciaGeradaMW = {0.1};
		
		//Potência Injetada em MVar (Qg)
		double[] potenciaGeradaMVar = {2};
		
		//Potência máxima de cada geração em MVar
		double[] qMax = {2};
		
		//Potência mínima de cada geração em MVar
		double[] qMin = {0.5};
		
		//Voltage magnitude setpoint (vg) (p.u.) - para cada load/feeder
		double[] vmSetpoint = new double[busG.length];
		Arrays.fill(vmSetpoint, TENSAO_REFERENCIA_PU);
		
		//Potência de base em MVa
		double[] mBase = new double[busG.length];
		Arrays.fill(mBase, POTENCIA_BASE / 1000000);
		
		//Status dos pontos de entrega de potência e de Geração distribuída (DG)
		double[] statusG = {1};
		
		//Potência máxima de cada geração em MW
		double[] pMax = {0.1};
		
		//Potência mínima de cada geração em MW
		double[] pMin = {0.1};
		
		
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
	
	private static double[][] getMpcBranch() {
		//branch De
		double[] branchFrom = {1, 2, 3, 4, 5, 6, 7, 8, 4, 10, 11, 12, 13, 14, 7, 68};
		
		//branch Para
		double[] branchTo = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 68, 69};
		
		//Impedância de base
		double zb = Math.pow(TENSAO_BASE, 2)/POTENCIA_BASE;
		
		//resistência em ohms
		double[] resistenciaOhms = {1.097, 1.463, 0.731, 0.366, 1.828, 1.097, 0.731, 0.731, 1.080, 1.620, 1.080, 1.350, 0.810, 1.944, 1.080, 1.620};
		
		//Vetor R [pu]
		double[] resistenciaPu = new double[resistenciaOhms.length];
		for (int i = 0; i < resistenciaOhms.length; i++) {
			resistenciaPu[i] = resistenciaOhms[i] / zb;
		}
		
		//reatância em ohms
		double[] reatanciaOhms = {1.074, 1.432, 0.716, 0.358, 1.790, 1.074, 0.716, 0.716, 0.734, 1.101, 0.734, 0.917, 0.550, 1.321, 0.734, 1.101};
		
		//Vetor X [pu]
		double[] reatanciaPu = new double[reatanciaOhms.length];
		for (int i = 0; i < reatanciaOhms.length; i++) {
			reatanciaPu[i] = reatanciaOhms[i] / zb;
		}
		
		//Vetor bshant
		double[] bsh = new double[branchFrom.length];
		
		//Corrente máxima em cada branch (Imax)
		double[] correnteMaxBranch = {270, 270, 270, 270, 270, 270, 270, 270, 208, 208, 208, 208, 208, 208, 208, 208};
		
		//Capacidade máxima em potência [MVA] (Smax)
		double[] correnteMaxBranchMVA = new double[correnteMaxBranch.length];
		for (int i = 0; i < correnteMaxBranch.length; i++) {
			correnteMaxBranchMVA[i] = correnteMaxBranch[i] * TENSAO_BASE / POTENCIA_BASE;
		}
		
		//Ratio
		double[] ratio = new double[branchFrom.length];
		
		//Angle
		double[] angle = new double[branchFrom.length];
		
		//Status Branch
		double[] statusBranch = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		
		//angulo minimo
		double[] anguloMin = new double[branchFrom.length];
		Arrays.fill(anguloMin, -360);
		
		//angulo maximo
		double[] anguloMax = new double[branchFrom.length];
		Arrays.fill(anguloMax, 360);
		
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
}
