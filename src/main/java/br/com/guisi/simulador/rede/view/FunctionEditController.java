package br.com.guisi.simulador.rede.view;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import br.com.guisi.simulador.rede.util.richtext.JavaKeywords;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FunctionEditController extends Controller {

	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	
	@FXML
	private TextField txFunctionName;
	private CodeArea codeArea;
	
	@Override
	public void initializeController(Object... data) {
		codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setPrefHeight(10000);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, JavaKeywords.computeHighlighting(newText));
        });
        vBoxInternal.getChildren().add(3, codeArea);
	}
	
	public void save() {
		
	}
	
	public void cancel() {
		
	}
	
	@Override
	public Node getView() {
		return root;
	}
	
}
