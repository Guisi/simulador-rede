package br.com.guisi.simulador.rede.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import br.com.guisi.simulador.rede.constants.PropertyKey;

public class PropertiesUtils {

	private static final String SIMULATOR_PROPERTIES = "simulator.properties";
	private static Properties simulatorProperties;
	
	private PropertiesUtils() {}
	
	private static Properties getInstance() {
		if (simulatorProperties == null) {
			loadProperties();
		}
		return simulatorProperties;
	}
	
	private static void loadProperties() {
		simulatorProperties = new Properties();
		if (Files.exists(Paths.get(SIMULATOR_PROPERTIES))) {
			try {
				simulatorProperties.load(new FileInputStream(SIMULATOR_PROPERTIES));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getProperty(PropertyKey propertyKey) {
		Properties prop = getInstance();
		return prop.getProperty(propertyKey.name(), propertyKey.getDefaultValue());
	}
	
	public static void saveProperty(PropertyKey key, String value) {
		Properties prop = getInstance();
		prop.put(key.name(), value);
		try {
			prop.store(new FileOutputStream(SIMULATOR_PROPERTIES), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double getDoubleValue(PropertyKey propertyKey) {
		String value = getProperty(propertyKey);
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return Double.valueOf(propertyKey.getDefaultValue());
		}
	}
	
	public static double getEGreedy() {
		return getDoubleValue(PropertyKey.E_GREEDY);
	}
	
	public static double getLearningConstant() {
		return getDoubleValue(PropertyKey.LEARNING_CONSTANT);
	}
	
	public static double getDiscountFactor() {
		return getDoubleValue(PropertyKey.DISCOUNT_FACTOR);
	}
}