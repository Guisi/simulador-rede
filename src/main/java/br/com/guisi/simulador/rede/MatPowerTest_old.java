package br.com.guisi.simulador.rede;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;

public class MatPowerTest_old {
	
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setHidden(true)
				//.setUseSingleComputationalThread(true)
				.setUsePreviouslyControlledSession(true).build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		MatlabProxy proxy = factory.getProxy();
		proxy.eval("addpath('matpower5.1')");

		long ini = System.currentTimeMillis();
		
		double[] bus = {2,3,4,5,6,7,8,9,10,11,12,13,14,15,68,69,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67};
		proxy.setVariable("testebus", bus);
		
		proxy.setVariable("tst", 1d);
		
		proxy.eval("ret = runpf(case2_withoutDG_c2, mpoption('OUT_ALL', 0));");
	    
		System.out.println("Result: " + proxy.getVariable("ret"));
	    
	    System.out.println("Tempo: " + (System.currentTimeMillis() - ini));
		
		proxy.eval("rmpath('matpower5.1')");

		// Disconnect the proxy from MATLAB
		proxy.disconnect();
	}
}
