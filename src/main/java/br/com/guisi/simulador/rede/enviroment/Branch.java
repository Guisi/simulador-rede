package br.com.guisi.simulador.rede.enviroment;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Classe representando uma branch
 * 
 * @author douglas.guisi
 */
public class Branch {

	private Integer number;
	private BranchId branchId;
	private NetworkNode node1;
	private NetworkNode node2;
	private double maxCurrent;
	private double resistance;
	private double reactance;
	private boolean switchBranch;
	private SwitchState switchState;
	
	private double instantCurrent;
	private double activeLossMW;
	private double reactiveLossMVar;
	
	public Branch(Integer number, NetworkNode node1, NetworkNode node2, double maxCurrent, double resistance, 
			double reactance, SwitchState switchState, boolean switchBranch, boolean fault) {
		this.number = number;
		this.node1 = node1;
		this.node2 = node2;
		this.maxCurrent = maxCurrent;
		this.resistance = resistance;
		this.reactance = reactance;
		this.switchState = switchState;
		this.switchBranch = switchBranch;
		this.branchId = new BranchId(node1.getNodeNumber(), node2.getNodeNumber());
	}
	
	/**
	 * Retorna se o switch está fechado (ligado)
	 * @return boolean
	 */
	public boolean isClosed() {
		return SwitchState.CLOSED.equals(switchState);
	}
	
	/**
	 * Retorna se o switch está aberto (desligado)
	 * @return
	 */
	public boolean isOpen() {
		return SwitchState.OPEN.equals(switchState);
	}
	
	/**
	 * Retorna se possui uma falta no switch
	 * @return
	 */
	public boolean hasFault() {
		return SwitchState.FAULT.equals(switchState);
	}
	
	/**
	 * Retorna se branch está desligado para isolamento de falta
	 * @return
	 */
	public boolean isIsolated() {
		return SwitchState.ISOLATED.equals(switchState);
	}
	
	/**
	 * Retorna verdadeiro se corrente necessária é maior que capacidade máxima do branch
	 * @return
	 */
	public boolean isMaxCurrentOverflow() {
		return maxCurrent < instantCurrent;
	}
	
	/**
	 * Retorna o load conectado por esta branch
	 * ao load passado como parametro
	 * @param networkNode
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getConnectedLoad(NetworkNode networkNode) {
		return node1.equals(networkNode) ? node2 : node1;
	}
	
	/**
	 * Retorna os nodes que o branch conecta
	 * @return
	 */
	public List<NetworkNode> getConnectedLoads() {
		return Arrays.asList(node1, node2);
	}
	
	/**
	 * Retorna a quantidade de potência disponível com relação à capacidade máxima
	 * @return double
	 */
	public double getAvailablePower() {
		return maxCurrent - instantCurrent;
	}
	
	/**
	 * Retorna a capacidade máxima desta branch (em ampères)
	 * @return double
	 */
	public double getMaxCurrent() {
		return maxCurrent;
	}

	/**
	 * Retorna o número desta branch
	 * @return {@link Integer}
	 */
	public Integer getNumber() {
		return number;
	}
	
	/**
	 * Retorna o {@link NetworkNode} de uma das pontas da branch
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNode1() {
		return node1;
	}
	
	public void setNode1(NetworkNode node1) {
		this.node1 = node1;
	}

	/**
	 * Retorna o {@link NetworkNode} de uma das pontas da branch
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNode2() {
		return node2;
	}
	public void setNode2(NetworkNode node2) {
		this.node2 = node2;
	}
	

	/**
	 * Retorna o {@link SwitchState} desta branch, aberto ou fechado
	 * @return
	 */
	public SwitchState getSwitchState() {
		return switchState;
	}

	/**
	 * Inverte o estado do switch (aberto -> fechado / fechado -> aberto)
	 * e incrementa o switchOperation
	 */
	public void reverse() {
		if (switchState == SwitchState.OPEN || switchState == SwitchState.CLOSED) {
			switchState = switchState == SwitchState.OPEN ? SwitchState.CLOSED : SwitchState.OPEN;
		}
	}
	
	public void isolateSwitch() {
		switchState = SwitchState.ISOLATED;
	}

	/**
	 * Retorna a resistencia do branch
	 * @return
	 */
	public double getResistance() {
		return resistance;
	}

	public void setResistence(double resistence) {
		this.resistance = resistence;
	}

	/**
	 * Retorna a reatancia do branch
	 * @return
	 */
	public double getReactance() {
		return reactance;
	}

	public void setReactance(double reactance) {
		this.reactance = reactance;
	}

	/**
	 * Retorna se esta branch é um switch
	 * @return boolean
	 */
	public boolean isSwitchBranch() {
		return switchBranch;
	}
	public void setSwitchBranch(boolean switchBranch) {
		this.switchBranch = switchBranch;
	}

	/**
	 * Retorna a corrente atual passando por esta branch
	 * @return
	 */
	public double getInstantCurrent() {
		return instantCurrent;
	}

	public void setInstantCurrent(double instantCurrent) {
		this.instantCurrent = instantCurrent;
	}

	public BranchId getBranchId() {
		return branchId;
	}

	/**
	 * Perda ativa em megawatts
	 * @return
	 */
	public double getActiveLossMW() {
		return activeLossMW;
	}

	public void setActiveLossMW(double activeLossMW) {
		this.activeLossMW = activeLossMW;
	}

	/**
	 * Perda reativa em mega volt ampères
	 * @return
	 */
	public double getReactiveLossMVar() {
		return reactiveLossMVar;
	}

	public void setReactiveLossMVar(double reactiveLossMVar) {
		this.reactiveLossMVar = reactiveLossMVar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Branch other = (Branch) obj;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
