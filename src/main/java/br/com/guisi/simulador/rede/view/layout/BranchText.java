package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class BranchText extends Text implements BranchNode {

	private final Integer branchNum;
	private final Line branchLine;

	public BranchText(Integer branchNum, Line branchLine, String text) {
		super(text);
		this.branchNum = branchNum;
		this.branchLine = branchLine;
	}
	
	@Override
	public Integer getBranchNum() {
		return branchNum;
	}
	
	@Override
	public Line getBranchLine() {
		return branchLine;
	}
}
