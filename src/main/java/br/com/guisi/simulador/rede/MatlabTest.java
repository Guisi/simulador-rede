package br.com.guisi.simulador.rede;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;
import org.n52.matlab.control.extensions.MatlabNumericArray;
import org.n52.matlab.control.extensions.MatlabTypeConverter;

public class MatlabTest {
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		/*
		 * MatlabProxyFactoryOptions options = new
		 * MatlabProxyFactoryOptions.Builder() .setHidden(true)
		 * .setUseSingleComputationalThread(true)
		 * .setUsePreviouslyControlledSession(true) .build(); MatlabProxyFactory
		 * factory = new MatlabProxyFactory(options); MatlabProxy proxy =
		 * factory.getProxy();
		 * 
		 * for (int i = 0; i < 100; i++) { //Set a variable, add to it, retrieve
		 * it, and print the result proxy.setVariable("a", i);
		 * proxy.eval("a = a + 6"); double result = ((double[])
		 * proxy.getVariable("a"))[0]; System.out.println("Result: " + result);
		 * }
		 * 
		 * // Disconnect the proxy from MATLAB proxy.exit();
		 */

		// Create a proxy, which we will use to control MATLAB
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setHidden(true)
				//.setMatlabLocation("C:\\Program Files\\MATLAB\\MATLAB Production Server\\R2015a\\bin")
				.setUseSingleComputationalThread(true)
				.setUsePreviouslyControlledSession(true).build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		MatlabProxy proxy = factory.getProxy();
		proxy.eval("addpath('examples\\matlab')");

		long ini = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			proxy.eval("ret = rightTri([7 9])");
			
			//Get the array from MATLAB
			MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
			MatlabNumericArray array = processor.getNumericArray("ret");
			
			//Print out the same entry, using Java's 0-based indexing
			array.getRealValue(0, 0);
			array.getRealValue(0, 1);
			array.getRealValue(0, 2);
		}
		System.out.println("Tempo: " + (System.currentTimeMillis() - ini));

		proxy.eval("rmpath('examples\\matlab')");

		// Disconnect the proxy from MATLAB
		proxy.disconnect();
	}
}
