package br.ufscar.dc.rejasp.indication.model;

/**
 * @author Daniel Kawakami
 * Storage class data that are represent an aspect indication.
 */
public class IndicationClass extends IndicationType {
	public IndicationClass(String sName) {
		super(sName);
	}
	public IndicationClass(String sName, String sDescription) {
		super(sName, sDescription);
	}
	public Object clone() {
		IndicationClass clone = new IndicationClass(this.sName, this.sDescription);
		return clone;
	}
}
