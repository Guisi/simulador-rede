package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchState;
import br.com.guisi.simulador.rede.util.PowerFlow;

@Named
public class QLearningAgent extends Agent {
	
	private QTable qTable;
	private Branch firstSwitch;
	private Branch secondSwitch;

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
	}
	
	/**
	 * Realiza uma intera��o no ambiente
	 * @param agentStepStatus
	 * @return {@link Integer} estado para o qual o agente se moveu
	 */
	@Override
	protected void runNextEpisode(AgentStepStatus agentStepStatus) {
		Environment environment = SimuladorRede.getEnvironment();
		
		List<SwitchOperation> switchOperations = new ArrayList<>();
		
		//Se randomico menor que E-greedy, escolhe melhor acao
		boolean randomAction = (Math.random() >= Constants.E_GREEDY);
		
		//TODO remover
		randomAction = true;
		
		//busca o switch a ser aberto
		firstSwitch = getClosestSwitch(environment, secondSwitch, SwitchState.CLOSED);
		
		if (randomAction) {
			//TODO adicionar while verificando se a rede est� radial
			
			if (!firstSwitch.hasFault()) {
				firstSwitch.reverse();
				switchOperations.add(new SwitchOperation(firstSwitch.getNumber(), firstSwitch.getSwitchState()));
			}
			
			//busca o switch a ser fechado
			secondSwitch = getClosestSwitch(environment, firstSwitch, SwitchState.OPEN);
			secondSwitch.reverse();
			switchOperations.add(new SwitchOperation(secondSwitch.getNumber(), secondSwitch.getSwitchState()));
			
			agentStepStatus.putInformation(AgentInformationType.SWITCH_OPERATIONS, switchOperations);
			
			try {
				//executa o fluxo de pot�ncia
				PowerFlow.execute(environment);
				
				//TODO s� atualizar quando mexer nos switches mesmo??
				
				//atualiza o qValue do primeiro switch
				updateQValue(firstSwitch);
				
				//atualiza o qValue do segundo switch
				updateQValue(secondSwitch);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
        /*
        //verifica se mudou pol�tica, somente se a��o n�o foi aleat�ria
        boolean changedPolicy = false;
        if (!randomAction) {
        	Action newBestAction = qTable.getBestAction(state);
        	changedPolicy = !action.equals(newBestAction);
        	this.getStatus().incrementPolicyChangeCount();
        }
        
        //crit�rio de parada trata o fim de uma intera��o
        stoppingCriteria.handleEpisode(randomAction, changedPolicy);
        
        //se foi para o objetivo, vai para uma posi��o aleat�ria no ambiente
        if (environment.isTarget(nextState)) {
        	nextState = environment.getRandomInitialStateForAgent();
        }
        */
	}
	
	private void updateQValue(Branch sw) {
		//recupera em sua QTable o valor de recompensa para o estado/a��o que estava antes
		QValue qValue = qTable.getQValue(sw.getNumber(), sw.getSwitchState());
        double q = qValue.getReward();
        
        //recupera em sua QTable o melhor valor para o estado para o qual se moveu
        //TODO ver o que vai ser esse nextState 
        //double nextStateQ = qTable.getBestQValue(nextState).getReward();
        
        //recupera a recompensa retornada pelo ambiente por ter realizado a a��o
        double r = 0;//TODO actionResult.getReward();

        //Algoritmo Q-Learning -> calcula o novo valor para o estado/a��o que estava antes
        //TODO double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * nextStateQ) - q);
        double value = q + Constants.LEARNING_CONSTANT * (r + (Constants.DISCOUNT_FACTOR * q) - q);
        
        //atualiza sua QTable com o valor calculado pelo algoritmo
        qValue.setReward(value);
	}
	
	/**
	 * Busca o switch mais pr�ximo ao switch passado
	 * @param environment
	 * @param refSwitch Switch de refer�ncia para busca do mais pr�ximo 
	 * @param switchState Estado a ser utilizado como filtro na busca
	 * @return
	 */
	public Branch getClosestSwitch(Environment environment, Branch refSwitch, SwitchState switchState) {
		Branch sw = null;

		//se o estado atual est� nulo, � a primeira intera��o
		if (refSwitch == null) {
			//verifica se existe alguma falta 
			sw = environment.getRandomFault();
			//se n�o existe, inicia por um switch aberto aleat�rio
			if (sw == null) {
				sw = environment.getRandomSwitch(switchState);
			}
		} else {
			//sen�o, busca o switch
			sw = environment.getClosestSwitch(refSwitch, switchState);
		}
		
		return sw;
	}
	
	public QValue getBestQValue(Integer state) {
		return qTable.getBestQValue(state);
	}
	
	public double getGreaterReward() {
		return qTable.getGreaterReward();
	}
	
	public double getLowerReward() {
		return qTable.getLowerReward();
	}
	
	public List<QValue> getQValues(Integer state) {
		return qTable.getQValues(state);
	}
	
	@Override
	public List<LearningProperty> getLearningProperties(Integer state) {
		List<QValue> qValues = this.getQValues(state);
		List<LearningProperty> learningProperties = new ArrayList<>();
		for (QValue qValue : qValues) {
			LearningProperty row = new LearningProperty("Q(s, " + qValue.getQKey().getAction().getDescription() + "):", String.valueOf(qValue.getReward()));
			learningProperties.add(row);
		}
		return learningProperties;
	}
}
