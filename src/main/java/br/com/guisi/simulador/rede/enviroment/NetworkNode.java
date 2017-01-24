package br.com.guisi.simulador.rede.enviroment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.guisi.simulador.rede.constants.Constants;
import br.com.guisi.simulador.rede.constants.Status;

/**
 * Classe representando um n� da rede (Load ou Feeder)
 * 
 * @author douglas.guisi
 */
public abstract class NetworkNode implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Integer nodeNumber;
	protected Integer x;
	protected Integer y;
	protected double activePowerKW;
	protected double reactivePowerKVar;
	protected double currentVoltagePU;
	protected Status status;
	protected List<Branch> branches = new ArrayList<>();
	
	public NetworkNode(Integer nodeNumber, Integer x, Integer y, double activePowerKW, double reactivePowerKVar, Status status) {
		super();
		this.nodeNumber = nodeNumber;
		this.x = x;
		this.y = y;
		this.activePowerKW = activePowerKW;
		this.reactivePowerKVar = reactivePowerKVar;
		this.status = status;
	}

	public void addBranch(Branch branch) {
		if (!branches.contains(branch)) {
			branches.add(branch);
		}
	}
	
	/**
	 * Retorna se este n� � um {@link Feeder}
	 * @return boolean
	 */
	public boolean isFeeder() {
		return this instanceof Feeder;
	}
	
	/**
	 * Retorna se este n� � um {@link Load}
	 * @return boolean
	 */
	public boolean isLoad() {
		return this instanceof Load;
	}
	
	/**
	 * Retorna se este n� est� ligado
	 * @return boolean
	 */
	public boolean isOn() {
		return Status.ON.equals(status);
	}
	
	/**
	 * Retorna se este n� est� desligado
	 * @return boolean
	 */
	public boolean isOff() {
		return Status.OFF.equals(status);
	}
	
	/**
	 * Retorna se este n� est� isolado por alguma falta
	 * @return boolean
	 */
	public boolean isIsolated() {
		return Status.ISOLATED.equals(status);
	}
	
	/**
	 * Desliga o load
	 */
	public void turnOff() {
		this.status = Status.OFF;
	}
	
	/**
	 * Liga o load
	 */
	public void turnOn() {
		this.status = Status.ON;
	}

	/**
	 * Retorna a posi��o X deste n� no ambiente
	 * @return {@link Integer}
	 */
	public Integer getX() {
		return x;
	}

	/**
	 * Retorna a posi��o Y deste n� no ambiente
	 * @return {@link Integer}
	 */
	public Integer getY() {
		return y;
	}

	/**
	 * Retorna um {@link List<Branch>} com os branches ligados neste n�
	 * @return {@link List<Branch>}
	 */
	public List<Branch> getBranches() {
		return branches;
	}
	
	/**
	 * Retorna o n�mero deste n�
	 * @return {@link Integer}
	 */
	public Integer getNodeNumber() {
		return nodeNumber;
	}

	/**
	 * Retorna a pot�ncia ativa deste n� em kW
	 * @return double
	 */
	public double getActivePowerKW() {
		return activePowerKW * 0.9; //TODO usar 100%
	}
	
	public double getActivePowerMW() {
		return getActivePowerKW() / 1000;
	}
	
	/**
	 * Retorna a pot�ncia reativa deste n� em KVar
	 * @return double
	 */
	public double getReactivePowerKVar() {
		return reactivePowerKVar * 0.75; //TODO usar 100%
	}
	
	public double getReactivePowerMVar() {
		return getReactivePowerKVar() / 1000;
	}

	/**
	 * Retorna o {@link Status} deste n�, on ou off
	 * @return {@link Status}
	 */
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Voltagem atual em PU
	 * @return
	 */
	public double getCurrentVoltagePU() {
		return currentVoltagePU;
	}

	public void setCurrentVoltagePU(double currentVoltagePU) {
		this.currentVoltagePU = currentVoltagePU;
	}
	
	public boolean isCurrentVoltageBelowLimit() {
		return currentVoltagePU < Constants.TENSAO_MIN_PU; 
	}
	
	public boolean isCurrentVoltageAboveLimit() {
		return currentVoltagePU > Constants.TENSAO_MAX_PU; 
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodeNumber;
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
		NetworkNode other = (NetworkNode) obj;
		if (nodeNumber != other.nodeNumber)
			return false;
		return true;
	}
	
	public String toStringReflection() {
		return ToStringBuilder.reflectionToString(this);
	}
}
