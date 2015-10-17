package br.com.guisi.simulador.rede.constants;

public interface Constants {

	/** network interface */
	int NETWORK_PANE_PADDING = 30;
	int NETWORK_GRID_SIZE_PX = 70;
	int LOAD_RADIUS_PX = 12;
	int BRANCH_TYPE_PX = 10;
	String POWER_DECIMAL_FORMAT = "##0.00";
	
	/** power flow */
	//Tensão de referencia em pu (VRef) (Fausto usou 1.02, vamos usar 1.0)
	double TENSAO_REFERENCIA_PU = 1.02;
	
	//Potência de base (Sbase)
	double POTENCIA_BASE = 1000000;
	
	//Tensão de base (VBase) 
	double TENSAO_BASE = 11000;
	
	//Restrição máxima de tensão em pu (Vmax_pu)
	double TENSAO_MAX_PU = 1.05;
	
	//Restrição mínima de tensão em pu (Vmin_pu)
	double TENSAO_MIN_PU = 0.9;
}
