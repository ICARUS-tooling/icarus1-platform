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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.result;

import java.awt.Component;

import javax.swing.JPanel;

import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import gnu.trove.list.TIntList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class WordDetailOutline implements AWTPresenter {

	private JPanel contentPanel;

	private ProsodicSentenceData sentence;
	private TIntList wordIndices;
	private int currentWord;

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		// TODO Auto-generated method stub
		return sentence!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public Object getPresentedData() {
		return sentence;
	}

	private JPanel createContentPanel() {
		//FIXME
		return new JPanel();
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			contentPanel = createContentPanel();
		}
		return contentPanel;
	}

}
