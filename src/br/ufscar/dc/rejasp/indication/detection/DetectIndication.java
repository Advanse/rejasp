package br.ufscar.dc.rejasp.indication.detection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import br.ufscar.dc.rejasp.indication.model.Indication;
import br.ufscar.dc.rejasp.indication.model.IndicationPackage;
import br.ufscar.dc.rejasp.indication.model.MatchText;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FieldInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.FileInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.MethodRefactoringInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.TypeInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.VariableInfo;
import br.ufscar.dc.rejasp.plugin.Plugin;
import br.ufscar.dc.rejasp.views.IndicationTree;
import br.ufscar.dc.rejasp.views.TreeObject;
import br.ufscar.dc.rejasp.views.TreeParent;

/**
 * @author Daniel Kawakami
 * This is class is responsible for detect indications in the source code of
 * only one file and show them in the indication tree view and problem view.
 */
public class DetectIndication {
	/**
	 * A provider for IndicationTree view.
	 */
	private final IndicationTree.ViewContentProvider provider;
	/**
	 * The file node in the model of indication tree.
	 */
	private TreeObject fileNode;
	/**
	 * The compilation unit for the file. It's the root for AST.
	 */
	private CompilationUnit unit;
	/**
	 * File resource to be analysed
	 */
	private IFile file;
	/**
	 * List of imports with indication found in source code. If the indication import
	 * is on-demand. The list will have only one element.
	 */
	private ArrayList lstIndicationImports;
	/**
	 * List of arguments with indication that exists in a particular method.
	 */
	private ArrayList lstMethodArguments;
	/**
	 * List of local variables. This variables have visibility only inside the block
	 * that they are declared.
	 */
	private VariableTable variables;
	/**
	 * List of fields of class or interface. Their visibility exists only inside
	 * the module (module = class or interface) they are defined or inside other 
	 * modules that are defined inside the module they exist.
	 */
	private VariableTable lstFields;
	/**
	 * Stores information about the file 's been analised.
	 */
	private FileInfo fileInfo;
	/**
	 * Reference to the last typeInfo created, which has another 
	 * typeInfo currently being analised. If no typeInfo is being 
	 * analised or the current typeInfo isn't a enclosured typeInfo,
	 * this field is null.
	 */
	private TypeInfo lastTypeInfo;

	/**
	 * Reference to the typeInfo currently being used. 
	 * It is used by TypeDeclarationStatement
	 */
	private TypeInfo currentTypeInfo;

	/**
	 * It stores all types found. It's used to find out all
	 * indication types found in an method. 
	 */
	private ArrayList lstTypesFoundInMethod;
	
	/**
	 * It stores the last indication created.
	 */
	private TreeObject lastIndication;
	
	/**
	 * It indicates that identification is based on types imported.
	 */
	private boolean ifFiltroByType = Plugin.getDefault().isFilterByImport();
	
	/**
	 * Default constructor for this class.
	 * @param provider
	 * @param fileNode
	 * @param unit
	 * @param file
	 */
	public DetectIndication ( IndicationTree.ViewContentProvider provider, TreeObject fileNode, CompilationUnit unit, IFile file ) {
		this.provider = provider;
		this.fileNode = fileNode;
		this.unit = unit;
		this.file = file;
		variables = new VariableTable();
		lstIndicationImports = new ArrayList();
		lstFields = new VariableTable();
		fileInfo = new FileInfo(file.getFullPath().toOSString());
		lstTypesFoundInMethod = new ArrayList();
		lastIndication = null;
	}
	
	/**
	 * @return Returns important informations about this file
	 * that wil be used to refactor field and methods
	 */
	public FileInfo getFileInfo() {
		return fileInfo;
	}

	/**
	 * This is the main method where the detection of indications are done.
	 * @return true if the process of detection have been successfully or false, 
	 * otherwise.
	 */
	public boolean detectIndication () {
		List lstTypeDeclarations = unit.types();
		List lstImports = unit.imports();
		ImportDeclaration impDeclaration = null;
		lstIndicationImports.clear();
		
		Indication indication = getActiveIndication();
		IndicationPackage libraryIndication = null;
		
		if ( indication != null && indication.getPackages().size() > 0 )
			// For each import declaration in file
			for ( int i = 0; i < lstImports.size(); i++ ) {
				impDeclaration = (ImportDeclaration)lstImports.get(i);
				// For each library indication
				for ( int j = 0; j < indication.getPackages().size(); j++ ) {
					libraryIndication = (IndicationPackage)indication.getPackages().get(j);
					String sIndicationType = impDeclaration.getName().getFullyQualifiedName();
					sIndicationType = sIndicationType.substring (sIndicationType.lastIndexOf('.') + 1, sIndicationType.length());
					String sPackageOfImport = impDeclaration.getName().getFullyQualifiedName();
					if ( ! impDeclaration.isOnDemand() ) {
						if(sPackageOfImport.lastIndexOf('.') == -1) // Tipo importado não tem pacote
							sPackageOfImport = "";
						else
							sPackageOfImport = sPackageOfImport.substring (0, sPackageOfImport.lastIndexOf('.'));
					}
					if ( sPackageOfImport.equals( libraryIndication.getName() ) )
						if ( impDeclaration.isOnDemand() ) {
							createIndication( impDeclaration );
							if ( ! lstIndicationImports.isEmpty() )
								lstIndicationImports.clear();
							lstIndicationImports.add (sPackageOfImport + ".*");
						}
						else if( libraryIndication.isType(sIndicationType) ) {
							createIndication( impDeclaration );
							if ( lstIndicationImports.size() == 0 || ! ((String)lstIndicationImports.get(0)).equalsIgnoreCase("*") ) {
								lstIndicationImports.add(sPackageOfImport + "." + sIndicationType);
							}
						}
				}
			}
		
		// No indication import was found and preferences accept only indication based on imported types, 
		// let's only find enclosured types, methods and fields and store them in file info
		if ( ifFiltroByType && lstIndicationImports.isEmpty() ) {
			// For each class or interface in the file
			for ( int i = 0; i < lstTypeDeclarations.size(); i++ )
				if ( lstTypeDeclarations.get(i) instanceof TypeDeclaration ) 
					fileInfo.addType(getTypeInfo((TypeDeclaration)lstTypeDeclarations.get(i)));
			return false;
		}
			
		// For each class or interface in the file
		for ( int i = 0; i < lstTypeDeclarations.size(); i++ ) {
			// Is a class or interface?
			if ( lstTypeDeclarations.get(i) instanceof TypeDeclaration ) {
				// The types about to be analised don't have a parent
				// type, so lastTypeInfo is null 
				lastTypeInfo = null;
				detectIndication ( (TypeDeclaration)lstTypeDeclarations.get(i) );
			}
			else if ( lstTypeDeclarations.get(i) instanceof EnumDeclaration )
				detectIndication ( (EnumDeclaration)lstTypeDeclarations.get(i) );
		}
		return true;
	}
	
	/**
	 * This method is used only when no indication was found in a typeDeclaration.
	 * All information about a type is collected. 
	 * @param type declaration type object.
	 * @return a complete TypeInfo object. 
	 */
	private TypeInfo getTypeInfo(TypeDeclaration type) {
		String sModifiers = "";
		for ( int i = 0; i < type.modifiers().size(); i++)
			if ( type.modifiers().get(i) instanceof Modifier )
				sModifiers += ((Modifier)type.modifiers().get(i)).getKeyword().toString() + " ";
		if( sModifiers.length() > 0 )
			sModifiers = sModifiers.substring(0, sModifiers.length() - 1);

		TypeInfo typeInfo = new TypeInfo(type.getName().getIdentifier(), sModifiers, type.isInterface() );
		
		// For each field
		FieldDeclaration[] fields = type.getFields();
		FieldInfo fieldInfo;
		String sInitializer;
		VariableDeclarationFragment fragment;
		for ( int i = 0; i < fields.length; i++ ) {
			sModifiers = "";
			for ( int j = 0; j < fields[i].modifiers().size(); j++)
				if ( fields[i].modifiers().get(j) instanceof Modifier )
					sModifiers += ((Modifier)fields[i].modifiers().get(j)).getKeyword().toString() + " ";
			if( sModifiers.length() > 0 )
				sModifiers = sModifiers.substring(0, sModifiers.length() - 1);
			for ( int j = 0; j < fields[i].fragments().size(); j++  ) {
				fragment = (VariableDeclarationFragment)fields[i].fragments().get(j);
				if ( fragment.getInitializer() == null )
					sInitializer = "";
				else
					sInitializer = " = " + fragment.getInitializer().toString();
				fieldInfo = new FieldInfo( fragment.getName().getIdentifier(),
						fields[i].getType().toString(), sModifiers, sInitializer);
				fieldInfo.setIndication(false);
				typeInfo.addField(fieldInfo);
			}
		}

		// For each method
		MethodDeclaration[] methods = type.getMethods();
		MethodInfo methodInfo;
		for ( int i = 0; i < methods.length; i++ ) {
			sModifiers = "";
			for ( int j = 0; j < methods[i].modifiers().size(); j++)
				if ( methods[i].modifiers().get(j) instanceof Modifier )
					sModifiers += ((Modifier)methods[i].modifiers().get(j)).getKeyword().toString() + " ";
			if( sModifiers.length() > 0 )
				sModifiers = sModifiers.substring(0, sModifiers.length() - 1);
			String sType;
			if(methods[i].isConstructor())
				sType = "";
			else 
				sType = methods[i].getReturnType2().toString();

			methodInfo = new MethodInfo( methods[i].getName().getIdentifier(),
					sType, sModifiers);
			methodInfo.setParameters(methods[i].parameters());
			methodInfo.setIndication(false);
			methodInfo.setConstructor(methods[i].isConstructor());
				
			typeInfo.addMethod(methodInfo);
		}
		typeInfo.setIndication(false);
		
		// For each enclosured type
		TypeDeclaration[] types = type.getTypes();
		for ( int i = 0; i < types.length; i++ )
			typeInfo.addType(getTypeInfo(types[i]));
		
		return typeInfo;
	}
	
	/**
	 * Look for indications in a TypeDeclaration structure.
	 * @param typeDeclaration: TypeDeclaration structure
	 * @return
	 */
	private boolean detectIndication (TypeDeclaration typeDeclaration) {
		/*
		 * TypeDeclaration:
		 *  		ClassDeclaration
		 *   		InterfaceDeclaration
		 * ClassDeclaration:
		 *       [ Javadoc ] { ExtendedModifier } class Identifier
		 *       			[ < TypeParameter { , TypeParameter } > ]
		 *       			[ extends Type ]
		 *       			[ implements Type { , Type } ]
		 *       			{ { ClassBodyDeclaration | ; } }
		 * InterfaceDeclaration:
		 *       [ Javadoc ] { ExtendedModifier } interface Identifier
		 *       			[ < TypeParameter { , TypeParameter } > ]
		 *       			[ extends Type { , Type } ]
		 *        			{ { InterfaceBodyDeclaration | ; } }
		 */

		FieldDeclaration[] fields = null;
		MethodDeclaration[] methods = null;
		List fieldFragments = null;
		VariableDeclarationFragment fieldFragment = null;
		SingleVariableDeclaration argument = null;
		List statements = null;
		
		// Create typeInfo
		String sModifiers = "", sInitializer = "";
		for ( int i = 0; i < typeDeclaration.modifiers().size(); i++)
			if ( typeDeclaration.modifiers().get(i) instanceof Modifier )
				sModifiers += ((Modifier)typeDeclaration.modifiers().get(i)).getKeyword().toString() + " ";
		if( sModifiers.length() > 0 )
			sModifiers = sModifiers.substring(0, sModifiers.length() - 1);

		TypeInfo typeInfo = new TypeInfo(typeDeclaration.getName().getIdentifier(), sModifiers, typeDeclaration.isInterface() );
		currentTypeInfo = typeInfo;
		if ( lastTypeInfo == null )
			fileInfo.addType(typeInfo);
		else
			lastTypeInfo.addType(typeInfo);
		
		// Is superclass a indication? 
		if ( typeDeclaration.getSuperclassType() != null ) {
			typeInfo.setSubType(true);
			if( isIndicationType(typeDeclaration.getSuperclassType()) ) {
				createIndication(typeDeclaration.getSuperclassType());
				typeInfo.setIndication(true);
			}
		}
		
		// Is there any type parameter? 
		if ( typeDeclaration.typeParameters() != null && typeDeclaration.typeParameters().size() > 0 ) {
			List typeParameters = typeDeclaration.typeParameters();
			for ( int i = 0; i < typeParameters.size(); i++ ) {
				/*
				 * TypeParameter:
				 *     TypeVariable [ extends Type { & Type } ]
				 */
				List types = ((TypeParameter)typeParameters.get(i)).typeBounds();
				for ( int j = 0; j < types.size(); j++ )
					if ( isIndicationType( (Type)types.get(j) )) {
							createIndication ( (Type)types.get(j) );
							typeInfo.setIndication(true);
					}
			}
		}
		
		// Is there any interface type?
		if ( typeDeclaration.superInterfaceTypes() != null && typeDeclaration.superInterfaceTypes().size() > 0 ) {
			typeInfo.setSubType(true);
			List interfaceTypes = typeDeclaration.superInterfaceTypes();
			for ( int i = 0; i < interfaceTypes.size(); i++ )
				if ( isIndicationType( (Type)interfaceTypes.get(i) ) ) {
						createIndication((Type)interfaceTypes.get(i));
						typeInfo.setIndication(true);
				}
		}
		
		// Adding a level (context) to fields of this class
		lstFields.addLevel();
		fields = typeDeclaration.getFields();
		methods = typeDeclaration.getMethods();
		lstMethodArguments = new ArrayList();
		
		// For each field declaration
		for ( int j = 0; j < fields.length; j++ ) {
			/*
			 * FieldDeclaration:
			 *     [Javadoc] { ExtendedModifier } Type VariableDeclarationFragment
			 *              { , VariableDeclarationFragment } ;
			 */
			
			// Modifier string
			sModifiers = "";
			for ( int k = 0; k < fields[j].modifiers().size(); k++)
				if ( fields[j].modifiers().get(k) instanceof Modifier )
					sModifiers += ((Modifier)fields[j].modifiers().get(k)).getKeyword().toString() + " ";
			if( sModifiers.length() > 0 )
				sModifiers = sModifiers.substring(0, sModifiers.length() - 1);
			
			fieldFragments = fields[j].fragments();
			// Is the field a declaration of a indication?
			if ( isIndicationType(fields[j].getType() ) ) {
				createIndication (fields[j]);
				lastIndication.addRefatoring(TreeObject.REFACTOR_FIELD);
				// Adding in list of types
				for ( int k = 0; k < fieldFragments.size(); k++ ) {
					// Getting field Initializer
					fieldFragment = (VariableDeclarationFragment)fieldFragments.get(k);
					if ( fieldFragment.getInitializer() == null )
						sInitializer = "";
					else
						sInitializer = " = " + fieldFragment.getInitializer().toString();

					lstFields.addElement ( new VariableInfo(fields[j].getType().toString(), 
								fieldFragment.getName().getIdentifier() ));
					// Store field info 
					FieldInfo fieldInfo = new FieldInfo( fieldFragment.getName().getIdentifier(),
							fields[j].getType().toString(), sModifiers, sInitializer);
					fieldInfo.setIndication(true);
					typeInfo.setIndication(true);
					typeInfo.addField(fieldInfo);
					lastIndication.addInfo(fieldInfo);
				}
			}
			// Let's search indications in field fragments
			else {
				for ( int k = 0; k < fieldFragments.size(); k++ ) {
					// Getting field Initializer
					fieldFragment = (VariableDeclarationFragment)fieldFragments.get(k);
					if ( fieldFragment.getInitializer() == null )
						sInitializer = "";
					else
						sInitializer = " = " + fieldFragment.getInitializer().toString();

					// Store field info 
					FieldInfo fieldInfo = new FieldInfo( fieldFragment.getName().getIdentifier(),
							fields[j].getType().toString(), sModifiers, sInitializer);
					typeInfo.addField(fieldInfo);
					
					if ( isIndicationVariableDeclarationFragment( (VariableDeclarationFragment)fieldFragments.get(k) ) ) {
						if ( fieldFragments.size() == 1 )
							createIndication ( fields[j] );
						else
							createIndication ( (VariableDeclarationFragment)fieldFragments.get(k) );

						lstFields.addElement ( new VariableInfo( fields[j].getType().toString(),
								((VariableDeclarationFragment)fieldFragments.get(k)).getName().getIdentifier() ));
						fieldInfo.setIndication(true);
						typeInfo.setIndication(true);
						lastIndication.addRefatoring(TreeObject.REFACTOR_FIELD);
						lastIndication.addInfo(fieldInfo);
					}
				}
			}
		}
			
		boolean bMethodHasIndication;
		// For each method
		for ( int j = 0; j < methods.length; j++ ) {
			bMethodHasIndication = false;
			
			sModifiers = "";
			for ( int k = 0; k < methods[j].modifiers().size(); k++)
				if ( methods[j].modifiers().get(k) instanceof Modifier )
					sModifiers += ((Modifier)methods[j].modifiers().get(k)).getKeyword().toString() + " ";
			if( sModifiers.length() > 0 )
				sModifiers = sModifiers.substring(0, sModifiers.length() - 1);
			String sType;
			if(methods[j].isConstructor())
				sType = "";
			else 
				sType = methods[j].getReturnType2().toString();
			MethodInfo methodInfo = new MethodInfo( methods[j].getName().getIdentifier(),
					sType, sModifiers );
			methodInfo.setParameters(methods[j].parameters());
			methodInfo.setConstructor(methods[j].isConstructor());
			lstTypesFoundInMethod.clear();

			if(isIndicationType(methods[j].getReturnType2())) {
				createIndication (methods[j].getReturnType2());
				lastIndication.addRefatoring(TreeObject.REFACTOR_METHOD);
				lastIndication.addInfo(methodInfo);
				bMethodHasIndication = true;
			}
			// Method is not empty 
			if ( ! ( methods[j].getBody() == null ) ) {
				lstMethodArguments.clear();
				variables.addLevel();
				statements = methods[j].getBody().statements();
				// For each parameter
				for ( int k = 0; k < methods[j].parameters().size(); k++ ) {
					argument = (SingleVariableDeclaration)methods[j].parameters().get(k);
					/*
					 * SingleVariableDeclaration:
					 *     { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
					 */
					if (isIndicationType(argument.getType())) {
						lstMethodArguments.add(argument.getName().getIdentifier());
						createIndication (argument);
						lastIndication.addRefatoring(TreeObject.REFACTOR_METHOD);
						lastIndication.addInfo(methodInfo);
						bMethodHasIndication = true;
					}
					else if ( isIndicationExpression( argument.getInitializer() ) ) {
						lstMethodArguments.add(argument.getName().getIdentifier());
						createIndication (argument);
						lastIndication.addRefatoring(TreeObject.REFACTOR_METHOD);
						lastIndication.addInfo(methodInfo);
						bMethodHasIndication = true;
					}
					else if ( isIndicationVariableName( argument.getName() ) ) {
						lstMethodArguments.add(argument.getName().getIdentifier());
						createIndication (argument);
						lastIndication.addRefatoring(TreeObject.REFACTOR_METHOD);
						lastIndication.addInfo(methodInfo);
						bMethodHasIndication = true;
					}
				}
					
				// References to TreeObjects related to statements in method that don't
				// belong to another statement and only belong to method. 
				// These methods need to have indications.
				ArrayList lstTreeObjects = new ArrayList();
				MethodRefactoringInfo methodStInfo = new MethodRefactoringInfo();
				// Storage boolean values that indicate if the statements have indication or not
//				ArrayList lstIndicationStatement = new ArrayList();
				try {
				// For each statement
				for ( int k = 0; k < statements.size(); k++ )
					if ( detectIndicationStatement ( (Statement)statements.get(k), methodInfo ) ) {
						bMethodHasIndication = true;
						if(lastIndication.getAstNode().equals(statements.get(k))) {
							methodStInfo.addStatement(true, (Statement)statements.get(k));
							lstTreeObjects.add(lastIndication);
						}
						else
							methodStInfo.addStatement(false, (Statement)statements.get(k));
					}
					else
						methodStInfo.addStatement(false, (Statement)statements.get(k));
				variables.removeLevel();
				if ( variables.getLevelCount() != 0 )
					System.err.println("Problems in handling local variables");
				} catch (NullPointerException e) {
					System.err.println(e.getMessage());
				}
				
				// Update TreeObject with refactoring information
				methodStInfo.generateRefactoring();
				TreeObject treeObject;
				for(int k = 0; k < lstTreeObjects.size(); k++) {
					treeObject = (TreeObject)lstTreeObjects.get(k);
					treeObject.addRefatoring(methodStInfo.getRefactoring((Statement)treeObject.getAstNode()));
				}
				
				// Stores informations about method
				if(bMethodHasIndication) {
					// Extract duplicated types in list of types found in this method
					int k = 0, l;
					while ( k < lstTypesFoundInMethod.size() ) {
						l = k + 1;
						while (l < lstTypesFoundInMethod.size()) {
							if ( lstTypesFoundInMethod.get(k).equals(lstTypesFoundInMethod.get(l)) )
								lstTypesFoundInMethod.remove(l);
							else l++;
						}
						k++;
					}
					methodInfo.setIndicationTypes(lstTypesFoundInMethod);

					methodInfo.setIndication(bMethodHasIndication);
					typeInfo.setIndication(true);
				}
				methodInfo.setRefactoringInfo(methodStInfo);
				typeInfo.addMethod(methodInfo);
			}
		}
		
		// Is there any type declaration inside this type declaration?
		if ( typeDeclaration.getTypes() != null && typeDeclaration.getTypes().length > 0 ) {
			TypeDeclaration[] arrayTypes = typeDeclaration.getTypes();
			// For each type declaration
			for ( int i = 0; i < arrayTypes.length; i++ ) {
				// The enclosured types about to be analised need to know what type
				// is their parent, so they would know through lastTypeInfo
				lastTypeInfo = typeInfo;
				detectIndication(arrayTypes[i]);
			}
		}
		// In the end of this class, we need to free the level (context) of fields created by this class
		lstFields.removeLevel();
		return true;
	}
	
	/**
	 * Look for indications in a EnumDeclaration structure.
	 * @param enumDeclaration
	 * @return
	 */
	private boolean detectIndication(EnumDeclaration enumDeclaration) {
		/*
		 * EnumDeclaration:
		 *      [ Javadoc ] { ExtendedModifier } enum Identifier
		 *               [ implements Type { , Type } ]
		 *               {
		 *               [ EnumConstantDeclaration { , EnumConstantDeclaration } ] [ , ]
		 *               [ ; { ClassBodyDeclaration | ; } ]
		 *               }
		 */
		List types = enumDeclaration.superInterfaceTypes();
		/*
		if ( types != null )
			for ( int i = 0; i < lstIndicationImports.size(); i++ )
				if ( isIndicationType( (Type)lstIndicationImports.get(i)) )
					createIndication( (Type)lstIndicationImports.get(i) );
		*/
		if ( types != null )
			for ( int i = 0; i < types.size(); i++ )
				if ( isIndicationType( (Type)types.get(i)) )
					createIndication( (Type)types.get(i) );
		List constants = enumDeclaration.enumConstants();
		if ( constants != null )
			for ( int i = 0; i < constants.size(); i++ ) {
				/*
				 * EnumConstantDeclaration:
				 *      [ Javadoc ] { ExtendedModifier } Identifier
				 *               [ ( [ Expression { , Expression } ] ) ]
				 *               [ AnonymousClassDeclaration ]
				 */
				EnumConstantDeclaration constantDeclaration = (EnumConstantDeclaration)constants.get(i);
				List arguments = constantDeclaration.arguments();
				for ( int j = 0; j < arguments.size(); j++ )
					if ( isIndicationExpression((Expression)arguments.get(j)) )
						createIndication( (Expression)arguments.get(j));
			}
		return true;
	}
	
	/**
	 * Search for a indication in a statement. If the statements
	 * contains other statement(s), each statement is analysed calling this
	 * method recursively.
	 * @param statement: statement to be analysed
	 * @return Returns true if indication was found
	 */
	private boolean detectIndicationStatement (Statement statement, MethodInfo methodInfo) {
		boolean bIndicationFound = false;
		/*
		 * AssertStatement:
		 *     assert Expression [ : Expression ] ;
		 */
		if ( statement instanceof AssertStatement ) {
			AssertStatement assertStatement = (AssertStatement)statement;
			if ( isIndicationExpression(assertStatement.getExpression()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
			else if ( isIndicationExpression( assertStatement.getMessage() ) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 * BreakStatement:
		 *     break [ Identifier ] ;
		 */
		else if ( statement instanceof BreakStatement ) {
			BreakStatement breakStatement = (BreakStatement)statement;
			if ( breakStatement.getLabel() != null && isIndicationIdentifier(breakStatement.getLabel().getIdentifier()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 * ConstructorInvocation:
		 *       [ < Type { , Type } > ]
		 *       		      this ( [ Expression { , Expression } ] ) ;
		 */
		else if ( statement instanceof ConstructorInvocation ) {
			ConstructorInvocation constructorInvocation = (ConstructorInvocation)statement;
			List types = constructorInvocation.typeArguments();
			for ( int i = 0; i < types.size(); i++ ) {
				if ( isIndicationType( (Type)types.get(i) ) ) {
					createIndication( statement );
					bIndicationFound = true;
					break;
				}
			}
			if ( ! bIndicationFound ) {
				List arguments = constructorInvocation.arguments();
				for ( int i = 0; i < arguments.size(); i++ )
					if ( isIndicationExpression( (Expression)arguments.get(i) ) ) {
						createIndication ( statement );
						bIndicationFound = true;
						break;
					}
			}
		}
		/*
		 * ContinueStatement:
		 *     continue [ Identifier ] ;
		 */
		else if ( statement instanceof ContinueStatement ) {
			ContinueStatement continueStatement = (ContinueStatement)statement;
			if ( isIndicationIdentifier(continueStatement.getLabel().getIdentifier()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 * DoStatement:
		 *     do Statement while ( Expression ) ;
		 */
		else if ( statement instanceof DoStatement ) {
			DoStatement doStatement = (DoStatement)statement;
			if ( isIndicationExpression(doStatement.getExpression()) ) {
				createIndication (doStatement.getExpression());
				bIndicationFound = true;
			}
		}
		/*
		 * EnhancedForStatement:
		 *     for ( FormalParameter : Expression )
		 *      			Statement
		 *      
		 * The FormalParameter is represented by a SingleVariableDeclaration  (without an initializer).
		 * 
		 * SingleVariableDeclaration:
		 *     { ExtendedModifier } Type [ ... ] Identifier { [] } [ = Expression ]
		 */
		else if ( statement instanceof EnhancedForStatement ) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
			SingleVariableDeclaration formalParameter = enhancedForStatement.getParameter();
			// Is the formal parameter a indication?
			if ( isIndicationType( formalParameter.getType() ) ) {
				createIndication ( formalParameter );
				bIndicationFound = true;
			}
			else if ( isIndicationIdentifier( formalParameter.getName().getIdentifier() ) ) {
				createIndication ( formalParameter );
				bIndicationFound = true;
			}
			// Is the expression of EnhancedForStatement a indication?
			if ( isIndicationExpression(enhancedForStatement.getExpression()) ) {
				createIndication (enhancedForStatement.getExpression());
				bIndicationFound = true;
			}
		}
		/*
		 *  ExpressionStatement:
		 *      StatementExpression ;
		 */
		else if ( statement instanceof ExpressionStatement ) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
			if ( isIndicationExpression(expressionStatement.getExpression()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 * ForStatement:
		 *     for (
		 *      			[ ForInit ];
		 *       			[ Expression ] ;
		 *       			[ ForUpdate ] )
		 *       			Statement
		 * ForInit:
		 *  		Expression { , Expression }
		 * ForUpdate:
		 *  		Expression { , Expression }
		 */
		else if ( statement instanceof ForStatement ) {
			ForStatement forStatement = (ForStatement)statement;
			// For each initializer
			List expressions = forStatement.initializers();
			for ( int i = 0; i < expressions.size(); i++ )
				if ( isIndicationExpression( (Expression)expressions.get(i) ) ) {
					createIndication ( (Expression)expressions.get(i) );
					bIndicationFound = true;
				}
			if ( isIndicationExpression(forStatement.getExpression()) ) {
				createIndication(forStatement.getExpression());
				bIndicationFound = true;
			}
			// For each updater
			expressions = forStatement.updaters();
			for ( int i = 0; i < expressions.size(); i++ )
				if ( isIndicationExpression( (Expression)expressions.get(i) ) ) {
					createIndication ( (Expression)expressions.get(i) );
					bIndicationFound = true;
				}
		}
		/*
		 * IfStatement:
		 *     if ( Expression ) Statement [ else Statement]
		 */
		else if ( statement instanceof IfStatement ) {
			IfStatement ifStatement = (IfStatement)statement;
			if ( isIndicationExpression(ifStatement.getExpression()) ) {
				createIndication (ifStatement.getExpression());
				bIndicationFound = true;
			}
		}
		/*
		 * LabeledStatement:
		 *     Identifier : Statement
		 */
		else if ( statement instanceof LabeledStatement ) {
			LabeledStatement labeledStatement = (LabeledStatement)statement;
			if ( isIndicationIdentifier(labeledStatement.getLabel().getIdentifier()) ) {
				createIndication (labeledStatement.getLabel());
				bIndicationFound = true;
			}
		}
		/*
		 * ReturnStatement:
		 *     return [ Expression ] ;
		 */
		else if ( statement instanceof ReturnStatement ) {
			ReturnStatement returnStatement = (ReturnStatement)statement;
			if ( isIndicationExpression(returnStatement.getExpression()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 *  SuperConstructorInvocation:
		 *       [ Expression . ]
		 *                [ < Type { , Type } > ]
		 *                super ( [ Expression { , Expression } ] ) ;
		 */
		else if ( statement instanceof SuperConstructorInvocation ) {
			SuperConstructorInvocation superConstructorInvocation = (SuperConstructorInvocation)statement;
			List types = superConstructorInvocation.typeArguments();
			boolean bIndication = false;
			for ( int i = 0; i < types.size(); i++ ) 
				if ( isIndicationType( (Type)types.get(i) ) ) {
					bIndication = true;
					break;
				}
			if ( !bIndication && isIndicationExpression( superConstructorInvocation.getExpression() ))
				bIndication = true;
			if ( !bIndication ) {
				List arguments = superConstructorInvocation.arguments();
				for ( int i = 0; i < arguments.size(); i++ ) 
					if ( isIndicationExpression( (Expression)arguments.get(i) ) ) {
						bIndication = true;
						break;
					}
			}
			if ( bIndication ) {
				createIndication( statement );
				bIndicationFound = true;
			}
		}
		/*
		 * SwitchCase:
		 * 		case Expression  :
		 * 		default :
		 */
		else if ( statement instanceof SwitchCase ) {
			SwitchCase switchCase = (SwitchCase)statement;
			if ( isIndicationExpression(switchCase.getExpression()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 * SwitchStatement:
		 * 		switch ( Expression )
		 *  	{ { SwitchCase | Statement } } }
		 */
		else if ( statement instanceof SwitchStatement ) {
			SwitchStatement switchStatement = (SwitchStatement)statement;
			if ( isIndicationExpression(switchStatement.getExpression()) ) {
				createIndication (switchStatement.getExpression());
				bIndicationFound = true;
			}
		}
		/*
		 * SynchronizedStatement:
		 *     synchronized ( Expression ) Block
		 */
		else if ( statement instanceof SynchronizedStatement ) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			if ( isIndicationExpression(synchronizedStatement.getExpression()) ) {
				createIndication (synchronizedStatement.getExpression());
				bIndicationFound = true;
			}
		}
		/*
		 * ThrowStatement:
		 *     throw Expression ;
		 */
		else if ( statement instanceof ThrowStatement ) {
			ThrowStatement throwStatement = (ThrowStatement)statement;
			if ( isIndicationExpression(throwStatement.getExpression()) ) {
				createIndication (statement);
				bIndicationFound = true;
			}
		}
		/*
		 * TryStatement:
		 *      try Block 
		 *               { CatchClause }
		 *               [ finally Block ]
		 */
		else if ( statement instanceof TryStatement ) {
			TryStatement tryStatement = (TryStatement)statement;
			List catchs = tryStatement.catchClauses();
			for ( int i = 0; i < catchs.size(); i++ ) 
				if ( isIndicationType (((CatchClause)catchs.get(i)).getException().getType()) ) {
					createIndication ((CatchClause)catchs.get(i));
					bIndicationFound = true;
				}
				else if ( isIndicationIdentifier( (((CatchClause)catchs.get(i)).getException().getName().getIdentifier()) )) {
					createIndication ((CatchClause)catchs.get(i));
					bIndicationFound = true;
				}
				else if ( isIndicationExpression( (((CatchClause)catchs.get(i)).getException().getInitializer()) )) {
					createIndication ((CatchClause)catchs.get(i));
					bIndicationFound = true;
				}
		}
		/*
		 * TypeDeclarationStatement:
		 *     TypeDeclaration
		 *     EnumDeclaration
		 */
		else if ( statement instanceof TypeDeclarationStatement ) {
			TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement)statement;
			if ( typeDeclarationStatement.getDeclaration() instanceof TypeDeclaration ) {
				lastTypeInfo = currentTypeInfo;
				detectIndication((TypeDeclaration)typeDeclarationStatement.getDeclaration());
			}
			else if ( typeDeclarationStatement.getDeclaration() instanceof EnumDeclaration )
				detectIndication((EnumDeclaration)typeDeclarationStatement.getDeclaration());
		}
		/*
		 * VariableDeclarationStatement:
		 *    { ExtendedModifier } Type VariableDeclarationFragment
		 *            { , VariableDeclarationFragment } ;
		 *            
		 *  VariableDeclarationFragment:
		 *      Identifier { [] } [ = Expression ]
		 */
		else if ( statement instanceof VariableDeclarationStatement ) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
			List fragments = variableDeclarationStatement.fragments();
			if ( isIndicationType(variableDeclarationStatement.getType()) ) {
				createIndication (statement);
				bIndicationFound = true;
				for ( int i = 0; i < fragments.size(); i++ )
					variables.addElement( new VariableInfo( variableDeclarationStatement.getType().toString(),
							((VariableDeclarationFragment)fragments.get(i)).getName().getIdentifier()) );
			}
			else {
				for (int i = 0; i < fragments.size(); i++)
					if ( isIndicationVariableDeclarationFragment((VariableDeclarationFragment)fragments.get(i)) ) {
						if ( fragments.size() == 1 )
							createIndication (statement);
						else
							createIndication ((VariableDeclarationFragment)fragments.get(i));
						variables.addElement( new VariableInfo( variableDeclarationStatement.getType().toString(),
								((VariableDeclarationFragment)fragments.get(i)).getName().getIdentifier() ));
						bIndicationFound = true;
					}
			}
		}
		/*
		 * WhileStatement:
		 *     while ( Expression ) Statement
		 */
		else if ( statement instanceof WhileStatement ) {
			WhileStatement whileStatement = (WhileStatement)statement;
			if ( isIndicationExpression(whileStatement.getExpression()) ) {
				createIndication (whileStatement.getExpression());
				bIndicationFound = true;
			}
		}
		if(bIndicationFound) {
			lastIndication.addRefatoring(TreeObject.REFACTOR_METHOD);
			lastIndication.addInfo(methodInfo);
		}
		
		// Will a new block begin?
		boolean bNewBlock = IsBlock( statement );
		
		// Does statement have other statements? 
		List statements = getStatements( statement );
		if ( statements != null ) {
			if ( bNewBlock )
				variables.addLevel();
			// Look at each statement
			for ( int i = 0; i < statements.size(); i++ ) {
				if(bIndicationFound)
					detectIndicationStatement ( (Statement)statements.get(i), methodInfo );
				else
					bIndicationFound = detectIndicationStatement ( (Statement)statements.get(i), methodInfo );
			}
			if ( bNewBlock )
				variables.removeLevel();
		}
		return bIndicationFound;
	} 
	
	/**
	 * The methods detectIndicationStatement is called recursively in order to find
	 * indications in statements. The method isIndicationExpression also calls itself
	 * recursively in order to find indications in expressions. One of finish 
	 * condition is when a indication type is found. This method verify if the type is
	 * an indication type. 
	 * @param type
	 * @return true if the type is an indication or false, otherwise.
	 */
	private boolean isIndicationType (Type type) {
		if ( ifFiltroByType && lstIndicationImports.isEmpty() ) {
			return false;
		}
		
		String sType = getTypeName(type);
		Indication indication = getActiveIndication();
		if ( sType.indexOf('.') == -1 ) {
			// The type doesn't have qualified name
			if ( !lstIndicationImports.isEmpty() ){
				if ( ((String)lstIndicationImports.get(0)).endsWith("*") ) {
					// Verify if the type belongs to current indication
					for( int i = 0; i < indication.getPackages().size(); i++)
						if( ((IndicationPackage)indication.getPackages().get(i)).isType(sType) ) {
							lstTypesFoundInMethod.add(((IndicationPackage)indication.getPackages().get(i)).getName() + 
									"." + sType);
							return true;
						}
				}
				else for ( int i = 0; i < lstIndicationImports.size(); i++ ) {
					// Verify if the type was imported and belongs to current indication
					String sImportedType = ((String) lstIndicationImports.get(i));
					sImportedType = sImportedType.substring(sImportedType.lastIndexOf('.') + 1,
							sImportedType.length());
					if ( sType.compareTo(sImportedType) == 0 ) {
						lstTypesFoundInMethod.add((String) lstIndicationImports.get(i));
						return true;
					}
				}
			}
		}
		else {
			// The type have qualified name
			return indication.isType(sType);
		}
		return false;
	}
	
	/**
	 * This method looks for indications in an expression. Some expressions 
	 *  have other expressions, and this expressions have other expressions, and
	 *  so on. So this method is called ina recursuvely way.	 * 
	 * @param expression an expression to be evaluated
	 * @return true if the method has an indication or false, otherwise.
	 */
	private boolean isIndicationExpression (Expression expression) {
		/*
		 * ArrayAccess:
		 *     Expression [ Expression ]
		 */
		if  ( expression instanceof ArrayAccess ) {
			ArrayAccess arrayAccess = (ArrayAccess)expression; 
			if ( isIndicationExpression (arrayAccess.getArray()) )
				return true;
			else if ( isIndicationExpression (arrayAccess.getIndex()) )
				return true;
		}
		/*
		 * ArrayCreation:
		 *     new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
		 *     new TypeName [ < Type { , Type } > ]
		 *         [ Expression ] { [ Expression ] } { [ ] }
		 *     new PrimitiveType [ ] { [ ] } ArrayInitializer
		 *     new TypeName [ < Type { , Type } > ]
		 *         [ ] { [ ] } ArrayInitializer
		 */
		else if  ( expression instanceof ArrayCreation ) {
			ArrayCreation arrayCreation = (ArrayCreation)expression;
			if ( isIndicationType( arrayCreation.getType().getComponentType() ) )
				return true;
			else if ( isIndicationType( arrayCreation.getType().getElementType() ) )
				return true;
			for ( int i = 0; i < arrayCreation.dimensions().size(); i++ )
				if ( isIndicationExpression( (Expression)arrayCreation.dimensions().get(i)) ) 
					return true;
			if ( isIndicationExpression( arrayCreation.getInitializer()) ) 
				return true;
		}
		/*
		 *  ArrayInitializer:
		 *   		{ [ Expression { , Expression} [ , ]] }
		 */
		else if  ( expression instanceof ArrayInitializer ) {
			ArrayInitializer arrayInitializer = (ArrayInitializer)expression; 
			for ( int i = 0; i < arrayInitializer.expressions().size(); i++ )
				if ( isIndicationExpression( (Expression) arrayInitializer.expressions().get(i)) )
					return true;
		}
		/*
		 * Assignment:
		 *     Expression AssignmentOperator Expression
		 */
		else if  ( expression instanceof Assignment ) {
			Assignment assignment = (Assignment)expression; 
			if ( isIndicationExpression(assignment.getLeftHandSide()) )
				return true;
			else if (isIndicationExpression(assignment.getRightHandSide()) )
				return true;
		}
		/*
		 * CastExpression:
		 *     ( Type ) Expression
		 */
		else if  ( expression instanceof CastExpression ) {
			CastExpression castExpression = (CastExpression)expression;
			if ( isIndicationType( castExpression.getType() ) )
				return true;
			else if ( isIndicationExpression( castExpression.getExpression() ) )
				return true;
		}
		/*
		 * ClassInstanceCreation:
		 *         [ Expression . ]
		 *           new [ < Type { , Type } > ]
		 *           Type ( [ Expression { , Expression } ] )
		 *           [ AnonymousClassDeclaration ]
		 */
		else if  ( expression instanceof ClassInstanceCreation ) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression; 
			if ( isIndicationType( classInstanceCreation.getType() ) )
				return true;
			for ( int i = 0; i < classInstanceCreation.typeArguments().size(); i++ )
				if ( isIndicationType((Type)classInstanceCreation.typeArguments().get(i)) )
					return true;
			if ( isIndicationExpression(classInstanceCreation.getExpression()) )
				return true;
			for ( int i = 0; i < classInstanceCreation.arguments().size(); i++ )
				if ( isIndicationExpression((Expression)classInstanceCreation.arguments().get(i)) )
					return true;
		}
		/*
		 * ConditionalExpression:
		 *     Expression ? Expression : Expression
		 */
		else if  ( expression instanceof ConditionalExpression ) {
			ConditionalExpression conditionalExpression = (ConditionalExpression)expression; 
			if ( isIndicationExpression(conditionalExpression.getExpression()) )
				return true;
			else if ( isIndicationExpression(conditionalExpression.getThenExpression()) )
				return true;
			else if ( isIndicationExpression(conditionalExpression.getElseExpression()) )
				return true;
		}
		/*
		 * FieldAccess:
		 *  		Expression . Identifier
		 */
		else if  ( expression instanceof FieldAccess ) {
			FieldAccess fieldAccess = (FieldAccess)expression;
			if ( isIndicationIdentifier( fieldAccess.getName().getIdentifier() ) )
				return true;
			else if ( isIndicationExpression(fieldAccess.getExpression()) )
				return true;
		}
		/*
		 * InfixExpression:
		 *     Expression InfixOperator Expression { InfixOperator Expression } 
		 */
		else if  ( expression instanceof InfixExpression ) {
			InfixExpression infixExpression = (InfixExpression)expression; 
			if ( isIndicationExpression(infixExpression.getLeftOperand()) )
				return true;
			else if ( isIndicationExpression(infixExpression.getRightOperand()) )
				return true;
			for ( int i = 0; i < infixExpression.extendedOperands().size(); i++ )
				if ( isIndicationExpression( (Expression)infixExpression.extendedOperands().get(i) ) )
					return true;
		}
		/*
		 * InstanceofExpression:
		 *     Expression instanceof Type
		 */
		else if  ( expression instanceof InstanceofExpression ) {
			InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
			if ( isIndicationType( instanceofExpression.getRightOperand() ) )
				return true;
			else if ( isIndicationExpression( instanceofExpression.getLeftOperand() ) )
				return true;
		}
		/*
		 * MethodInvocation:
		 *      [ Expression . ]
		 *               [ < Type { , Type } > ]
		 *               Identifier ( [ Expression { , Expression } ] )
		 */
		else if  ( expression instanceof MethodInvocation ) {
			MethodInvocation methodInvocation = (MethodInvocation)expression;
			for ( int i = 0; i < methodInvocation.typeArguments().size(); i++ )
				if ( isIndicationType((Type)methodInvocation.typeArguments().get(i)) )
					return true;
			if ( methodInvocation.getExpression() != null && 
				isIndicationExpression (methodInvocation.getExpression() ) )
				return true;
			if ( isIndicationIdentifier( methodInvocation.getName().getIdentifier() ) )
				return true;
			if ( methodInvocation.arguments() != null )
				for ( int i = 0; i < methodInvocation.arguments().size(); i++ )
					if ( isIndicationExpression((Expression)methodInvocation.arguments().get(i)) )
						return true;
		}
		/*
		 * Name:
		 *      SimpleName
		 *      QualifiedName
		 */
		else if  ( expression instanceof SimpleName ) {
			SimpleName simpleName = (SimpleName)expression;
			if ( isIndicationIdentifier(simpleName.getIdentifier()) )
					return true;
		}
		else if  ( expression instanceof QualifiedName ) {
			QualifiedName qualifiedName = (QualifiedName)expression;
			if ( isIndicationExpression(qualifiedName.getName()) )
				return true;
		}
		/*
		 * ParenthesizedExpression:
		 *      ( Expression )
		 */
		else if  ( expression instanceof ParenthesizedExpression ) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression)expression;
			if ( isIndicationExpression( parenthesizedExpression.getExpression() ) )
				return true;
		}
		/*
		 * PostfixExpression:
		 *     Expression PostfixOperator
		 */
		else if  ( expression instanceof PostfixExpression ) {
			PostfixExpression postfixExpression = (PostfixExpression)expression;
			if ( isIndicationExpression ( postfixExpression.getOperand() ) )
				return true;
		}
		/*
		 * StringLiteral:
		 * 		<SQL>
		 */
		else if  ( expression instanceof StringLiteral ) {
			StringLiteral stringLiteral = (StringLiteral)expression;
			if ( isIndicationStringLiteral(stringLiteral) )
				return true;
		}
		/*
		 * SuperFieldAccess:
		 *      [ ClassName . ] super . Identifier
		 */
		else if  ( expression instanceof SuperFieldAccess ) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
			if ( isIndicationExpression( superFieldAccess.getQualifier() ) )
				return true;
			if ( isIndicationIdentifier( superFieldAccess.getName().getIdentifier() ) )
				return true;
		}
		/*
		 * SuperMethodInvocation:
		 *      [ ClassName . ] super .
		 *               [ < Type { , Type } > ]
		 *               Identifier ( [ Expression { , Expression } ] )
		 */
		else if  ( expression instanceof SuperMethodInvocation ) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
			if ( superMethodInvocation.typeArguments() != null && 
				 superMethodInvocation.typeArguments().size() == 0 )
				for ( int i = 0; i < superMethodInvocation.typeArguments().size(); i++ )
					if ( isIndicationType( (Type)superMethodInvocation.typeArguments().get(i) ) )
						return true;
			if ( isIndicationIdentifier( superMethodInvocation.getName().getIdentifier() ) )
				return true;
			if ( isIndicationExpression ( superMethodInvocation.getQualifier() ) )
				return true;
			if ( superMethodInvocation.arguments() != null )
				for ( int i = 0; i < superMethodInvocation.arguments().size(); i++ )
					if ( isIndicationExpression((Expression)superMethodInvocation.arguments().get(i) ))
						return true;
		}
		/*
		 * TypeLiteral:
		 *      ( Type | void ) . class
		 */
		else if  ( expression instanceof TypeLiteral ) {
			TypeLiteral typeLiteral = (TypeLiteral)expression;
			if ( isIndicationType( typeLiteral.getType() ) )
				return true;
		}
		/*
		 * VariableDeclarationExpression:
		 *     { ExtendedModifier } Type VariableDeclarationFragment
		 *              { , VariableDeclarationFragment } 
		 */
		else if  ( expression instanceof VariableDeclarationExpression ) {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
			boolean bIndicationType = false;
			VariableDeclarationFragment fragment = null;
			if ( isIndicationType( variableDeclarationExpression.getType() ) )
				bIndicationType = true;
			// For each fragment
			for ( int i = 0; i < variableDeclarationExpression.fragments().size(); i++ ){
				/*
				 *  VariableDeclarationFragment:
				 *      Identifier { [] } [ = Expression ]
				 */
				fragment = (VariableDeclarationFragment)variableDeclarationExpression.fragments().get(i);
				if ( ! bIndicationType ) {
					if ( isIndicationExpression( fragment.getInitializer() ) )
						variables.addElement( new VariableInfo( variableDeclarationExpression.getType().toString(),
								fragment.getName().getIdentifier() ));
				}
				else
					variables.addElement( new VariableInfo( variableDeclarationExpression.getType().toString(), 
							fragment.getName().getIdentifier() ));
			}
		}
		return false;
	}
	
	/**
	 * This method verify if the variable has a name that could be an indication.
	 * @param name variable name
	 * @return true if the variable has an indication name or false, otherwise.
	 */
	private boolean isIndicationVariableName ( SimpleName name ) {
		String sName = name.getIdentifier();
		return isIndicationMatch( sName, MatchText.VARIABLE_NAME );
	}
	
	/**
	 * Create an indication node in the model of indication tree and include
	 * the indication in the list of problems in problem view. 
	 * @param node it's the statement with indications.
	 * @return
	 */
	private boolean createIndication( ASTNode node ) {
		IMarker marker = null;
		
		// Marker creation
		try {
			marker = file.createMarker(IMarker.TASK);
			marker.setAttribute (IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute (IMarker.MESSAGE, node.toString().replace('\n',' '));
			marker.setAttribute (IMarker.CHAR_START, node.getStartPosition() );
			marker.setAttribute (IMarker.CHAR_END, node.getStartPosition() + node.getLength() );
			marker.setAttribute (IMarker.LINE_NUMBER, unit.getLineNumber(node.getStartPosition()) );
		} catch (CoreException e) {
			System.err.println("Problems found during marker creation in " + 
					node.toString().replace('\n',' '));
			return false;
		}
		// Adding indication node in tree
		lastIndication = new TreeObject(node.toString().replace('\n',' '), 
				IndicationTree.ASPECT_INDICATION, node);
		lastIndication.setId( marker.getId() );
		((TreeParent)fileNode).addChild(lastIndication);

		return true;
	}
	
	/**
	 * The methods detectIndicationStatement is called recursively in order to find
	 * indications in statements. The method isIndicationExpression also calls itself
	 * recursively in order to find indications in expressions. One of finish 
	 * condition is when a variable of indication type is found. This method verify 
	 * if the variable has an indication type. 
	 * @param sQualifiedName
	 * @return
	 */
	private boolean isIndicationIdentifier ( String sQualifiedName ) {
		String[] sParts = sQualifiedName.split (".");
		List fields;
		if ( sParts.length > 1 ) {
			int i;
			// Compara com os elementos da lista de atributos da classe / interface
			fields = lstFields.getAllElements();
			for ( i = 0; i < fields.size(); i++ )
				if ( ((VariableInfo)fields.get(i)).getName().compareTo(sParts[sParts.length - 2]) == 0 )
					return true;
			// Compara com os elementos da lista de argumentos do método
			for ( i = 0; i < lstMethodArguments.size(); i++ )
				if ( ((String)lstMethodArguments.get(i)).compareTo(sParts[sParts.length - 2]) == 0 )
					return true;
			// Compara com os elementos da lista de variáveis locais de métodos
			// da classe / interface
			ArrayList varLst = variables.getAllElements();
			for ( i = 0; i < varLst.size(); i++ )
				if ( ((VariableInfo)varLst.get(i)).getName().compareTo(sParts[sParts.length - 2]) == 0 )
					return true;
		}
		else if ( (! sQualifiedName.contains(".") && sQualifiedName.length() > 0 ) ) {
			int i;
			fields = lstFields.getAllElements();
			// Compara com os elementos da lista de atributos da classe / interface
			for ( i = 0; i < fields.size(); i++ )
				if ( ((VariableInfo)fields.get(i)).getName().compareTo(sQualifiedName) == 0 )
					return true;
			// Compara com os elementos da lista de parâmetros do método
			for ( i = 0; i < lstMethodArguments.size(); i++ )
				if ( ((String)lstMethodArguments.get(i)).compareTo(sQualifiedName) == 0 )
					return true;
			// Compara com os elementos da lista de variáveis locais de métodos
			// da classe / interface
			ArrayList varLst = variables.getAllElements();
			for ( i = 0; i < varLst.size(); i++ )
				if ( ((VariableInfo)varLst.get(i)).getName().compareTo(sQualifiedName) == 0 )
					return true;
		}
		return false;
	}
	
	/**
	 * Verify if the string literal has indications.
	 * @param stringLiteral
	 * @return true if the string literal has indications or false, otherwise.
	 */
	private boolean isIndicationStringLiteral ( StringLiteral stringLiteral ) {
		String sContent = stringLiteral.getLiteralValue();
		return isIndicationMatch( sContent, MatchText.STRING_LITERAL);
	}
	
	/**
	 * Look for indication in a variable declaration fragment of the AST
	 * @param fragment
	 * @return true if some indication was found or false, otherwise.
	 */
	private boolean isIndicationVariableDeclarationFragment ( VariableDeclarationFragment fragment ) {
		/*
		 *  VariableDeclarationFragment:
		 *      Identifier { [] } [ = Expression ]
		 */
		boolean bReturn = false;
		if ( isIndicationExpression(fragment.getInitializer()) )
			bReturn = true;
		else if ( isIndicationVariableName( fragment.getName() ) )
			bReturn = true;
		return bReturn;
	}
	
	/**
	 * Look for the first active indication.
	 * @return: return the first active indication or null if there
	 * is no active indication.
	 */
	private Indication getActiveIndication () {
		// Looking for the first indication active
		ArrayList lstIndications = provider.getIndications();
		for ( int i = 0; i < lstIndications.size(); i++ )
			if ( ((Indication)lstIndications.get(i)).isActive() ) 
				return (Indication)lstIndications.get(i);
		return null;
	}
	
	/**
	 * It looks for indications in some text (string literal or variable name).
	 * @param sContent text where the indication will be search.
	 * @param sTarget string literal or variable name
	 * @return true if indication was found or false, otherwise.
	 */
	private boolean isIndicationMatch (String sContent, String sTarget) {
		Indication indication = getActiveIndication();
		ArrayList stringMatches = indication.getMatches();
		MatchText match = null;
		ArrayList lstWords = null;
		for ( int i = 0; i < stringMatches.size(); i++ )
			if ( ((MatchText)stringMatches.get(i)).getTarget() == sTarget ) {
				match = (MatchText)stringMatches.get(i);
				lstWords = match.getWords(); 
				if ( match.isCaseSensity() ) {
					if ( match.getRule().equalsIgnoreCase(MatchText.CONTAINS) ) {
						for ( int j = 0; j < lstWords.size(); j++ )
							if ( sContent.contains( (String)lstWords.get(j) ) )
								return true;
						return false;
					}
					else if ( match.getRule().equalsIgnoreCase(MatchText.STARTS_WITH) ) {
						for ( int j = 0; j < lstWords.size(); j++ )
							if ( sContent.startsWith( (String)lstWords.get(j) ) )
								return true;
						return false;
					}
					else if ( match.getRule().equalsIgnoreCase(MatchText.ENDS_WITH) ) {
						for ( int j = 0; j < lstWords.size(); j++ )
							if ( sContent.endsWith( (String)lstWords.get(j) ) )
								return true;
						return false;
					}
					else
						return false;
				}
				else{
					if ( match.getRule().equalsIgnoreCase(MatchText.CONTAINS) ) {
						for ( int j = 0; j < lstWords.size(); j++ )
							if ( sContent.toLowerCase().contains( ((String)lstWords.get(j)).toLowerCase() ) )
								return true;
						return false;
					}
					else if ( match.getRule().equalsIgnoreCase(MatchText.STARTS_WITH) ) {
						for ( int j = 0; j < lstWords.size(); j++ )
							if ( sContent.toLowerCase().startsWith( ((String)lstWords.get(j)).toLowerCase() ) )
								return true;
						return false;
					}
					else if ( match.getRule().equalsIgnoreCase(MatchText.ENDS_WITH) ) {
						for ( int j = 0; j < lstWords.size(); j++ )
							if ( sContent.toLowerCase().endsWith( ((String)lstWords.get(j)).toLowerCase() ) )
								return true;
						return false;
					}
					else
						return false;
				}
			}
		return false;
	}
	
	/**
	 * Verify if the statement ALWAYS has an associated statement.
	 * @param statement
	 * @return true if the statement always has an associated statement or false, 
	 * otherwise.
	 */
	boolean IsBlock( Statement statement ) {
		if ( statement instanceof Block || 
			 statement instanceof SynchronizedStatement )
			return true;
		else 
			return false;
	}
	
	/**
	 * Get a list of statements from a statement.
	 * @param statement
	 * @return a list of statements.
	 */
	List getStatements ( Statement statement ) {
		// Does the statement have block?
		Block block = null;
		Statement st = null;
		List statements = null;
		if ( statement instanceof Block )
			block = (Block)statement;
		else if ( statement instanceof DoStatement )
			st = ((DoStatement)statement).getBody();
		else if ( statement instanceof EnhancedForStatement )
			st = ((EnhancedForStatement)statement).getBody();
		else if ( statement instanceof ForStatement )
			st = ((ForStatement)statement).getBody();
		else if ( statement instanceof LabeledStatement )
			st = ((LabeledStatement)statement).getBody();
		else if ( statement instanceof SwitchStatement )
			statements = ((SwitchStatement)statement).statements();
		else if ( statement instanceof SynchronizedStatement )
			block = ((SynchronizedStatement)statement).getBody();
		else if ( statement instanceof WhileStatement )
			st = ((WhileStatement)statement).getBody();
		else if ( statement instanceof IfStatement ) {
			statements = new ArrayList();
			statements.add(((IfStatement)statement).getThenStatement());
			// Is there a else part?
			if ( ((IfStatement)statement).getElseStatement() != null )
				statements.add(((IfStatement)statement).getElseStatement());
		}
		else if ( statement instanceof TryStatement ) {
			statements = new ArrayList();
			statements.add(((TryStatement)statement).getBody());
			List catchClauses = ((TryStatement)statement).catchClauses();
			for ( int i = 0; i < catchClauses.size(); i++ ) 
				statements.add(((CatchClause)catchClauses.get(i)).getBody());
			if ( ((TryStatement)statement).getFinally() != null )
				statements.add(((TryStatement)statement).getFinally());
		}
		
		if ( block != null )
			statements = block.statements();
		else if ( st != null ) {
			statements = new ArrayList();
			statements.add( st );
		}
		return statements;
	}
	
	/**
	 * Get the type name of a type object. A type object has six varitions. 
	 * Each variation is considered 
	 * @param type
	 * @return type name or empty string when the type is primitive.
	 */
	String getTypeName ( Type type ) {
		/*
		 * Type:
		 *     PrimitiveType
		 *     ArrayType
		 *     SimpleType
		 *     QualifiedType
		 *     ParameterizedType
		 *     WildcardType
		 * PrimitiveType:
		 *     byte
		 *     short
		 *     char
		 *     int
		 *     long
		 *     float
		 *     double
		 *     boolean
		 *     void
		 * ArrayType:
		 *     Type [ ]
		 * SimpleType:
		 *     TypeName
		 * ParameterizedType:
		 *     Type < Type { , Type } >
		 * QualifiedType:
		 *     Type . SimpleName
		 * WildcardType:
		 *     ? [ ( extends | super) Type ] 
		 */
		String sReturn = "";
		if ( type instanceof ArrayType )
			return getTypeName(((ArrayType)type).getElementType());
		else if ( type instanceof ParameterizedType )
			return getTypeName(((ParameterizedType)type).getType());
		else if ( type instanceof PrimitiveType )
			return sReturn;
		else if ( type instanceof QualifiedType )
			return (((QualifiedType)type).getName().getIdentifier());
		else if ( type instanceof SimpleType ) {
			SimpleType simpleType = (SimpleType)type;
			if ( simpleType.getName().isSimpleName() )
				return ((SimpleName)simpleType.getName()).getIdentifier();
			else if ( simpleType.getName().isQualifiedName() )
				return ((QualifiedName)simpleType.getName()).getFullyQualifiedName();
		}
		else if ( type instanceof WildcardType )
			return getTypeName( ((WildcardType)type).getBound() );
		return sReturn;
	}
}
