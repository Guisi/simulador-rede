package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.shape.Rectangle;

public class BranchRectangle extends Rectangle implements BranchNode {

	private final Integer branchNum;

	public BranchRectangle(Integer branchNum) {
		this.branchNum = branchNum;
	}
	
	@Override
	public Integer getBranchNum() {
		return branchNum;
	}
}
