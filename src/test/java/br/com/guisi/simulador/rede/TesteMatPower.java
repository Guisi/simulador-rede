package br.com.guisi.simulador.rede;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

import org.n52.matlab.control.MatlabConnectionException;
import org.n52.matlab.control.MatlabInvocationException;

import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.Load;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.Matlab;
import br.com.guisi.simulador.rede.util.PowerFlow;

public class TesteMatPower {

	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException {
		File f = new File("C:/Users/Guisi/Desktop/modelo-zidan-1feeder.xlsx");
		//File f = new File("C:/Users/p9924018/Desktop/Pesquisa/modelo-zidan.xlsx");
		Environment environment = null;
		
		try {
			environment = EnvironmentUtils.getEnvironmentFromFile(f);
			
			//isola as faltas
			EnvironmentUtils.isolateFaultSwitches(environment);
			
			EnvironmentUtils.validateTieSwitches(environment);
			
			try {
				PowerFlow.execute(environment);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			List<Load> loads = environment.getLoads();
			Collections.sort(loads, (o1, o2) -> {
				return o1.getNodeNumber().compareTo(o2.getNodeNumber());
			});

			for (Load load : loads) {
				System.out.println(load.getNodeNumber() + ": " + new BigDecimal(load.getCurrentVoltagePU()).setScale(10, RoundingMode.HALF_UP).doubleValue());
			}
			
			List<Branch> branches = environment.getBranches();
			Collections.sort(branches, (o1, o2) -> {
				return o1.getNumber().compareTo(o2.getNumber());
			});
			
			System.out.println("Branch    |   Instant Current   |   ActiveLossMW    |    ReactiveLossMVar");
			for (Branch branch : branches) {
				System.out.println(branch.getNumber() + "    |    "
						+ new BigDecimal(branch.getInstantCurrent()).setScale(10, RoundingMode.HALF_UP).toPlainString() + "   |    " 
						+ new BigDecimal(branch.getActiveLossMW()).setScale(10, RoundingMode.HALF_UP).toPlainString() + "    |    "
						+ new BigDecimal(branch.getReactiveLossMVar()).setScale(10, RoundingMode.HALF_UP).toPlainString());
			}
			
			Matlab.disconnectMatlabProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}