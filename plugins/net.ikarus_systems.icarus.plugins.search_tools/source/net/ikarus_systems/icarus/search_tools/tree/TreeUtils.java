/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools.tree;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.standard.GraphValidationResult;
import net.ikarus_systems.icarus.search_tools.standard.GraphValidator;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.dialog.DialogFactory;
import net.ikarus_systems.icarus.util.Options;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class TreeUtils {

	private TreeUtils() {
		// no-op
	}
	
	public static boolean validateTree(SearchGraph graph) {
		Options options = new Options();
		options.put(GraphValidator.ALLOW_CYCLES, false);
		options.put(GraphValidator.ALLOW_LINKS, false);
		options.put(GraphValidator.ALLOW_MULTIPLE_ROOTS, true);
		options.put(GraphValidator.ALLOW_NEGATED_TRANSITIVES, false);
		options.put(GraphValidator.MAX_INCOMING_EDGES, 1);
		options.put(GraphValidator.ALLOW_UNDEFINED_GRAPH, true);
		
		GraphValidator validator = new GraphValidator();
		
		GraphValidationResult result = validator.validateGraph(graph, options, new GraphValidationResult());
		
		if(!result.isEmpty()) {
			
			JPanel panel = new JPanel(new BorderLayout(0, 7));
			
			String title = ResourceManager.getInstance().get("plugins.searchTools.graphValidation.title"); //$NON-NLS-1$
			String message = ResourceManager.getInstance().get(result.getErrorCount()>0 ?
					"plugins.searchTools.graphValidation.errorMessage" //$NON-NLS-1$
					: "plugins.searchTools.graphValidation.warningMessage"); //$NON-NLS-1$
			
			JTextArea infoLabel = UIUtil.defaultCreateInfoLabel(panel);
			infoLabel.setText(message);
			panel.add(infoLabel, BorderLayout.NORTH);
			
			StringBuilder sb = new StringBuilder(500);
			if(result.getErrorCount()>0) {
				String label = "["+ResourceManager.getInstance().get( //$NON-NLS-1$
						"plugins.searchTools.graphValidation.errorLabel")+"]  "; //$NON-NLS-1$ //$NON-NLS-2$
				for(int i=0; i<result.getErrorCount(); i++) {
					sb.append(label).append(result.getErrorMessage(i)).append("\n"); //$NON-NLS-1$
				}
			}
			if(result.getWarningCount()>0) {
				String label = "["+ResourceManager.getInstance().get( //$NON-NLS-1$
						"plugins.searchTools.graphValidation.warningLabel")+"]  "; //$NON-NLS-1$ //$NON-NLS-2$
				for(int i=0; i<result.getWarningCount(); i++) {
					sb.append(label).append(result.getWarningMessage(i)).append("\n"); //$NON-NLS-1$
				}
			}
			
			JTextArea outputLabel = new JTextArea(sb.toString());
			outputLabel.setFont(infoLabel.getFont());
			
			JScrollPane scrollPane = new JScrollPane(outputLabel);
			panel.add(scrollPane, BorderLayout.NORTH);
			
			DialogFactory.getGlobalFactory().showGenericDialog(
					null, title, null, panel, true);
		}
		
		return result.getErrorCount()==0;
	}
}
