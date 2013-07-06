/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.view;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import de.ims.icarus.plugins.core.View;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIDummies;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.helper.Outline;
import de.ims.icarus.ui.helper.UIHelperRegistry;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.mpi.Commands;
import de.ims.icarus.util.mpi.Message;
import de.ims.icarus.util.mpi.ResultMessage;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class GraphView extends View implements Outline {
	
	protected GraphPresenter presenter;
	
	protected JTextArea infoLabel;
	protected JToolBar toolBar;
	protected JPanel contentPanel;

	public GraphView() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		container.setLayout(new BorderLayout());

		infoLabel = UIUtil.defaultCreateInfoLabel(container);
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
	
	protected void setPresenter(GraphPresenter presenter) {
		if(this.presenter==presenter) {
			return;
		}
		
		if(this.presenter!=null) {
			this.presenter.close();
		}
		
		this.presenter = presenter;
		
		if(this.presenter==null) {
			showInfo(null);
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
		GraphPresenter presenter = this.presenter;
		
		// Fetch new presenter
		if(presenter==null || !PresenterUtils.presenterSupports(presenter, data)) {
			ContentType contentType = (ContentType) options.get(Options.CONTENT_TYPE);
			if(contentType!=null) {
				presenter = UIHelperRegistry.globalRegistry().findHelper(
						GraphPresenter.class, contentType, true, false);
			} else {
				presenter = UIHelperRegistry.globalRegistry().findHelper(
						GraphPresenter.class, data);
			}
		}
		
		// Abort if presenter not available for content type
		if(presenter==null) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.graphView.unsupportedType", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		boolean refreshPresentingComponent = presenter!=this.presenter;
		
		setPresenter(presenter);
		
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

		infoLabel.setVisible(false);
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
	 * @see de.ims.icarus.plugins.core.View#handleRequest(de.ims.icarus.util.mpi.Message)
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
