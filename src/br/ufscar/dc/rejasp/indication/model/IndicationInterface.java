package br.ufscar.dc.rejasp.indication.model;

/**
 * @author Daniel Kawakami
 * Storage interface information that is considered an indication.
 */
public class IndicationInterface extends IndicationType {
	public IndicationInterface(String sName) {
		super(sName);
	}
	public IndicationInterface(String sName, String sDescription) {
		super(sName, sDescription);
	}
	public Object clone() {
		IndicationInterface clone = new IndicationInterface(this.sName, this.sDescription);
		return clone;
	}
}
