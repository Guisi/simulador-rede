package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class BranchStackPane extends StackPane {
	
	private final Integer branchNum;
	private Text branchText;
	private Rectangle switchRectangle;
	
	public BranchStackPane(Integer branchNum) {
		this.branchNum = branchNum;
	}

	public Integer getBranchNum() {
		return branchNum;
	}
	
	public Text getBranchText() {
		return branchText;
	}

	public void setBranchText(Text branchText) {
		this.branchText = branchText;
	}

	public Rectangle getSwitchRectangle() {
		return switchRectangle;
	}

	public void setSwitchRectangle(Rectangle switchRectangle) {
		this.switchRectangle = switchRectangle;
	}

	public Line getBranchLine() {
		for (Node node : getChildren()) {
			if (node instanceof Line) {
				return (Line) node;
			}
		}
		throw new IllegalStateException("No way! A BranchStackPane always have a Line!");
	}
}
