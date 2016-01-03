package br.com.guisi.simulador.rede;

import java.io.File;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;

public class Teste {

	public static void main(String[] args) throws Exception {
		int cont = 0;
		double[][] arr = new double[2][100000000]; 
		
		while (true) {
			arr[0][cont] = cont;
			arr[1][cont] = 1;
			
			cont++;
			
			if (cont%100000 == 0) {
				System.out.println(cont);
				Thread.sleep(1000);
			}
		}
	}
	
	public static void teste() throws Exception {
		File f = new File("C:/Users/Guisi/Desktop/modelo.csv");
		Environment environment = null;
		
		try {
			environment = EnvironmentUtils.getEnvironmentFromFile(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (environment != null) {
			//EnvironmentUtils.validateEnvironment(environment);
		
			StringBuilder sb = new StringBuilder();
			sb.append("var funcao = function(environment) { ");
			//sb.append("    return lista.stream().mapToInt(function(v) {return v}).sum(); ");
			sb.append("     return environment.loads.stream().filter(function(load) {return load.isSupplied()}).count(); ");
			sb.append("};");
			
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
			engine.eval(sb.toString());
			
			Invocable invocable = (Invocable) engine;
			Object result = invocable.invokeFunction("funcao", environment);
			
			System.out.println(result);
		}
	}
}
