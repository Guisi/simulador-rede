package br.com.guisi.simulador.rede.functions;

import java.io.Serializable;

import javafx.beans.property.StringProperty;

public class FunctionItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String functionKey;
	private StringProperty functionName;
	private StringProperty functionExpression;
	
	public FunctionItem(String functionKey, StringProperty functionName, StringProperty functionExpression) {
		super();
		this.functionKey = functionKey;
		this.functionName = functionName;
		this.functionExpression = functionExpression;
	}

	public String getFunctionKey() {
		return functionKey;
	}

	public void setFunctionKey(String functionKey) {
		this.functionKey = functionKey;
	}

	public StringProperty getFunctionName() {
		return functionName;
	}

	public void setFunctionName(StringProperty functionName) {
		this.functionName = functionName;
	}

	public StringProperty getFunctionExpression() {
		return functionExpression;
	}

	public void setFunctionExpression(StringProperty functionExpression) {
		this.functionExpression = functionExpression;
	}
	
}
