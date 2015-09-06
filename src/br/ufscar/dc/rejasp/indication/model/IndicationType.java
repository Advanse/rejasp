package br.ufscar.dc.rejasp.indication.model;

/**
 * @author Daniel Kawakami
 * An abstract structure for storage indication information of types (class,
 * interface or exception)
 */
public abstract class IndicationType {
	protected String sName;
	protected String sDescription;
	public IndicationType(String sName) {
		this.sName = sName;
		this.sDescription = "";
	}
	public IndicationType(String sName, String sDescription) {
		this.sName = sName;
		this.sDescription = sDescription;
	}
	public String getName() {
		return sName;
	}
	public String getDescription() {
		return sDescription;
	}
	public void setName(String sName) {
		this.sName = sName;
	}
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}
}
