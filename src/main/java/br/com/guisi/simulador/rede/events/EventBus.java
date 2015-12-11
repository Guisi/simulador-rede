package br.com.guisi.simulador.rede.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.springframework.stereotype.Component;

@Singleton
@Component
public class EventBus {

	/**
	 * Registros por eventos
	 */
	private Map<EventType, Set<EventListener>> registers;

	private EventBus() {
		registers = new HashMap<EventType, Set<EventListener>>();
	}

	/**
	 * Registra em um evento
	 * 
	 * @param eventType
	 * @param listener
	 */
	public void register(EventType eventType, EventListener listener) {
		Set<EventListener> listeners = registers.get(eventType);
		if (listeners == null) {
			listeners = new HashSet<>();
			registers.put(eventType, listeners);
		}
		listeners.add(listener);
	}

	/**
	 * Lanca um evento
	 * 
	 * @param eventType
	 * @param data
	 */
	public void fire(EventType eventType, Object data) {
		Set<EventListener> listeners = registers.get(eventType);
		if (listeners != null) {
			for (EventListener listener : listeners) {
				listener.onEvent(eventType, data);
			}
		}
	}
}
