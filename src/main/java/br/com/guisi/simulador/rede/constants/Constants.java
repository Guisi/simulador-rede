package br.com.guisi.simulador.rede.constants;

public interface Constants {

	/** Q-Learning */
	/* E-greedy de 90% */
	double E_GREEDY = 0.9;

	/* Constante de aprendizagem (alpha) */
	double LEARNING_CONSTANT = 0.1;
	
	/* Fator de desconto (gamma) */
	double DISCOUNT_FACTOR = 0.9;
	
	/** network interface */
	int NETWORK_PANE_PADDING = 30;
	int NETWORK_GRID_SIZE_PX = 70;
	int LOAD_RADIUS_PX = 12;
	int BRANCH_TYPE_PX = 10;
	String DECIMAL_FORMAT_2 = "##0.00";
	String DECIMAL_FORMAT_3 = "##0.000";
	String DECIMAL_FORMAT_5 = "##0.00000";
	
	/** power flow */
	//Tens�o de referencia em pu (VRef) (Fausto usou 1.02, vamos usar 1.0)
	double TENSAO_REFERENCIA_PU = 1.00;
	
	//Pot�ncia de base (Sbase)
	double POTENCIA_BASE = 1000000;
	
	//Tens�o de base (VBase) 
	double TENSAO_BASE = 11000;
	
	//Restri��o m�xima de tens�o em pu (Vmax_pu)
	double TENSAO_MAX_PU = 1.05;
	
	//Restri��o m�nima de tens�o em pu (Vmin_pu)
	double TENSAO_MIN_PU = 0.9;
}
