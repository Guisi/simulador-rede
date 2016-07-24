package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchStatus;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;

import com.google.common.collect.Lists;

public class Teste {

	public static void main(String[] args) throws Exception {
		System.out.println(String.valueOf(true));
		
		/*List<Set<SwitchState>> listas = new ArrayList<>();
		
		for (int i = 0; i < 16; i++) {
			Set<SwitchState> states = new LinkedHashSet<>();
			states.add(new SwitchState(i, SwitchStatus.OPEN));
			states.add(new SwitchState(i, SwitchStatus.CLOSED));
			listas.add(states);
		}
		
		Set<List<SwitchState>> result = Sets.cartesianProduct(listas);
		System.out.println(result.size());*/
		
		/*List<List<SwitchState>> listas = new ArrayList<>();

		int[] arr = {72, 66, 63, 64, 13, 12, 11};
		
		for (int i : arr) {
			List<SwitchState> states = new ArrayList<>();
			states.add(new SwitchState(i, SwitchStatus.OPEN));
			states.add(new SwitchState(i, SwitchStatus.CLOSED));
			listas.add(states);
		}
		
		List<List<SwitchState>> result = Lists.cartesianProduct(listas);
		System.out.println(result.size());
		
		for (List<SwitchState> list : result) {
			if (list.stream().filter(st -> st.getStatus() == SwitchStatus.OPEN).count() < 2) { 
				System.out.println(list);
			}
		}*/
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
