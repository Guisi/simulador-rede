package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.text.Text;

public class BranchText extends Text {

	private final Integer branchNum;

	public BranchText(Integer branchNum, String text) {
		super(text);
		this.branchNum = branchNum;
	}
	
	public Integer getBranchNum() {
		return branchNum;
	}
}
