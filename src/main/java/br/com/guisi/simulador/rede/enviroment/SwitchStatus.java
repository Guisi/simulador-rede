package br.com.guisi.simulador.rede.enviroment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum SwitchStatus {

	OPEN("Open", "Opened"), CLOSED("Closed", "Closed"), ISOLATED("Isolated", "Isolated"), FAULT("Fault", "");
	
	private static final Random RANDOM = new Random(System.currentTimeMillis());
	public static final List<SwitchStatus> OPERATIONAL_SWITCHES = Collections.unmodifiableList(Arrays.asList(OPEN, CLOSED));
	private static final int SIZE = OPERATIONAL_SWITCHES.size();
	
	private final String description;
	private final String pastTenseDescription;
	
	private SwitchStatus(String description, String pastTenseDescription) {
		this.description = description;
		this.pastTenseDescription = pastTenseDescription;
	}
	
	public String getDescription() {
		return description;
	}

	public String getPastTenseDescription() {
		return pastTenseDescription;
	}

	public static SwitchStatus getRandomAction() {
		return OPERATIONAL_SWITCHES.get(RANDOM.nextInt(SIZE));
	}
}
