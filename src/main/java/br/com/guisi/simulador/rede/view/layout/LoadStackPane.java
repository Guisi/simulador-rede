package br.com.guisi.simulador.rede.view.layout;

import javafx.scene.layout.StackPane;

public class LoadStackPane extends StackPane {
	
	private final Integer loadNum;
	
	public LoadStackPane(Integer loadNum) {
		this.loadNum = loadNum;
	}

	public Integer getLoadNum() {
		return loadNum;
	}
}
