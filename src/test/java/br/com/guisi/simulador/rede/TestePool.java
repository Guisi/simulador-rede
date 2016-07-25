package br.com.guisi.simulador.rede;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import br.com.guisi.simulador.rede.agent.qlearning.Cluster;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.util.EnvironmentUtils;
import br.com.guisi.simulador.rede.util.PowerFlow;

public class TestePool {

	public static void main(String[] args) throws Exception {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(10);
        config.setMaxTotal(10);
		
		Environment environment = loadEnvironment();

		EnvironmentPool pool = new EnvironmentPool(new EnvironmentFactory(environment), config);
		
		for (int i = 0; i < 9; i++) {
			System.out.println(pool.borrowObject());
		}
		

		System.out.println(pool.getBorrowedCount());

		Environment env = pool.borrowObject();
		System.out.println(env);
		
		pool.returnObject(env);
		
		System.out.println(pool.getNumActive());
		
	}
	
	private static Environment loadEnvironment() {
		//File f = new File("C:/Users/Guisi/Desktop/modelo-zidan.xlsx");
		File f = new File("C:/Users/p9924018/Desktop/Pesquisa/modelo-zidan.xlsx");
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
			
			List<Cluster> clusters = EnvironmentUtils.mountClusters(environment);
			environment.setClusters(clusters);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return environment;
	}
}

class EnvironmentPool extends GenericObjectPool<Environment>{

    public EnvironmentPool(PooledObjectFactory<Environment> factory) {
        super(factory);
    }

    public EnvironmentPool(PooledObjectFactory<Environment> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }
}

class EnvironmentFactory extends BasePooledObjectFactory<Environment> {

	private Environment environment;
	
	public EnvironmentFactory(Environment environment) {
		this.environment = environment;
	}
	
    @Override
    public Environment create() throws Exception {
        return SerializationUtils.clone(environment);
    }

    @Override
    public PooledObject<Environment> wrap(Environment environment) {
        return new DefaultPooledObject<Environment>(environment);
    }

}
