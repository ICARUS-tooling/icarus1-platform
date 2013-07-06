package net.ikarus_systems.icarus.plugins.weblicht;

public interface WeblichtConstants {
	
	// Plugin ID
	public static final String WEBLICHT_PLUGIN_ID = 
			"net.ikarus_systems.icarus.weblicht"; //$NON-NLS-1$
		
	// Perspective IDs
	public static final String WEBLICHT_PERSPECTIVE_ID = 
			WEBLICHT_PLUGIN_ID+"@WeblichtPerspective"; //$NON-NLS-1$
	
	// Event constants
	public static final String WEBLICHT_CHAIN_VIEW_CHANGED = 
			"weblichtTools:explorerSelectionChanged"; //$NON-NLS-1$
	
	public static final String WEBLICHT_WEBSERVICE_VIEW_CHANGED = 
			"weblichtTools:explorerSelectionChanged"; //$NON-NLS-1$
	
	// View IDs
	public static final String WEBLICHT_CHAIN_VIEW_ID = 
			WEBLICHT_PLUGIN_ID+"@WeblichtChainView"; //$NON-NLS-1$
	
	public static final String WEBLICHT_WEBSERVICE_VIEW_ID = 
			WEBLICHT_PLUGIN_ID+"@WeblichtWebserviceView"; //$NON-NLS-1$
	
	public static final String WEBLICHT_EDIT_VIEW_ID = 
			WEBLICHT_PLUGIN_ID+"@WeblichtEditView"; //$NON-NLS-1$

	public static final String WEBSERVICE_EDIT_VIEW_ID = 
			WEBLICHT_PLUGIN_ID+"@WebserviceEditView"; //$NON-NLS-1$
}
