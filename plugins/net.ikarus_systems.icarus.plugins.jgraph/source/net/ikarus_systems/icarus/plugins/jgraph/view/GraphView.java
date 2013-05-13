/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.view;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class GraphView extends View {
	
	protected GraphPresenter presenter;
	
	protected JLabel infoLabel;
	protected JPanel contentPanel;

	public GraphView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		container.setLayout(new BorderLayout());
		
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		container.add(infoLabel, BorderLayout.NORTH);
		
		contentPanel = new JPanel(new BorderLayout());
		container.add(contentPanel, BorderLayout.CENTER);
		
		showInfo(null);
	}
	
	public GraphPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void close() {
		super.close();

		if(presenter!=null) {
			presenter.close();
			presenter = null;
		}
	}

	@Override
	public void reset() {
		if(contentPanel==null) {
			return;
		}
		
		displayData(null, null);
	}
	
	protected void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.jgraph.graphView.notAvailable"); //$NON-NLS-1$
		}
		infoLabel.setText(text);
		
		infoLabel.setVisible(true);
		contentPanel.setVisible(false);
		contentPanel.removeAll();
		
		// Close any active presenter and discard its reference
		if(presenter!=null) {
			presenter.close();
			presenter = null;
		}
	}
	
	protected void displayData(Object data, Options options) {
		
		// Show default info if nothing available to be displayed
		if(data==null) {
			showInfo(null);
			return;
		}
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		// Fetch content type for data
		ContentType contentType = (ContentType) options.get(Options.CONTENT_TYPE);
		if(contentType==null) {
			contentType = ContentTypeRegistry.getInstance().getEnclosingType(data);
		}
		
		GraphPresenter presenter = this.presenter;
		
		// Fetch new presenter
		if(presenter==null || !presenter.supports(contentType)) {
			presenter = UIHelperRegistry.globalRegistry().findHelper(
					GraphPresenter.class, contentType, true, false);
		}
		
		// Abort if presenter not available for content type
		if(presenter==null || presenter.supports(contentType)) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.graphView.unsupportedType", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		boolean refreshPresentingComponent = false;
		
		// Close old presenter if we found a new one and initialize the new
		if(presenter!=this.presenter) {
			this.presenter.close();
			refreshPresentingComponent = true;
			
			presenter.init();
		}
		
		this.presenter = presenter;
		
		try {
			presenter.present(data, options);
		} catch (UnsupportedPresentationDataException e) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.graphView.presentationFailed", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			UIDummies.createDefaultErrorOutput(contentPanel, e);
			refreshPresentingComponent = false;
			return;
		}
		
		if(refreshPresentingComponent) {
			contentPanel.removeAll();
			contentPanel.add(presenter.getPresentingComponent(), BorderLayout.CENTER);
		}
		
		contentPanel.setVisible(true);
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#DISPLAY}</li>
	 * <li>{@link Commands#PRESENT}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 * 
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		
		if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			displayData(message.getData(), message.getOptions());
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}

}
