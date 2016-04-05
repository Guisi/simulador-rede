package br.com.guisi.simulador.rede.controller.options;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import javax.annotation.PostConstruct;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.EnvironmentKeyType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.functions.EvaluationObject;
import br.com.guisi.simulador.rede.util.EvaluatorUtils;
import br.com.guisi.simulador.rede.util.richtext.JavaKeywords;

public class ExpressionEvaluatorController extends Controller {

	public static final String FXML_FILE = "/fxml/options/ExpressionEvaluator.fxml";
	
	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	@FXML
	private TextArea taEvalResult;
	
	private CodeArea codeArea;
	
	private EvaluationObject evaluationObject;
	
	@PostConstruct
	public void initializeController() {
		evaluationObject = new EvaluationObject();
		
		codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setPrefHeight(10000);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, JavaKeywords.computeHighlighting(newText));
        });
        vBoxInternal.getChildren().add(0, codeArea);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
		//TODO fazer com que o ambiente possa ser selecionado
		evaluationObject.setEnvironment(SimuladorRede.getEnvironment(EnvironmentKeyType.INTERACTION_ENVIRONMENT));
	}
	
	public void evaluateExpression() {
		taEvalResult.setText("");
		try {
			Object result = EvaluatorUtils.evaluateExpression(evaluationObject, codeArea.getText());
			taEvalResult.setText(String.valueOf(result));
		} catch (Exception e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			e.printStackTrace();
			alert.showAndWait();
		}
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
