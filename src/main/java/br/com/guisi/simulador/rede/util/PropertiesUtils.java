package br.com.guisi.simulador.rede.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

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
		simulatorProperties = new OrderedProperties();
		if (Files.exists(Paths.get(SIMULATOR_PROPERTIES))) {
			try {
				simulatorProperties.load(new FileInputStream(SIMULATOR_PROPERTIES));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getProperty(PropertyKey propertyKey) {
		return getProperty(propertyKey, null);
	}
	
	public static String getProperty(PropertyKey propertyKey, String keySuffix) {
		Properties prop = getInstance();
		String key = propertyKey.name() + (StringUtils.isNotBlank(keySuffix) ? "_" + keySuffix : "");
		return prop.getProperty(key, propertyKey.getDefaultValue());
	}
	
	public static void saveProperty(PropertyKey propertyKey, String value) {
		saveProperty(propertyKey, null, value);
	}
	
	public static void saveProperty(PropertyKey propertyKey, String keySuffix, String value) {
		Properties prop = getInstance();
		String key = propertyKey.name() + (StringUtils.isNotBlank(keySuffix) ? "_" + keySuffix : "");
		prop.put(key, value);
		try {
			prop.store(new FileOutputStream(SIMULATOR_PROPERTIES), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static double getDoubleProperty(PropertyKey propertyKey) {
		return getDoubleProperty(propertyKey, null);
	}
	
	public static double getDoubleProperty(PropertyKey propertyKey, String keySuffix) {
		String value = getProperty(propertyKey, keySuffix);
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return Double.valueOf(propertyKey.getDefaultValue());
		}
	}
	
	public static double getEGreedy() {
		return getDoubleProperty(PropertyKey.E_GREEDY);
	}
	
	public static double getLearningConstant() {
		return getDoubleProperty(PropertyKey.LEARNING_CONSTANT);
	}
	
	public static double getDiscountFactor() {
		return getDoubleProperty(PropertyKey.DISCOUNT_FACTOR);
	}
}