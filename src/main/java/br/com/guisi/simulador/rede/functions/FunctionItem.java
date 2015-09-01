package br.com.guisi.simulador.rede.functions;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FunctionItem implements Serializable, Comparable<FunctionItem> {

	private static final long serialVersionUID = 1L;
	
	private static final String FUNCTION_KEY_PREFIX = "FUNCTION_EXPRESSION_";
	
	private String functionKey;
	private StringProperty functionName;
	private StringProperty functionType;
	private String functionExpression;
	
	public FunctionItem() {
		this.functionName = new SimpleStringProperty();
		this.functionType = new SimpleStringProperty();
	}
	
	public FunctionItem(String functionKey, String functionName, String functionType, String functionExpression) {
		super();
		this.functionKey = functionKey;
		this.functionName = new SimpleStringProperty(functionName);
		this.functionType = new SimpleStringProperty(functionType);
		this.functionExpression = functionExpression;
	}

	public String getFunctionKey() {
		return functionKey;
	}

	public void setFunctionKey(int index) {
		this.functionKey = FUNCTION_KEY_PREFIX + index;
	}

	public StringProperty getFunctionName() {
		return functionName;
	}

	public void setFunctionName(StringProperty functionName) {
		this.functionName = functionName;
	}

	public StringProperty getFunctionType() {
		return functionType;
	}

	public void setFunctionType(StringProperty functionType) {
		this.functionType = functionType;
	}

	public String getFunctionExpression() {
		return functionExpression;
	}

	public void setFunctionExpression(String functionExpression) {
		this.functionExpression = functionExpression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functionKey == null) ? 0 : functionKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FunctionItem other = (FunctionItem) obj;
		if (functionKey == null) {
			if (other.functionKey != null)
				return false;
		} else if (!functionKey.equals(other.functionKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionItem [functionKey=" + functionKey + ", functionName=" + functionName + ", functionType=" + functionType + ", functionExpression="
				+ functionExpression + "]";
	}
	
	@Override
	public int compareTo(FunctionItem o) {
		return functionKey.compareTo(o.functionKey);
	}
}
