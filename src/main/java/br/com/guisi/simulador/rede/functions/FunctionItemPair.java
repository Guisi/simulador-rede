package br.com.guisi.simulador.rede.functions;

import java.io.Serializable;

public class FunctionItemPair implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private FunctionItem functionItem1;
	private FunctionItem functionItem2;

	public FunctionItem getFunctionItem1() {
		return functionItem1;
	}
	public void setFunctionItem1(FunctionItem functionItem1) {
		this.functionItem1 = functionItem1;
	}
	public FunctionItem getFunctionItem2() {
		return functionItem2;
	}
	public void setFunctionItem2(FunctionItem functionItem2) {
		this.functionItem2 = functionItem2;
	}
	
}
