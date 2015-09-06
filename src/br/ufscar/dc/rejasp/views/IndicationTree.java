package br.ufscar.dc.rejasp.views;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import br.ufscar.dc.rejasp.file.xml.IndicationXml;
import br.ufscar.dc.rejasp.indication.detection.DetectIndication;
import br.ufscar.dc.rejasp.indication.model.Indication;
import br.ufscar.dc.rejasp.indication.model.IndicationClass;
import br.ufscar.dc.rejasp.indication.model.IndicationException;
import br.ufscar.dc.rejasp.indication.model.IndicationInterface;
import br.ufscar.dc.rejasp.indication.model.IndicationPackage;
import br.ufscar.dc.rejasp.indication.model.MatchText;
import br.ufscar.dc.rejasp.plugin.Plugin;
import br.ufscar.dc.rejasp.wizards.refactoring.IntroductionFieldWizard.IntroductionFieldWizard;
import br.ufscar.dc.rejasp.wizards.refactoring.IntroductionMethodWizard.IntroductionMethodWizard;

/**
 * @author Daniel Kawakami
 * ISelectionListener é implementado para escutar o evento de seleção
 *  de algum elemento na view de Package Explorer
 */
public class IndicationTree extends ViewPart
	implements ISelectionListener
{
	private TreeViewer viewer;
	private ViewLabelProvider labelProvider;
	private DrillDownAdapter drillDownAdapter;
	private ArrayList projectActions, indicationActions; 
	private Action refreshAction;
	private Action doubleClickAction;
	private Action openViewAction;
	IPropertyChangeListener  preferenceListener =
		new IPropertyChangeListener() {
		/*
		 * @see IPropertyChangeListener.propertyChange()
		 */
		public void propertyChange(PropertyChangeEvent event) {

			if (event.getProperty().equals(Plugin.FILTER_BY_IMPORT_PREFERENCE)) {
				//Update the indicationTree
				IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
				ViewContentProvider indicationView = (ViewContentProvider)viewer.getContentProvider();
				IProject project = workspace.getProject(indicationView.getProjectName());
				if(project != null) {
					indicationView.updateTree(project);
				}
				else
					showMessage("The project " + indicationView.getProjectName() + " was not found.");
			}
		}
	};

	
	/**
	 * memento makes possible the persistence of configuration of plugin
	 */
	private IMemento memento;
	/**
	 * List of indication model. Elements' type is Indication 
	 */
	private ArrayList indications;
	/**
	 * Full path for xml file that describes all indications
	 */
	private String sXmlFilePath = "";
	
	/**
	 * List of all fields with aspect indication. The type of each element is VariableDeclarationFragment.
	 * This list will be used to introduce fields in a aspect.
	 */
	//private ArrayList lstIndicationFields;
	
	/**
	 * List of all methods with aspect indication. The type of each element is MethodDeclaration.
	 * This list will be used to introduce methods in a aspect.
	 */
	//private ArrayList lstIndicationMethods;
	
	private ArrayList lstFileInfo;
	
	public static final String PROJECT = "rejasp.project"; 
	public static final String OBJECT = "rejasp.object";
	public static final String ASPECT = "rejasp.aspect";
	public static final String PACKAGE = "rejasp.package";
	public static final String EMPTY_PACKAGE = "rejasp.package.empty";
	public static final String FILE = "rejasp.file";
	public static final String CLASS = "rejasp.class";
	public static final String METHOD_PUBLIC = "rejasp.method.PUBLIC";
	public static final String METHOD_PRIVATE = "rejasp.method.PRIVATE";
	public static final String METHOD_PROTECTED = "rejasp.method.PROTECTED";
	public static final String ASPECT_FILE = "rejasp.aspect.file";
	public static final String ASPECT_FOLDER = "rejasp.aspect.folder";
	public static final String ASPECT_MODULE = "rejasp.aspect.module";
	public static final String ASPECT_ADVICE = "rejasp.aspect.advice";
	public static final String ASPECT_INDICATION = "rejasp.indication.persistence";
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	/**
	 * @author Daniel
	 *
	 */
	public class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		/**
		 * It's the model part of indication tree
		 */
		private TreeParent invisibleRoot;
		
		private ArrayList lstAspectFile;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null) restoreState();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
		public boolean isEmpty() {
			return invisibleRoot == null;
		}
		
		/**
		 * @return the name of project represented in the indication tree.
		 */
		public String getProjectName() {
			if(invisibleRoot != null && invisibleRoot.getChildren() != null) {
				TreeObject project = invisibleRoot.getChildren()[0];
				return project.getName();
			}
			else
				return null;
		}
		/**
		 * @return clone of each indication
		 */
		public ArrayList getIndications() {
			ArrayList lstIndications = new ArrayList();
			for ( int i = 0; i < indications.size(); i++ )
				lstIndications.add(((Indication)indications.get(i)).clone());
			return lstIndications;
		}
		
		public TreeParent getInvisibleRoot() {
			return invisibleRoot;
		}

		/**
		 * Função que gera uma string que representa o elemento aberto na árvore
		 * de indícios. O elemento pode estar dentro de níveis na árvore. O caractere
		 *'\' delimita cada nível. Essa função é importante para retomar o último
		 *local navegado pelo usuário
		 * @param selection: é o objeto selecionado na árvore
		 * @return string que descreve elemento e seu caminho na árvore de indícios
		 */
		public String getTreeSelection(Object selection) {
			String sSelection = "";
			if ( selection instanceof TreeObject ) {
				sSelection = ((TreeObject)selection).getName();
				TreeParent treeParent = ((TreeObject)selection).getParent();
				while (treeParent != null && treeParent.getName().compareTo("") != 0) {
					sSelection = treeParent.getName() + "\\" + sSelection;
					treeParent = treeParent.getParent();
				}
			}
			return sSelection;
		}
	
		/**
		 * Contruct model of indication tree of a project
		 * @param project that will be used to contruct tree.
		 */
		public void constructTree (IProject project) {
			if (project == null)
				return;
			TreeParent projectParent = new TreeParent(project.getName(), IndicationTree.PROJECT);
			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild( projectParent );

			TreeParent object = new TreeParent ("Base Codes", IndicationTree.OBJECT);
			TreeParent aspect = new TreeParent ("Aspect", IndicationTree.ASPECT);

			projectParent.addChild(object);
			projectParent.addChild(aspect);

			IResource[] resources = null;
			
			// Sync project and all children of this project 
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch(CoreException e) {
				System.err.println(e.getLocalizedMessage());				
			}

			// Reseting the list that contain informations about file
			lstFileInfo.clear();
			lstAspectFile = new ArrayList();
			
			if ( ((IResource)project).exists() && project.isOpen() )
			{
				try {
					resources = project.members();
				} catch (CoreException e) {
					System.err.println( e.getMessage() );
				}
				
				// For each folder and file
				for ( int i = 0; i < resources.length; i++ ) {
					if ( ! resources[i].exists() )
						System.err.println("O recurso " + resources[i].getName() + "não existe!");
					else {
						if ( resources[i] instanceof IFolder )
							constructNode ((IFolder)resources[i]);
						else if ( resources[i] instanceof IFile && 
								((IFile)resources[i]).getFileExtension().compareTo("java") == 0 ) {
							TreeParent fileNode = getFileNode ((IFile)resources[i]);
							constructFileNode (fileNode, (IFile)resources[i]);
							object.addChild(fileNode);
						}
						else if ( resources[i] instanceof IFile && 
								((IFile)resources[i]).getFileExtension().compareTo("aj") == 0 ) {
							lstAspectFile.add(resources[i]);
						}
					}
				}
				contructAspectSubtree();
			}
 		}
		
		/**
		 * Contruct the node that represents a folder in the indication tree model.
		 * @param folder
		 */
		public void constructNode(IFolder folder) {
			TreeObject nodeReturn = null;
			IResource[] resources = null;
			
			try {
				resources = folder.members();
			} catch ( CoreException e ) {
				System.err.println( e.getMessage() );
			}
			
			// Referencia para o nó de Base Source
			TreeParent objectNode = (TreeParent)((TreeParent)invisibleRoot.getChildrenAt(0)).getChildrenAt(0);
			
			// Nó terminal?
			if ( resources.length == 0 ) {
				nodeReturn = new TreeObject(folder.getProjectRelativePath().toString().replaceAll("/", "."), IndicationTree.EMPTY_PACKAGE);
				objectNode.addChild(nodeReturn);
			}
			else {
				for ( int i = 0; i < resources.length; i++ ) {
					if ( resources[i].getType() == IResource.FILE &&
							((IFile)resources[i]).getFileExtension().equalsIgnoreCase("java")) {
						if( nodeReturn == null ) {
							nodeReturn = new TreeParent(folder.getProjectRelativePath().toString().replaceAll("/", "."), IndicationTree.PACKAGE);
							objectNode.addChild(nodeReturn);
						}
						TreeParent fileNode = getFileNode((IFile)resources[i]);
						constructFileNode (fileNode, (IFile)resources[i]);
						((TreeParent)nodeReturn).addChild(fileNode);
					}
					else if ( resources[i].getType() == IResource.FILE &&
							((IFile)resources[i]).getFileExtension().equalsIgnoreCase("aj")) {
						lstAspectFile.add(resources[i]);
					}
					else if ( resources[i].getType() == IResource.FOLDER ) {
						constructNode((IFolder)resources[i]);
					}
				}
			}
		}
		
		/**
		 * Contruct the node that represents a file in the indication tree model.
		 * @param fileNode
		 * @param file
		 */
		public void constructFileNode (TreeObject fileNode, IFile file) {
			// Delete all markers associated
			int depth = IResource.DEPTH_INFINITE;
			try {
				file.deleteMarkers(IMarker.TASK, true, depth);
			} catch (CoreException e) {
				System.err.println("Problems found when deleteting markers in file " + file.getName());
			}
			
			InputStream input = null;
			try {
				input = file.getContents();
			} catch ( CoreException e ) {
				System.err.println( e.getMessage() );
			}
			
			String buffer = new String();
			
			try	{
				for(int i = input.read(); i != -1; i = input.read()) {
		            buffer += (char) i;
		        }
				input.close();
		    } catch(IOException e) {
		        System.err.println("Could not read from file");
		    }
		    
		    // Build the AST structure
		    char[] source = buffer.toCharArray();
		    ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5
		    parser.setSource(source);
		    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
		    DetectIndication detectIndications = new DetectIndication (this, fileNode, unit, file );
		    detectIndications.detectIndication();
		    lstFileInfo.add(detectIndications.getFileInfo());
		}
		
		/**
		 * @param file file resource
		 * @return a file node of indication tree based on a file resource.
		 */
		public TreeParent getFileNode(IFile file) {
			String sFile = file.getName().substring(0, file.getName().lastIndexOf("."));
			TreeParent fileNode = new TreeParent (sFile + ".java", IndicationTree.FILE);
			return fileNode;
		}
		
		/**
		 * Constructs the aspect subtree
		 */
		public void contructAspectSubtree() {
			// Reference to aspect subtree
			TreeParent aspectNode = (TreeParent)((TreeParent)invisibleRoot.getChildrenAt(0)).getChildrenAt(1);
			String sPackage, sFile;
			IFile file;
			int nIndex;
			HashMap map = new HashMap();
			TreeParent packageNode, ajFileNode;
			for(int i = 0; i < lstAspectFile.size(); i++) {
				file = (IFile)lstAspectFile.get(i);
				if(file.getFullPath().toString().length() > 1) {
					sPackage = file.getFullPath().toString().substring(1);
					nIndex = sPackage.indexOf('/'); 
					sPackage = sPackage.substring(nIndex + 1);
					nIndex = sPackage.lastIndexOf('/');
					if (nIndex > 0) {
						// Get package name
						sPackage = sPackage.substring(0, nIndex);
						sPackage = sPackage.replace('/', '.');

						// Get file name
						sFile = file.getFullPath().toString();
						nIndex = sFile.lastIndexOf('/');
						sFile = sFile.substring(nIndex + 1);
					}
					else {
						// File is in the root
						sFile = sPackage;
						sPackage = "";
					}
					if(sFile.length() > 3) {
						ajFileNode = new TreeParent(sFile, IndicationTree.ASPECT_FILE);
						if(sPackage.length() == 0)
							// Arquivo de aspecto está na raiz do projeto
							aspectNode.addChild(ajFileNode);
						else {
							packageNode = (TreeParent)map.get(sPackage);
							if(packageNode == null) {
								packageNode = new TreeParent(sPackage, IndicationTree.PACKAGE);
								map.put(sPackage, packageNode);
								aspectNode.addChild(packageNode);
							}
							packageNode.addChild(ajFileNode);
						}
					}
				}
			}
		}
		
		/**
		 * @param node node in the indication tree 
		 * @return a file resource represented in a node.
		 */
		public IFile getFile(TreeParent node) {
			String sPath = node.getParent().getName();
			TreeObject objectNode = getObjectNode(), aspectNode = getAspectNode();
			if( (objectNode != null && sPath.equals(objectNode.getName())) || 
				(aspectNode != null && sPath.equals(aspectNode.getName()))) {
				sPath = node.getName();
			}
			else {
				sPath = sPath.replace('.', '/');
				sPath = sPath + '/' + node.getName();
			}
			sPath = getProjectName() + '/' + sPath;
			IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
			IFile file = workspace.getFile( new Path(workspace.getFullPath() + sPath));
			return file;
		}

		/**
		 * Retorna o nó especificado por uma string. Tal string possui
		 * os níveis separados por '\'
		 * @param sSelection: String que indica o nó selecionado 
		 * @return retorna um objeto nó da árvore de indícios, 
		 * especificado por uma string
		 */
		public TreeObject select (String sSelection) {
			if ( sSelection == null )
				return null;
			
			// Split each level in a array of string
			String[] sParts = sSelection.split("\\\\");
			
			// Is there only one level? 
			if ( sParts.length == 0 ) {
				sParts = new String[1];
				sParts[0] = sSelection;
			}

			// Get project node
			TreeObject[] arrObjects = invisibleRoot.getChildren();
			int i = 0;
			int j;
			
			// For each level in the selection string
			while ( i < sParts.length - 1 ) {
				j = 0;
				// Looking for the next level of the selection string in the current level
				while ( j < arrObjects.length && arrObjects[j].getName().compareTo(sParts[i]) != 0) 
					j++;
				
				// The next level have been found
				if ( j < arrObjects.length && arrObjects[j].getName().compareTo(sParts[i]) == 0 )
					arrObjects = ((TreeParent)arrObjects[j]).getChildren(); 
				i++;
			}
			
			// Let's look for the element in the last level of the selection
			j = 0;
			while ( j < arrObjects.length && arrObjects[j].getName().compareTo(sParts[i]) != 0) 
				j++;
			if ( j >= arrObjects.length ) {
				System.err.println(" j = " + j + ", arrObjects.length = " + arrObjects.length);
				System.err.println(arrObjects.toString());
			}
			
			// Return the element of the selection
			return arrObjects[j]; 
		}
		
		/**
		 * Update the model of the indication tree that it was presviously buit. 
		 */
		public void updateTree (IProject project) {
			//IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
			//IProject project = workspace.getProject(getProjectName());
			constructTree(project);
			viewer.setInput(getViewSite());
		}
		
		/**
		 * The model of indication tree become empty.
		 */
		public void removeAll () {
			invisibleRoot = new TreeParent("", IndicationTree.PROJECT);
			viewer.setInput(getViewSite());
		}
		
		/**
		 * @return TreeObject reference to Object Node (Base Code) of the tree
		 */
		public TreeObject getObjectNode() {
			if( invisibleRoot != null && invisibleRoot.getChildrenAt(0) != null && 
					invisibleRoot.getChildrenAt(0) instanceof TreeParent)
				return (TreeObject)((TreeParent)invisibleRoot.getChildrenAt(0)).getChildrenAt(0);
			return null;
		}
		
		/**
		 * @return TreeObject reference to Aspect Node of the tree
		 */
		public TreeObject getAspectNode() {
			if( invisibleRoot != null && invisibleRoot.getChildrenAt(0) != null && 
					invisibleRoot.getChildrenAt(0) instanceof TreeParent)
				return (TreeObject)((TreeParent)invisibleRoot.getChildrenAt(0)).getChildrenAt(1);
			return null;
		}
	}
	
	/**
	 * The constructor of view.
	 */
	public IndicationTree() {
		sXmlFilePath = "";
		lstFileInfo = new ArrayList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site,IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;	
		Plugin.getDefault().getPreferenceStore().addPropertyChangeListener(preferenceListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		Plugin.getDefault().getPreferenceStore().removePropertyChangeListener(preferenceListener);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento){
        memento.putString("xml.file", sXmlFilePath);
        String sProjectName = ((ViewContentProvider)viewer.getContentProvider()).getProjectName();
        if( sProjectName != null )
        	memento.putString("indication.project", sProjectName);
        // Get index of active indication
        int index;
		for ( index = 0; index < indications.size(); index++ )
			if ( ((Indication)indications.get(index)).isActive() ) 
				break;
		// If no indication is active, select the first one
        if(index >= indications.size())
        	index = 0;
        memento.putInteger("active.indication", index);
   }
	
    /**
     * It restores the last state of view stored by saveState method.
     */
    private void restoreState() {
    	String sDescriptor = null;
    	Object objSelection;
    	if (memento == null) {
    		createDefaultIndications();
    		objSelection = constructTree(sDescriptor);
    		return;
    	}
    	else {
    		sXmlFilePath = memento.getString("xml.file");
    		if (sXmlFilePath == null || sXmlFilePath.length() == 0)
    			createDefaultIndications();
    		else
    			if( ! loadIndicationsFromXml()) {
    				if ( MessageDialog.openQuestion( viewer.getControl().getShell(), "Indication File not found", 
    						"The file \"" + sXmlFilePath + "\" not found. Do you want look for the indication file? (Click No to load default indications)") ) {
    					FileDialog dialog = new FileDialog(viewer.getControl().getShell(), SWT.OPEN);
    					String[] extensions = {"*.xml"};
    					String[] names = {"eXtensible Markup Language File (*.xml)"};
    					dialog.setFilterExtensions(extensions);
    					dialog.setFilterNames(names);
    					dialog.setFileName(sXmlFilePath);
    					sXmlFilePath = dialog.open();
    	    			if( ! loadIndicationsFromXml()) {
    	    				MessageDialog.openError( viewer.getControl().getShell(), "Parser Error", 
    	    					"Can't parse the file \"" + sXmlFilePath + "\". Default indications will be loaded.");
    	    				createDefaultIndications();
    	    			}
    				}
    				else
    					createDefaultIndications();
    			}
    			else { // XML was loaded successfully
    				int index = 0;
    				if(memento.getInteger("active.indication") != null)
    					index = memento.getInteger("active.indication").intValue();
    				// Is index valid?
    				if(index >= 0 && index < indications.size()) {
    					// Become indication active
    					((Indication)indications.get(index)).setActive(true);
    				}
    				else
    					// First indication becomes active
    					((Indication)indications.get(0)).setActive(true);
    			}
    		// Getting project
	    	String sProject = memento.getString("indication.project");
	    	objSelection = constructTree(sProject);
    	}
    	if(objSelection != null)
			viewer.setSelection(new StructuredSelection(objSelection));
    	memento = null;
    }
    
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setInput(getViewSite());
		
		labelProvider = new ViewLabelProvider();
		viewer.setLabelProvider(labelProvider);
		
		getViewSite().getPage().addSelectionListener(this);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookOpenViewAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				IndicationTree.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.removeAll();
		MenuManager menuIndications = new MenuManager("Indications");
		for ( int i = 0; i < indicationActions.size(); i++ )
			menuIndications.add((IAction)indicationActions.get(i));
		manager.add(menuIndications);
		MenuManager menuProjects = new MenuManager("Projects");
		for ( int i = 0; i < projectActions.size(); i++ )
			menuProjects.add((IAction)projectActions.get(i));
		manager.add(menuProjects);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.removeAll();
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if( sel.size() == 1 ) {
			TreeObject treeObject = (TreeObject)sel.getFirstElement();
			if(( treeObject.getRefactoring() & TreeObject.REFACTOR_EXTRACT_BEGINNIG ) != 0)
				manager.add(new Action("Extract Beginning"){});
			if(( treeObject.getRefactoring() & TreeObject.REFACTOR_EXTRACT_END ) != 0)
				manager.add(new Action("Extract End"){});
			if(( treeObject.getRefactoring() & TreeObject.REFACTOR_PRE_RETURN ) != 0)
				manager.add(new Action("Pre Return"){});
			if(( treeObject.getRefactoring() & TreeObject.REFACTOR_FIELD ) != 0)
				manager.add(new Action("Extract Field"){
					public void run() {
						// Getting workspace root
						IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
						IProject project = (IProject)workspace.findMember(((ViewContentProvider)viewer.getContentProvider()).getProjectName());
						if(project == null) {
							System.err.println("Can't find project!");
							return;
						}
						// Instantiates and initializes the wizard
						IntroductionFieldWizard wizard = new IntroductionFieldWizard();
						wizard.setProject(project);
						IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
						TreeObject treeObject = (TreeObject)sel.getFirstElement();
						wizard.setSelectedFields(treeObject.getInfo());
						wizard.init(getViewSite().getWorkbenchWindow().getWorkbench(), null);

						// Instantiates the wizard container with the wizard and opens it
						WizardDialog dialog = new WizardDialog( getSite().getShell(), wizard);
						dialog.create();
						wizard.OnEnterMainPage();
						dialog.open();
					}
				});
			if(( treeObject.getRefactoring() & TreeObject.REFACTOR_METHOD ) != 0)
				manager.add(new Action("Extract Method"){
					public void run() {
						// Getting workspace root
						IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
						IProject project = (IProject)workspace.findMember(((ViewContentProvider)viewer.getContentProvider()).getProjectName());
						if(project == null) {
							System.err.println("Can't find project!");
							return;
						}
						// Instantiates and initializes the wizard
						IntroductionMethodWizard wizard = new IntroductionMethodWizard();
						wizard.setProject(project);
						IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
						TreeObject treeObject = (TreeObject)sel.getFirstElement();
						wizard.setSelectedMethods(treeObject.getInfo());
						wizard.init(getViewSite().getWorkbenchWindow().getWorkbench(), null);

						// Instantiates the wizard container with the wizard and opens it
						WizardDialog dialog = new WizardDialog( getSite().getShell(), wizard);
						dialog.create();
						wizard.OnEnterMainPage();
						dialog.open();
					}
				});
		}
	}
	
	/**
	 * Create most of all actions of view
	 */
	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				makeActionProjects();
				contributeToActionBars();
				ViewContentProvider viewProvider = ((ViewContentProvider)viewer.getContentProvider());
				boolean bResetTree = true;
				if ( viewProvider.getProjectName() != null )
					for ( int i = 0; i < projectActions.size(); i++ )
						if ( ((Action)projectActions.get(i)).getText().equals(viewProvider.getProjectName()) ) {
							((Action)projectActions.get(i)).run();
							bResetTree = false;
							break;
						}
				if ( bResetTree ) {
					// Tree become empty
					viewProvider.removeAll();
				}
			}
		};
		refreshAction.setText("Update");
		refreshAction.setToolTipText("Update indication tree");
		refreshAction.setImageDescriptor(Plugin.getImageDescriptor("refresh.gif"));
		
		makeIndicationActions();
		
		makeActionProjects();

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				// Expande o nó em um nível
				viewer.expandToLevel(obj, 1);
				if ( obj instanceof TreeObject ) 
				{
					if (obj instanceof TreeParent && ((TreeObject)obj).getType().equalsIgnoreCase(FILE)) {
						IJavaElement javaElement = JavaCore.create(((ViewContentProvider)viewer.getContentProvider()).getFile((TreeParent)obj));
						try {
							JavaUI.openInEditor(javaElement);
						}catch ( JavaModelException e ) {
							showMessage(e.getMessage());
						}catch ( PartInitException e ) {
							showMessage(e.getMessage());
						}
					}
					else if(obj instanceof TreeParent && ((TreeObject)obj).getType().equalsIgnoreCase(ASPECT_FILE)) {
						IJavaElement javaElement = JavaCore.create(((ViewContentProvider)viewer.getContentProvider()).getFile((TreeParent)obj));
						try {
							JavaUI.openInEditor(javaElement);
						}catch ( JavaModelException e ) {
							showMessage(e.getMessage());
						}catch ( PartInitException e ) {
							showMessage(e.getMessage());
						}
					}
					else if (obj instanceof TreeObject & ((TreeObject)obj).getType().equalsIgnoreCase(ASPECT_INDICATION)) {
						TreeParent parent = ((TreeObject)obj).getParent();
						IFile file = ((ViewContentProvider)viewer.getContentProvider()).getFile(parent);

						IJavaElement javaElement = JavaCore.create(file);
						IEditorPart editorPart;
						IMarker marker = null;
						
						try {
							editorPart = JavaUI.openInEditor(javaElement);
							marker = file.findMarker(((TreeObject)obj).getId());
							IDE.gotoMarker(editorPart, marker);
						}catch ( JavaModelException e ) {
							showMessage(e.getMessage());
						}catch ( PartInitException e ) {
							showMessage(e.getMessage());
						} catch (CoreException e) {
							System.err.println( e.getMessage() );
						}						
					}
				}
			}
		};
		openViewAction = new Action() {
			public void run() {
			}
		};
	}
	
	/**
	 * Create a action for each indication
	 */
	private void makeIndicationActions() {
		indicationActions = new ArrayList();
		for(int i = 0; i < indications.size(); i++) {
			indicationActions.add(new Action(((Indication)indications.get(i)).getName()) {
				public void run() {
					Indication indication;
					for(int i = 0; i < indications.size(); i++) {
						indication = (Indication)indications.get(i);
						if(indication.getName().equals(this.getText()))
							indication.setActive(true);
						else
							indication.setActive(false);
					}
				}
			});
		}
	}

	/**
	 * Create a action for each project opened
	 */
	private void makeActionProjects() {
		projectActions = new ArrayList(); 
		// Getting workspace root
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		// Getting Projects
		IProject[] arrProjects = workspace.getProjects();
		IProject project = null;
		// For each project
		for ( int i = 0; i < arrProjects.length; i++ ) {
			project = arrProjects[i];
			if ( ((IResource)project).exists() && project.isOpen() ) {
				projectActions.add(new Action(project.getName()) {
						public void run() {
							ViewContentProvider viewProvider = ((ViewContentProvider)viewer.getContentProvider());
							// Getting workspace root
							IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
							IProject project = workspace.getProject(getText());
							if(project != null) {
								viewProvider.updateTree(project);
							}
							else
								showMessage("The project " + getText() + " was not found.");
						}
				});
			}
		}
	}
	
	/**
	 * Associate a double click action.
	 */
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void hookOpenViewAction() {
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				openViewAction.run();
			}
		});
	}
	
	/**
     * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
     */
	/***************************************************************************
	 *	Essa função será importante no futuro para identificar a seleção de elementos
	 *	java nas demais view. Seria interessante que a indication view abrisse o arquivo
	 *	nela automaticamente, caso o mesmo existe no indication tree 
	 ***************************************************************************/
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
            Object first = ((IStructuredSelection)selection).getFirstElement();
            if (first instanceof IJavaElement) {
            	;
            }
        }
    }

	/**
	 * Constrói a árvore de indícios e deixa um elemento selecionado.
	 * Esse elemento é descrita por uma string onde os níveis são separados
	 * pelo caractere '\'.
	 * @param sSelection: string que indica qual elemento deve ser selecionado. 
	 * @return retorna o elemento selecionado.
	 */
	public Object constructTree(String sSelection) {
		// Get project name
		int nIndex;
		String sProject = null;
		if ( sSelection != null ) {
			nIndex = sSelection.indexOf('\\');
			if ( nIndex == -1 )
				sProject = sSelection;
			else
				sProject = sSelection.substring(0, nIndex);
		}
		// Getting workspace root
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		
		// Getting project
		IProject project = null;
		IProject[] arrProjects = workspace.getProjects();
		if ( sProject != null )
			project = workspace.getProject(sProject);
		else {
			// Getting the first project open
			int i = 0;
			while ( i < arrProjects.length )
				if ( arrProjects[i].isOpen() ) {
					project = arrProjects[0];
					sSelection = project.getName();
					break;
				}
				else
					i++;
			// All projects are closed, warn user
			if ( project == null ) {
				showMessage("Please open at least one project.");
				return null;
			}
		}
		ViewContentProvider viewProvider = ((ViewContentProvider)viewer.getContentProvider());
		if( ((IResource)project).exists() && project.isOpen() )
			viewProvider.constructTree(project);
		else
			sSelection = null;
		
		// If selection remains null no projects exist in workspace
		return viewProvider.select(sSelection);
	}

	/**
	 * Open a dialog that display a message to user.
	 * @param message
	 */
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Indication Tree",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	/**
	 * This method is intended to be called by external views
	 * @return a list of clones of indication model
	 */
	public ArrayList getIndications() {
		ArrayList lstIndications = new ArrayList();
		for ( int i = 0; i < indications.size(); i++ )
			lstIndications.add(((Indication)indications.get(i)).clone());
		return lstIndications;
	}
	
	/**
	 * Update the indications of model by setting a list of indications of type
	 * Indication.
	 * @param indications
	 */
	public void setIndications(ArrayList indications) {
		this.indications = indications;
	}
	
	/**
	 * @return full path for xml file that describes all indications.
	 */
	public String getXmlFilePath() {
		return sXmlFilePath;
	}
	
	/**
	 * Set full path for xml file that describes all indications.
	 * @param sXmlFilePath
	 */
	public void setXmlFilePath(String sXmlFilePath) {
		this.sXmlFilePath = sXmlFilePath;
	}
	
	/**
	 * Create default indications, when xml file is unknown. 
	 */
	public void createDefaultIndications() {
		// Creating the initial indications
		indications = new ArrayList();
		String sDescription;
		
		// Creating persistence indications
		sDescription = "Provides the API for accessing and processing data stored in a data source (usually a relational database) using the JavaTM programming language.";
		IndicationPackage libraryIndication = new IndicationPackage("java.sql", sDescription);
		
		// The interfaces below belong to J2SE v.5.0
		ArrayList lstInterfaces = new ArrayList();
		lstInterfaces.add(new IndicationInterface("Array", "The mapping in the Java programming language for the SQL type ARRAY."));
		lstInterfaces.add(new IndicationInterface("Blob", "The representation (mapping) in the JavaTM programming language of an SQL BLOB value."));
		lstInterfaces.add(new IndicationInterface("CallableStatement", "The interface used to execute SQL stored procedures."));
		lstInterfaces.add(new IndicationInterface("Clob", "The mapping in the JavaTM programming language for the SQL CLOB type."));
		lstInterfaces.add(new IndicationInterface("Connection", "A connection (session) with a specific database."));
		lstInterfaces.add(new IndicationInterface("DatabaseMetaData", "Comprehensive information about the database as a whole."));
		lstInterfaces.add(new IndicationInterface("Driver", "The interface that every driver class must implement."));
		lstInterfaces.add(new IndicationInterface("ParameterMetaData", "An object that can be used to get information about the types and properties of the parameters in a PreparedStatement object."));
		lstInterfaces.add(new IndicationInterface("PreparedStatement", "An object that represents a precompiled SQL statement."));
		lstInterfaces.add(new IndicationInterface("Ref", "The mapping in the Java programming language of an SQL REF  value, which is a reference to an SQL structured type value in the database."));
		lstInterfaces.add(new IndicationInterface("ResultSet", "A table of data representing a database result set, which is usually generated by executing a statement that queries the database."));
		lstInterfaces.add(new IndicationInterface("ResultSetMetaData", "An object that can be used to get information about the types and properties of the columns in a ResultSet object."));
		lstInterfaces.add(new IndicationInterface("Savepoint", "The representation of a savepoint, which is a point within the current transaction that can be referenced from the Connection.rollback method."));
		lstInterfaces.add(new IndicationInterface("SQLData", "The interface used for the custom mapping of an SQL user-defined type (UDT) to a class in the Java programming language."));
		lstInterfaces.add(new IndicationInterface("SQLInput", "An input stream that contains a stream of values representing an instance of an SQL structured type or an SQL distinct type."));
		lstInterfaces.add(new IndicationInterface("SQLOutput", "The output stream for writing the attributes of a user-defined type back to the database."));
		lstInterfaces.add(new IndicationInterface("Statement", "The object used for executing a static SQL statement and returning the results it produces."));
		lstInterfaces.add(new IndicationInterface("Struct", "The standard mapping in the Java programming language for an SQL structured type."));
		libraryIndication.setInterfaces(lstInterfaces);
		
		// The classes below belong to J2SE v.5.0
		ArrayList lstClasses = new ArrayList();
		lstClasses.add(new IndicationClass("Date", "A thin wrapper around a millisecond value that allows JDBC to identify this as an SQL DATE value."));
		lstClasses.add(new IndicationClass("DriverManager", "The basic service for managing a set of JDBC drivers."));
		lstClasses.add(new IndicationClass("DriverPropertyInfo", "Driver properties for making a connection."));
		lstClasses.add(new IndicationClass("SQLPermission", "The permission for which the SecurityManager will check when code that is running in an applet calls the DriverManager.setLogWriter method or the DriverManager.setLogStream (deprecated) method."));
		lstClasses.add(new IndicationClass("Time", "A thin wrapper around the java.util.Date class that allows the JDBC API to identify this as an SQL TIME value."));
		lstClasses.add(new IndicationClass("Timestamp", "A thin wrapper around java.util.Date that allows the JDBC API to identify this as an SQL TIMESTAMP value."));
		lstClasses.add(new IndicationClass("Types", "The class that defines the constants that are used to identify generic SQL types, called JDBC types."));
		libraryIndication.setClasses(lstClasses);
		
		// The exceptions below belong to J2SE v.5.0
		ArrayList lstExceptions = new ArrayList();
		lstExceptions.add(new IndicationException("BatchUpdateException", "An exception thrown when an error occurs during a batch update operation."));
		lstExceptions.add(new IndicationException("DataTruncation", "An exception that reports a DataTruncation warning (on reads) or throws a DataTruncation exception (on writes) when JDBC unexpectedly truncates a data value."));
		lstExceptions.add(new IndicationException("SQLException", "An exception that provides information on a database access error or other errors."));
		lstExceptions.add(new IndicationException("SQLWarning", "	An exception that provides information on database access warnings."));
		libraryIndication.setExceptions(lstExceptions);
		
		Indication indication = new Indication ( "Database Persistence" );
		indication.setDescription("Indication for database persistence using java.sql package and text matchs.");
		indication.setActive(true);
		if (!indication.addPackage( libraryIndication ))
			System.err.println("Package not added to indication.");

		MatchText sqlInVariable = new MatchText( MatchText.VARIABLE_NAME, MatchText.CONTAINS, false);
		sqlInVariable.addWord("sql");
		indication.addMatch(sqlInVariable);

		MatchText sqlQuery = new MatchText(MatchText.STRING_LITERAL, MatchText.STARTS_WITH, false);
		sqlQuery.addWord("insert into");
		sqlQuery.addWord("select");
		sqlQuery.addWord("update");
		sqlQuery.addWord("delete from");
		indication.addMatch(sqlQuery);
		indications.add( indication );
		
		// Creating logging indications
		sDescription = "Provides the classes and interfaces of the JavaTM 2 platform's core logging facilities.";
		libraryIndication = new IndicationPackage("java.util.logging", sDescription);
		
		// The interfaces below belong to J2SE v.5.0
		lstInterfaces = new ArrayList();
		lstInterfaces.add(new IndicationInterface("Filter", "A Filter can be used to provide fine grain control over what is logged, beyond the control provided by log levels."));
		lstInterfaces.add(new IndicationInterface("LoggingMXBean", "The management interface for the logging facility."));
		libraryIndication.setInterfaces(lstInterfaces);
		
		// The classes below belong to J2SE v.5.0
		lstClasses = new ArrayList();
		lstClasses.add(new IndicationClass("ConsoleHandler", "This Handler publishes log records to System.err."));
		lstClasses.add(new IndicationClass("ErrorManager", "ErrorManager objects can be attached to Handlers to process any error that occur on a Handler during Logging."));
		lstClasses.add(new IndicationClass("FileHandler", "Simple file logging Handler."));
		lstClasses.add(new IndicationClass("Formatter", "A Formatter provides support for formatting LogRecords."));
		lstClasses.add(new IndicationClass("Handler", "A Handler object takes log messages from a Logger and exports them."));
		lstClasses.add(new IndicationClass("Level", "The Level class defines a set of standard logging levels that can be used to control logging output."));
		lstClasses.add(new IndicationClass("Logger", "A Logger object is used to log messages for a specific system or application component."));
		lstClasses.add(new IndicationClass("LoggingPermission", "The permission which the SecurityManager will check when code that is running with a SecurityManager calls one of the logging control methods (such as Logger.setLevel)."));
		lstClasses.add(new IndicationClass("LogManager", "There is a single global LogManager object that is used to maintain a set of shared state about Loggers and log services."));
		lstClasses.add(new IndicationClass("LogRecord", "LogRecord objects are used to pass logging requests between the logging framework and individual log Handlers."));
		lstClasses.add(new IndicationClass("MemoryHandler", "Handler that buffers requests in a circular buffer in memory."));
		lstClasses.add(new IndicationClass("SimpleFormatter", "Print a brief summary of the LogRecord in a human readable format."));
		lstClasses.add(new IndicationClass("SocketHandler", "Simple network logging Handler."));
		lstClasses.add(new IndicationClass("StreamHandler", "Stream based logging Handler."));
		lstClasses.add(new IndicationClass("XMLFormatter", "Format a LogRecord into a standard XML format."));
		libraryIndication.setClasses(lstClasses);
		
		lstExceptions = new ArrayList();
		libraryIndication.setExceptions(lstExceptions);
		
		indication = new Indication ( "Logging" );
		indication.setDescription("Indication for logging using java.util.logging package.");
		indication.setActive(false);
		if (!indication.addPackage( libraryIndication ))
			System.err.println("Package not added to indication.");
		
		indications.add( indication );
		
		// Creating buffering indications
		indication = new Indication ( "Buffering" );
		indication.setDescription("Indication for buffering.");
		indication.setActive(false);
		
		sDescription = "Provides classes that are fundamental to the design of the Java programming language.";
		libraryIndication = new IndicationPackage("java.lang", sDescription);

		// Add StringBuffer
		lstClasses = new ArrayList();
		lstClasses.add(new IndicationClass("StringBuffer", "A string buffer implements a mutable sequence of characters."));
		libraryIndication.setClasses(lstClasses);

		if (!indication.addPackage( libraryIndication ))
			System.err.println("Package not added to indication.");
		
		sDescription = "Provides for system input and output through data streams, serialization and the file system.";
		libraryIndication = new IndicationPackage("java.io", sDescription);

		// The classes below belong to J2SE v.5.0
		lstClasses = new ArrayList();
		lstClasses.add(new IndicationClass("BufferedInputStream", "A BufferedInputStream adds functionality to another input stream-namely, the ability to buffer the input and to support the mark and reset  methods."));
		lstClasses.add(new IndicationClass("BufferedReader", "Read text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines."));
		lstClasses.add(new IndicationClass("FileOutputStream", "A file output stream is an output stream for writing data to a File or to a FileDescriptor."));
		libraryIndication.setClasses(lstClasses);

		if (!indication.addPackage( libraryIndication ))
			System.err.println("Package not added to indication.");
		
		indications.add( indication );

	}
	
	/**
	 * @return Returns a list that contains information about files.
	 */
	public ArrayList getFileInfo() {
		return lstFileInfo;
	}
	
	public boolean loadIndicationsFromXml() {
		IndicationXml indicationXml = new IndicationXml (sXmlFilePath, indications);
		if(indicationXml.loadIndicationsFromXml()) {
			indications = indicationXml.getIndications();
			return true; 
		}
		return false;
	}
	
	public boolean saveIndicationsToXml() {
		return new IndicationXml (sXmlFilePath,indications).saveIndicationsToXml();
	}
	
	/**
	 * Update only a file node that which path is described by
	 * sRelativePath.
	 * @param sRelativePath It's a relative path considering the workspace
	 * path as the reference path.. P. ex.: sRelativePath = 
	 * "\AgenciaBancaria\Bank\Account.java" where AgenciaBancaria = project name,
	 * Bank = package, and Account.java = java file
	 */
	public void updateFileNode (String sRelativePath) {
		// Find the FileNode in the tree viewer
		ViewContentProvider provider = ((ViewContentProvider)viewer.getContentProvider());
		TreeParent visitedNode = provider.getInvisibleRoot();
		TreeObject[] children = visitedNode.getChildren();
		if(children.length == 0) {
			System.err.println("No projects node found");
			return;
		}
		sRelativePath = sRelativePath.substring(1);
		String[] paths = sRelativePath.split("\\\\");
		// Visit project node
		for( int i = 0; i < children.length; i++ )
			if( children[i].getName().equals(paths[0]) ) {
				visitedNode = (TreeParent)children[i];
				break;
			}
		// Project not found
		if(!visitedNode.getName().equals(paths[0])) {
			System.err.println("Project " + paths[0] + " not found in tree");
			return;
		}
		
		// Visit Code Base Node
		visitedNode = (TreeParent)visitedNode.getChildrenAt(0);
		
		// Figure out the package
		String sPackage = "";
		for(int i = 1; i < paths.length - 1; i++)
			sPackage += paths[i] + '.';
		
		// Visit package node
		if ( ! (sPackage.length() == 0) ) {
			sPackage = sPackage.substring(0, sPackage.length() - 1);
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
		visitedNode.removeAllChildren();
		provider.constructFileNode(visitedNode, provider.getFile(visitedNode));
		viewer.setInput(getViewSite());
	}
	
	/**
	 * It constructs a aspect file node in tree. If this aspect already
	 * have been created, no element is created.
	 * @param file
	 */
	public void constructAspectFile(IFile file) {
		ViewContentProvider provider = (ViewContentProvider)viewer.getContentProvider();
		// Reference to aspect subtree
		TreeParent aspectNode = (TreeParent)((TreeParent)provider.invisibleRoot.getChildrenAt(0)).getChildrenAt(1);

		String sPackage = file.getFullPath().toString().substring(1);
		int nIndex = sPackage.indexOf('/'); 
		sPackage = sPackage.substring(nIndex + 1);
		nIndex = sPackage.lastIndexOf('/');
		if( nIndex == -1 )
			// Pacote é vazio
			sPackage = "";
		else {
			sPackage = sPackage.substring(0, nIndex);
			sPackage = sPackage.replace('/', '.');
		}
		
		String sFile = file.getFullPath().toString();
		nIndex = sFile.lastIndexOf('/');
		sFile = sFile.substring(nIndex + 1);
		TreeParent packageNode = null;
		if(sPackage.length() == 0)
			packageNode = aspectNode;
		else {
			TreeObject[]aspectPackages = aspectNode.getChildren();
			for(int i = 0; i < aspectPackages.length; i++)
				if(aspectPackages[i].getName().equals(sPackage)) {
					packageNode = (TreeParent)aspectPackages[i]; 
					break;
				}
		}
		if(packageNode == null) {
			packageNode = new TreeParent(sPackage, IndicationTree.PACKAGE);
			aspectNode.addChild(packageNode);
		}
		TreeObject[]aspectFiles = packageNode.getChildren();
		for( int i = 0; i < aspectFiles.length; i++)
			if(aspectFiles[i].getName().equals(sFile))
				return;
		TreeParent ajFileNode = new TreeParent(sFile, IndicationTree.ASPECT_FILE);
		packageNode.addChild(ajFileNode);
	}
	
	public IProject getCurrentProject() {
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = (IProject)workspace.findMember(((ViewContentProvider)viewer.getContentProvider()).getProjectName());
		return project;
	}
}