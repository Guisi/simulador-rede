package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.text.Text;

public class BranchText extends Text implements BranchNode {

	private final Integer branchNum;

	public BranchText(Integer branchNum, String text) {
		super(text);
		this.branchNum = branchNum;
	}
	
	@Override
	public Integer getBranchNum() {
		return branchNum;
	}
}
