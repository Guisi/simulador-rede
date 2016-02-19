package br.com.guisi.simulador.rede.exception;

import br.com.guisi.simulador.rede.enviroment.NetworkNode;

public class NonRadialNetworkException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	private final NetworkNode networkNode;

	public NonRadialNetworkException(String msg, NetworkNode networkNode) {
		super(msg);
		this.networkNode = networkNode;
	}

	public NetworkNode getNetworkNode() {
		return networkNode;
	}

}
