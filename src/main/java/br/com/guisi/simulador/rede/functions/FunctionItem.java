package br.com.guisi.simulador.rede.functions;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FunctionItem implements Serializable, Comparable<FunctionItem> {

	private static final long serialVersionUID = 1L;
	
	private Integer functionIndex;
	private StringProperty functionName;
	private StringProperty functionType;
	private String functionExpression;
	private StringProperty functionResult;
	
	public FunctionItem() {
		this.functionName = new SimpleStringProperty();
		this.functionType = new SimpleStringProperty();
		this.functionResult = new SimpleStringProperty();
	}
	
	public FunctionItem(Integer functionIndex, String functionName, String functionType, String functionExpression) {
		super();
		this.functionIndex = functionIndex;
		this.functionName = new SimpleStringProperty(functionName);
		this.functionType = new SimpleStringProperty(functionType);
		this.functionExpression = functionExpression;
		this.functionResult = new SimpleStringProperty();
	}

	public Integer getFunctionIndex() {
		return functionIndex;
	}

	public void setFunctionIndex(Integer functionIndex) {
		this.functionIndex = functionIndex;
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

	public StringProperty getFunctionResult() {
		return functionResult;
	}

	public void setFunctionResult(StringProperty functionResult) {
		this.functionResult = functionResult;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functionIndex == null) ? 0 : functionIndex.hashCode());
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
		if (functionIndex == null) {
			if (other.functionIndex != null)
				return false;
		} else if (!functionIndex.equals(other.functionIndex))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionItem [functionIndex=" + functionIndex + ", functionName=" + functionName + ", functionType=" + functionType
				+ ", functionExpression=" + functionExpression + "]";
	}

	@Override
	public int compareTo(FunctionItem o) {
		return functionIndex.compareTo(o.functionIndex);
	}
}
