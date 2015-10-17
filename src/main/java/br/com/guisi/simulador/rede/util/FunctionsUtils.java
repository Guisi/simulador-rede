package br.com.guisi.simulador.rede.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import br.com.guisi.simulador.rede.functions.FunctionItem;

public class FunctionsUtils {

	private static final String FUNCTIONS_PROPERTIES = "functions.properties";
	private static final String FUNCTIONS_QUANTITY_KEY = "FUNCTIONS_QUANTITY";
	private static final String FUNCTION_KEY_PREFIX = "FUNCTION_EXPRESSION_";
	
	private FunctionsUtils() {}
	
	public static List<FunctionItem> loadProperties() throws IOException {
		List<FunctionItem> functions = new ArrayList<>();
		if (Files.exists(Paths.get(FUNCTIONS_PROPERTIES))) {
			Properties prop = new OrderedProperties();
			prop.load(new FileInputStream(FUNCTIONS_PROPERTIES));
			
			Object qtdStr = prop.get(FUNCTIONS_QUANTITY_KEY);
			if (qtdStr != null) {
				Integer quantity = Integer.valueOf((String) qtdStr);
				
				for (int i = 0; i < quantity; i++) {
					String prefix = FUNCTION_KEY_PREFIX + String.format("%03d", i) + "_";
					String functionName = (String) prop.get(prefix + "NAME");
					String functionType = (String) prop.get(prefix + "TYPE");
					String functionExpression = (String) prop.get(prefix + "EXPRESSION");
					FunctionItem fi = new FunctionItem(i, functionName, functionType, functionExpression);
					functions.add(fi);
				}
			}
		}
		Collections.sort(functions);
		return functions;
	}
	
	public static void saveProperties(List<FunctionItem> functions) throws IOException {
		Properties prop = new OrderedProperties();
		prop.put(FUNCTIONS_QUANTITY_KEY, String.valueOf(functions.size()));
		for (int i = 0; i < functions.size(); i++) {
			FunctionItem function = functions.get(i);
			String prefix = FUNCTION_KEY_PREFIX + String.format("%03d", i) + "_";
			prop.put(prefix + "NAME", function.getFunctionName().get());
			prop.put(prefix + "TYPE", function.getFunctionType().get());
			prop.put(prefix + "EXPRESSION", function.getFunctionExpression());
		}
		
		prop.store(new FileOutputStream(FUNCTIONS_PROPERTIES), null);
	}
}