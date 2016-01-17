package br.com.guisi.simulador.rede.agent.qlearning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.context.annotation.Scope;

import br.com.guisi.simulador.rede.SimuladorRede;
import br.com.guisi.simulador.rede.agent.Agent;
import br.com.guisi.simulador.rede.agent.status.AgentInformationType;
import br.com.guisi.simulador.rede.agent.status.AgentStepStatus;
import br.com.guisi.simulador.rede.agent.status.LearningProperty;
import br.com.guisi.simulador.rede.agent.status.SwitchOperation;
import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.enviroment.Branch;
import br.com.guisi.simulador.rede.enviroment.Environment;
import br.com.guisi.simulador.rede.enviroment.SwitchDistance;
import br.com.guisi.simulador.rede.enviroment.SwitchState;
import br.com.guisi.simulador.rede.util.PowerFlow;

@Named
@Scope("prototype")
public class QLearningAgent extends Agent {
	
	private QTable qTable;
	private Branch firstSwitch;
	private Branch secondSwitch;
	
	private Map<Integer, List<SwitchDistance>> visitedSwitchesMap;
	
	private final Random RANDOM = new Random(System.currentTimeMillis());

	@PostConstruct
	public void init() {
		this.reset();
	}
	
	@Override
	public void reset() {
		this.qTable = new QTable();
		this.visitedSwitchesMap = new HashMap<>();
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
		
		//TODO pensar em l�gica para evitar ficar voltando para switch que j� passou
		
		if (randomAction) {
			//busca o switch a ser aberto
			firstSwitch = getClosestSwitch(environment, firstSwitch, SwitchState.CLOSED);

			if (!firstSwitch.hasFault()) {
				firstSwitch.reverse();
				switchOperations.add(new SwitchOperation(firstSwitch.getNumber(), firstSwitch.getSwitchState()));
			}
			
			//busca o switch a ser fechado
			secondSwitch = getClosestSwitch(environment, firstSwitch, SwitchState.OPEN);
			secondSwitch.reverse();
			
			//TODO adicionar while verificando se a rede est� radial
			//somente para o switch fechado, pois abrir switch n�o causa perda de radialidade
			
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
			//sen�o, busca o switch mais pr�ximo
			
			//busca uma lista dos switches mais pr�ximos
			List<SwitchDistance> closestSwitches = environment.getClosestSwitches(refSwitch, switchState);
			
			//quando est� procurando um switch para abrir, verifica switches j� visitados
			List<SwitchDistance> visitedSwitches = null;
			if (switchState == SwitchState.CLOSED) {
				visitedSwitches = visitedSwitchesMap.get(refSwitch.getNumber());
				if (visitedSwitches != null) {
					//se para o switch de refer�ncia, j� foi visitado todos os mais pr�ximos, limpa a lista de visitados
					if (visitedSwitches.containsAll(closestSwitches)) {
						visitedSwitches.clear();
					} else {
						//sen�o, remove os j� visitados para que visite os demais
						closestSwitches.removeAll(visitedSwitches);
					}
				} else {
					visitedSwitches = new ArrayList<>();
					visitedSwitchesMap.put(refSwitch.getNumber(), visitedSwitches);
				}
			}

			//Verifica o menor valor de dist�ncia encontrado
			Integer minDistance = closestSwitches.stream().min(Comparator.comparing(value -> value.getDistance())).get().getDistance();
			
			//filtra por todos os switches da lista com a menor dist�ncia
			closestSwitches = closestSwitches.stream().filter(valor -> valor.getDistance() == minDistance).collect(Collectors.toList());
			
			//retorna um dos switches mais pr�ximos aleatoriamente
			SwitchDistance switchDistance = closestSwitches.get(RANDOM.nextInt(closestSwitches.size()));
			sw = switchDistance.getTheSwitch();
			
			//adiciona o switch na lista de visitados
			if (switchState == SwitchState.CLOSED) {
				visitedSwitches.add(switchDistance);
			}
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
