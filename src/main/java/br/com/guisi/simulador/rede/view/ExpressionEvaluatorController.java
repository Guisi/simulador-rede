package br.com.guisi.simulador.rede.view;

import java.text.MessageFormat;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import br.com.guisi.simulador.rede.util.EvaluationObject;

public class ExpressionEvaluatorController extends Controller {

	private final String EVALUATE_FUNCTION_NAME = "evaluateFunction";
	private final String EVALUATE_FUNCTION = "var " + EVALUATE_FUNCTION_NAME + " = function(evalObj) '{' return evalObj.{0}; '}';";
	
	@FXML
	private VBox root;
	@FXML
	private TextArea taExpression;
	@FXML
	private Label lblResult;
	
	private EvaluationObject evaluationObject;
	
	public void initialize() {
		evaluationObject = new EvaluationObject();
		evaluationObject.setEnvironment(getEnvironment());
	}
	
	public void evaluateExpression() {
		long ini = System.currentTimeMillis();
		String expression = MessageFormat.format(EVALUATE_FUNCTION, taExpression.getText());
		
		try {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
			engine.eval(expression);
			
			Invocable invocable = (Invocable) engine;
			Object result = invocable.invokeFunction(EVALUATE_FUNCTION_NAME, evaluationObject);
			
			lblResult.setText(String.valueOf(result));
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
		
		System.out.println("Tempo: " + (System.currentTimeMillis() - ini));
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
