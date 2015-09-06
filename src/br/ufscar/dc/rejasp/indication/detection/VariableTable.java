package br.ufscar.dc.rejasp.indication.detection;

import java.util.ArrayList;

import br.ufscar.dc.rejasp.model.ASTNodeInfo.FieldInfo;
import br.ufscar.dc.rejasp.model.ASTNodeInfo.VariableInfo;


/**
 * @author Daniel Kawakami
 * Class of two dimensions, used to control and store local variable. It is the
 * struture used to store local variable or fields in a class. Local variables
 * have diferent visibility which depends on the place they are declared. If a 
 * variable is declared inside a for-block, this variable exists inside this block
 * and other blocks inside the block it was declared, but it is not visible outside 
 * this block. The block more external (method block), let's name as block A, has 
 * another block, block B. Variables declared in block A belong to level 1, variables
 * declared in block B level 2, and so on. The first element in VariableTable is a
 * collection of variables declared in level 1, second element is a collection of
 * variables declared in level 2, third element in level 3, and so on. This is 
 * structure can be applied to fields in a module (module = class or interface) 
 * because modules has other modules, and these modules other modules too, and so on.
 * Therefore the fields can be handled in diferent levels and have different 
 * visibility. 
 */
public class VariableTable {
	/**
	 * Each element of this list is another list. 
	 */
	private ArrayList table;
	/**
	 * Default contructor
	 */
	public VariableTable () {
		table = new ArrayList();
	}
	public int addLevel() {
		table.add( new ArrayList () );
		return table.size();
	}
	public int removeLevel() {
		if ( table.size() == 0 )
			return 0;
		table.remove( table.size() - 1 );
		return table.size();
	}
	
	public void removeAll() {
		table.clear();
	}
	/**
	 * Add an variable that was declared in the last level handled, so this
	 * variable belong to this level.
	 * @param sElement
	 * @return
	 */
	public boolean addElement( VariableInfo element ) {
		if ( table.size() == 0 )
			return false;
		ArrayList lastLevel = (ArrayList)table.get( table.size() - 1 );
		lastLevel.add(element);
		return true;
	}
	/**
	 * Add an field that was declared in the last level handled, so this
	 * field belong to this level.
	 * @param sElement
	 * @return
	 */
	public boolean addElement( FieldInfo element ) {
		if ( table.size() == 0 )
			return false;
		ArrayList lastLevel = (ArrayList)table.get( table.size() - 1 );
		lastLevel.add(element);
		return true;
	}
	/**
	 * Make a list of all elements visible in the last level.
	 * @return a list with all elements 
	 */
	public ArrayList getAllElements() {
		ArrayList lstAll = new ArrayList();
		for ( int i = table.size() - 1; i >= 0; i-- )
			lstAll.addAll ((ArrayList)table.get(i));
		return lstAll;
	}
	/**
	 * @return the number of levels.
	 */
	public int getLevelCount() {
		return table.size();
	}
	
	public void invertLevels() {
		ArrayList newTable = new ArrayList();
		for( int i = table.size() - 1; i >= 0; i-- )
			newTable.add(table.get(i));
		table = newTable;
	}
	
	/**
	 * @param level 1 is first level and number of level is the last one
	 * @return null for invalidate parameter of level
	 */
	public ArrayList getElementsAtLevel(int level) {
		if ( level < 1 || level > table.size())
			return null;
		return ((ArrayList)table.get(level - 1));
	}
}
