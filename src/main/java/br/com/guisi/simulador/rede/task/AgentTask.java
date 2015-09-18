package br.com.guisi.simulador.rede.task;

import javafx.concurrent.Task;

public class AgentTask extends Task<Integer> {

	private Integer count;
	
	public AgentTask(Integer count) {
		this.count = count;
	}
	
	@Override
	protected Integer call() throws Exception {
		for (int i = 0; i < 100; i++) {
			count++;
			updateValue(count);
			Thread.sleep(50);
		}
		return count;
	}
}
