package br.com.guisi.simulador.rede.controller.modal;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.functions.EvaluationObject;
import br.com.guisi.simulador.rede.util.EvaluatorUtils;
import br.com.guisi.simulador.rede.util.richtext.JavaKeywords;

public class ExpressionEvaluatorController extends Controller {

	public static final String FXML_FILE = "/fxml/modal/ExpressionEvaluator.fxml";
	
	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	@FXML
	private TextArea taEvalResult;
	
	private CodeArea codeArea;
	
	private EvaluationObject evaluationObject;
	
	@Override
	public void initializeController(Object... data) {
		evaluationObject = new EvaluationObject();
		evaluationObject.setEnvironment(getEnvironment());
		
		codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setPrefHeight(10000);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, JavaKeywords.computeHighlighting(newText));
        });
        vBoxInternal.getChildren().add(0, codeArea);
	}
	
	public void evaluateExpression() {
		taEvalResult.setText("");
		try {
			Object result = EvaluatorUtils.evaluateExpression(evaluationObject, codeArea.getText());
			taEvalResult.setText(String.valueOf(result));
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