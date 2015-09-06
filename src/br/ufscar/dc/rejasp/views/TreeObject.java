package br.ufscar.dc.rejasp.views;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.dom.ASTNode;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.ModelInfo;
/**
 * @author Daniel Kawakami
 * Leaf nodes of the tree model
 */
public class TreeObject implements IAdaptable {
	public static final int REFACTOR_FIELD = 1;
	public static final int REFACTOR_METHOD = 1 << 1;
	public static final int REFACTOR_EXTRACT_BEGINNIG = 1 << 2;
	public static final int REFACTOR_EXTRACT_END = 1 << 3;
	public static final int REFACTOR_EXTRACT_BEFORE_CALL = 1 << 4;
	public static final int REFACTOR_EXTRACT_AFTER_CALL = 1 << 5;
	public static final int REFACTOR_EXTRACT_CONDICIONAL = 1 << 6;
	public static final int REFACTOR_PRE_RETURN = 1 << 7;
	public static final int REFACTOR_EXTRACT_WRAPPER = 1 << 8;
	public static final int JOIN_POINT_BEFORE_CALL = 1 << 9;
	public static final int JOIN_POINT_AFTER_CALL = 1 << 10;
	
	private long id;
	private String name;
	private String type;
	private TreeParent parent;
	private ASTNode astNode;
	private int refactoring;
	private ArrayList lstInfo;
	
	public TreeObject(String name, String type) {
		this.name = name;
		this.type = type;
		this.parent = null;
		this.refactoring = 0;
		lstInfo = new ArrayList();
	}
	public TreeObject(String name, String type, ASTNode astNode) {
		this.name = name;
		this.type = type;
		this.astNode = astNode;
		this.parent = null;
		this.refactoring = 0;
		lstInfo = new ArrayList();
	}
	public TreeObject(String name) {
		this.name = name;
		this.parent = null;
		this.refactoring = 0;
		lstInfo = new ArrayList();
	}
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	public void setId( long id ) {
		this.id = id;
	}
	public TreeParent getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	public Object getAdapter(Class key) {
		return null;
	}
	public String getType() {
		return type;
	}
	public ASTNode getAstNode() {
		return astNode;
	}
	public int getRefactoring() {
		return refactoring;
	}
	public void addRefatoring(int refactoring) {
		this.refactoring |= refactoring;
	}
	public ArrayList getInfo() {
		return lstInfo;
	}
	public void addInfo(ModelInfo modelInfo) {
		lstInfo.add(modelInfo);
	}
}
