package br.ufscar.dc.rejasp.indication.model;

/**
 * @author Daniel Kawakami
 * Storage exception information that is considered an indication.
 */
public class IndicationException extends IndicationType {
	public IndicationException(String sName) {
		super(sName);
	}
	public IndicationException(String sName, String sDescription) {
		super(sName, sDescription);
	}
	public Object clone() {
		IndicationException clone = new IndicationException(this.sName, this.sDescription);
		return clone;
	}
}
