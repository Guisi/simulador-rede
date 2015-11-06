package br.com.guisi.simulador.rede.task;

import java.util.Observable;
import java.util.Observer;

import javafx.concurrent.Task;
import br.com.guisi.simulador.rede.constants.TaskExecutionType;
import br.com.guisi.simulador.rede.qlearning.QLearningAgent;
import br.com.guisi.simulador.rede.qlearning.QLearningStatus;

public class AgentTask extends Task<QLearningStatus> implements Observer {

	private TaskExecutionType executionType;
	private boolean stop;
	private QLearningAgent qLearningAgent;
	
	public AgentTask(TaskExecutionType executionType, QLearningAgent qLearningAgent) {
		super();
		this.executionType = executionType;
		this.qLearningAgent = qLearningAgent;
		this.qLearningAgent.addObserver(this);
	}

	@Override
	protected QLearningStatus call() throws Exception {
		switch (executionType) {
			case CONTINUOUS_UPDATE_END_ONLY:
				while (true) {
					if (stop) {
						break;
					}
				}
			case CONTINUOUS_UPDATE_EVERY_STEP:
				while (true) {
					qLearningAgent.run();
					if (stop) {
						break;
					}
				}
			case STEP_BY_STEP:
				qLearningAgent.run();
				break;
		}
		return qLearningAgent.getStatus();
	}
	
	@Override
	protected void cancelled() {
		super.cancelled();
		stop = true;
		updateValue(qLearningAgent.getStatus());
	}
	
	@Override
	public void update(Observable o, Object arg) {
		updateValue((QLearningStatus) arg);
	}

	public TaskExecutionType getExecutionType() {
		return executionType;
	}

	public void setExecutionType(TaskExecutionType executionType) {
		this.executionType = executionType;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public QLearningAgent getQLearningAgent() {
		return qLearningAgent;
	}

	public void setQLearningAgent(QLearningAgent qLearningAgent) {
		this.qLearningAgent = qLearningAgent;
	}
	
}
