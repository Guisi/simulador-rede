package br.com.guisi.simulador.rede;

import java.util.Arrays;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;
import org.n52.matlab.control.extensions.MatlabNumericArray;
import org.n52.matlab.control.extensions.MatlabTypeConverter;

public class MatPowerTest {
	
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		/*double[][] mpcBus = getMpcBus();
		
		for (int x = 0; x < mpcBus.length; x++) {
			for (int y = 0; y < mpcBus[x].length; y++) {
				System.out.print(mpcBus[x][y] + ";");
			}
			System.out.println();
		}*/
		
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

		long ini = System.currentTimeMillis();
		
		MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		double[][] mpcBus = getMpcBus();
		processor.setNumericArray("mpcBus", new MatlabNumericArray(mpcBus, null));
		
		proxy.eval("ret = case_simulador_fluxo(mpcBus);");
	    
		System.out.println("Result: " + proxy.getVariable("ret"));
	    
	    System.out.println("Tempo: " + (System.currentTimeMillis() - ini));
		
		proxy.eval("rmpath('matpower5.1')");

		// Disconnect the proxy from MATLAB
		proxy.disconnect();
	}
	
	/**
	 * Dados dos loads e feeders
	 * @return
	 */
	private static double[][] getMpcBus() {
		//Tensão de base (VBase) (perguntar pro Fausto)
		final double TENSAO_BASE = 11000;
		
		//Tensão de referencia em pu (VRef) (perguntar pro Fausto)
		final double TENSAO_REFERENCIA_PU = 1.02;
		
		//Restrição máxima de tensão em pu (Vmax_pu)
		final double TENSAO_MAX_PU = 1.05;
		
		//Restrição mínima de tensão em pu (Vmin_pu)
		final double TENSAO_MIN_PU = 0.9;
		
		//numeros dos loads/feeders
		double[] loadNums = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,68,69,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,70};
		
		//tipos dos loads
		double[] loadTypes = new double[loadNums.length];
		Arrays.fill(loadTypes, 1); //loads
		loadTypes[0] = 3; //feeders
		loadTypes[69] = 3;
		
		//cargas em megawatts (Pd)
		double[] loadsPowerMW = {0, 0.108, 0.0648, 0.162, 0.081, 0.01944, 0.01944, 0.01404, 0.0171, 0.0216, 0.01728, 0.054, 0.1134, 0.027, 0.0432, 0.108, 0.0432, 0.0648, 0.0432, 0.0162, 0.01404, 0.0324, 0.0972,
				0.054, 0.0648, 0.108, 0.0864, 0.108, 0.108, 0.1296, 0.1134, 0.0864, 0.0648, 0.01404, 0.01728, 0.054, 0.0432, 0.0648, 0.0432, 0.0324, 0.162, 0.0648, 0.1296, 0.0972, 0.01944, 0.01728,
				0.108, 0.0648, 0.0972, 0.11016, 0.108, 0.1512, 0.0648, 0.0216, 0.0432, 0.03888, 0.0324, 0.04644, 0.0864, 0.2592, 0.135, 0.027, 0.0108, 0.162, 0.054, 0.0324, 0.1404, 0.162, 0.027, 0};
	
		//cargas em MVar (Qd) (perguntar pro Fausto de onde tirou)
		double[] loadsPowerMVar = {0, 0.081, 0.036, 0.117, 0.045, 0.00975, 0.01275, 0.009, 0.00975, 0.009, 0.00825, 0.036, 0.081, 0.0135, 0.0225, 0.054, 0.027, 0.027, 0.0225, 0.00825, 0.0063, 0.018, 0.045,
				0.027, 0.036, 0.072, 0.0585, 0.054, 0.0495, 0.063, 0.063, 0.045, 0.036, 0.0072, 0.00885, 0.027, 0.0252, 0.036, 0.027, 0.0225, 0.09, 0.0315, 0.063, 0.054, 0.009, 0.009, 0.045,
				0.036, 0.063, 0.0594, 0.063, 0.081, 0.036, 0.0099, 0.027, 0.0216, 0.018, 0.027, 0.045, 0.108, 0.099, 0.009, 0.0045, 0.117, 0.027, 0.018, 0.108, 0.117, 0.0135, 0};
		
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
			mpcBus[i][0] = loadNums[i]; //numero loads/feeders
			mpcBus[i][1] = loadTypes[i]; //tipos loads/feeders
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
}
