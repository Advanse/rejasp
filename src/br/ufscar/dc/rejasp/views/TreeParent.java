package br.ufscar.dc.rejasp.views;

import java.util.ArrayList;

import br.ufscar.dc.rejasp.views.IndicationTree.ViewContentProvider;

/**
 * @author Daniel Kawakami
 * Non-leaf nodes fo tree model.
 */
public class TreeParent extends TreeObject {
	private ArrayList children;
	public TreeParent(String name, String type) {
		super(name,type);
		children = new ArrayList();
	}
	public TreeParent(String name) {
		super(name);
		children = new ArrayList();
	}
	public void addChild(TreeObject child) {
		children.add(child);
		child.setParent(this);
	}
	public void removeChild(TreeObject child) {
		children.remove(child);
		child.setParent(null);
	}
	public void removeAllChildren() {
		int nCount = children.size();
		for(int i = 0; i < nCount; i++)
			((TreeObject)children.remove(0)).setParent(null);
	}
	public TreeObject [] getChildren() {
		return (TreeObject [])children.toArray(new TreeObject[children.size()]);
	}
	public boolean hasChildren() {
		return children.size() > 0;
	}
	public Object getChildrenAt(int i) {
		if (i >= children.size()) return null;
		return children.get(i);
	}
	/**
	 * Update only a file node that which path is described by
	 * sRelativePath.
	 * @param provider
	 * @param sRelativePath It's a relative path considering the workspace
	 * path as the reference path.. P. ex.: sRelativePath = 
	 * "/AgenciaBancaria/Bank/Account.java" where AgenciaBancaria = project name,
	 * Bank = package, and Account.java = java file
	 */
	public void updateFileNode (ViewContentProvider provider, String sRelativePath) {
		// Find the FileNode in the tree viewer
		// Visiting object node
		TreeParent visitedNode = (TreeParent)((TreeParent)getChildrenAt(0)).getChildrenAt(0);
		TreeObject[] children = visitedNode.getChildren();
		if(children.length == 0) {
			System.err.println("No projects node found");
			return;
		}
		String[] paths = sRelativePath.split("/");
		// Visit project node
		for( int i = 0; i < children.length; i++ )
			if( children[i].getName().equals(paths[0]) ) {
				visitedNode = (TreeParent)children[i];
				return;
			}
		// Project not found
		if(!visitedNode.getName().equals(paths[0])) {
			System.err.println("Project " + paths[0] + " not found in tree");
			return;
		}
		
		// Figure out the package
		String sPackage = "";
		for(int i = 1; i < paths.length - 1; i++)
			sPackage += paths[i] + '.';
		
		// Visit package node
		if ( ! (sPackage.length() == 0) ) {
			children = visitedNode.getChildren();
			for(int i = 0; i < children.length; i++) 
				if (children[i].getName().equals(sPackage)){
					visitedNode = (TreeParent)children[i];
					break;
				}
			// Package not found
			if(!visitedNode.getName().equals(sPackage)) {
				System.err.println("Package " + sPackage + " not found in tree");
				return;
			}
		}
		
		// Visit file node
		children = visitedNode.getChildren();
		if(children.length == 0) {
			System.err.println("No file node found");
			return;
		}
		String sFile = paths[paths.length - 1];
		for(int i = 0; i < children.length; i++)
			if(sFile.equals(children[i].getName())) {
				visitedNode = (TreeParent)children[i];
				break;
			}
		// File not found
		if(!visitedNode.getName().equals(sFile)) {
			System.err.println("File " + sFile + " not found in tree");
			return;
		}
		provider.constructFileNode(visitedNode, provider.getFile(visitedNode));
	}
}
