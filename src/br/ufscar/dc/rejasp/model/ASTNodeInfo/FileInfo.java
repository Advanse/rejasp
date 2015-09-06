package br.ufscar.dc.rejasp.model.ASTNodeInfo;

import java.util.ArrayList;

public class FileInfo extends ModelInfo {
	private String sPath;
	private ArrayList lstTypes;
	
	public FileInfo() {
		super();
		lstTypes = new ArrayList();
	}
	
	public FileInfo(String sPath) {
		super();
		this.sPath = sPath;
		lstTypes = new ArrayList();
	}
	
	/**
	 * @return Returns a relative path from workspace directory.
	 */
	public String getPath() {
		return sPath;
	}
	
	/**
	 * @return Returns a live list of types (classes and interfaces). 
	 */
	public ArrayList getTypes() {
		return lstTypes;
	}
	
	public String getPackage() {
		String sPackage = "";
		// p.ex: sPath = \agenciaBancaria\Bank\Account\Account.java
		sPackage = sPath.substring(1);
		// p.ex: sPackage = agenciaBancaria\Bank\Account\Account.java
		int nIndex = sPackage.indexOf('\\');
		sPackage = sPackage.substring(nIndex + 1);
		// p.ex: sPackage = Bank\Account\Account.java
		nIndex = sPackage.lastIndexOf('\\');
		if(nIndex == -1)	// Não achou nenhum pacote
			return "";
		sPackage = sPackage.substring(0, nIndex);
		// p.ex: sPackage = Bank\Account
		sPackage = sPackage.replace('\\', '.');
		// p.ex: sPackage = Bank.Account
		return sPackage;
	}
	
	public String getName() {
		int nIndex = sPath.lastIndexOf('\\');
		if (nIndex == -1) {
			System.err.println("File name not found");
			return null;
		}
		nIndex++;
		return sPath.substring(nIndex, sPath.lastIndexOf('.'));
	}

	/**
	 * Sets the relative path from workspace directory.
	 * @param sPath
	 */
	public void setPath(String sPath) {
		this.sPath = sPath; 
	}
	
	public void addType(TypeInfo type) {
		lstTypes.add(type);
		type.setFile(this);
	}
}
