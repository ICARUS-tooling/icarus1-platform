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
package de.ims.icarus.plugins.prosody;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.plugins.prosody.params.PaIntEParams;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.GridStyle;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.view.PreviewSize;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyPreferences {

	private static String escapePattern(LabelPattern pattern) {
		String s = pattern.getPattern();
		return s==null ? "" : LabelPattern.escapePattern(s); //$NON-NLS-1$
	}

	public ProsodyPreferences() {
		ConfigBuilder builder = new ConfigBuilder();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// COREF GROUP
		builder.addGroup("prosody", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// OUTLINE GROUP
		builder.addGroup("outline", true); //$NON-NLS-1$

		builder.addOptionsEntry("antiAliasingType", PanelConfig.DEFAULT_ANTIALIASING_TYPE.ordinal(), //$NON-NLS-1$
				(Object[])AntiAliasingType.values());
		builder.addBooleanEntry("mouseWheelScrollSupported", PanelConfig.DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED); //$NON-NLS-1$
		builder.addBooleanEntry("loopSound", PanelConfig.DEFAULT_LOOP_SOUND); //$NON-NLS-1$
		builder.addColorEntry("wordAlignmentColor", PanelConfig.DEFAULT_WORD_ALIGNMENT_COLOR); //$NON-NLS-1$N-NLS-1$
		builder.addColorEntry("syllableAlignmentColor", PanelConfig.DEFAULT_SYLLABLE_ALIGNMENT_COLOR); //$NON-NLS-1$

		// TEXT GROUP
		builder.addGroup("text", true); //$NON-NLS-1$

		builder.addStringEntry("sentencePattern", escapePattern(PanelConfig.DEFAULT_SENTENCE_PATTERN)); //$NON-NLS-1$
		builder.addStringEntry("headerPattern", escapePattern(PanelConfig.DEFAULT_HEADER_PATTERN)); //$NON-NLS-1$
		builder.addBooleanEntry("showAlignment", PanelConfig.DEFAULT_TEXT_SHOW_ALIGNMENT); //$NO //$NON-NLS-1$
		// FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder,
				TextArea.DEFAULT_FONT.getName(),
				TextArea.DEFAULT_FONT.getSize(),
				TextArea.DEFAULT_TEXT_COLOR);
		// END FONT SUBGROUP
		builder.back();

		// END TEXT GROUP
		builder.back();

		// PREVIEW GROUP
		builder.addGroup("preview", true); //$NON-NLS-1$

		builder.addOptionsEntry("previewSize", PanelConfig.DEFAULT_PREVIEW_SIZE.ordinal(), //$NON-NLS-1$
				(Object[])PreviewSize.values());
		builder.addDoubleEntry("leftSyllableBound", PanelConfig.DEFAULT_LEFT_SYLLABLE_BOUND, -2D, 0D, 0.05); //$NON-NLS-1$
		builder.addDoubleEntry("rightSyllableBound", PanelConfig.DEFAULT_RIGHT_SYLLABLE_BOUND, 0D, 2D, 0.05); //$NON-NLS-1$
		builder.addColorEntry("curveColor", PanelConfig.DEFAULT_CURVE_COLOR); //$NON-NLS-1$
		builder.addBooleanEntry("showAlignment", PanelConfig.DEFAULT_PREVIEW_SHOW_ALIGNMENT); //$NO //$NON-NLS-1$

		// END PREVIEW GROUP
		builder.back();

		// DETAIL GROUP
		builder.addGroup("detail", true); //$NON-NLS-1$

		builder.addIntegerEntry("wordScope", PanelConfig.DEFAULT_WORD_SCOPE, 0, 5); //$NON-NLS-1$
		builder.addIntegerEntry("syllableScope", PanelConfig.DEFAULT_SYLLABLE_SCOPE, 1, 10); //$NON-NLS-1$
		builder.addIntegerEntry("graphHeight", PanelConfig.DEFAULT_GRAPH_HEIGHT, 50, 400); //$NON-NLS-1$
		builder.addIntegerEntry("graphWidth", PanelConfig.DEFAULT_GRAPH_WIDTH, 100, 500); //$NON-NLS-1$
		builder.addIntegerEntry("wordSpacing", PanelConfig.DEFAULT_WORD_SPACING, 0, 50); //$NON-NLS-1$
		builder.addIntegerEntry("graphSpacing", PanelConfig.DEFAULT_GRAPH_SPACING, 0, 50); //$NON-NLS-1$
		builder.addBooleanEntry("clearLabelBackground", PanelConfig.DEFAULT_CLEAR_LABEL_BACKGROUND); //$NON-NLS-1$
		builder.addStringEntry("detailPattern", escapePattern(PanelConfig.DEFAULT_DETAIL_PATTERN)); //$NON-NLS-1$
		// FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder,
				TextArea.DEFAULT_FONT.getName(),
				TextArea.DEFAULT_FONT.getSize(),
				TextArea.DEFAULT_TEXT_COLOR);
		// END FONT SUBGROUP
		builder.back();
		builder.addColorEntry("axisColor", Axis.DEFAULT_AXIS_COLOR); //$NON-NLS-1$
		builder.addColorEntry("axisMarkerColor", Axis.DEFAULT_MARKER_COLOR); //$NON-NLS-1$
		builder.addIntegerEntry("axisMarkerHeight", Axis.DEFAULT_MARKER_HEIGHT, 3, 10); //$NON-NLS-1$
		// AXIS FONT SUBGROUP
		builder.addGroup("axisFont", true); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder,
				Axis.DEFAULT_FONT.getName(),
				Axis.DEFAULT_FONT.getSize(),
				Axis.DEFAULT_LABEL_COLOR);
		// END AXIS FONT SUBGROUP
		builder.back();
		builder.addBooleanEntry("paintBorder", PaIntEGraph.DEFAULT_PAINT_BORDER); //$NON-NLS-1$
		builder.addColorEntry("borderColor", PaIntEGraph.DEFAULT_BORDER_COLOR); //$NON-NLS-1$
		builder.addBooleanEntry("paintGrid", PaIntEGraph.DEFAULT_PAINT_GRID); //$NON-NLS-1$
		builder.addColorEntry("gridColor", PaIntEGraph.DEFAULT_GRID_COLOR); //$NON-NLS-1$
		builder.addOptionsEntry("gridStyle", PaIntEGraph.DEFAULT_GRID_STYLE.ordinal(), //$NON-NLS-1$
				(Object[])GridStyle.values());

		// END DETAIL GROUP
		builder.back();
		// END OUTLINE GROUP
		builder.back();

		// PAINTE EDITOR GROUP
		builder.addGroup("painteEditor", true); //$NON-NLS-1$

		//TODO add missing bounds below!!!

		builder.addDoubleEntry("a1LowerBound", -20.0, -1000.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a1UpperBound", 20.0, 0.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a1Default", 4.4, -1000.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a2LowerBound", -20.0); //$NON-NLS-1$
		builder.addDoubleEntry("a2UpperBound", 20.0); //$NON-NLS-1$
		builder.addDoubleEntry("a2Default", 6.72); //$NON-NLS-1$
		builder.addDoubleEntry("bLowerBound", -3.0); //$NON-NLS-1$
		builder.addDoubleEntry("bUpperBound", 3.0); //$NON-NLS-1$
		builder.addDoubleEntry("bDefault", 0.57); //$NON-NLS-1$
		builder.addDoubleEntry("c1LowerBound", 0.0); //$NON-NLS-1$
		builder.addDoubleEntry("c1UpperBound", 150.0); //$NON-NLS-1$
		builder.addDoubleEntry("c1Default", 133.35); //$NON-NLS-1$
		builder.addDoubleEntry("c2LowerBound", 0.0); //$NON-NLS-1$
		builder.addDoubleEntry("c2UpperBound", 150.0); //$NON-NLS-1$
		builder.addDoubleEntry("c2Default", 66.30); //$NON-NLS-1$
		builder.addDoubleEntry("dLowerBound", 50.0); //$NON-NLS-1$
		builder.addDoubleEntry("dUpperBound", 200.0); //$NON-NLS-1$
		builder.addDoubleEntry("dDefault", 189.0); //$NON-NLS-1$
		builder.addDoubleEntry("alignmentLowerBound", 0.0); //$NON-NLS-1$
		builder.addDoubleEntry("alignmentUpperBound", 10.0); //$NON-NLS-1$
		builder.addDoubleEntry("alignmentDefault", PaIntEParams.DEFAULT_ALIGNMENT); //$NON-NLS-1$

		// END PAINTE EDITOR GROUP
		builder.back();
	}

}
