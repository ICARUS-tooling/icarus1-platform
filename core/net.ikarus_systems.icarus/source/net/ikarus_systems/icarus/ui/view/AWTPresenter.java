/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.view;

import java.awt.Component;

/**
 * Abstract {@code Presenter} that uses a {@code Component} to render
 * its presentation data. Implementations are not restricted to the
 * limitations of the AWT toolkit and may use Swing as well since there
 * is no special {@code Presenter} interface that explicitly enforces
 * the use of Swing components.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AWTPresenter extends Presenter {

	/**
	 * Returns the {@code Component} this presenter is using to render
	 * its data. This method should never return {@code null} regardless
	 * of valid presentation data being set or not. This state is rather
	 * to be displayed by the visual {@code content} of the returned component.
	 */
	Component getPresentingComponent();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface TablePresenter extends AWTPresenter {
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface GraphPresenter extends AWTPresenter {
		
	}
}
