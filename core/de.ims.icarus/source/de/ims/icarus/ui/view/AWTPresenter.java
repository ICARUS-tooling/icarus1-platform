/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.view;

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
}
