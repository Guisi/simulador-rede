package br.com.guisi.simulador.rede.enviroment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum SwitchState {

	OPEN, CLOSED;
	
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	private static final List<SwitchState> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	
	public static SwitchState getRandomAction() {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}
}
