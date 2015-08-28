package br.com.guisi.simulador.rede.view;

import java.text.MessageFormat;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import br.com.guisi.simulador.rede.util.EvaluationObject;
import br.com.guisi.simulador.rede.util.richtext.JavaKeywords;

public class ExpressionEvaluatorController extends Controller {

	private final String EVALUATE_FUNCTION_NAME = "evaluateFunction";
	private final String EVALUATE_FUNCTION = "var " + EVALUATE_FUNCTION_NAME + " = function(eval) '{' {0}; '}';";
	
	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	@FXML
	private Label lblResult;
	
	private CodeArea codeArea;
	
	private EvaluationObject evaluationObject;
	
	public void initialize() {
		evaluationObject = new EvaluationObject();
		evaluationObject.setEnvironment(getEnvironment());
		
		codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setPrefHeight(1000);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, JavaKeywords.computeHighlighting(newText));
        });
        vBoxInternal.getChildren().add(0, codeArea);
        //codeArea.replaceText(0, 0, sampleCode);
	}
	
	public void evaluateExpression() {
		String expression = MessageFormat.format(EVALUATE_FUNCTION, codeArea.getText());
		
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
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
