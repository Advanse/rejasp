package br.ufscar.dc.rejasp.preferences.pages;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import br.ufscar.dc.rejasp.plugin.Plugin;


/**
 * MainPreferencePage is a preference that allows to choose
 * a strategic when identification is performing.
 * @author Daniel
 *
 */
public class MainPreferencePage 
	extends PreferencePage
	implements IWorkbenchPreferencePage {
	
	private Button btnUseImport;
	private Button btnUseAllTypes;
	
	protected Control createContents(Composite parent) {
		Composite entryTable = new Composite(parent, SWT.NULL);

		//Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);

		Group useImport = new Group( entryTable, SWT.SHADOW_IN );
		useImport.setText("Indication Identification");
		useImport.setLayout(new RowLayout(SWT.VERTICAL));
		
		btnUseImport = new Button( useImport, SWT.RADIO );
		btnUseImport.setText("Based on imported types");
		
		btnUseAllTypes = new Button( useImport, SWT.RADIO );
		btnUseAllTypes.setText("Using all types belonging to a particular Indication");

		boolean bisFilterByImport = Plugin.getDefault().isFilterByImport();
		btnUseImport.setSelection( bisFilterByImport );
		btnUseAllTypes.setSelection( ! bisFilterByImport );
		
		return entryTable;
	}

	public void init(IWorkbench workbench) {

		setPreferenceStore ( Plugin.getDefault().getPreferenceStore() );		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {

		boolean bisFilterByImport = Plugin.getDefault().getDefaultFilterByImport();
		
		btnUseImport.setSelection( bisFilterByImport );
		btnUseAllTypes.setSelection( ! bisFilterByImport );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		Plugin.getDefault().setFilterByImport( btnUseImport.getSelection() );
		return super.performOk();
	}
}
