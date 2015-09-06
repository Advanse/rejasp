package br.ufscar.dc.rejasp.plugin;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Plugin extends AbstractUIPlugin {

	public static final String FILTER_BY_IMPORT_PREFERENCE = "filterByImport";
	//The shared instance.
	private static Plugin plugin;
	
	/**
	 * The constructor.
	 */
	public Plugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Plugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		String iconPath = "icons/";
		try {
			URL installURL = getDefault().getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + name);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeDefaultPreferences(org.eclipse.jface.preference.IPreferenceStore)
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(FILTER_BY_IMPORT_PREFERENCE, true);
	}

	public boolean getDefaultFilterByImport() {
		return getPreferenceStore().getDefaultBoolean(FILTER_BY_IMPORT_PREFERENCE);
	}
	
	public boolean isFilterByImport() {
		return getPreferenceStore().getBoolean(FILTER_BY_IMPORT_PREFERENCE);
	}
	
	public void setFilterByImport( boolean bisFilterByImport ) {
		getPreferenceStore().setValue(FILTER_BY_IMPORT_PREFERENCE, bisFilterByImport);		
	}
}
