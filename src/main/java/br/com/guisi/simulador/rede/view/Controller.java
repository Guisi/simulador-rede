package br.com.guisi.simulador.rede.view;

import javafx.scene.Node;

/**
 * Classe abstrata dos controllers
 * @author douglas.guisi
 *
 */
public abstract class Controller {
	
	/**
	 * Retorna a o elemento raiz da view
	 * @return
	 */
	public abstract Node getView();
	
}
