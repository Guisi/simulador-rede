package br.com.guisi.simulador.rede.controller.options;

import javax.annotation.PostConstruct;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.constants.FunctionType;
import br.com.guisi.simulador.rede.controller.Controller;
import br.com.guisi.simulador.rede.events.EventType;
import br.com.guisi.simulador.rede.functions.FunctionItem;
import br.com.guisi.simulador.rede.util.richtext.JavaKeywords;

public class FunctionEditController extends Controller {
	
	public static final String FXML_FILE = "/fxml/options/FunctionEdit.fxml";
	
	@FXML
	private VBox root;
	@FXML
	private VBox vBoxInternal;
	
	@FXML
	private TextField txFunctionName;
	@FXML
	private ComboBox<String> cbFunctionType;
	private CodeArea codeArea;
	
	private FunctionItem functionItem;
	
	@PostConstruct
	public void initializeController() {
		cbFunctionType.setItems(FXCollections.observableArrayList());
		for (FunctionType functionType : FunctionType.values()) {
			cbFunctionType.getItems().add(functionType.getDescription());
		}
		
		codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setPrefHeight(10000);

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, JavaKeywords.computeHighlighting(newText));
        });
        vBoxInternal.getChildren().add(5, codeArea);
	}
	
	@Override
	public void initializeControllerData(Object... data) {
		this.functionItem = (FunctionItem) data[0];
		
		txFunctionName.setText(this.functionItem.getFunctionName().get());
        String functionType = functionItem.getFunctionType().get();
        if (StringUtils.isNotBlank(functionType)) {
        	cbFunctionType.setValue(functionType);
        }
        String functionExpression = this.functionItem.getFunctionExpression();
        if (StringUtils.isNotEmpty(functionExpression)) {
        	codeArea.replaceText(functionExpression);
        } else {
        	codeArea.clear();
        }
	}
	
	public void save() {
		String functionName = txFunctionName.getText();
		String functionType = cbFunctionType.getValue();
		String functionExpression = codeArea.getText();
		
		if (StringUtils.isBlank(functionName) || StringUtils.isBlank(functionType) || StringUtils.isBlank(functionExpression)) {
			Alert alert = new Alert(AlertType.ERROR, "All fields are mandatory!");
			alert.showAndWait();
			return;
		}

		this.functionItem.getFunctionName().set(functionName);
		this.functionItem.getFunctionType().set(functionType);
        this.functionItem.setFunctionExpression(codeArea.getText());
        this.fireEvent(EventType.FUNCTION_UPDATE, this.functionItem);
        SimuladorRede.closeScene(this);
	}
	
	public void cancel() {
		SimuladorRede.closeScene(this);
	}
	
	@Override
	public Node getView() {
		return root;
	}
}
