package br.com.guisi.simulador.rede.enviroment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum SwitchState {

	OPEN("Open"), CLOSED("Closed"), ISOLATED("Isolated"), FAULT("Fault");
	
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final List<SwitchState> VALUES = Collections.unmodifiableList(Arrays.asList(OPEN, CLOSED));
	private static final int SIZE = VALUES.size();
	
	private final String description;
	
	private SwitchState(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public static SwitchState getRandomAction() {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}
}
