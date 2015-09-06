package br.ufscar.dc.rejasp.wizards.refactoring.IntroductionMethodWizard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IViewPart;

import br.ufscar.dc.rejasp.model.Aspect;
import br.ufscar.dc.rejasp.model.CommentInterval;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.views.IndicationTree;
public class AspectVisualisationPage extends WizardPage implements Listener {
	public class FileModificationLocations {
		FileInfo file;
		ArrayList lstStyleRange;
		String sOriginalSource;
		String sFinalSource;
		
		public FileModificationLocations() {
			lstStyleRange = new ArrayList();
		}
	}
	
	/**
	 * Reference to wizard
	 */
	private IntroductionMethodWizard wizard;
	private ArrayList lstModifications;
	private ArrayList lstMethodsInAspect;
	private String sSourceCode;
	
	private Combo cbFile;
	private StyledText javaText;
	private StyledText aspectText;
	private Label lbAspect;
	
	private Display display;
	
	public AspectVisualisationPage() {
		super("Aspect Visualisation");
		setTitle("Preview of Aspect introduction");
		setDescription("If the preview of aspect is correct, confirm changes clicking Finish");
		lstMethodsInAspect = new ArrayList();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 * Interface of the page is created
	 */
	public void createControl(Composite parent) {
		wizard = (IntroductionMethodWizard)getWizard();

		// create the composite to hold the widgets
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);
		display = composite.getDisplay();

		// create the desired layout for this wizard page
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		gl.makeColumnsEqualWidth = true;
		composite.setLayout(gl);

		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		Label lbOriginal = new Label(composite, SWT.NONE);
		lbOriginal.setText("Original source");
		lbOriginal.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		cbFile = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY );
		cbFile.setLayoutData(gd);
		
		lbAspect = new Label(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		lbAspect.setText("Aspect Selected in previous screen");
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 2;
		lbAspect.setLayoutData(gd);
		
		javaText = new StyledText(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		javaText.setLayoutData(gd);
		
		aspectText = new StyledText(composite, SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		aspectText.setLayoutData(gd);
		
		setControl(composite);
		addListeners();
	}

	private void addListeners() {
		cbFile.addListener(SWT.Selection, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * Events are handled here.
	 */
	public void handleEvent(Event event) {
		if( event.widget == cbFile ) {
			if (cbFile.getSelectionIndex() < lstModifications.size()) {
				FileModificationLocations modification = 
					(FileModificationLocations)lstModifications.get(cbFile.getSelectionIndex());
				javaText.setText(modification.sOriginalSource);
				try {
					for( int i = 0; i < modification.lstStyleRange.size(); i++ )
						javaText.setStyleRange((StyleRange)modification.lstStyleRange.get(i));
				} catch ( SWTException e ) {
					System.err.println(e.getLocalizedMessage());
				} catch ( IllegalArgumentException e ) {
					System.err.println(e.getLocalizedMessage());
				}
			}
		}
	}

	/**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getPreviousPage()
	 */
	public IWizardPage getPreviousPage() {
		wizard.mainPage.onEnterPage();
		return wizard.mainPage;
	}

	/**
	 * Before show this page, the interface can be set.
	 */
	public void onEnterPage() {
		if( prepareModifications() ) {
			cbFile.removeAll();
			for(int i = 0; i < lstModifications.size(); i++ )
				cbFile.add(((FileModificationLocations)lstModifications.get(i)).file.getName());
		}
		// Show the first file:
		if (lstModifications.size() != 0) {
			cbFile.select(0);
			FileModificationLocations modification = (FileModificationLocations)lstModifications.get(0);
			javaText.setText(modification.sOriginalSource);
			try {
				for(int i = 0; i < modification.lstStyleRange.size(); i++ )
					javaText.setStyleRange((StyleRange)modification.lstStyleRange.get(i));
			} catch ( SWTException e ) {
				System.err.println(e.getLocalizedMessage());
			} catch ( IllegalArgumentException e ) {
				System.err.println(e.getLocalizedMessage());
			} catch ( Exception e ) {
				System.err.println(e.getLocalizedMessage());
			}
		}

		// Getting aspect file
		wizard.setCanFinish(true);
		IFile file;
		file = wizard.getAspectFile();
		
		// update interface
		int nIndex = file.getName().lastIndexOf('.');
		lbAspect.setText("Aspect " + file.getName().substring(0, nIndex));
		// Set combo
			// Identifing files to be modified
		ArrayList lstFileInfo = new ArrayList();
		ArrayList lstMethods = wizard.getSelectedMethods();
		boolean bFileIncluded;
		MethodInfo methodInfo;

		for( int i = 0; i < lstMethods.size(); i++ ) {
			bFileIncluded = false;
			methodInfo = (MethodInfo)lstMethods.get(i);
			for(int j = 0; j < lstFileInfo.size(); j++) 
				if ( methodInfo.getFile().equals(lstFileInfo.get(j)) ){
					bFileIncluded = true;
					break;
				}
			if ( ! bFileIncluded )
				lstFileInfo.add(methodInfo.getFile());
		}
		
		// Getting aspect source code (in string)
		try {
			AJCompilationUnit unit =
				AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
			unit.requestOriginalContentMode();
			sSourceCode = unit.getBuffer().getContents();
		} catch (JavaModelException e) {
			System.err.println(e.getLocalizedMessage());
			return;
		}
		
		// Find comments in aspect
		Aspect aspectParse = new Aspect(sSourceCode);
		if( !aspectParse.parse() )
			System.err.println("Something wrong happened");
		
		ArrayList lstNewTypes = new ArrayList(); 
		
		// Join all indication types found in all select methods
		for(int i = 0; i < lstMethods.size(); i++) {
			methodInfo = (MethodInfo)lstMethods.get(i);
			if(methodInfo.getIndicationTypes() != null)
				lstNewTypes.addAll(methodInfo.getIndicationTypes());
		}
		
		// The files of methods to be introduced need to be imported,
		// if they don't belong to the same package of the aspect
		String sAspectPath = file.getFullPath().toOSString();
		sAspectPath = sAspectPath.substring(0, sAspectPath.lastIndexOf('\\'));
		ArrayList lstFiles = new ArrayList();
		ArrayList lstMethodInfo = wizard.getSelectedMethods();
		// Find out all files of the methods
		for( int i = 0; i < lstMethodInfo.size(); i++ ) {
			bFileIncluded = false;
			methodInfo = (MethodInfo)lstMethodInfo.get(i);
			for(int j = 0; j < lstFiles.size(); j++) 
				if ( methodInfo.getFile().equals(lstFiles.get(j)) ){
					bFileIncluded = true;
					break;
				}
			if ( ! bFileIncluded )
				lstFiles.add(methodInfo.getFile());
		}
		// It will only include the files that belong to a different package from
		// the aspect
		String sTypePath;
		for(int i = 0; i < lstFiles.size(); i++) {
			sTypePath = ((FileInfo)lstFiles.get(i)).getPath();
			sTypePath = sTypePath.substring(0, sTypePath.lastIndexOf('\\'));
			if(! sAspectPath.equals(sTypePath))
				lstNewTypes.add(((FileInfo)lstFiles.get(i)).getPackage() + "." + 
						((FileInfo)lstFiles.get(i)).getName());
		}

		// Removing duplicated elements
		int i = 0, j;
		while ( i < lstNewTypes.size() ) {
			j = i + 1;
			while (j < lstNewTypes.size()) {
				if ( lstNewTypes.get(i).equals(lstNewTypes.get(j)) )
					lstNewTypes.remove(j);
				else j++;
			}
			i++;
		}
		
		int nImportResult;
		ArrayList lstRanges = new ArrayList();
		for ( i = 0; i < lstNewTypes.size(); i++ ) {
			nImportResult = aspectParse.addImport((String)lstNewTypes.get(i));
			if(nImportResult != -1)
				lstRanges.add(new StyleRange(nImportResult, 
						((String)lstNewTypes.get(i)).length() + 8, 
						display.getSystemColor(SWT.COLOR_BLACK), 
						display.getSystemColor(SWT.COLOR_YELLOW)));
		}
		
		// updating source code string
		sSourceCode = aspectParse.getCode();
		
		// For each method, insert it in aspect
		int nStart, nLength;
		for(i = 0; i < lstMethodsInAspect.size(); i++) {
			nStart = aspectParse.addMethod((String)lstMethodsInAspect.get(i));
			nLength = ((String)lstMethodsInAspect.get(i)).length() + 1;
			lstRanges.add(new StyleRange(nStart, nLength, 
					display.getSystemColor(SWT.COLOR_BLACK), 
					display.getSystemColor(SWT.COLOR_YELLOW)));
		}

		// Marking all inserted element
		aspectText.setText(aspectParse.getCode());
		for(i = 0; i < lstRanges.size(); i++)
			aspectText.setStyleRange((StyleRange)lstRanges.get(i)); 
		// Marking all commented element
		ArrayList lstComments = aspectParse.getComments();
		CommentInterval interval;
		for( i = 0; i < lstComments.size(); i++ ) {
			interval = (CommentInterval)lstComments.get(i);
			if(interval.isLineComment())
				aspectText.setStyleRange(new StyleRange(interval.getStart(),interval.getLength(),
						display.getSystemColor(SWT.COLOR_DARK_GREEN), 
						display.getSystemColor(SWT.COLOR_WHITE)));
			else
				aspectText.setStyleRange(new StyleRange(interval.getStart(),interval.getLength(),
						display.getSystemColor(SWT.COLOR_BLUE), 
						display.getSystemColor(SWT.COLOR_WHITE)));
		}
	
		int nEnd = aspectParse.getCode().length() - 1;
		while(aspectParse.getCode().charAt(nEnd) == '\n' || 
			  aspectParse.getCode().charAt(nEnd) == '\r')
			nEnd--;
		aspectText.setSelection(nEnd);
		aspectText.showSelection();
		
		wizard.setCanFinish(true);
	}
	
	public boolean saveAjFile() {
		IViewPart viewPart = wizard.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		IFile file;
		file = wizard.getAspectFile();
		// Sync resource 
		if (saveInFile(file.getFullPath().toOSString(), aspectText.getText())) {
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				((IndicationTree)viewPart).constructAspectFile(file);
			} catch (CoreException e) {
				System.err.println(e.getLocalizedMessage());
			}
		}
		else
			return false;
		return true;
	}
	
	public boolean saveJavaFiles() {
		IViewPart viewPart = wizard.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("br.ufscar.dc.rejasp.views.IndicationTree");
		if (viewPart == null) {
			System.err.println("Couldn't find indication view");
			return false;
		}
		// For each file
		for( int i = 0; i < lstModifications.size(); i++) {
			FileModificationLocations modification = (FileModificationLocations)lstModifications.get(i);
			FileInfo fileInfo = modification.file;
			saveInFile(fileInfo.getPath(), modification.sFinalSource);
			
			// Getting file name
			String sPath = fileInfo.getPath();
			int nIndex = sPath.lastIndexOf('\\');
			sPath = sPath.substring(nIndex + 1, sPath.length());
			sPath = '/' + fileInfo.getPackage().replace('.', '/') + '/' + sPath;
			IProject project = wizard.getProject();
			IFile file = (IFile)project.findMember(sPath);

			// Sync resource 
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				((IndicationTree)viewPart).updateFileNode(fileInfo.getPath());
				ArrayList lstFileInfo = ((IndicationTree)viewPart).getFileInfo();
				for( int j = 0; j < lstFileInfo.size(); j++)
					if(fileInfo.equals(lstFileInfo.get(j))) {
						lstFileInfo.remove(j);
						break;
					}
			} catch (CoreException e) {
				System.err.println(e.getLocalizedMessage());
				return false;
			}
		}
		return true;
	}
	
	private boolean prepareModifications() {
		lstMethodsInAspect.clear();
		// Identifing files to be modified
		lstModifications = new ArrayList();
		boolean bFileIncluded;
		MethodInfo methodInfo;
		FileInfo fileInfo;
		HashMap map = new HashMap();

		ArrayList lstFileInfo = new ArrayList();
		ArrayList lstMethodInfo = wizard.getSelectedMethods();
		for( int i = 0; i < lstMethodInfo.size(); i++ ) {
			bFileIncluded = false;
			methodInfo = (MethodInfo)lstMethodInfo.get(i);
			for(int j = 0; j < lstFileInfo.size(); j++) 
				if ( methodInfo.getFile().equals(lstFileInfo.get(j)) ){
					bFileIncluded = true;
					break;
				}
			if ( ! bFileIncluded )
				lstFileInfo.add(methodInfo.getFile());
			
			fileInfo = methodInfo.getFile();
			map.put(methodInfo, fileInfo);
		}
		
		IProject project = wizard.getProject();
		String sPath;
		int nIndex;
		FileModificationLocations modifications = new FileModificationLocations();
		// For each file
		for( int i = 0; i < lstFileInfo.size(); i++) {
			// Getting CompilationUnit
			IFile file;
			fileInfo = (FileInfo)lstFileInfo.get(i);
			modifications = new FileModificationLocations();
			modifications.file = fileInfo;
			// Getting file name
			sPath = fileInfo.getPath();
			nIndex = sPath.lastIndexOf('\\');
			sPath = sPath.substring(nIndex + 1, sPath.length());
			sPath = '/' + fileInfo.getPackage().replace('.', '/') + '/' + sPath;
			file = (IFile)project.findMember(sPath);
			
			InputStream input = null;
			try {
				input = file.getContents();
			} catch ( CoreException e ) {
				System.err.println( e.getMessage() );
				return false;
			}
			
			String buffer = new String();
			try	{
				for(int j = input.read(); j != -1; j = input.read()) {
		            buffer += (char) j;
		        }
				input.close();
		    } catch(IOException e) {
		        System.err.println("Could not read from file");
		        return false;
		    }
		    
		    modifications.sOriginalSource = buffer;
		    
		    // Build the AST structure
		    Document document = new Document(buffer);
		    ASTParser parser = ASTParser.newParser(AST.JLS3);  // handles JDK 1.0, 1.1, 1.2, 1.3, 1.4, 1.5
		    parser.setSource(document.get().toCharArray());
		    CompilationUnit unit = (CompilationUnit) parser.createAST(null);
			unit.recordModifications();
			
		    // Getting ranges for comments in original tree
		    List lstComments = unit.getCommentList();
		    Comment comment;
		    for( int j = 0 ; j < lstComments.size(); j++ ) {
	    		comment = (Comment)lstComments.get(j);
		    	if( comment instanceof LineComment )
		    		modifications.lstStyleRange.add(new StyleRange(comment.getStartPosition(),
		    				comment.getLength(), display.getSystemColor(SWT.COLOR_DARK_GREEN), 
		    				display.getSystemColor(SWT.COLOR_WHITE)));
		    	else if( comment instanceof BlockComment )
		    		modifications.lstStyleRange.add(new StyleRange(comment.getStartPosition(),
		    				comment.getLength(), display.getSystemColor(SWT.COLOR_BLUE), 
		    				display.getSystemColor(SWT.COLOR_WHITE)));
		    }
		    
		    // Getting all typeDeclaration of compilation unit
			List lstTypes = unit.types();
			int nTypeCount = 0;
			for (int j = 0; j < lstTypes.size(); j++)
				if(lstTypes.get(j) instanceof TypeDeclaration)
					nTypeCount++;
			TypeDeclaration[] typesUnit = new TypeDeclaration[nTypeCount];
			nTypeCount = 0;
			for (int j = 0; j < lstTypes.size(); j++)
				if(lstTypes.get(j) instanceof TypeDeclaration) {
					typesUnit[nTypeCount] = (TypeDeclaration)lstTypes.get(j);
					nTypeCount++;
				}
			
			// Find all methods belong to the current file (CompilationUnit)
			Iterator it = map.keySet().iterator();
			TypeDeclaration lastTypeVisited = null;
			// For each method in this Compilation Unit
			while (it.hasNext()) {
				TypeDeclaration[] types = typesUnit;
				methodInfo = (MethodInfo)it.next();
				if(((FileInfo)map.get(methodInfo)).equals(fileInfo)) {
					// Find type that contains the current method in ast
					ArrayList lstEnclosuredTypes = methodInfo.getEnclosuredTypes();
					// For each type that is enclosured
					for( int j = 0; j < lstEnclosuredTypes.size(); j++ ) {
						String sTypeName = ((TypeInfo)lstEnclosuredTypes.get(j)).getName();
						int k = 0;
						while ( k < types.length && !types[k].getName().getIdentifier().equals(sTypeName) )
							k++;
						if ( k == types.length ) {
							System.err.println("Type " + sTypeName + " not found!");
							return false;
						}
						// Type found, we will analise next type enclosured
						lastTypeVisited = types[k]; 
						types = types[k].getTypes();
						if( types.length == 0 )
							break;
					}
					List lstDeclarations = lastTypeVisited.bodyDeclarations();
					// Find the method in the type that it belongs.
					MethodDeclaration methodDeclaration;
					String methodInAspect;
					int index;
					for(int j = 0; j < lstDeclarations.size(); j++) {
						if ( lstDeclarations.get(j) instanceof MethodDeclaration  && 
							 ((MethodDeclaration)lstDeclarations.get(j)).getName().getIdentifier().equals(methodInfo.getName()) ) {
							// Changing AST
							methodDeclaration = (MethodDeclaration)lstDeclarations.get(j);
							modifications.lstStyleRange.add(new StyleRange(methodDeclaration.getStartPosition(),
									methodDeclaration.getLength(), display.getSystemColor(SWT.COLOR_BLACK), 
									display.getSystemColor(SWT.COLOR_YELLOW)));
							methodInAspect = modifications.sOriginalSource.substring(
									methodDeclaration.getStartPosition(), methodDeclaration.getStartPosition() +
									methodDeclaration.getLength());
							index = methodInAspect.indexOf(methodDeclaration.getName().getIdentifier());
							methodInAspect = methodInAspect.substring(0, index) + methodInfo.getType().getQualifiedName() + 
											 "."+ (methodInfo.isConstructor()?"new":methodInfo.getName()) + 
											 methodInAspect.substring(index + methodDeclaration.getName().getIdentifier().length(), 
											 methodInAspect.length());
							lstMethodsInAspect.add(methodInAspect);
							lstDeclarations.remove(j);
						}
					}
				}
			}
			String sSourceOOCode;
			try {
				TextEdit edits = unit.rewrite(document, null);
				edits.apply(document);
				sSourceOOCode = document.get();
			} catch (BadLocationException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalStateException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			modifications.sFinalSource = sSourceOOCode;
			lstModifications.add(modifications);
		}
		return true;
	}
	
	private boolean saveInFile( String sFullPath, String sContent) {
		String sPath = wizard.getProject().getWorkspace().getRoot().getLocation() + sFullPath;
		FileOutputStream  outputStream = null;
		try { 
			outputStream = new FileOutputStream( sPath );
		} catch ( IOException ex ) {
			System.err.println(ex.getLocalizedMessage());
			return false;
		}
		PrintWriter printWriter = new PrintWriter(outputStream);
		printWriter.print(sContent);
		printWriter.flush();
		try {
			outputStream.flush();
			outputStream.close();
		} catch ( IOException ex ) {
			System.err.println(ex.getLocalizedMessage());
			return false;
		}
		return true;
	}
}
