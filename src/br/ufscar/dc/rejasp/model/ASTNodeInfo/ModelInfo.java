package br.ufscar.dc.rejasp.model.ASTNodeInfo;

public abstract class ModelInfo {
	protected boolean bIndication;

	public ModelInfo() {
		bIndication = false;
	}
	
	public boolean getIndication() {
		return bIndication;
	}

	public void setIndication(boolean bIndication) {
		this.bIndication = bIndication;
	}
	

}
