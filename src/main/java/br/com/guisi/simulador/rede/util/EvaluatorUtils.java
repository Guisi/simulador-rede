package br.com.guisi.simulador.rede.util;

import java.text.MessageFormat;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import br.com.guisi.simulador.rede.functions.EvaluationObject;

public class EvaluatorUtils {

	private EvaluatorUtils() {}
	
	private static final String EVALUATE_FUNCTION_NAME = "evaluateFunction";
	private static final String EVALUATE_FUNCTION = "var " + EVALUATE_FUNCTION_NAME + " = function(eval) '{' {0}; '}';";

	public static Object evaluateExpression(EvaluationObject evaluationObject, String expression) throws Exception {
		String function = MessageFormat.format(EVALUATE_FUNCTION, expression);
		
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		engine.eval(function);
		
		Invocable invocable = (Invocable) engine;
		Object result = invocable.invokeFunction(EVALUATE_FUNCTION_NAME, evaluationObject);

		return result;
	}
}
