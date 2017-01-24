package br.com.guisi.simulador.rede.enviroment;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.agent.qlearning.Cluster;


/**
 * Classe representando uma branch
 * 
 * @author douglas.guisi
 */
public class Branch implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer number;
	private BranchKey branchKey;
	private NetworkNode node1;
	private NetworkNode node2;
	private double maxCurrent;
	private double resistance;
	private double reactance;
	private boolean switchBranch;
	private SwitchStatus switchStatus;
	private int switchIndex;
	private boolean tieSwitchCandidate;
	
	private double instantCurrent;
	private double activeLossMW;
	private double reactiveLossMVar;
	
	private SwitchStatus statusBeforeFault;
	
	private Cluster cluster;
	
	public Branch(Integer number, NetworkNode node1, NetworkNode node2, double maxCurrent, double resistance, 
			double reactance, SwitchStatus switchStatus, boolean switchBranch, boolean fault) {
		this.number = number;
		this.node1 = node1;
		this.node2 = node2;
		this.maxCurrent = maxCurrent;
		this.resistance = resistance;
		this.reactance = reactance;
		this.switchStatus = switchStatus;
		this.switchBranch = switchBranch;
	}
	
	/**
	 * Retorna se o switch está fechado (ligado)
	 * @return boolean
	 */
	public boolean isClosed() {
		return SwitchStatus.CLOSED.equals(switchStatus);
	}
	
	/**
	 * Retorna se o switch está aberto (desligado)
	 * @return
	 */
	public boolean isOpen() {
		return SwitchStatus.OPEN.equals(switchStatus);
	}
	
	/**
	 * Retorna se possui uma falta no switch
	 * @return
	 */
	public boolean hasFault() {
		return SwitchStatus.FAULT.equals(switchStatus);
	}
	
	/**
	 * Retorna se branch está desligado para isolamento de falta
	 * @return
	 */
	public boolean isIsolated() {
		return SwitchStatus.ISOLATED.equals(switchStatus);
	}
	
	/**
	 * Retorna verdadeiro se corrente necessária é maior que capacidade máxima do branch
	 * @return
	 */
	public boolean isMaxCurrentOverflow() {
		return maxCurrent < instantCurrent;
	}
	
	/**
	 * Retorna os nodes que o branch conecta
	 * @return
	 */
	public List<NetworkNode> getConnectedNodes() {
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
	 * Retorna o {@link NetworkNode} de uma das pontas da branch, de onde a corrente está vindo
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNodeFrom() {
		return branchKey != null ? branchKey.getNodeFrom() : node1;
	}
	
	/**
	 * Retorna o {@link NetworkNode} de uma das pontas da branch, para onde a corrente está indo
	 * @return {@link NetworkNode}
	 */
	public NetworkNode getNodeTo() {
		return branchKey != null ? branchKey.getNodeTo() : node2;
	}

	/**
	 * Retorna o {@link SwitchStatus} desta branch, aberto ou fechado
	 * @return
	 */
	public SwitchStatus getSwitchStatus() {
		return switchStatus;
	}
	
	public void setSwitchStatus(SwitchStatus switchStatus) {
		this.switchStatus = switchStatus;
	}

	/**
	 * Inverte o estado do switch (aberto -> fechado / fechado -> aberto)
	 * e incrementa o switchOperation
	 */
	public void reverse() {
		if (switchStatus == SwitchStatus.OPEN || switchStatus == SwitchStatus.CLOSED) {
			switchStatus = switchStatus == SwitchStatus.OPEN ? SwitchStatus.CLOSED : SwitchStatus.OPEN;
		}
	}
	
	public void isolateSwitch() {
		switchStatus = SwitchStatus.ISOLATED;
	}
	
	public SwitchStatus getReverseStatus() {
		if (switchStatus == SwitchStatus.OPEN || switchStatus == SwitchStatus.CLOSED) {
			return switchStatus == SwitchStatus.OPEN ? SwitchStatus.CLOSED : SwitchStatus.OPEN;
		}
		return switchStatus;
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

	public BranchKey getBranchKey() {
		return branchKey;
	}

	public void setBranchKey(BranchKey branchKey) {
		this.branchKey = branchKey;
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

	public int getSwitchIndex() {
		return switchIndex;
	}

	public void setSwitchIndex(int switchIndex) {
		this.switchIndex = switchIndex;
	}
	
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public boolean isTieSwitchCandidate() {
		return tieSwitchCandidate;
	}

	public void setTieSwitchCandidate(boolean tieSwitchCandidate) {
		this.tieSwitchCandidate = tieSwitchCandidate;
	}

	public SwitchStatus getStatusBeforeFault() {
		return statusBeforeFault;
	}

	public void setStatusBeforeFault(SwitchStatus statusBeforeFault) {
		this.statusBeforeFault = statusBeforeFault;
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
		return "Branch [number=" + number + "]";
	}
	
	public String toStringReflection() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}
