/*
 * $Revision: 29 $
 * $Date: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/view/AWTPresenter.java $
 *
 * $LastChangedDate: 2013-05-03 20:03:21 +0200 (Fr, 03 Mai 2013) $ 
 * $LastChangedRevision: 29 $ 
 * $LastChangedBy: mcgaerty $
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
 * @version $Id: AWTPresenter.java 29 2013-05-03 18:03:21Z mcgaerty $
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
}
