package br.com.guisi.simulador.rede.events;

/**
 * Interface dos controllers que escutam eventos
 * @author eliomarcolino
 *
 */
public interface EventListener {

	public void onEvent(EventType eventType, Object data);
	
}
