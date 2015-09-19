package br.com.guisi.simulador.rede.task;

import javafx.concurrent.Task;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;

public class AgentTask extends Task<Integer> {

	private Integer count;
	private TaskExecutionType executionType;
	private boolean stop;
	
	public AgentTask(Integer count, TaskExecutionType executionType) {
		this.count = count;
		this.executionType = executionType;
	}
	
	@Override
	protected Integer call() throws Exception {
		switch (executionType) {
			case CONTINUOUS_UPDATE_END_ONLY:
				while (true) {
					count++;
					if (stop) {
						break;
					}
				}
			case CONTINUOUS_UPDATE_EVERY_STEP:
				while (true) {
					count++;
					updateValue(count);
					if (stop) {
						break;
					}
				}
			case STEP_BY_STEP:
				count++;
				updateValue(count);
				break;
		}
		return count;
	}
	
	@Override
	protected void cancelled() {
		super.cancelled();
		stop = true;
		updateValue(count);
	}
}
