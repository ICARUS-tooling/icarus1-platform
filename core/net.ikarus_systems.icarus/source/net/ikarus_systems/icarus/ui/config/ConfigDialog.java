/**
 * 
 */
package net.ikarus_systems.icarus.ui.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.ikarus_systems.icarus.config.ConfigConstants;
import net.ikarus_systems.icarus.config.ConfigEvent;
import net.ikarus_systems.icarus.config.ConfigListener;
import net.ikarus_systems.icarus.config.ConfigRegistry;
import net.ikarus_systems.icarus.config.ConfigRegistry.EntryType;
import net.ikarus_systems.icarus.config.ConfigRegistry.Handle;
import net.ikarus_systems.icarus.config.ConfigUtils;
import net.ikarus_systems.icarus.config.EntryHandler;
import net.ikarus_systems.icarus.config.MapHandler;
import net.ikarus_systems.icarus.resources.ResourceDomain;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.GridBagUtil;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.Actions;
import net.ikarus_systems.icarus.util.CollectionUtils;



/**
 * 
 * @author Gregor Thiele
 *
 */
public class ConfigDialog extends JDialog implements ConfigConstants {

	private static final long serialVersionUID = 6372741052385278814L;
	
	protected final JTree tree;	
	protected final Handle masterhandle;	
	protected final Actions actions;
	protected final JPanel configHeaderPanel = new JPanel();	
	protected final JPanel configOptionsPanel = new JPanel();
	protected final JScrollPane spOpt = new JScrollPane();
	protected final JPanel configBottomPanel = new JPanel();
	
	
	
	static Map<Handle, TreePath> treeMap = new LinkedHashMap<Handle, TreePath>();
	List< Handle> defaultList = new ArrayList<Handle>();
	Map<Handle, Object> changesMap = new HashMap<Handle, Object>();
	//protected final ConfigRegistry config;
	
	// prepare config
	private final ConfigRegistry config;

	
	// Handle Constructor
	public ConfigDialog(ConfigRegistry config, Handle starthandle) {
		super();
		
		this.config = config;
		
        this.setTitle(ResourceManager.getInstance().get("config.PreferencesTitle.title"));  //$NON-NLS-1$
        
		actions = createActions();        
		tree = createTree(config);	
		spOpt.getVerticalScrollBar().setUnitIncrement(16);
		masterhandle = starthandle;
		
		configHeaderPanel.setLayout(new BorderLayout());
		createHeader(ResourceManager.getInstance().get("config.PreferencesTitle.title"));	 //$NON-NLS-1$
		configOptionsPanel.setLayout(new GridBagLayout());	
		iniButtons();
		iniOptionPanel(convertHandleToTreePath(starthandle));	
		
		//treeexpand works only if last node isnt leaf, workaround, expand to parent
		if (tree.getExpandedDescendants(convertHandleToTreePath(starthandle)) == null){
			tree.expandPath(convertHandleToTreePath(config.getParentHandle(starthandle)));
		}
		
        //Selection Listener
        tree.getSelectionModel().addTreeSelectionListener(
        		  new TreeSelectionListener(){
        		    @Override public void valueChanged( TreeSelectionEvent e ){
        		    
        		      TreePath path = e.getNewLeadSelectionPath();
        		      if (path != null && path.getPathCount() > 1){
        		    	  iniOptionPanel(path);
        		      }
        		    }
        		  }
        		);
        
        buildDialog();
        
        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        pack();
        this.setMinimumSize(new Dimension(750, 600));
        config.addListener(new CfgListener(starthandle));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing ( WindowEvent e) {
            	dialogExitAction();
              }
            } );
        this.setLocationRelativeTo(null);
	}
	
	
	// String Constructor
	public ConfigDialog(ConfigRegistry config, String startpath) {
		super();
		
		this.config = config;
		
        this.setTitle(ResourceManager.getInstance().get("config.PreferencesTitle.title"));  //$NON-NLS-1$
        
		actions = createActions();        
		tree = createTree(config);	
		spOpt.getVerticalScrollBar().setUnitIncrement(16);
		masterhandle = config.getHandle(startpath);
		
		configHeaderPanel.setLayout(new BorderLayout());
		createHeader(ResourceManager.getInstance().get("config.PreferencesTitle.title"));	 //$NON-NLS-1$
		configOptionsPanel.setLayout(new GridBagLayout());				
		
		Handle handle = config.getHandle(startpath);
		iniButtons();
		iniOptionPanel(convertHandleToTreePath(handle));
		
		
		//treeexpand works only if last node isnt leaf, workaround, expand to parent
		if (tree.getExpandedDescendants(convertHandleToTreePath(handle)) == null){
			tree.expandPath(convertHandleToTreePath(config.getParentHandle(handle)));
		}

		
        //Selection Listener
        tree.getSelectionModel().addTreeSelectionListener(
        		  new TreeSelectionListener(){
        		    @Override public void valueChanged( TreeSelectionEvent e ){
        		      TreePath path = e.getNewLeadSelectionPath();
        		      if (path != null && path.getPathCount() > 1){
        		    	  iniOptionPanel(path);
        		      }
        		    }
        		  }
        		);
        
        buildDialog();
        //System.out.println(changesMap);

        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        pack();
        this.setMinimumSize(new Dimension(750, 600));
        config.addListener(new CfgListener(handle));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing ( WindowEvent e) {
            	dialogExitAction();
              }
            } );
        this.setLocationRelativeTo(null);
	}
	
	
	// Default Constructor, select first group
	public ConfigDialog(ConfigRegistry config) {
		super();
		
		this.config = config;
		
        this.setTitle(ResourceManager.getInstance().get("config.PreferencesTitle.title"));  //$NON-NLS-1$
        
		actions = createActions();        
		tree = createTree(config);	
		spOpt.getVerticalScrollBar().setUnitIncrement(16);
		masterhandle = treeMap.entrySet().iterator().next().getKey();
		
		configHeaderPanel.setLayout(new BorderLayout());
		createHeader(ResourceManager.getInstance().get("config.PreferencesTitle.title"));	 //$NON-NLS-1$
		configOptionsPanel.setLayout(new GridBagLayout());	

		Handle handle = treeMap.entrySet().iterator().next().getKey();
		iniButtons();
		iniOptionPanel(convertHandleToTreePath(handle));

        //Selection Listener
        tree.getSelectionModel().addTreeSelectionListener(
        		  new TreeSelectionListener(){
        		    @Override public void valueChanged( TreeSelectionEvent e ){
        		      TreePath path = e.getNewLeadSelectionPath();
        		      if (path != null && path.getPathCount() > 1){
        		    	  //rebuildOptionPanel(path);
        		    	  iniOptionPanel(path);
        		      }

        		    }
        		  }
        		);
        
        buildDialog();

        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        pack();
        this.setMinimumSize(new Dimension(750, 600));    
		config.addListener(new CfgListener(handle));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing ( WindowEvent e) {
            	dialogExitAction();
              }
            } );
        this.setLocationRelativeTo(null);
	}

	//DialogExitAction
	public void dialogExitAction()  {	
	
	
		if (changesMap.isEmpty()){
			ConfigDialog.this.setVisible(false);
			ConfigDialog.this.dispose();
			config.removeListener(new CfgListener(masterhandle));
		} else {
			int exit = showYN("config.exitWarning.title", //$NON-NLS-1$
					"config.exitWarning.message"); //$NON-NLS-1$
			if (exit == 0){
				ConfigDialog.this.setVisible(false);
				ConfigDialog.this.dispose();
				changesMap.clear();
			}		
		}	
	}


	/**
	 * Creates Header for Configuration menu. Header always display the current selected
	 * Treegroup
	 * @param headerString
	 */
	private void createHeader(String headerString) {
		configHeaderPanel.removeAll();
	    JLabel header = new JLabel(" " + headerString); //$NON-NLS-1$
	    header.setFont(new Font("Arial", Font.BOLD, 18)); //$NON-NLS-1$
		configHeaderPanel.add(header, BorderLayout.NORTH);
		//filler between header and components
		configHeaderPanel.add(new JLabel(" "), BorderLayout.CENTER); //$NON-NLS-1$
		configHeaderPanel.revalidate();
		configHeaderPanel.repaint();
	}



	/**
	 * initialize option panel, input treepath
	 * @param firstpath
	 */
	private void iniOptionPanel(TreePath firstpath) {
	      tree.expandPath(firstpath);
	      rebuildOptionPanel(firstpath);
	}
	
	//check if item is virtual group? when virtual go back until correct path found
	private Handle findParentHandle (Handle handle){
		if (convertHandleToTreePath(handle) == null){
			Handle parent = config.getParentHandle(handle);
			return findParentHandle(parent);
		}
		return handle;
	}
	
	
	private void restoreDefaultValues(List<Handle> defaultList) {
		
		Handle parent = null;
		for (Iterator<Handle> i = defaultList.iterator(); i.hasNext();) {
			Handle h = (Handle) i.next();
			if (!(config.getDefaultValue(h) == null)) {
				config.setDefaultValue(h, config.getDefaultValue(h));
				parent = findParentHandle(config.getParentHandle(h));
				//System.out.println("Has default " + config.getDefaultValue(h));
			}
		}


		final Handle parentHandle = parent;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// refresh view. parenthandle needed, tabbedentries got no entry in tree
				if (!((config.getProperty(parentHandle, DISPLAY_MODE)) == null)
						&& config.getProperty(parentHandle, DISPLAY_MODE).equals(MODE_TABBED)) {			
					iniOptionPanel(convertHandleToTreePath(config
							.getParentHandle(parentHandle)));
				} else {
					iniOptionPanel(convertHandleToTreePath(parentHandle));
				}				
			}
		});


	}
	
	
	private void setNewValues(Map<Handle, Object> cmap) {
		config.setValues(cmap);
		changesMap.clear();
		configBottomPanel.getComponent(0).setEnabled(false);		
	}
	
	
	
	// Returns a TreePath containing the specified node.
	public TreePath getPath(TreeNode rn) {
	    List<TreeNode> list = new ArrayList<TreeNode>();

	    // Add all nodes to list
	    while (rn != null) {
	        list.add(rn);
	        rn = rn.getParent();
	    }
	    Collections.reverse(list);

	    // Convert array of nodes to TreePath
	    return new TreePath(list.toArray());
	}
	

	/**
	 * Input Handle, Return Treepath
	 * @param key
	 * @return
	 */
	private TreePath convertHandleToTreePath(Handle key){
		return treeMap.get(key);		
	}

	
	
	/**
	 * initialize Tree. Input configuration File
	 * @param config
	 * @return
	 */
	private JTree createTree(final ConfigRegistry config) {
		Handle roothandler = config.ROOT_HANDLE;       
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(
        		config.getName(roothandler));
        
        for ( int nodeCnt = 0; nodeCnt < config.getItemCount(roothandler); nodeCnt++ ){
        	Handle childNodes = config.getChildHandle(roothandler, nodeCnt);
        	//System.out.println("Nodefield: " + nodeCnt);        	
        	
        	if (config.isGroup(childNodes) && !(config.isVirtual(childNodes))
        			&& !(config.isHidden(childNodes))) {      		

	        	DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(
	        			config.getName(childNodes));
	        			//localizationName(childNodes));

	        	root.add(dmtn); 
	        	
	        	//create Hashmap handle<->TreePath
	        	TreeNode tn = root.getChildAt(nodeCnt);
	        	treeMap.put(childNodes, getPath(tn)); 
	        	walkTree (dmtn, childNodes);
	            convertHandleToTreePath(childNodes);
        	}
        }
        
        
        JTree tree = new JTree(root);  
        tree.setBorder(new EmptyBorder(5, 5, 5, 5));
        tree.setLargeModel(true);
        //stuff to get eclipse config look and feel
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
        	
			private static final long serialVersionUID = 7567894886782628787L;
			
			{
                //setLeafIcon(GridBagUtil.getIcon("info.gif"));				
                setLeafIcon(null);
                setOpenIcon(null);
                setClosedIcon(null);                
            }
			
            public Component getTreeCellRendererComponent(JTree tree, Object value,
            		boolean sel, boolean expanded, boolean leaf,
            		int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                TreePath path = tree.getPathForRow(row);
                
                if (path != null) {
                    String loca = getQueryFromTreePath(path);
                	Handle h = config.getHandle(loca); 
                    //System.out.println(config.getHandle(loca)); 
                    if (h != null){
                    	this.setText(localizationName(h));
                    }
                }                
                return this;
            }
        };
        
        BasicTreeUI ui = (BasicTreeUI) tree.getUI();
        ui.setCollapsedIcon(IconRegistry.getGlobalRegistry().getIcon("node_closed.gif")); //$NON-NLS-1$
        ui.setExpandedIcon(IconRegistry.getGlobalRegistry().getIcon("node_open.gif")); //$NON-NLS-1$
 
        tree.setCellRenderer(renderer);
        
		return tree;
	}		

	
	
	//create left jtree menu: adding all groups but virtualgroups
	protected void walkTree (DefaultMutableTreeNode dmtn, Handle childNodes){
		
        for ( int leafCnt = 0; leafCnt < config.getItemCount(childNodes); leafCnt++ ){
        	
        	Handle leafhandle = config.getChildHandle(childNodes, leafCnt);
        	
        	//Dont Display Tabbed Groups
    		if (!((config.getProperty(leafhandle, DISPLAY_MODE))==null)
	    			&&	config.getProperty(leafhandle, DISPLAY_MODE)
	    							.equals(MODE_TABBED)){
    			//System.out.println("Tabbed");
    			//more to do?
        		
    		} else {        	        	
	        	if (config.isGroup(leafhandle) 
	        			&& !(config.isVirtual(leafhandle))
	        			&& !(config.isHidden(leafhandle))
	        		){
	        		DefaultMutableTreeNode next = new DefaultMutableTreeNode(
	        				config.getName(leafhandle));
	        				//localizationName(leafhandle)); 
	
	    			dmtn.add(next); 
	        		walkTree(next , config.getChildHandle(childNodes, leafCnt));
	        		
	        		//create Hashmap handle<->TreePath
		        	TreeNode tn = dmtn.getLastChild();
		        	treeMap.put(leafhandle, getPath(tn)); 
	        	}
    		}
        }
	}
	
	
	protected void getComponentPaintType(Handle leafhandle,
			GridBagConstraints gbc, State state) {

		switch (config.getType(leafhandle)) {
		case INTEGER:
			// System.out.println(config.getType(leafhandle));
			if (!(config.getProperty(leafhandle, MIN_VALUE) == null)
					&& !(config.getProperty(leafhandle, MAX_VALUE) == null)) {
				int min = (Integer) config.getProperty(leafhandle, MIN_VALUE);
				int max = (Integer) config.getProperty(leafhandle, MAX_VALUE);
				int diff = Math.abs(max - min);

				// choose view related to diff
				if (diff <= 20) {
					addIntSliderSpinnerField(leafhandle, gbc, state);
				} else {
					// no startval? > default "0"
					addIntSpinnerField(leafhandle, gbc, state);
				}
			} else {
				// no startval? > default "0"
				addIntSpinnerField(leafhandle, gbc, state);
			}
			break;

		case FLOAT:
			// System.out.println(config.getType(leafhandle));
			addFloatSpinnerField(leafhandle, gbc, state);
			break;

		case DOUBLE:
			// System.out.println(config.getType(leafhandle));
			addDoubleSpinnerField(leafhandle, gbc, state);
			break;

		case LONG:
			// System.out.println(config.getType(leafhandle));
			addLongSpinnerField(leafhandle, gbc, state);
			break;

		case BOOLEAN:
			// System.out.println(config.getType(leafhandle));
			addBooleanField(leafhandle, gbc, state);
			break;

		case STRING:
			// System.out.println(config.getType(leafhandle));
			addTextField(leafhandle, gbc, state);
			break;

		case CUSTOM:
			// System.out.println(config.getType(leafhandle));
			addCustomField(leafhandle, gbc, state);
			break;

		case OPTIONS:
			// System.out.println(config.getType(leafhandle));
			List<?> list = (List<?>) (config.getProperty(leafhandle, OPTIONS));
			if (list.size() <= 3
					&& config.getProperty(leafhandle, RENDERER) == null) {
				addMultiRadioField(leafhandle, gbc, state);
			} else {
				addComboBoxField(leafhandle, gbc, state);
			}
			break;

		case COLOR:
			// System.out.println(config.getType(leafhandle));
			addColorField(leafhandle, gbc, state);
			break;

		case MAP:
			// System.out.println(config.getType(leafhandle));
			addMapField(leafhandle, gbc, state);
			break;

		case LIST:
			// System.out.println(config.getType(leafhandle));
			// addTableListField(leafhandle,gbc);
			addListField(leafhandle, gbc, state);
			break;

		case FILE:
			// System.out.println(config.getType(leafhandle));
			addPathField(leafhandle, gbc, state);
			break;
     }
	}
	
	
	//path to string
	protected String getLocalizationFromHandle(Handle handle, String loca) {		
		if (config.getParentHandle(handle) != config.ROOT_HANDLE) {
			Handle parent = config.getParentHandle(handle);
			loca = localizationName(parent) + " " +  loca;			 //$NON-NLS-1$
			//System.out.println(loca);			
			return getLocalizationFromHandle(parent, loca);
		}
		return loca;
	}
	
	
	//path to string
	protected String getQueryFromTreePath (TreePath path){
	      String querypath = ""; //$NON-NLS-1$
	      //System.out.println(path);
	      for (int i = 0; i < path.getPathCount(); i++){
	    	  if (i == 0){ 
	    		  querypath = path.getPathComponent(i).toString();}
	    	  else {
	    		  querypath = querypath + "." + path.getPathComponent(i); //$NON-NLS-1$
	    	  }
	      }	      
	      return querypath;
	};
	
	
	//check is item has changed but unsaved value
	protected Object getLatestItem (Handle handle){
		if (changesMap.containsKey(handle)){
			return changesMap.get(handle);
		}
		return null;
	
	}
	
	
	// switch for different entry types
	protected EntryHandler getEntryHandler (Handle handle){
		EntryHandler eh = (EntryHandler) config.getProperty(handle, HANDLER);
		if (eh != null)	
			return eh;
		EntryType et = (EntryType) config.getProperty(handle, ITEM_TYPE);
		if ( et == null)
			config.getType(handle);
		switch (et) {
		case STRING:
			return ConfigUtils.sharedStringEntryHandler;
		case COLOR:
			return ConfigUtils.sharedColorEntryHandler;
		case INTEGER:
			return ConfigUtils.sharedIntegerEntryHandler;
		case DOUBLE:
			return ConfigUtils.sharedDoubleEntryHandler;

		default:
			return ConfigUtils.dummyHandler;
		}	
	}
	
	
	// switch for different entry types
	protected MapHandler getMapHandler (Handle handle){
		MapHandler mh = (MapHandler) config.getProperty(handle, HANDLER);
		if (mh != null)	
			return mh;
		EntryType et = (EntryType) config.getProperty(handle, ITEM_TYPE);
		if ( et == null)
			config.getType(handle);
		switch (et) {
		case STRING:
			return ConfigUtils.sharedStringMapHandler;
		case COLOR:
			return ConfigUtils.sharedColorMapHandler;
		case INTEGER:
			return ConfigUtils.sharedIntegerMapHandler;
		case DOUBLE:
			return ConfigUtils.sharedDoubleMapHandler;

		default:
			return ConfigUtils.dummyHandler;
		}	
	}
	

	class tabComponents extends JPanel	{ 	
		  /**
		 * 
		 */
		private static final long serialVersionUID = 7825871000376409564L;

		protected tabComponents() {
			  super();	
			  this.setLayout(new BorderLayout());
		  }
	}


	protected JTabbedPane tabbedPanel(Handle handle, State state) {
		JTabbedPane tabbedPane = new JTabbedPane();

        for ( int leafCnt = 0; leafCnt < config.getItemCount(handle); leafCnt++ ){ 
        	Handle leaf = config.getChildHandle(handle, leafCnt);
        	
        	//only add groups with tabbed modifier
    		if (!((config.getProperty(leaf, DISPLAY_MODE))==null)
	    			&&	config.getProperty(leaf, DISPLAY_MODE)
	    							.equals(MODE_TABBED)){
        	
    		tabbedPane.addChangeListener(new tabChange(leaf));   		
    		
    		
				if (config.isGroup(leaf) && !(config.isHidden(leaf))) {
					
					// groupdesc
					if (!((config.getProperty(handle, DESCRIPTION_KEY)) == null)) {
						JLabel desclabel = new JLabel(localizationDesc(handle));
						GridBagConstraints gbc = new GridBagConstraints();
						gbc = GridBagUtil.makeGbc(0, state.getCurrentLine(),
								1, 1, 0);
						state.getStatePanel().add(desclabel, gbc);
						state.currentLine++;
					}
					tabbedPane.addTab(localizationName(leaf), null,
							new tabComponents(), null);

				}
			}
		}
		return tabbedPane;	
	}
	
	protected void rebuildTabPanel(Handle handle) {   		
		GridBagConstraints gbc = new GridBagConstraints();
		
		State state = new State();
		state.setStatePanel(configOptionsPanel);

		//gbc = GridBagUtil.makeGbcHN(0, 1, GridBagConstraints.REMAINDER, 1);
		
		int fieldCount = 0;
		int Indent = 0;
	      
	    //clear all before adding new components
	    configOptionsPanel.removeAll();
	    defaultList.clear();	     

	    if (config.isGroup(handle) && !(config.isHidden(handle))){ 
	       for ( int leafCnt = 0; leafCnt < config.getItemCount(handle); leafCnt++ ){            	
	          	Handle leafhandle = config.getChildHandle(handle, leafCnt);
	          	gbc = GridBagUtil.makeGbc(0, fieldCount, 1, 1,0);

	           	//only print entry items which are not hidden	            	
	           	if (config.isEntry(leafhandle) && !config.isHidden(leafhandle)){	           		
	           		
	           		//create list of values for easy reset to defaults
	           		defaultList.add(leafhandle);
	           		
	           		//Indent?
	           		if (!((config.getProperty(handle, INDENT))==null) &&
	           				config.getProperty(leafhandle, INDENT)
	           				.equals(true) && fieldCount >= 1){
	            			Indent = 1;
	           		}
	           		//####
	           		gbc = GridBagUtil.makeGbc(Indent, fieldCount, 1, 1, 0);
	           		getComponentPaintType(leafhandle,gbc,state); 
	            	fieldCount++;	            	
	           	}
	           	
	           	//virtual group stuff	            	
	           	if (config.isVirtual(leafhandle) && !config.isHidden(leafhandle)){
	           		gbc = GridBagUtil.makeGbc(0, state.getCurrentLine(), 1, 1, 0);		           		
	           		
	           		//create list of values for easy reset to default
	           		defaultList.add(leafhandle);	           		           		
	           		
	           		//Indent?
	           		if (!((config.getProperty(handle, INDENT))==null) &&
	           				config.getProperty(leafhandle, INDENT)
	           				.equals(true) && fieldCount >= 1){
	            			Indent = 1;
	           		}	
	           		
	           		feedComponent(leafhandle, state);
	           		
	           	}	           	
	       }
   	    }	  
	    
		JLabel filler = new JLabel();
		gbc = GridBagUtil.makeGbc(
		    		GridBagConstraints.REMAINDER,
		    		GridBagConstraints.REMAINDER,
		    		100,
		    		100);
		filler.setPreferredSize(new Dimension(400,500));

		configOptionsPanel.add(filler,gbc);
	    configOptionsPanel.revalidate();
	    configOptionsPanel.repaint();	 
	}
	
	
	protected void rebuildOptionPanel(TreePath path) {
		
		State state = new State();
		state.setStatePanel(configOptionsPanel);
			
		GridBagConstraints gbc = new GridBagConstraints();

		gbc = GridBagUtil.makeGbc(0, 1, GridBagConstraints.REMAINDER, 1,0);
			      
		//prepare treepath for handlequery		
	    String querypath = getQueryFromTreePath(path);
	    //System.out.println(querypath);
	    Handle handle = config.getHandle(querypath);
	    
	    //clear all before adding new components
	    configOptionsPanel.removeAll();
	    defaultList.clear();	     

	    String loca = localizationName(handle);
	    loca = getLocalizationFromHandle(handle, loca);
	    //System.out.println(loca);
	    createHeader(loca);    
	    
	      
		int fieldCount = 2;
		int Indent = 0;
		
		//group DESCRIPTION_KEY if property set 
		if (!((config.getProperty(handle, DESCRIPTION_KEY))==null)){
			String desc = config.getProperty
					(handle, DESCRIPTION_KEY).toString();
			JLabel desclabel = new JLabel(desc);
			gbc = GridBagUtil.makeGbc(Indent, state.currentLine, 1, 1, 0);
			desclabel.setFont(new Font("Arial", Font.ITALIC, 12)); //$NON-NLS-1$
			configOptionsPanel.add(desclabel,gbc);
			state.currentLine++;
		}	
	    
	    if (config.isGroup(handle) && !(config.isHidden(handle))){ 
	       for ( int leafCnt = 0; leafCnt < config.getItemCount(handle); leafCnt++ ){            	
	          	Handle leafhandle = config.getChildHandle(handle, leafCnt);
	          	gbc = GridBagUtil.makeGbc(0, state.currentLine, 1, 1, 0);
	          	
	          	
	          	//tabbedstuff
	    		if (!((config.getProperty(leafhandle, DISPLAY_MODE))==null)
	    			&&	config.getProperty(leafhandle, DISPLAY_MODE)
	    							.equals(MODE_TABBED)){	
	    			rebuildTabPanel(handle);
	    			spOpt.setViewportView(tabbedPanel(handle, state));	 
	    			
	    		}  		
	    		else {
	    			spOpt.setViewportView(configOptionsPanel);

	           	//only print entry items which are not hidden	            	
	           	if (config.isEntry(leafhandle) && !config.isHidden(leafhandle)){	           		
	           		
	           		//create list of values for easy reset to defaults
	           		defaultList.add(leafhandle);
	           		
	           		//Indent?
	           		if (!((config.getProperty(handle, INDENT))==null) &&
	           				config.getProperty(leafhandle, INDENT)
	           				.equals(true) && state.currentLine >= 3){
	            			Indent = 1;
	           		}	           		
	           		getComponentPaintType(leafhandle,gbc,state); 
	            	state.currentLine++;
	           	}
	           	
	           	//virtual group stuff	            	
	           	if (config.isVirtual(leafhandle) && !config.isHidden(leafhandle)){
	           		gbc = GridBagUtil.makeGbc(0, state.currentLine, 1, 1, 0);	           		
	           		
	           		//create list of values for easy reset to default
	           		defaultList.add(leafhandle);	           		           		
	           		
	           		//Indent?
	           		if (!((config.getProperty(handle, INDENT))==null) &&
	           				config.getProperty(leafhandle, INDENT)
	           				.equals(true) && fieldCount >= 1){
	            			Indent = 1;
	           		}	           			           		
	           		feedComponent(leafhandle, state);

	           	}	 
	           	
	       }}

   	    }	    
	    

	   
	   JLabel filler = new JLabel();
	    gbc = GridBagUtil.makeGbc(
	    		GridBagConstraints.REMAINDER,
	    		GridBagConstraints.REMAINDER,
	    		100,
	    		100);
	    filler.setPreferredSize(new Dimension(400,500));
	    
	    configOptionsPanel.add(filler,gbc);
	    configOptionsPanel.revalidate();
	    configOptionsPanel.repaint();
	}
	



	void feedComponent(Handle handle, State state) {
	  GridBagConstraints gbc = new GridBagConstraints();
	  gbc = GridBagUtil.makeGbc(0, state.getCurrentLine(), 1, 1, 0);
	
	  int Indent = 0;
	
	  if(config.isVirtual(handle)) {
		JPanel virtual = new JPanel();
		virtual.setLayout(new GridBagLayout());
		
	    State newState = new State(); // with new JPanel and line=0	
	    newState.panel = virtual;
	    newState.setCurrentLine(0);
	    
	    for ( int vCnt = 0; vCnt < config.getItemCount(handle); vCnt++ ){
			Handle vhandle = config.getChildHandle(handle, vCnt);
	    	feedComponent(vhandle, newState);
	    	//System.out.println("items" + vCnt);
	    }
	    
	    // place in at correct location
	    newState.panel.setBorder(BorderFactory.createTitledBorder(
	    		localizationName(handle)));
	    		    
		//desc
		if (!((config.getProperty(handle, DESCRIPTION_KEY))==null)){
			JLabel desclabel = new JLabel(localizationDesc(handle));
			gbc = GridBagUtil.makeGbc(Indent, state.getCurrentLine(), 1, 1, 0);
			state.getStatePanel().add(desclabel,gbc);
			state.currentLine++;
		}
	    
		gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
	    state.panel.add(newState.panel, gbc);
	    
	    state.currentLine++;
	    
	   } else  {
	    
	   	if (config.isEntry(handle) && !config.isHidden(handle)){	           		
	   		//create list of values for easy reset to defaults
	   		defaultList.add(handle); 
	   		getComponentPaintType(handle,gbc,state); 
	   		state.currentLine++;
	   	}
	  }  
	}

	class State {
		  JPanel panel;
		  int currentLine = 2;
		  boolean indentLevel;
		  
		  public JPanel getStatePanel (){
			  return this.panel;
		  }
		  public void setStatePanel (JPanel panel){
			  this.panel = panel;
		  }
		  
		  public int getCurrentLine (){
			  return this.currentLine;
		  }
		  public void setCurrentLine (int line){
			  this.currentLine = line;
		  }
	}

	private static final String CONFIG_SAVE_ACTION = "config.save"; //$NON-NLS-1$
	private static final String CONFIG_EXIT_ACTION = "config.exit"; //$NON-NLS-1$
	private static final String CONFIG_RESTOREDEFAULTS_ACTION = "config.restoreDefaults"; //$NON-NLS-1$

	protected Actions createActions() {
		Actions actions = new Actions();
		Action action;
		
		ResourceDomain resourceDomain = ResourceManager.getInstance().getGlobalDomain();

		//Save Config
		action = new ExitSaveAction(false);
		resourceDomain.prepareAction(action,
				"config.saveConfig.name", //$NON-NLS-1$
				"config.saveConfig.desc"); //$NON-NLS-1$
		resourceDomain.addAction(action);
		actions.addAction(CONFIG_SAVE_ACTION, action);
	
		//Exit Config
		action = new ExitSaveAction(true);
		resourceDomain.prepareAction(action,
				"config.exitConfig.name", //$NON-NLS-1$
				"config.exitConfig.desc"); //$NON-NLS-1$
		resourceDomain.addAction(action);
		actions.addAction(CONFIG_EXIT_ACTION, action);
		
		//Default Config
		action = new RestoreDefaultsAction();
		resourceDomain.prepareAction(action,
				"config.restoreDefaultstAction.name", //$NON-NLS-1$
				"config.restoreDefaultstAction.desc"); //$NON-NLS-1$
		resourceDomain.addAction(action);
		actions.addAction(CONFIG_RESTOREDEFAULTS_ACTION, action);
	
		return actions;
	}
	

	// Create control Buttons
	protected void iniButtons() {		
		JButton savebutton = new JButton();
		savebutton.setAction(actions.getAction(CONFIG_SAVE_ACTION));
		savebutton.setEnabled(false);
		configBottomPanel.add(savebutton, BorderLayout.CENTER);
		JButton restorebutton = new JButton();
		restorebutton.setAction(actions.getAction(CONFIG_RESTOREDEFAULTS_ACTION));
		configBottomPanel.add(restorebutton, BorderLayout.CENTER);
		JButton exitbutton = new JButton();
		exitbutton.setAction(actions.getAction(CONFIG_EXIT_ACTION));
		configBottomPanel.add(exitbutton, BorderLayout.CENTER);		
	}
	
	



	protected void buildDialog() {		

		//TODO get nice size for tree and options
        JScrollPane spTree = new JScrollPane(tree);
        spTree.getVerticalScrollBar().setUnitIncrement(16);
        UIUtil.resizeComponent(spTree, 150, 150, 1100, 300, 500, 1100); 
        
        // Panel with Options
        JPanel rightView = new JPanel();
        rightView.setLayout(new BorderLayout());
        
        rightView.add(configHeaderPanel, BorderLayout.NORTH);
        spOpt.setViewportView(configOptionsPanel);
        spOpt.setBorder(null);
        //UIUtil.resizeComponent(spOpt, 400, 500, 1100, 300, 500, 1100);
        rightView.add(spOpt, BorderLayout.CENTER);        
        
        rightView.add(configBottomPanel, BorderLayout.SOUTH);        
        
		JSplitPane spMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				spTree, rightView);
		spMain.setMinimumSize(new Dimension(200,200));
        
        this.add(spMain); 
     }

	
	protected String localizationName (Handle handle){
		String name = (String) config.getProperty(handle, NAME_KEY);
		
		if (name == null) 
			name = "config." + config.getName(handle); //$NON-NLS-1$
		try {
		return ResourceManager.getInstance().get(name);
		}
		catch (MissingResourceException e){
			return config.getName(handle);
		}
	}
	
	//get localization for description
	protected String localizationDesc (Handle handle){
		String desc = (String) config.getProperty(handle, DESCRIPTION_KEY);
		
		if (desc == null) 
			desc = "config.desc." + config.getName(handle); //$NON-NLS-1$
		try {
		String result = ResourceManager.getInstance().get(desc);
		if(desc.equals(result)) {
			result = null;
		}
		return result;
		}
		catch (MissingResourceException e){
			return null;
		}
	}
	
	// localization for notes
	protected String localizationNote (Handle handle){
		
				String note = (String) config.getProperty(handle, NOTE_KEY);
		
		if (note == null) 
			note = "config.note." + config.getName(handle); //$NON-NLS-1$
		try {

			String result = ResourceManager.getInstance().get(note);
			if(note.equals(result)) {
				result = null;
			}
			return result;
		}
		catch (MissingResourceException e){
			return null;
		}
	}
	
	//localization for options
	protected String localizationOption (Handle handle){
		String name = (String) config.getProperty(handle, NAME_KEY);
		
		if (name == null) 
			name = "config.options" + config.getName(handle); //$NON-NLS-1$
		try {
		return ResourceManager.getInstance().get(name);
		}
		catch (MissingResourceException e){
			return config.getName(handle);
		}
	}
	
	protected String[] localizationOptions(Handle handle, List<?> options) {
		String[] optionsArray = new String[options.size()];

		@SuppressWarnings("unchecked")
		List<String> optionsKeys = (List<String>) config.getProperty(handle,
				OPTIONS_KEYS);

		for (int i = 0; i < optionsArray.length; i++) {
			String loca = optionsKeys == null ? null : optionsKeys.get(i);
			if (loca == null) {
				loca = options.get(i) instanceof String ? (String) options
						.get(i) : String.valueOf(options.get(i));
				try {
					optionsArray[i] = ResourceManager.getInstance().get("config." + loca); //$NON-NLS-1$
				} catch (MissingResourceException e) {
					optionsArray[i] = loca;
				}	
				continue;
			}
			try {
				optionsArray[i] = ResourceManager.getInstance().get(loca);
			} catch (MissingResourceException e) {
				optionsArray[i] = loca;
			}
		}
		
		return optionsArray;

	}
	
	
	
	
	protected void paintLabel (Handle handle, GridBagConstraints gbc,State state) {
		
		//gbc.anchor = GridBagConstraints.NORTHWEST;
		//gbc.weighty = 1;
		
		//gbc.gridx = 0;
		
		//label shouldn't resize
		gbc.weightx = 0;
		gbc.insets = new Insets(2,5,2,2);  
		
		JLabel jn = new JLabel(localizationName(handle));
		//jn.setPreferredSize((new Dimension(170, 15)));
		jn.setToolTipText(localizationDesc(handle));
		
		gbc.fill = GridBagConstraints.NONE;	

		//seperator needed?
		if (config.getProperty(handle, SEPARATED) != null
				&& config.getProperty(handle, 
						SEPARATED).toString() == "separated"){ //$NON-NLS-1$
			paintSeperatorLine(gbc);
		}
		state.getStatePanel().add(jn,gbc);

		gbc.gridx = GridBagConstraints.RELATIVE;
		//following components left should resize
		gbc.insets = new Insets(2,2,2,2);  
		gbc.weightx = 1;		
	}
	
	
	//paint note label
	protected void paintNote (Handle handle, GridBagConstraints gbc,State state) {
		//label shouldn't resize
		gbc.weightx = 0;
		gbc.insets = new Insets(2,5,2,2);  
		
		JLabel jn = new JLabel(localizationNote(handle));
		//jn.setPreferredSize((new Dimension(170, 15)));
		
		//System.out.println(localizationNote(handle));
		
		gbc.fill = GridBagConstraints.NONE;	

		//seperator needed?
		if (config.getProperty(handle, SEPARATED) != null
				&& config.getProperty(handle, 
						SEPARATED).toString() == "separated"){ //$NON-NLS-1$
			paintSeperatorLine(gbc);
		}
		state.getStatePanel().add(jn,gbc);

		gbc.gridx = GridBagConstraints.RELATIVE;
		//following components left should resize
		gbc.insets = new Insets(2,2,2,2);  
		gbc.weightx = 1;		
	}


	
	// puts line on option panel to seperate items, instead of use virtual groups
	protected void paintSeperatorLine(GridBagConstraints gbc){
		JSeparator seperator = new JSeparator();
		gbc.gridwidth = 5;
		gbc.insets = new Insets(3,3,3,3);  
		gbc.fill = GridBagConstraints.HORIZONTAL;
	    configOptionsPanel.add(seperator,gbc);	
	    gbc.gridwidth = 1;
	}
	

	// puts line on option panel to seperate items, instead of use virtual
	// groups
	protected Component paintFiller(GridBagConstraints gbc) {		
		JLabel filler = new JLabel();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		gbc = GridBagUtil.makeGbc(GridBagConstraints.REMAINDER,
				GridBagConstraints.REMAINDER, 100, 100);
		//filler.setPreferredSize(new Dimension(100, 100));
		return filler;
	}
	
	
	/* *********************************************
	 * 
	 * paint methods for each type
	 *  
	 * ********************************************/


	//boolean
	protected void addBooleanField(Handle handle, GridBagConstraints gbc, State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel(handle,gbc,state);		
		
		//JCheckBox checkBox = new JCheckBox(localizationName(handle));
		JCheckBox checkBox = new JCheckBox();
		if (!((config.getProperty(handle, DESCRIPTION_KEY))==null)){
			checkBox.setToolTipText(config.getProperty
					(handle, DESCRIPTION_KEY).toString());
		}
		
		if (config.getValue(handle) != null){
			if (getLatestItem(handle) != null){
				checkBox.setSelected((Boolean) getLatestItem(handle));
			} else {
				checkBox.setSelected(config.getValue(handle).equals(true));
			}
		    if (config.isLocked(handle))checkBox.setEnabled(false);		    
		} 				
		checkBox.addActionListener(new checkBoxChangedAction(handle));
	    //configOptionsPanel.add(checkBox,gbc);
	    state.getStatePanel().add(checkBox,gbc);
	    
	    paintNote(handle, gbc, state);
	    
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}
	
	//Color
	protected void addColorField(Handle handle, GridBagConstraints gbc, State state) {	
		
		//gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel (handle,gbc,state);
		
		
		if (config.getValue(handle) != null){
			JPanel colorPanel = new JPanel();
			if (getLatestItem(handle) != null){
				colorPanel.setBackground(new Color((Integer) getLatestItem(handle)));
			} else {
				colorPanel.setBackground(new Color((Integer) config.getValue(handle)));
			}
			colorPanel.setPreferredSize((new Dimension(45, 15)));
			colorPanel.setBorder(BorderFactory.createLineBorder(Color.black));
			JButton colorButton = new JButton(ResourceManager.getInstance().get("config.chooseColor.desc")); //$NON-NLS-1$
			colorButton.setToolTipText(ResourceManager.getInstance().get("config.chooseColor.desc")); //$NON-NLS-1$
			//colorButton.setPreferredSize((new Dimension(50, 25)));
			colorButton.addActionListener(new ColorChooseAction(handle, colorPanel));
			if (config.isLocked(handle))colorButton.setEnabled(false);	
			
			colorButton.add(colorPanel);
			//state.getStatePanel().add(colorPanel,gbc);
			state.getStatePanel().add(colorButton,gbc);
		}
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}

	
	//Path
	protected void addPathField(Handle handle, GridBagConstraints gbc,
			State state) {
		// gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel(handle, gbc, state);
		JLabel currentPathLabel = new JLabel();
		String currentPath = "/~"; //$NON-NLS-1$

		if (!((config.getValue(handle)) == null)) {
			if (getLatestItem(handle) != null) {
				currentPathLabel.setText(getLatestItem(handle).toString());
				currentPath = getLatestItem(handle).toString();
			} else {
				currentPathLabel.setText(config.getValue(handle).toString());
				currentPath = config.getValue(handle).toString();
			}
		}

		JButton pathButton = new JButton(
				ResourceManager.getInstance().get("config.choosePath.name")); //$NON-NLS-1$
		pathButton.setToolTipText(ResourceManager.getInstance()
				.get("config.choosePath.desc")); //$NON-NLS-1$
		pathButton.setPreferredSize((new Dimension(70, 20)));

		pathButton.addActionListener(new PathChooseAction(handle,
				currentPathLabel, currentPath));

		if (config.isLocked(handle))
			pathButton.setEnabled(false);
		state.getStatePanel().add(pathButton, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;
		state.getStatePanel().add(currentPathLabel, gbc);

		paintNote(handle, gbc, state);
		// jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}
	
	
	//Only display value no changes possible (used for all number types)
	protected void addNumberField(Handle handle, GridBagConstraints gbc, State state) {

		//gbc.gridx = GridBagConstraints.RELATIVE;	
		paintLabel (handle,gbc,state);
	
		if (!((config.getValue(handle))==null)){
			JFormattedTextField numField = new JFormattedTextField();
			numField.setValue(config.getValue(handle));			
			numField.setPreferredSize(new Dimension(150, 20));
			numField.addPropertyChangeListener(new numFieldChanged(handle, config.getValue(handle)));
						
			JButton enter = new JButton(ResourceManager.getInstance().get("enter")); //$NON-NLS-1$
			enter.addActionListener(new numFieldAction(handle, numField, config.getValue(handle)));
			
			JPanel tmp = new JPanel();
			tmp.setLayout(new GridBagLayout());
			GridBagConstraints gbctmp = new GridBagConstraints();
			gbctmp = GridBagUtil.makeGbc(0, 1, 1, 1, 0);
			tmp.add(numField,gbctmp); 
			gbctmp = GridBagUtil.makeGbc(1, 1, 1, 1, 0);
			tmp.add(enter,gbctmp); 
			
			
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;			
			
			state.getStatePanel().add(tmp,gbc);
		}	  
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	
	}
	// Double Spinner
	protected void addDoubleSpinnerField(Handle handle, GridBagConstraints gbc,State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel (handle,gbc,state);
		
		// initial , min , max , step
		
		double startval = 0;
		double min = Double.MIN_VALUE;
		double max = Double.MAX_VALUE;
		
		
		if (getLatestItem(handle) != null){
			startval = (Double) getLatestItem(handle);
		} else {
			if (!(config.getValue(handle)==null)){
				startval = (Double) config.getValue(handle);
			}
		}

		if (!(config.getProperty(handle, MIN_VALUE)==null)){
			min = (Double) config.getProperty(handle, MIN_VALUE);
		}
		if (!(config.getProperty(handle, MAX_VALUE)==null)){
			max = (Double) config.getProperty(handle, MAX_VALUE);
		}
		SpinnerModel model;

		
		if (!((config.getProperty(handle, PRECISION))==null)){
			double precision =  (Double) config.getProperty(handle, PRECISION);
			model = new SpinnerNumberModel(startval,min,max,precision);
		} else {
			model = new SpinnerNumberModel(startval,min,max,0.1);
		}
		JSpinner spinner = new JSpinner(model);
		spinner.addChangeListener(new spinnerChanged(handle));
		spinner.setPreferredSize(new Dimension(150, 20));
		if (config.isLocked(handle))spinner.setEnabled(false);

		//gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridwidth = 4;
		state.getStatePanel().add(spinner,gbc);	
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}
		
		
	// Float Spinner
	protected void addFloatSpinnerField(Handle handle, GridBagConstraints gbc, State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;	
		paintLabel (handle,gbc,state);		
		
		// initial , min , max , step
		float startval = 0;
		float min = Float.MIN_VALUE;
		float max = Float.MAX_VALUE;
		
		
		if (getLatestItem(handle) != null){
			startval = (Float) getLatestItem(handle);
		} else {
			if (!(config.getValue(handle)==null)){
				startval = (Float) config.getValue(handle);
			}
		}

		if (!(config.getProperty(handle, MIN_VALUE)==null)){
			min = (Float) config.getProperty(handle, MIN_VALUE);
		}
		if (!(config.getProperty(handle, MAX_VALUE)==null)){
			max = (Float) config.getProperty(handle, MAX_VALUE);
		}
		SpinnerModel model;
		
		if (!((config.getProperty(handle, PRECISION))==null)){
			float precision =  (Float) config.getProperty(handle, PRECISION);
			model = new SpinnerNumberModel(startval,min,max,precision);
		} else {
			model = new SpinnerNumberModel(startval,min,max,0.1);
		}
		JSpinner spinner = new JSpinner(model);
		spinner.addChangeListener(new spinnerChanged(handle));
		spinner.setPreferredSize(new Dimension(150, 20));
		if (config.isLocked(handle))spinner.setEnabled(false);		
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridwidth = 4;
		state.getStatePanel().add(spinner,gbc);	    
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}

	
	
	// Long Spinner
	protected void addLongSpinnerField(Handle handle, GridBagConstraints gbc, State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;	
		paintLabel (handle,gbc,state);		
		
		// initial , min , max , step
		long startval = 0;
		long min = Long.MIN_VALUE;
		long max = Long.MAX_VALUE;
		
		
		if (getLatestItem(handle) != null){
			startval = (Long) getLatestItem(handle);
		} else {
			if (!(config.getValue(handle)==null)){
				startval = (Long) config.getValue(handle);
			}
		}

		if (!(config.getProperty(handle, MIN_VALUE)==null)){
			min = (Long) config.getProperty(handle, MIN_VALUE);
		}
		if (!(config.getProperty(handle, MAX_VALUE)==null)){
			max = (Long) config.getProperty(handle, MAX_VALUE);
		}
		SpinnerModel model;
		
		if (!((config.getProperty(handle, PRECISION))==null)){
			long precision =  (Long) config.getProperty(handle, PRECISION);
			model = new SpinnerNumberModel(startval,min,max,precision);
		} else {
			model = new SpinnerNumberModel(startval,min,max,1);
		}
		JSpinner spinner = new JSpinner(model);
		spinner.addChangeListener(new spinnerChanged(handle));
		spinner.setPreferredSize(new Dimension(150, 20));
		if (config.isLocked(handle))spinner.setEnabled(false);		
		//gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridwidth = 4;
		state.getStatePanel().add(spinner,gbc);	    
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}
	
	
	// Int Spinner
	protected void addIntSpinnerField(Handle handle, GridBagConstraints gbc, State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel (handle,gbc,state);
		
		
		// initial , min , max , step
		int startval = 0;
		int min = Integer.MIN_VALUE;
		int max = Integer.MAX_VALUE;
		
		
		if (getLatestItem(handle) != null){
			startval = (Integer) getLatestItem(handle);
		} else {
			if (!(config.getValue(handle)==null)){
				startval = (Integer) config.getValue(handle);
			}
		}

		if (!(config.getProperty(handle, MIN_VALUE)==null)){
			min = (Integer) config.getProperty(handle, MIN_VALUE);
		}
		if (!(config.getProperty(handle, MAX_VALUE)==null)){
			max = (Integer) config.getProperty(handle, MAX_VALUE);
		}
		
		SpinnerModel model;
		
		if (!((config.getProperty(handle, PRECISION))==null)){
			int precision =  (Integer) config.getProperty(handle, PRECISION);
			model = new SpinnerNumberModel(startval,min,max,precision);
		} else {
			model = new SpinnerNumberModel(startval,min,max,1);
		}
		JSpinner spinner = new JSpinner(model);
		spinner.addChangeListener(new spinnerChanged(handle));
		spinner.setPreferredSize(new Dimension(150, 20));
		if (config.isLocked(handle))spinner.setEnabled(false);

		//gbc.fill = GridBagConstraints.HORIZONTAL;
		//gbc.gridwidth = 4;
		state.getStatePanel().add(spinner,gbc);			
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}	
	
	protected void addIntSliderField(Handle handle, GridBagConstraints gbc,State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;	
		paintLabel (handle,gbc,state);
		
		// initial , min , max , step
		
		int startval;
		
		if (getLatestItem(handle) != null){
			startval = (Integer) getLatestItem(handle);
		} else {
			startval = (Integer) config.getValue(handle);
		}

		int min = (Integer) config.getProperty(handle, MIN_VALUE);
		int max = (Integer) config.getProperty(handle, MAX_VALUE);

		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, min, max, startval);
		JLabel sliderval = new JLabel(String.valueOf(slider.getValue()));
		
		int precision = 1;
		
		//slider
		if (!((config.getProperty(handle, PRECISION))==null)){
			precision =  (Integer) config.getProperty(handle, PRECISION);
		}
		
		slider.setMajorTickSpacing(precision);
		slider.setMinorTickSpacing(precision);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.setToolTipText(String.valueOf(slider.getValue()));
		slider.addChangeListener(new sliderChanged(handle,sliderval));

		if (config.isLocked(handle))slider.setEnabled(false);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 4;
		state.getStatePanel().add(slider,gbc);	
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}	
	
	
	protected void addIntSliderSpinnerField(Handle handle, GridBagConstraints gbc, State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;	
		paintLabel (handle,gbc,state);
		
		// initial , min , max , step
		int startval;
		
		if (getLatestItem(handle) != null){
			startval = (Integer) getLatestItem(handle);
		} else {
			startval = (Integer) config.getValue(handle);
		}
		
		int min = (Integer) config.getProperty(handle, MIN_VALUE);
		int max = (Integer) config.getProperty(handle, MAX_VALUE);

		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, min, max, startval);
		//slider.setToolTipText(String.valueOf(slider.getValue()));
		slider.setPreferredSize(new Dimension(150, 30));
		
		//default prec
		int precision = 1;
		
		
		//Spinner
		SpinnerModel model;
		if (!((config.getProperty(handle, PRECISION))==null)){
			precision =  (Integer) config.getProperty(handle, PRECISION);
			model = new SpinnerNumberModel(startval,min,max,precision);
		} else {
			model = new SpinnerNumberModel(startval,min,max,1);
		}

		slider.setMajorTickSpacing(precision);
		slider.setMinorTickSpacing(precision);
		slider.setPaintTicks(true);
		//slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
	
		JSpinner spinner = new JSpinner(model);
		
		slider.addChangeListener(new spinnersliderChanged(handle,slider,spinner));	
		spinner.addChangeListener(new spinnersliderChanged(handle,slider,spinner));
		spinner.setPreferredSize(new Dimension(150, 20));
		
		JLabel minLabel = new JLabel(String.valueOf(min));
		minLabel.setPreferredSize(new Dimension(20,20));
		JLabel maxLabel = new JLabel(String.valueOf(max));
		minLabel.setPreferredSize(new Dimension(20,20));
				
		state.getStatePanel().add(spinner,gbc);
		
		gbc.gridx= 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		state.getStatePanel().add(minLabel,gbc);		
		
		gbc.gridx= 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		state.getStatePanel().add(slider,gbc);
		
		gbc.gridx= 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		state.getStatePanel().add(maxLabel,gbc);

		//gbc.gridx= 3;

		
		if (config.isLocked(handle))spinner.setEnabled(false);
		if (config.isLocked(handle))slider.setEnabled(false);
		
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	}

	
	
	// combo
	protected void addComboBoxField(Handle handle, GridBagConstraints gbc, State state) {

		//gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel (handle,gbc,state);
		
		List<?> list = null;	
		
		DefaultComboBoxModel<Object> cbm = new DefaultComboBoxModel<Object>();
			
			
		//retrieve elements and push them into comboboxmodel
		if (config.getProperty(handle, OPTIONS) != null){
			list = (List<?>) (config.getProperty(handle, OPTIONS));
		}		
		
		JComboBox<Object> cb = new JComboBox<Object>();
		cb.setPreferredSize(new Dimension(250, 20));
		
		if (list != null) {
			for (int i = 0; i < list.size(); i++){		
				cbm.addElement(list.get(i));				
			}	
			
			
			cb.setModel(cbm);			
			
			if (getLatestItem(handle) != null){
				cb.setSelectedItem(getLatestItem(handle));
			} else {
				cb.setSelectedItem(config.getValue(handle));
			}			
		
			cb.addActionListener(new comboBoxChangedAction(handle));
			
			
			//no custom renderer = use default combobox
			if (config.getProperty(handle, RENDERER) != null){	
				cb.setRenderer((ListCellRenderer) config.getProperty(handle,RENDERER));
			}
			
			
			
			if (config.getProperty(handle, ITEM_TYPE) != null 
					&& config.getProperty(handle, ITEM_TYPE).toString() == "COLOR"){	 //$NON-NLS-1$
				ListCellRenderer<Object> renderer = new ColorCellRenderer();
				cb.setRenderer(renderer);
			}
			
	
				
			if (config.isLocked(handle))cb.setEnabled(false);
			//gbc.fill = GridBagConstraints.HORIZONTAL;			
			gbc.gridwidth = 4;
			state.getStatePanel().add(cb,gbc);	
		}
				
		paintNote(handle, gbc, state);
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));	
	}
	
	//multiradio field
	protected void addMultiRadioField(Handle handle, GridBagConstraints gbc, State state) {
		
		//custom Label paint, otherwise label would get wrong vertical alignment
		gbc.weightx = 0;
		gbc.insets = new Insets(2,5,2,2);  
		
		JLabel jn = new JLabel(localizationName(handle));
		jn.setPreferredSize((new Dimension(170, 36)));
		jn.setToolTipText(localizationDesc(handle));
		
		gbc.fill = GridBagConstraints.NONE;	

		//seperator needed?
		if (config.getProperty(handle, SEPARATED) != null
				&& config.getProperty(handle, 
						SEPARATED).toString() == "separated"){ //$NON-NLS-1$
			paintSeperatorLine(gbc);
		}
		
		state.getStatePanel().add(jn,gbc);

		gbc.gridx = GridBagConstraints.RELATIVE;
		//following components left should resize
		gbc.insets = new Insets(2,2,2,2);  
		gbc.weightx = 1;
		

		//retrieve elements
		List<?> list = (List<?>) (config.getProperty(handle, OPTIONS));
		String[] labels = localizationOptions(handle, list);
		JPanel radfieldchoose = new JPanel();
				
		if (!(config.getProperty(handle, ORIENTATION)==null)) {
			if (config.getProperty(handle, ORIENTATION).equals(SwingConstants.VERTICAL)){
				GridLayout vertical = new GridLayout(1,0);		
				radfieldchoose.setLayout(vertical);
			}
			if (config.getProperty(handle, ORIENTATION).equals(SwingConstants.HORIZONTAL)){
				GridLayout horizontal = new GridLayout(1,0);		
				radfieldchoose.setLayout(horizontal);
			}
		}		

		
		ButtonGroup group = new ButtonGroup();
		
		for (int i = 0; i < list.size(); i++){
			JRadioButton radioButton = new JRadioButton(labels[i]);
						
			radfieldchoose.add(radioButton);
			group.add(radioButton);
			radioButton.addActionListener(new radioChangedAction(handle, group));
			radioButton.setActionCommand(String.valueOf(i));
			
			if (getLatestItem(handle) != null){
		        if (getLatestItem(handle).equals(list.get(i))){
		        	radioButton.setSelected(true);
		        }
			} else {
		        if (config.getValue(handle).equals(list.get(i))){
		        	radioButton.setSelected(true);
		        }
			}				
		}	
		
		
		if (config.isLocked(handle))radfieldchoose.setEnabled(false);

		//gbc.gridwidth = 4;
		//gbc.gridx = 0;  
		state.getStatePanel().add(radfieldchoose,gbc);		
		
		paintNote(handle, gbc, state);		
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));	
	}
	
	
	
	//custom
	protected void addCustomField(Handle handle, GridBagConstraints gbc, State state) {		
		
		//gbc.gridx = GridBagConstraints.RELATIVE;
		paintLabel(handle,gbc,state);
		
		EntryHandler eh = null;
		eh = getEntryHandler(handle);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;			
		
		state.getStatePanel().add(eh.getComponent(),gbc);
				
	}
	
	
	// Text
	protected void addTextField(Handle handle, GridBagConstraints gbc, State state) {
		
		//gbc.gridx = GridBagConstraints.RELATIVE;	
		paintLabel (handle,gbc,state);
		
	
		if (config.getValue(handle) != null){
	    	gbc.fill=GridBagConstraints.HORIZONTAL;
			
			//Textarea or Textfield?
			if (config.getProperty(handle, MULTILINE) != null){
				JTextArea taText = new JTextArea();				
				
				taText.setLineWrap(true);
				//taText.setWrapStyleWord(true);	
	
				if (config.getProperty(handle, MAX_LENGTH)!= null){
					int limit = (Integer) config.getProperty(handle, MAX_LENGTH);
					taText.setDocument(new JTextFieldLimit(limit));
				}
	
				if (getLatestItem(handle) != null){
					taText.setText(getLatestItem(handle).toString());
				} else {
					taText.setText(config.getValue(handle).toString());
				}
				
				taText.getDocument().addDocumentListener(new DocumentTextAreaListener(handle));
				taText.getDocument().putProperty("parent", taText); //$NON-NLS-1$
								
				UIUtil.createUndoSupport(taText, 20);
				UIUtil.addPopupMenu(taText, UIUtil.createDefaultTextMenu(
						taText, true));
				if (config.isLocked(handle))taText.setEnabled(false);
				
				JScrollPane spane = new JScrollPane(taText);
				spane.getVerticalScrollBar().setUnitIncrement(16);
				spane.setPreferredSize((new Dimension(80, 105)));
				gbc.gridwidth = 4;
				gbc.fill = GridBagConstraints.HORIZONTAL;
	        	state.getStatePanel().add(spane, gbc);				
			} 
			//Textfield
			else {
			JTextField tf = new JTextField();
	
	
			if (config.getProperty(handle, MAX_LENGTH)!= null){
				int limit = (Integer) config.getProperty(handle, MAX_LENGTH);
				tf.setDocument(new JTextFieldLimit(limit));
			}
	
			
			if (config.getProperty(handle, PATTERN)!= null){
				//ToDo
			}
			
			if (getLatestItem(handle) != null){
				tf.setText(getLatestItem(handle).toString());
			} else {
				tf.setText(config.getValue(handle).toString());
			}
			tf.getDocument().addDocumentListener(new DocumentTextFieldListener(handle));
			tf.getDocument().putProperty("parent", tf); //$NON-NLS-1$
			
			UIUtil.createUndoSupport(tf, 20);
			UIUtil.addPopupMenu(tf, UIUtil.createDefaultTextMenu(tf, true));
			if (config.isLocked(handle))tf.setEnabled(false);			
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.gridwidth = 4;
				state.getStatePanel().add(tf,gbc);
	
			}
			
			paintNote(handle, gbc, state);
		}
	
		
		//jp.setBorder(BorderFactory.createLineBorder(Color.gray));
	
	}


	//Map visualized by Table
	protected void addMapField (Handle handle, GridBagConstraints gbc, State state) {
		//System.out.println(config.getValue(handle));
		paintLabel (handle,gbc,state);
		
		Map<?,?> map = null;
		
		if (getLatestItem(handle) != null){
			map = (Map<?, ?>) getLatestItem(handle);
		} else {
			map = (Map<?, ?>) (config.getValue(handle));
		}
		
		if (map != null){
	
			final TableCellRenderer tcr;
			//no custom renderer = use default list
			if (config.getProperty(handle, RENDERER) != null){
				tcr = (TableCellRenderer) config.getProperty(handle,RENDERER);
			}		
			else if (config.getProperty(handle, ITEM_TYPE) != null 
					&& config.getProperty(handle, ITEM_TYPE) == EntryType.COLOR){	
				tcr = new ColorCellRenderer();
			} else {
				tcr = null;
			}
			
			
			DefaultTableModel dtm = toTableModel(map);
					    
		    JTable table = new JTable(dtm)
	        {
	        	public boolean isCellEditable(int row, int column){
	        		return false;
	        	}
	        	public TableCellRenderer getCellRenderer (int row, int column){
	        		if (column == 0 || tcr == null)
	        			return super.getCellRenderer(row, column);
	        		else
	        			return tcr;
	        	}
	        };
		    table.setShowGrid(false);
		    table.setDragEnabled(false);		    
		    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		    table.setRowHeight(24);		
		    
		    
		    		   
		    JScrollPane sp = new JScrollPane(table);
		    sp.setPreferredSize((new Dimension(80, 105)));		
		    sp.getVerticalScrollBar().setUnitIncrement(16);
		    
		    JPanel controll = tableControll(table,dtm,handle);
		    
		   // table.addPropertyChangeListener(new changeMapPropertyListener(handle, table, controll));
		    dtm.addTableModelListener(new tableModelListener(handle, controll));
		    
		    if (config.isLocked(handle))table.setEnabled(false);
			gbc.fill = GridBagConstraints.HORIZONTAL;
	
	
			gbc.gridwidth = 3;
			state.getStatePanel().add(sp,gbc);
			gbc.fill = GridBagConstraints.NONE;
			state.getStatePanel().add(controll,gbc);
			
			paintNote(handle, gbc, state);
		}
	}


	//add a new List
	@SuppressWarnings("unchecked")
	protected void addListField (Handle handle, GridBagConstraints gbc, State state) {
		
		//gbc.gridx = GridBagConstraints.RELATIVE;		
		paintLabel (handle,gbc,state);	
	
		List<?> list = null;		
	
		if (getLatestItem(handle) != null){
			list = (List<?>) (getLatestItem(handle));			
		} else {
			list = (List<?>) (config.getValue(handle));
		}
	
		if (list != null) {
			//System.out.print(config.getProperty(handle, ITEM_TYPE));
		
			final DefaultListModel dlm = new DefaultListModel();
			
			for (int i = 0; i < list.size(); i++){		
				dlm.addElement(list.get(i));
			}	
			
			
			JList jl = new JList(dlm);
			jl.setFixedCellHeight(24);
			JPanel controll = listControll(jl,dlm,handle);
			//only one item selectable
			jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			jl.addListSelectionListener(new changeListSelectionListener(handle, controll));			
			
			// no custom renderer = use default list
			if (config.getProperty(handle, RENDERER) != null) {
				jl.setCellRenderer((ListCellRenderer<?>) config.getProperty(
						handle, RENDERER));
			}
	
			else if (config.getProperty(handle, ITEM_TYPE) != null
					&& config.getProperty(handle, ITEM_TYPE) == EntryType.COLOR) {
				ListCellRenderer renderer = new ColorCellRenderer();
				jl.setCellRenderer(renderer);
			}
	
			if (config.isLocked(handle))
				jl.setEnabled(false);
	
			gbc.fill = GridBagConstraints.HORIZONTAL;
	
			JScrollPane scrollList = new JScrollPane(jl);
			scrollList
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			scrollList.getVerticalScrollBar().setUnitIncrement(16);
			// scrollList.setPreferredSize(new Dimension(200, 80));
			// gbc.gridwidth = 4;
			state.getStatePanel().add(scrollList, gbc);
	
			// buttonstuff
			gbc.fill = GridBagConstraints.NONE;
			state.getStatePanel().add(controll, gbc);
			
			paintNote(handle, gbc, state);
		}
	}


	//map
	private Map<?, ?> arrayListToMap(List al) {
		
		Map<String, Collection<String>> map = new HashMap<String, Collection<String>>();
		
		for (int i =0 ; i < al.size(); i++){
			String key = al.get(i).toString();
			String value = al.get(2).toString();
			Collection<String> values = map.get(key);
			if (values==null) {
				values = new ArrayList<String>();
				map.put(key, values);
			}
			values.add(value);
		}
		return map;
	}
    
    
    public static DefaultTableModel toTableModel(Map<?,?> map) {
        DefaultTableModel model = new DefaultTableModel(
            new Object[] { ResourceManager.getInstance().get("key"), //$NON-NLS-1$
            			   ResourceManager.getInstance().get("value")}, 0 //$NON-NLS-1$
        );
        
        for (Map.Entry<?,?> entry : map.entrySet()) {
            model.addRow(new Object[] { entry.getKey(), entry.getValue() });
        }        
        return model;
    }
    
    

    //listcontroll
	protected JPanel tableControll (JTable table, DefaultTableModel dtm, Handle handle){
		JPanel pAll = new JPanel();
		pAll.setLayout(new BorderLayout());
	    JButton newE = new JButton(ResourceManager.getInstance().get("config.listNew.name")); //$NON-NLS-1$
	    newE.setToolTipText(ResourceManager.getInstance().get("config.listNew.desc")); //$NON-NLS-1$
	    newE.addActionListener(new TableAction(table, dtm, true, handle,pAll,1));
	    //newE.setEnabled(false);
	    
	    JButton editE = new JButton(ResourceManager.getInstance().get("config.listEdit.name")); //$NON-NLS-1$
	    editE.setToolTipText(ResourceManager.getInstance().get("config.listEdit.desc")); //$NON-NLS-1$
	    editE.addActionListener(new TableAction(table, dtm, false, handle, pAll,0));
	    //editE.setEnabled(false);
	    
	    JButton deleteE = new JButton(ResourceManager.getInstance().get("config.listDelete.name")); //$NON-NLS-1$
	    deleteE.setToolTipText(ResourceManager.getInstance().get("config.listDelete.desc"));	 //$NON-NLS-1$
	    deleteE.addActionListener(new TableAction(table, dtm, false, handle, pAll,-1));
	    //deleteE.setEnabled(false);		
	      
	    pAll.add(newE,BorderLayout.NORTH);
	    pAll.add(editE,BorderLayout.CENTER);
	    pAll.add(deleteE,BorderLayout.SOUTH);

		return pAll;
	}
	
	
	protected class TableAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2808459167112719230L;
		protected final JTable jt;
		protected final JPanel controll;
		protected final boolean newentry;
		protected final DefaultTableModel dtm;
		protected final Handle handle;
		protected final int mode;

		TableAction(JTable jt, DefaultTableModel dtm, boolean newentry,
				Handle handle, JPanel controll, int mode) {
			this.jt = jt;
			this.dtm = dtm;
			this.newentry = newentry;
			this.handle = handle;
			this.controll = controll;
			this.mode = mode;
		}

		public Vector createDataVector(String key, Object val, 
				HashMap<Object, Object> data) {
			Vector<Object> vector = new Vector(2);
			vector.add(key);
			vector.add(val);

			data.put(key, val);
			return vector;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean dtmnchange = false;
			MapHandler mh = getMapHandler(handle);
			int mode = this.mode;
			int indexRow = jt.getSelectedRow();
			
			LinkedHashMap<Object, Object> data = (LinkedHashMap<Object, Object>) (config
					.getValue(handle));

			String title = null;
			String key = null;
			Boolean newEntry = false;
			Object val = null;
			
			// new
			if (mode == 1) {
				int cc = dtm.getColumnCount();
				val = mh.newEntry();
				key = ""; //$NON-NLS-1$
				//after creating new node go to edit mode, let the user set value
				indexRow = dtm.getRowCount();
				title = ResourceManager.getInstance().get("config.listNew.desc"); //$NON-NLS-1$
				mode = 0;
				newEntry = true;
			}
						
			
			if (indexRow != -1 && indexRow <= dtm.getRowCount()) {
				
				if (key == null)
					key = (String) jt.getValueAt(indexRow, 0);
				if (val == null)
					val = jt.getValueAt(indexRow, 1);
				

				
				// add selected value to entry handler
				mh.setValue(val);
				mh.setKey(key);				

								
				
				// Edit
				if (mode == 0 && mh.isValueEditable()) {
										
					Component comp = mh.getComponent();
					if (comp == null) 
						return;
					Boolean valid = true;
					if (title == null){
						title = ResourceManager.getInstance().get("config.listEdit.name"); //$NON-NLS-1$
					}
					
					if (comp instanceof Dialog) {
						((Dialog)comp).setVisible(true);
					} else if (comp instanceof JFileChooser){
						valid = JFileChooser.APPROVE_OPTION ==
						((JFileChooser)comp).showDialog(ConfigDialog.this,
								ResourceManager.getInstance().get("config.choosePathSet.name")); //$NON-NLS-1$
					} else {
						valid = JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
								ConfigDialog.this, (Object)new Object[]{
							comp}, title, 
							JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
							null, new Object[]{ResourceManager.getInstance().get("ok"), //$NON-NLS-1$
							ResourceManager.getInstance().get("cancel")}, null); //$NON-NLS-1$
					}					

					//only when edit, key did not change
					if (key.equals(mh.getKey())){
						if (valid && mh.isValueValid()) {
							val = mh.getValue();
							dtm.setValueAt(key, indexRow, 0);
							dtm.setValueAt(val, indexRow, 1);
							data.put(key, val);
						}
					}
					
					
					else if (valid && mh.isValueValid()) {						

						key = mh.getKey();
						val = mh.getValue();
						
						if (data.containsKey(key)) {
							JOptionPane.showMessageDialog(ConfigDialog.this,
									ResourceManager.getInstance().get("config.keyDuplicate.desc"), //$NON-NLS-1$
									ResourceManager.getInstance().get("config.keyDuplicate.name"), //$NON-NLS-1$
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						
						//Edit Action and key Changed => add new and delete old
						if (!newEntry){
							if (indexRow < dtm.getRowCount()){						
								dtm.setValueAt(key, indexRow, 0);
								dtm.setValueAt(val, indexRow, 1);	
								data.clear();
								for (Object row:dtm.getDataVector()){
									key = (String) ((Vector) row).get(0);
									val = ((Vector) row).get(1);
									data.put(key,val);
								}
							}
							
						} else {
						
						
							if (indexRow < dtm.getRowCount()){						
								dtm.setValueAt(key, indexRow, 0);
								dtm.setValueAt(val, indexRow, 1);
								data.put(key, val);
							} else {
								Vector<?> newData = createDataVector(key,
										val, data);
								dtm.addRow(newData);
							}
						}		

						dtmnchange = true;
					}
					
				}
				
				// Delete
				if (mode == -1) {
					data.remove(key);
					dtm.removeRow(indexRow);
					dtmnchange = true;
				}
			}


			if (dtmnchange) {
				changesMap.put(handle, data);
				configBottomPanel.getComponent(0).setEnabled(true);
				//System.out.println(changesMap);
				//System.out.println(dtm.getDataVector());
			}
		}
	}
	
	private Vector List2Vector(List<?> list) {
		Vector vector = new Vector();
		for (int i = 0; i < list.size(); i++) {
			Vector v = new Vector();
			v.addElement(list.get(i));
			vector.addElement(v);
		}
		return vector;
	}


	//listcontroll
	protected JPanel listControll (JList jl, DefaultListModel dlm, Handle handle){
		JPanel pAll = new JPanel();
		pAll.setLayout(new BorderLayout());
	    JButton newE = new JButton(ResourceManager.getInstance().get("config.listNew.name")); //$NON-NLS-1$
	    newE.setToolTipText(ResourceManager.getInstance().get("config.listNew.desc")); //$NON-NLS-1$
	    newE.addActionListener(new ListAction(jl,dlm, 1,handle));
	    newE.setEnabled(true);
	    
	    JButton editE = new JButton(ResourceManager.getInstance().get("config.listEdit.name")); //$NON-NLS-1$
	    editE.setToolTipText(ResourceManager.getInstance().get("config.listEdit.desc")); //$NON-NLS-1$
	    editE.addActionListener(new ListAction(jl,dlm, 0,handle));
	    editE.setEnabled(false);
	    
	    JButton deleteE = new JButton(ResourceManager.getInstance().get("config.listDelete.name")); //$NON-NLS-1$
	    deleteE.setToolTipText(ResourceManager.getInstance().get("config.listDelete.desc")); //$NON-NLS-1$
	    deleteE.addActionListener(new ListAction(jl,dlm, -1,handle));
	    deleteE.setEnabled(false);
		
	    pAll.add(newE,BorderLayout.NORTH);
	    pAll.add(editE,BorderLayout.CENTER);
	    pAll.add(deleteE,BorderLayout.SOUTH);
		return pAll;
	}
	
	
	//render component for color entries
	public class ColorCellRenderer extends JPanel implements ListCellRenderer<Object>,
	TableCellRenderer{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1574536281722355620L;
		Color color;

        
		@Override
		public Component getListCellRendererComponent(JList<? extends Object> 
				list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			 	color = new Color((Integer)value);	            

	            if (isSelected) {
	            	color = new Color (getBrighterColor(color));
	            	setBorder(BorderFactory.createLineBorder(
	            			list.getSelectionForeground()));
	            	setBackground(list.getSelectionBackground());
	            }
	            else{
	                setBackground(list.getBackground());
	                setBorder(null);
	            }
			return this;
		}
		

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
		 	color = new Color((Integer)value);	            
            if (isSelected) {
            	color = new Color (getBrighterColor(color));
            	setBackground(table.getSelectionBackground());
            }
            else{
                setBackground(table.getBackground());
            }
			return this;
		}
		
		public int getBrighterColor(Color selectedColor){
			// Convert RGB to HSB
			float[] hsb = Color.RGBtoHSB(selectedColor.getRed(),selectedColor.getGreen(),
								selectedColor.getBlue(), null);
			
			float hue = hsb[0];          // .58333
			float saturation = (float) (hsb[1] * 0.7);   // .66667
			float brightness = hsb[2];   // .6
			
			// Convert HSB to RGB value
			int rgb_brighter = Color.HSBtoRGB(hue, saturation, brightness);
			return rgb_brighter;			
		}
		
		protected void paintComponent (Graphics g){
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			int x,y;
			if ( h <= 26){
				h = h - 6;
				y = 3;
			} else {
				y = (h - 20) / 2;
				h = 20;				
			}
			if (w <= 66){
				w = w - 6;
				x = 3;
			} else {
				x = (w - 60) / 2;
				w = 60;				
			}
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(Color.black);
			g.drawRect(x, y, w, h);
		}

		/*
		@Override
		public void validate() {
		}

		@Override
		public void invalidate() {
		}

		@Override
		public void revalidate() {
		}
		 

		@Override
		public void repaint(long tm, int x, int y, int width, int height) {
		}

		@Override
		public void repaint(Rectangle r) {
		}
		
		@Override
		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
		}
		*/

		@Override
		public void firePropertyChange(String propertyName, byte oldValue,
				byte newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, char oldValue,
				char newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, short oldValue,
				short newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, int oldValue,
				int newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, long oldValue,
				long newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, float oldValue,
				float newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, double oldValue,
				double newValue) {
		}

		@Override
		public void firePropertyChange(String propertyName, boolean oldValue,
				boolean newValue) {
		}

	
	}
	
	
	//List Action
	protected class ListAction extends AbstractAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7651667787057391391L;
		protected final JList jl;
		protected final int mode;
		protected final DefaultListModel dlm;
		protected final Handle handle;
		
		ListAction(JList jl, DefaultListModel dlm, int mode, Handle handle) {
			this.jl = jl;			
			this.dlm = dlm;
			this.mode = mode;
			this.handle = handle;
		}
		
		private int getColor(Color selectedColor){
			return selectedColor.getRGB();
		}				
        
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean dlmnchange = false;
			EntryHandler eh = getEntryHandler(handle);
			int index = jl.getSelectedIndex();
			int mode = this.mode;
			String title = null;
			Object newVal = null;			
			
			
			// new
			if (mode == 1) {
				newVal = eh.newEntry();
				eh.setValue(newVal);
				index = dlm.getSize();
				dlmnchange = true;
				title = ResourceManager.getInstance().get("config.listNew.desc"); //$NON-NLS-1$
				//after creating new node go to edit mode, let the user set value
				mode = 0;
			}

			if (index != -1 && index <= dlm.getSize()) {
				
				if (newVal == null)
					newVal = dlm.getElementAt(index);
				
				// add selected value to entry handler
				eh.setValue(newVal);

				// Delete
				if (mode == -1) {
					if (eh.isValueEditable()) {
						dlm.remove(index);
						dlmnchange = true;
					}
				}

				// Edit
				if (mode == 0 && eh.isValueEditable()) {
					
					Component comp = eh.getComponent();
					if (comp == null) 
						return;
					Boolean valid = true;
					
					if (title == null){
						title = ResourceManager.getInstance().get("config.listEdit.name"); //$NON-NLS-1$
					}
					
					if (comp instanceof Dialog) {
						((Dialog)comp).setVisible(true);
					} else if (comp instanceof JFileChooser){
						valid = JFileChooser.APPROVE_OPTION ==
						((JFileChooser)comp).showDialog(ConfigDialog.this,
								ResourceManager.getInstance().get("config.choosePathSet.name")); //$NON-NLS-1$
					} else {
						valid = JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
								ConfigDialog.this, (Object)new Object[]{
							comp}, title, 
							JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
							null, new Object[]{ResourceManager.getInstance().get("ok"), //$NON-NLS-1$
							ResourceManager.getInstance().get("cancel")}, null); //$NON-NLS-1$
					}						
					
					if (valid && eh.isValueValid()) {
						newVal = eh.getValue();
						
						if (index < dlm.size()){						
							dlm.set(index, eh.getValue());
						} else {
							dlm.addElement(newVal);
						}	
					dlmnchange = true;
					}

				}
			}
			
	
			
			if (dlmnchange){				          	
            	changesMap.put(handle, CollectionUtils.asList(dlm.toArray()));
            	configBottomPanel.getComponent(0).setEnabled(true);
            	//System.out.println(changesMap);   
			}
		}	
	}
	
	
	class CustomDialog extends JDialog implements PropertyChangeListener {
		 /**
		 * 
		 */
		private static final long serialVersionUID = 8730334291227865442L;
		private String typedText = ""; //$NON-NLS-1$
		 private JTextField textField;
		 private JOptionPane optionPane;
		 
		 private String enter = ResourceManager.getInstance().get("enter"); //$NON-NLS-1$
		 private String cancel = ResourceManager.getInstance().get("cancel"); //$NON-NLS-1$
		 
		 /**
		   * Returns null if the typed string was invalid;
		   * otherwise, returns the string as the user entered it.
		   */
		 public String getValidatedText() {
		      return typedText;
		 }
		 
		 public CustomDialog(String operation, String input) {
		 
		    textField = new JTextField(input);
		    UIUtil.createUndoSupport(textField, 10);
			UIUtil.addPopupMenu(textField, UIUtil.createDefaultTextMenu(
					textField, true));
		    
		    Object[] array = {operation, textField};
		 
		    //Create dialog buttons
		    Object[] options = {enter, cancel};
		    
	        //Create the JOptionPane.
	        optionPane = new JOptionPane(array,
	                                    JOptionPane.QUESTION_MESSAGE,
	                                    JOptionPane.YES_NO_OPTION,
	                                    null,
	                                    options,
	                                    options[0]);
	 
	        //Make this dialog display it.
	        setContentPane(optionPane);
	        
	        //Handle window closing correctly.
	        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	        addWindowListener(new WindowAdapter() {
	                public void windowClosing(WindowEvent we) {
	                /*
	                 * Instead of directly closing the window,
	                 * we're going to change the JOptionPane's
	                 * value property.
	                 */
	                    optionPane.setValue(new Integer(
	                                        JOptionPane.CLOSED_OPTION));
	            }
	        });
	 
	        //text field always gets focus.
	        addComponentListener(new ComponentAdapter() {
	            public void componentShown(ComponentEvent ce) {
	                textField.requestFocusInWindow();
	            }
	        });
	        
	        //Register an event handler that puts the text into the option pane.
	        // textField.addActionListener(this);
	 
	        //Register an event handler that reacts to option pane state changes.
	        optionPane.addPropertyChangeListener(this);
	        
	        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);	       
	        pack();
	        this.setVisible(true);   
		 }
		
		 
		public String gettypedText () {
			return typedText;
		} 
		

		@Override
		/** This method reacts to state changes in the option pane. */
	    public void propertyChange(PropertyChangeEvent e) {
	        String prop = e.getPropertyName();
	 
	        if (isVisible()
	         && (e.getSource() == optionPane)
	         && (JOptionPane.VALUE_PROPERTY.equals(prop) ||
	             JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
	            Object value = optionPane.getValue();
	 
	            if (value == JOptionPane.UNINITIALIZED_VALUE) {
	                //ignore reset	                
	            }
	 
	            //Reset the JOptionPane's value.
	            //If you don't do this, then if the user
	            //presses the same button next time, no
	            //property change event will be fired.
	            optionPane.setValue(
	                    JOptionPane.UNINITIALIZED_VALUE);
	 
	            if (enter.equals(value)) {
                    typedText = textField.getText();	                
	            } else { //user closed dialog or clicked cancel
	                typedText = ""; //$NON-NLS-1$
	                clearAndHide();
	            }
	        }
	    }
	 
	    /** This method clears the dialog and hides it. */
	    public void clearAndHide() {
	        //textField.setText(null);
	        setVisible(false);
	    }
	}
	
	
	// limit field stringsize
	class JTextFieldLimit extends PlainDocument {
		  /**
		 * 
		 */
		private static final long serialVersionUID = 3884808623509948397L;
		private int limit;
		  
		  JTextFieldLimit(int limit) {
		    super();
		    this.limit = limit;
		  }
	
		  public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		    if (str == null)
		      return;
	
		    if ((getLength() + str.length()) <= limit) {
		      super.insertString(offset, str, attr);
		    }
		  }
	}
	
	//config listener
	protected class CfgListener implements ConfigListener {
		

		protected final Handle handle;
		
		CfgListener(Handle handle) {
			this.handle = handle;	
		}
		
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			//System.out.println("ConfigListener: " + event.getName());
			
			
		}
	}	
	
	
	// tab change
	protected class tabChange implements ChangeListener {
		
		protected final Handle handle;
		
		tabChange(Handle handle) {
			this.handle = handle;	
		}
		
	    public void stateChanged(ChangeEvent ce) {
	    	JTabbedPane tabs = (JTabbedPane)ce.getSource();
	    	tabs.getComponentAt(tabs.getSelectedIndex());
	    	String tmp = config.getParentPath(handle) + "."  //$NON-NLS-1$
	    				+ tabs.getTitleAt(tabs.getSelectedIndex());	    	
	
	    	JPanel panel = (JPanel)tabs.getSelectedComponent();
	    	panel.add(configOptionsPanel, BorderLayout.CENTER);
	    	rebuildTabPanel(config.getHandle(tmp));	
	    }
	}
	
	
	protected class tableModelListener implements TableModelListener {
		protected final Handle handle;
		protected final JPanel controll;

		tableModelListener(Handle handle, JPanel controll) {
			this.handle = handle;
			this.controll = controll;
		}

		@Override
		public void tableChanged(TableModelEvent e) {

			DefaultTableModel dtm = (DefaultTableModel) e.getSource();
			int tablesize = dtm.getRowCount();
			int maxItems = Integer.MAX_VALUE;
			int minItems = 0;

			// custom min max valuesset?
			if (config.getProperty(handle, MIN_ITEM_COUNT) != null)
				minItems = (Integer) config.getProperty(handle, MIN_ITEM_COUNT);

			if (config.getProperty(handle, MAX_ITEM_COUNT) != null)
				maxItems = (Integer) config.getProperty(handle, MAX_ITEM_COUNT);

			// delete button
			if ((tablesize - 1) >= minItems) {
				controll.getComponent(2).setEnabled(true);
			} else {
				controll.getComponent(2).setEnabled(false);
			}
			// new button
			if ((tablesize + 1) <= maxItems) {
				controll.getComponent(0).setEnabled(true);
			} else {
				controll.getComponent(0).setEnabled(false);
			}

			// edit button
			controll.getComponent(1).setEnabled(true);

			// System.out.println("e" + dtm.getRowCount());
		}
	}
	
	//table change
	protected class changeMapPropertyListener implements PropertyChangeListener {
		protected final Handle handle;
		protected final JTable table;
		protected final JPanel controll;
		
		changeMapPropertyListener(Handle handle, JTable table, JPanel controll) {
			this.handle = handle;	
			this.table = table;
			this.controll = controll;
		}
		
		private Object [] getObjects (DefaultTableModel dm){
			Object[] objects = null;
			return objects;
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent pce){
			
			JTable tb = (JTable) pce.getSource();
			int row = tb.getSelectedRow();
			int col = tb.getSelectedColumn();			
	
			if (row != -1 && col != -1) {
				
				tb.getModel().setValueAt(tb.getValueAt(row, col), row, col);				
				
				Map<Object, Object> map = new LinkedHashMap<Object, Object>();
				for (int r =0 ; r < tb.getModel().getRowCount(); r++){
					Object key = tb.getModel().getValueAt(r, 0);
					Object value = tb.getModel().getValueAt(r, 1);
					map.put(key, value);
				}
				
	        	changesMap.put(handle,map);
	        	configBottomPanel.getComponent(0).setEnabled(true);
				//System.out.println(changesMap);	        	
			}	
		
		}	
	}



	// list
	// also used for handling listcontroll buttons enable/disable
	protected class changeListSelectionListener implements ListSelectionListener {
		
		protected final Handle handle;
		protected final JPanel controll;
		
		changeListSelectionListener(Handle handle, JPanel controll) {
			this.handle = handle;	
			this.controll = controll;
		}

	    public void valueChanged(ListSelectionEvent e) {
	        //mouse button release -> selection complete -> getValueIsAdjusting() = false
	        if (!e.getValueIsAdjusting()) {
	            JList<?> list = (JList<?>)e.getSource();
	            List<?> selected = list.getSelectedValuesList();

	            int listsize = list.getModel().getSize();
	            //System.out.println(config.isModifiable(handle));
	            int maxItems = Integer.MAX_VALUE;
	            int minItems = 0;
	            
	            //if (config.isModifiable(handle)) {	            
		            //custom min max valuesset?
		            if (config.getProperty(handle, MIN_ITEM_COUNT) != null) 	
		            	minItems = (Integer)config.getProperty(handle, MIN_ITEM_COUNT);
	  
		            if (config.getProperty(handle, MAX_ITEM_COUNT) != null) 
		            	maxItems = (Integer)config.getProperty(handle, MAX_ITEM_COUNT);
		            
		            //delete button
		            if ((listsize - 1) >= minItems) {
			           controll.getComponent(2).setEnabled(true);
			        } else {
			        	controll.getComponent(2).setEnabled(false);
			        }
		            //new button
		            if ((listsize + 1) <= maxItems) {
		            	controll.getComponent(0).setEnabled(true);		            	
		            } else {
		            	controll.getComponent(0).setEnabled(false);	
		            }
		            
		            //edit button
		            controll.getComponent(1).setEnabled(true);

	        }
	    }
	}
	

	// Restore Default Values Action
	protected class RestoreDefaultsAction extends AbstractAction{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6778383178138216136L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int exit = showYN("config.restoreWarning.title", //$NON-NLS-1$
					"config.restoreWarning.message"); //$NON-NLS-1$
			if (exit == 0){
				restoreDefaultValues(defaultList);
				changesMap.clear();
				configBottomPanel.getComponent(0).setEnabled(false);
			}	
	    }	
	}
	
	
	//Basic save & exit operations
	protected class ExitSaveAction extends AbstractAction{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 7681721446277096555L;
		private final boolean exit;
		
		ExitSaveAction(boolean exit) {
			this.exit = exit;

			if (exit) {				
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("remove.gif")); //$NON-NLS-1$
			} else {
				putValue(Action.SMALL_ICON, IconRegistry.getGlobalRegistry().getIcon("complete.gif")); //$NON-NLS-1$
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (exit){
				dialogExitAction();				
			}else{
            	setNewValues(changesMap);
			}	
		}
	}
	
	public Integer showYN(String titleKey, String messageKey, Object... args) {
		Object[] options = {ResourceManager.getInstance().get("continue"), //$NON-NLS-1$
                ResourceManager.getInstance().get("cancel")}; //$NON-NLS-1$
		int i = JOptionPane.showOptionDialog(this, String.format(ResourceManager.getInstance()
				.get(messageKey), args), ResourceManager.getInstance().get(titleKey),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
			    options,
			    options[1]);
			return i;
	}

	
	//Change Spinner
	protected class spinnersliderChanged implements ChangeListener {
		
		protected final Handle handle;
		protected final JSlider slider;
		protected final JSpinner spinner;
		
		spinnersliderChanged(Handle handle, JSlider slider, JSpinner spinner) {
			this.handle = handle;
			this.slider = slider;
			this.spinner = spinner;
		}
		
	    public void stateChanged(ChangeEvent ce) {
	    	
	    	//change event belong to spinner
	    	if (ce.getSource().toString().contains("JSpinner")){ //$NON-NLS-1$
	    		JSpinner source = (JSpinner)ce.getSource();
	    		slider.setValue((Integer) source.getValue());
	    	};
	    	
	    	//change event belong to slider
	    	if (ce.getSource().toString().contains("JSlider")){	    		 //$NON-NLS-1$
	    		JSlider source = (JSlider)ce.getSource();

	    		/* slider shows _while adjusting(!) val=i, valnext=i+1,... always stepsize
	    		 * 1 even if other precision tick was set, to prevent spinner showing 
	    		 * false values modulo check added
	    		 */
	    
	    		if (source.getValue() % source.getMinorTickSpacing() == 0){
	    			spinner.setValue((Integer) source.getValue());
	    		}
	    		
	    		/* slidervalue automatic snap to next allowed value when not adjusting
	    		 * therefor no modulo hack needed at this point
	    		 */
	    		if (!slider.getValueIsAdjusting()) {
	    			changesMap.put(handle,slider.getValue());
	    			configBottomPanel.getComponent(0).setEnabled(true);
	    		}
	    	};	  
	    	//System.out.println(changesMap);
	    }		
	}
	
	
	
	//Change Radio Action
	protected class radioChangedAction extends AbstractAction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6784704206491598387L;
		protected final Handle handle;
		protected final ButtonGroup group;
		
		radioChangedAction(Handle handle, ButtonGroup group) {
			this.handle = handle;
			this.group = group;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
	    	List<?> list = (List<?>) (config.getProperty(handle, OPTIONS));
	    	int selectionindex = Integer.parseInt(group.getSelection().getActionCommand());
	    	changesMap.put(handle,list.get(selectionindex));	
	        configBottomPanel.getComponent(0).setEnabled(true);			
		}		
	}
	  
	
	protected class numFieldAction extends AbstractAction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -6983119048486338595L;
		protected final Handle handle;
		protected final Object oldval;
		protected final JFormattedTextField field;
		
		numFieldAction(Handle handle, JFormattedTextField field, Object oldval) {
			this.handle = handle;
			this.field = field;
			this.oldval = oldval;
		}


		@Override
		public void actionPerformed(ActionEvent e) {
			
	        if (config.getType(handle)== EntryType.INTEGER) {
	            int val = ((Number)field.getValue()).intValue();
	            if ((Integer)oldval != val){
	            	changesMap.put(handle,val);	
	            	configBottomPanel.getComponent(0).setEnabled(true);
	            }
	        } else if (config.getType(handle) == EntryType.FLOAT) {
	        	float val = ((Number)field.getValue()).floatValue();
	        	if ((Float)oldval != val){
	            	changesMap.put(handle,val);	
	            	configBottomPanel.getComponent(0).setEnabled(true);
	            }	
	        } else if (config.getType(handle) == EntryType.DOUBLE) {
	        	double val = ((Number)field.getValue()).doubleValue();
	        	if ((Double)oldval != val){
	            	changesMap.put(handle,val);	
	            	configBottomPanel.getComponent(0).setEnabled(true);
	            }	
	        } else if (config.getType(handle) == EntryType.LONG) {
	        	long val = ((Number)field.getValue()).longValue();
	        	if ((Long)oldval != val){
	            	changesMap.put(handle,val);	
	            	configBottomPanel.getComponent(0).setEnabled(true);
	            }	
	        }
		}		
	}
	
	//Change Number
	protected class numFieldChanged implements PropertyChangeListener {
		
		protected final Handle handle;
		protected final Object oldval;
		
		numFieldChanged(Handle handle, Object oldval) {
			this.handle = handle;
			this.oldval = oldval;
		}


		@Override
		public void propertyChange(PropertyChangeEvent e) {
			JFormattedTextField source = (JFormattedTextField) e.getSource();
			
	 
		        if (config.getType(handle) == EntryType.INTEGER) {
		            int val = ((Number)source.getValue()).intValue();
		            if ((Integer)oldval != val){
		            	changesMap.put(handle,val);	
		            	configBottomPanel.getComponent(0).setEnabled(true);
		            }
		        } else if (config.getType(handle) == EntryType.FLOAT) {
		        	float val = ((Number)source.getValue()).floatValue();
		        	if ((Float)oldval != val){
		            	changesMap.put(handle,val);	
		            	configBottomPanel.getComponent(0).setEnabled(true);
		            }	
		        } else if (config.getType(handle) == EntryType.DOUBLE) {
		        	double val = ((Number)source.getValue()).doubleValue();
		        	if ((Double)oldval != val){
		            	changesMap.put(handle,val);	
		            	configBottomPanel.getComponent(0).setEnabled(true);
		            }	
		        } else if (config.getType(handle) == EntryType.LONG) {
		        	long val = ((Number)source.getValue()).longValue();
		        	if ((Long)oldval != val){
		            	changesMap.put(handle,val);	
		            	configBottomPanel.getComponent(0).setEnabled(true);
		            }	
		        }
		        
	       // System.out.println(changesMap);
	        
		}		
	}
	
	
	//Change Spinner
	protected class spinnerChanged implements ChangeListener {
		
		protected final Handle handle;
		
		spinnerChanged(Handle handle) {
			this.handle = handle;
		}
		
	    public void stateChanged(ChangeEvent ce) {
	        JSpinner source = (JSpinner)ce.getSource();
	        changesMap.put(handle,source.getValue());	
	        configBottomPanel.getComponent(0).setEnabled(true);
	    }		
	}
	
	//Change Slider
	protected class sliderChanged implements ChangeListener {
		
		protected final Handle handle;
		protected final JLabel sliderval;
		
		sliderChanged(Handle handle, JLabel sliderval) {
			this.handle = handle;
			this.sliderval = sliderval;
		}
		
	    public void stateChanged(ChangeEvent ce) {
	        JSlider slider = (JSlider)ce.getSource();
	        if (slider.getValueIsAdjusting()) {
	        	slider.setToolTipText(String.valueOf(slider.getValue()));
	        	sliderval.setText(String.valueOf(slider.getValue()));	
	        }
	        if (!slider.getValueIsAdjusting()) {
	        	slider.setToolTipText(String.valueOf(slider.getValue()));
	        	changesMap.put(handle,slider.getValue());
	        	configBottomPanel.getComponent(0).setEnabled(true);
	        }
	    }		
	}
	
	
	//Textfield
	protected class DocumentTextFieldListener implements DocumentListener {
		
		protected final Handle handle;
		
		public DocumentTextFieldListener(Handle handle) {
			this.handle = handle;
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
			change (de);			
		}

		@Override
		public void insertUpdate(DocumentEvent de) {
			change (de);		
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			change (de);
		}
		
		public void change (DocumentEvent de){
			JTextField textField = (JTextField) de.getDocument().getProperty("parent"); //$NON-NLS-1$
			changesMap.put(handle,textField.getText());
			configBottomPanel.getComponent(0).setEnabled(true);	
			//System.out.println(changesMap);
		}
	
	}	
	
	
	//Textarea
	protected class DocumentTextAreaListener implements DocumentListener {
		
		protected final Handle handle;

		public DocumentTextAreaListener(Handle handle) {
			this.handle = handle;
		}

		@Override
		public void changedUpdate(DocumentEvent de) {
			change (de);			
		}

		@Override
		public void insertUpdate(DocumentEvent de) {
			change (de);		
		}

		@Override
		public void removeUpdate(DocumentEvent de) {
			change (de);
		}
		
		public void change (DocumentEvent de){
			JTextArea textArea = (JTextArea) de.getDocument().getProperty("parent"); //$NON-NLS-1$
			changesMap.put(handle,textArea.getText());
			configBottomPanel.getComponent(0).setEnabled(true);	
			//System.out.println(changesMap);
		}
	
	}
	
	//Change comboBox
	protected class comboBoxChangedAction extends AbstractAction{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8454473665735295355L;
		protected final Handle handle;
		
		comboBoxChangedAction(Handle handle) {
			this.handle = handle;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> cb = (JComboBox<?>)e.getSource();
			
			List<?> list = null;
			//retrieve elements 
			if (config.getProperty(handle, OPTIONS) != null){
				list = (List<?>) (config.getProperty(handle, OPTIONS));
			}
			
			changesMap.put(handle,list.get(cb.getSelectedIndex()));
			configBottomPanel.getComponent(0).setEnabled(true);
        }		
	}
	
	
	//Change Checkbox
	protected class checkBoxChangedAction extends AbstractAction{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8268358354503018341L;
		protected final Handle handle;
		
		checkBoxChangedAction(Handle handle) {
			this.handle = handle;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox cb = (JCheckBox)e.getSource();
			Object newVal = true;
			if (!(cb.isSelected())) newVal = false;
			changesMap.put(handle,newVal);
			configBottomPanel.getComponent(0).setEnabled(true);
            //System.out.println(changesMap);
        }		
	}
	
	
	//Color Chooser
	protected class ColorChooseAction extends AbstractAction{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2838576980447568918L;
		protected final JColorChooser colorChooser = new JColorChooser();
		protected final Handle handle;
		protected final JPanel colorPanel;
		
		ColorChooseAction(Handle handle, JPanel colorPanel) {
			this.handle = handle;
			this.colorPanel = colorPanel;
		}		
		
		private int getColor(Color selectedColor){
			return selectedColor.getRGB();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
            Color selectedColor = JColorChooser.showDialog(null,
            		ResourceManager.getInstance().get("config.chooseColorPick.name"), //$NON-NLS-1$
            		colorPanel.getBackground());
            if (selectedColor != null && getColor(selectedColor)!=colorPanel.getBackground().getRGB() ) {            	
            	colorPanel.setBackground(new Color(getColor(selectedColor)));
            	changesMap.put(handle,getColor(selectedColor));
            	configBottomPanel.getComponent(0).setEnabled(true);
            	//System.out.println(changesMap);
            }

        }	
	}
	
	// Path Chooser
	protected class PathChooseAction extends AbstractAction{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7264060828066381998L;
		protected final Handle handle;
		protected final JLabel pathlabel;
		protected final JFileChooser pathChooser = new JFileChooser();
		protected final String lastdir;

		
		PathChooseAction(Handle handle, JLabel pathlabel, String lastdir) {
			this.handle = handle;			
			this.pathlabel = pathlabel;		
			this.lastdir = lastdir;
			iniPathChooser(pathChooser,lastdir);
		}
		
		void iniPathChooser(JFileChooser pathChooser, String lastdir) {		
			// Setting up Filter
			//pathChooser.setCurrentDirectory(new File("/~"));	
			pathChooser.setCurrentDirectory(new File(lastdir));
			pathChooser.setDialogTitle(ResourceManager.getInstance().get("config.choosePathDialog.title")); //$NON-NLS-1$
			pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			pathChooser.setToolTipText("null"); //$NON-NLS-1$
		}
		
		public String getPath(){			
		    return pathChooser.getSelectedFile().getAbsolutePath();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			pathChooser.showDialog(null, ResourceManager.getInstance().get("config.choosePathSet.name")); //$NON-NLS-1$
			if (pathChooser.getSelectedFile() != null){
				if (getPath() != null && getPath() != lastdir){
					pathlabel.setText(getPath());
					pathChooser.setCurrentDirectory(new File(getPath()));
					
		        	changesMap.put(handle,getPath());
		        	configBottomPanel.getComponent(0).setEnabled(true);
		        	//System.out.println(changesMap);
				}
			}
        }	
	}
}
