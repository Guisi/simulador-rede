package br.com.guisi.simulador.rede.util;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;
import org.n52.matlab.control.MatlabProxy;
import org.n52.matlab.control.MatlabProxyFactory;
import org.n52.matlab.control.MatlabProxyFactoryOptions;

public class Matlab {
	
	private static MatlabProxy proxy;

	public static MatlabProxy getMatlabProxy() throws MatlabConnectionException, MatlabInvocationException {
		if (proxy == null) {
			// Create a proxy, which we will use to control MATLAB
			MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
					.setHidden(true)
					.setUseSingleComputationalThread(true)
					.setUsePreviouslyControlledSession(true).build();
			MatlabProxyFactory factory = new MatlabProxyFactory(options);
			proxy = factory.getProxy();
			proxy.eval("addpath('matpower5.1')");
		}
		
		return proxy;
	}
	
	public static void disconnectMatlabProxy() throws MatlabConnectionException, MatlabInvocationException {
		if (proxy != null) {
			proxy.eval("rmpath('matpower5.1')");
		
			// Disconnect the proxy from MATLAB
			proxy.disconnect();
			
			proxy = null;
		}
	}
}
