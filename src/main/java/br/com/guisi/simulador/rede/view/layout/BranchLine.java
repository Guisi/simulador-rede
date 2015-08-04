package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.shape.Line;

public class BranchLine extends Line implements BranchNode {

	private final Integer branchNum;

	public BranchLine(Integer branchNum) {
		this.branchNum = branchNum;
	}
	
	@Override
	public Integer getBranchNum() {
		return branchNum;
	}

	@Override
	public Line getBranchLine() {
		return this;
	}
	
}
