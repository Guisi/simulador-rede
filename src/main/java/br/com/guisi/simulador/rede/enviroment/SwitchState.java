package br.com.guisi.simulador.rede.enviroment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum SwitchState {

	OPEN("Open"), CLOSED("Closed"), ISOLATED("Isolated"), FAULT("Fault");
	
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	public static final List<SwitchState> OPERATIONAL_SWITCHES = Collections.unmodifiableList(Arrays.asList(OPEN, CLOSED));
	private static final int SIZE = OPERATIONAL_SWITCHES.size();
	
	private final String description;
	
	private SwitchState(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public static SwitchState getRandomAction() {
		return OPERATIONAL_SWITCHES.get(RANDOM.nextInt(SIZE));
	}
}
