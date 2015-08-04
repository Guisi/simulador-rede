package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class BranchRectangle extends Rectangle implements BranchNode {

	private final Integer branchNum;
	private final Line branchLine;

	public BranchRectangle(Integer branchNum, Line branchLine) {
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
