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
		for (EventType eventType : EventType.values()) {
			registers.put(eventType, new HashSet<>());
		}
	}

	/**
	 * Registra em um evento
	 * 
	 * @param eventType
	 * @param listener
	 */
	public void register(EventType eventType, EventListener listener) {
		registers.get(eventType).add(listener);
	}

	/**
	 * Lanca um evento
	 * 
	 * @param eventType
	 * @param data
	 */
	public void fire(EventType eventType, Object data) {
		registers.get(eventType).forEach(listener -> listener.onEvent(eventType, data));
		System.out.println("-- Disparou " + eventType);
	}
	
	public void fire(EventType eventType) {
		registers.get(eventType).forEach(listener -> listener.onEvent(eventType, null));
	}
}
