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

import java.util.ArrayList;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigRegistry.EntryType;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.io.IOUtil;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.jgraph.JGraphPreferences;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.details.ProsodySentenceDetailPresenter;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.GridStyle;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntECurve;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.helper.ProsodyListCellRenderer;
import de.ims.icarus.plugins.prosody.ui.view.PreviewSize;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.annotation.HighlightType;
import de.ims.icarus.util.strings.pattern.PatternFactory;
import de.ims.icarus.util.strings.pattern.TextSource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyPreferences {

	private static String escapePattern(Object pattern) {
		String s = null;
		if(pattern instanceof String) {
			s = (String) pattern;
		} else if(pattern instanceof TextSource) {
			s = ((TextSource)pattern).getExternalForm();
		}

		return s==null ? "" : PatternFactory.escape(s); //$NON-NLS-1$
	}

	public ProsodyPreferences() {
		ConfigBuilder builder = new ConfigBuilder();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// DEFAULT GROUP
		builder.addGroup("prosody", true); //$NON-NLS-1$
		// DEPENDENCY GRAPH GROUP
		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
		builder.addBooleanEntry("showLemma", true); //$NON-NLS-1$
		builder.addBooleanEntry("showFeatures", true); //$NON-NLS-1$
		builder.addBooleanEntry("showForm", true); //$NON-NLS-1$
		builder.addBooleanEntry("showPos", true); //$NON-NLS-1$
		builder.addBooleanEntry("showRelation", true); //$NON-NLS-1$
		builder.addBooleanEntry("showDirection", false); //$NON-NLS-1$
		builder.addBooleanEntry("showDistance", false); //$NON-NLS-1$
		builder.addBooleanEntry("markRoot", true); //$NON-NLS-1$
		builder.addBooleanEntry("markNonProjective", false); //$NON-NLS-1$
		JGraphPreferences.buildDefaultGraphConfig(builder, null);
		builder.reset();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// COREF GROUP
		builder.addGroup("prosody", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$

		// OUTLINE GROUP
		builder.addGroup("outline", true); //$NON-NLS-1$
		defaultBuildOutlineGroup(builder, null);
		// END OUTLINE GROUP
		builder.back();

		// DETAIL GROUP
		builder.addGroup("details", true); //$NON-NLS-1$
		builder.addBooleanEntry("showConstraints", ProsodySentenceDetailPresenter.WordPanelConfig.DEFAULT_SHOW_CONSTRAINTS); //$NON-NLS-1$
		builder.addColorEntry("constraintColor", ProsodySentenceDetailPresenter.WordPanelConfig.DEFAULT_CONSTRAINT_COLOR); //$NON-NLS-1$
		defaultBuildOutlineGroup(builder, new Options("sentencePattern", ProsodySentenceDetailPresenter.DEFAULT_SENTENCE_PATTERN)); //$NON-NLS-1$
		// END DETAIL GROUP
		builder.back();

		// PAINTE EDITOR GROUP
		builder.addGroup("painteEditor", true); //$NON-NLS-1$
		builder.addIntegerEntry("leftSyllableExtent", 1, 0, 10); //$NON-NLS-1$
		builder.addIntegerEntry("rightSyllableExtent", 2, 1, 10); //$NON-NLS-1$
		builder.addIntegerEntry("graphHeight", 200, 50, 400); //$NON-NLS-1$
		builder.addIntegerEntry("graphWidth", 300, 100, 500); //$NON-NLS-1$
		builder.addBooleanEntry("paintGrid", PaIntEGraph.DEFAULT_PAINT_GRID); //$NON-NLS-1$
		builder.addColorEntry("gridColor", PaIntEGraph.DEFAULT_GRID_COLOR); //$NON-NLS-1$
		builder.addOptionsEntry("gridStyle", PaIntEGraph.DEFAULT_GRID_STYLE.ordinal(), //$NON-NLS-1$
				(Object[])GridStyle.values());

		// PARAMETER BOUNDS SUBGROUPS
		builder.addGroup("a1Bounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", -40.0, -1000.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 40.0, 0.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("default", 4.4, -1000.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.back();
		builder.addGroup("a2Bounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", -40.0, -1000.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 40.0, 0.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("default", 6.72, -1000.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.back();
		builder.addGroup("bBounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", -3.0, -5.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 3.0, 0.0, 5.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("default", 0.57, -5.0, 5.0, 0.01); //$NON-NLS-1$
		builder.back();
		builder.addGroup("c1Bounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", 0.0, 0.0, 100.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 150.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("default", 133.35, 0.0, 300.0, 1.0); //$NON-NLS-1$
		builder.back();
		builder.addGroup("c2Bounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", 0.0, 0.0, 100.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 150.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("default", 66.30, 0.0, 300.0, 1.0); //$NON-NLS-1$
		builder.back();
		builder.addGroup("dBounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", 50.0, 0.0, 100.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 200.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("default", 189.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.back();
		builder.addGroup("alignmentBounds", true); //$NON-NLS-1$
		builder.virtual();
		builder.addDoubleEntry("lower", 0.0, 0.0, 5.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("upper", 10.0, 5.0, 20.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("default", PaIntEParams.DEFAULT_ALIGNMENT, 0.0, 20.0, 0.01); //$NON-NLS-1$
		builder.back();
		// END PARAMETER BOUNDS SUBGROUPS

		// END PAINTE EDITOR GROUP
		builder.back();

		// SEARCH GROUP
		builder.addGroup("search", true); //$NON-NLS-1$

		// LIST GROUP
		builder.addGroup("listOutline", true); //$NON-NLS-1$

		builder.addStringEntry("headerPattern", escapePattern(ProsodyListCellRenderer.DEFAULT_HEADER_PATTERN)); //$NON-NLS-1$
		builder.addBooleanEntry("showCurvePreview", true); //$NON-NLS-1$

		// END LIST GROUP
		builder.back();

		// RESULT GROUP
		builder.addGroup("resultOutline", true); //$NON-NLS-1$
		builder.setProperties(
				builder.addOptionsEntry("defaultSentencePresenter", 0,  //$NON-NLS-1$
						collectPresenterExtensions()),
				ConfigConstants.RENDERER, new ExtensionListCellRenderer());

		// END RESULT GROUP
		builder.back();

		// END SEARCH GROUP
		builder.back();

		// END APPEARANCE GROUP
		builder.back();

		// AUDIO PLAYER GROUP
		builder.addGroup("audioPlayer", true); //$NON-NLS-1$

		builder.setProperties(builder.addListEntry("folders", EntryType.STRING), //$NON-NLS-1$
				ConfigConstants.HANDLER, new ConfigUtils.FileHandler(false, true));
		builder.addBooleanEntry("includeSubFolders", false); //$NON-NLS-1$

		// END AUDIO PLAYER GROUP
		builder.back();

		// PROSODY READER GROUP
		builder.addGroup("prosodyReader", true); //$NON-NLS-1$

		builder.addBooleanEntry("syllableOffsetsFromSampa", true); //$NON-NLS-1$
		builder.addBooleanEntry("markAccentOnWords", true); //$NON-NLS-1$
		builder.addIntegerEntry("accentExcursion", 50, 10, 150); //$NON-NLS-1$
		builder.addBooleanEntry("onlyConsiderStressedSylables", false); //$NON-NLS-1$

		// END PROSODY READER GROUP
		builder.back();

		// FESTIVAL READER GROUP
		builder.addGroup("festivalReader", true); //$NON-NLS-1$

		builder.addBooleanEntry("syllableOffsetsFromSampa", true); //$NON-NLS-1$
		builder.addBooleanEntry("markAccentOnWords", true); //$NON-NLS-1$
		builder.addIntegerEntry("accentExcursion", 50, 10, 150); //$NON-NLS-1$
		builder.addBooleanEntry("onlyConsiderStressedSylables", false); //$NON-NLS-1$

		// END FESTIVAL READER GROUP
		builder.back();

		// SEARCH GROUP
		builder.addGroup("search", true); //$NON-NLS-1$
		builder.addBooleanEntry("allowCompactConstraints", true); //$NON-NLS-1$

		// ACCENT SHAPE SUBGROUP
		builder.addGroup("accentShape", true); //$NON-NLS-1$
		builder.virtual();

		// Minimum difference between c values
		builder.addIntegerEntry("delta", 10, 0, 50); //$NON-NLS-1$
		// => Minimum values for c1 and c2
		builder.addIntegerEntry("excursion", 30, 10, 150); //$NON-NLS-1$
		builder.addDoubleEntry("minBRise", 1.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("maxBRise", 2.0, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("minBFall", 0.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("maxBFall", 1.1, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("minBRiseFall", 0.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("maxBRiseFall", 2.0, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addBooleanEntry("ignoreUnstressedSyllables", true); //$NON-NLS-1$

		// END ACCENT SHAPE SUBGROUP
		builder.back();

		// PAINTE DISTANCE SUBGROUP
		builder.addGroup("painteDistance", true); //$NON-NLS-1$
		builder.virtual();

		builder.addBooleanEntry("normalize", false); //$NON-NLS-1$
		builder.addBooleanEntry("ignoreUnstressedSyllables", true); //$NON-NLS-1$

		// END PAINTE DISTANCE SUBGROUP
		builder.back();

		// PAINTE INTEGRAL SUBGROUP
		builder.addGroup("painteIntegral", true); //$NON-NLS-1$
		builder.virtual();

		builder.addDoubleEntry("leftBorder", -1.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("rightBorder", 2.0, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addBooleanEntry("ignoreUnstressedSyllables", true); //$NON-NLS-1$

		// END PAINTE INTEGRAL SUBGROUP
		builder.back();

		// PAINTE CURVE SUBGROUP
		builder.addGroup("painteCurve", true); //$NON-NLS-1$
		builder.virtual();

		builder.addDoubleEntry("leftBorder", -1.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("rightBorder", 2.0, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addIntegerEntry("resolution", 100, 20, 300, 1); //$NON-NLS-1$
		builder.addBooleanEntry("ignoreUnstressedSyllables", true); //$NON-NLS-1$

		// END PAINTE CURVE SUBGROUP
		builder.back();

		// PAINTE CHANNEL SUBGROUP
		builder.addGroup("painteChannel", true); //$NON-NLS-1$
		builder.virtual();

		builder.addDoubleEntry("leftBorder", -1.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("rightBorder", 2.0, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addIntegerEntry("resolution", 100, 20, 300, 1); //$NON-NLS-1$
		builder.addBooleanEntry("ignoreUnstressedSyllables", true); //$NON-NLS-1$

		// END PAINTE CHANNEL SUBGROUP
		builder.back();

		// PAINTE ANGLE SUBGROUP
		builder.addGroup("painteAngle", true); //$NON-NLS-1$
		builder.virtual();

		builder.addBooleanEntry("useA1", true); //$NON-NLS-1$
		builder.addBooleanEntry("useA2", true); //$NON-NLS-1$
		builder.addBooleanEntry("useB", true); //$NON-NLS-1$
		builder.addBooleanEntry("useC1", true); //$NON-NLS-1$
		builder.addBooleanEntry("useC2", true); //$NON-NLS-1$
		builder.addBooleanEntry("useD", true); //$NON-NLS-1$
		builder.addBooleanEntry("useAlignment", false); //$NON-NLS-1$

		// END PAINTE ANGLE SUBGROUP
		builder.back();

		// SYLLABLE DIFFERENCE SUBGROUP
		builder.addGroup("sylDif", true); //$NON-NLS-1$
		builder.virtual();

		builder.addBooleanEntry("ignoreUnstressedSyllables", true); //$NON-NLS-1$
		builder.addDoubleEntry("minB", 0.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("maxB", 2.0, -2.0, 3.0, 0.1); //$NON-NLS-1$

		// END SYLLABLE DIFFERENCE SUBGROUP
		builder.back();

		// END SEARCH GROUP
		builder.back();

		// SAMPA VALIDATION GROUP
		builder.addGroup("sampaValidation", true); //$NON-NLS-1$

		builder.addBooleanEntry("useExternalSampaTable", false); //$NON-NLS-1$
		builder.addEntry("sampaTableFile", EntryType.FILE, ""); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addBooleanEntry("pairFilesByName", true); //$NON-NLS-1$
		builder.addBooleanEntry("verboseOutput", false); //$NON-NLS-1$
		builder.addDoubleEntry("minSyllableCoverage", 0.5, 0.1, 0.9, 0.1); //$NON-NLS-1$
		builder.addBooleanEntry("decodeEscapedCharacters", true); //$NON-NLS-1$
		builder.addStringEntry("wordFilesEncoding", IOUtil.UTF8_ENCODING); //$NON-NLS-1$
		builder.addStringEntry("syllableFilesEncoding", IOUtil.UTF8_ENCODING); //$NON-NLS-1$

		// END SAMPA VALIDATION GROUP
		builder.back();

		// HIGHLIGHTING GROUP
		builder.addGroup("highlighting", true); //$NON-NLS-1$
//		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
//		builder.addBooleanEntry("showCorpusIndex", false); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("highlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, new TooltipListCellRenderer());
		builder.setProperties(builder.addOptionsEntry("groupHighlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, new TooltipListCellRenderer());
		builder.addBooleanEntry("markMultipleAnnotations", true); //$NON-NLS-1$
		builder.addColorEntry("nodeHighlight", ProsodyHighlighting.getInstance().getNodeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("edgeHighlight", ProsodyHighlighting.getInstance().getEdgeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("transitiveHighlight", ProsodyHighlighting.getInstance().getTransitiveHighlightColor().getRGB()); //$NON-NLS-1$
		for(String token : ProsodyHighlighting.getInstance().getTokens()) {
			builder.addColorEntry(token+"Highlight", ProsodyHighlighting.getInstance().getHighlightColor(token).getRGB()); //$NON-NLS-1$
		}
		builder.back();
		// END HIGHLIGHTING GROUP

		ProsodyHighlighting.getInstance().loadConfig();
	}

	private Object[] collectPresenterExtensions() {
		java.util.List<Object> items = new ArrayList<>();

		for(Extension extension : ProsodyPlugin.getProsodySentencePresenterExtensions()) {
			items.add(extension.getUniqueId());
		}

		return items.toArray();
	}

	private void defaultBuildOutlineGroup(ConfigBuilder builder, Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}

		builder.addOptionsEntry("antiAliasingType", PanelConfig.DEFAULT_ANTIALIASING_TYPE.ordinal(), //$NON-NLS-1$
				(Object[])AntiAliasingType.values());
		builder.addBooleanEntry("mouseWheelScrollSupported", PanelConfig.DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED); //$NON-NLS-1$
		builder.addColorEntry("backgroundColor", PanelConfig.DEFAULT_BACKGROUND_COLOR); //$NON-NLS-1$N-NLS-1$
		builder.addBooleanEntry("loopSound", PanelConfig.DEFAULT_LOOP_SOUND); //$NON-NLS-1$
		builder.addColorEntry("wordAlignmentColor", PanelConfig.DEFAULT_WORD_ALIGNMENT_COLOR); //$NON-NLS-1$N-NLS-1$
		builder.addColorEntry("syllableAlignmentColor", PanelConfig.DEFAULT_SYLLABLE_ALIGNMENT_COLOR); //$NON-NLS-1$

		// TEXT GROUP
		builder.addGroup("text", true); //$NON-NLS-1$

		builder.addStringEntry("sentencePattern", escapePattern(options.getOptional("sentencePattern", PanelConfig.DEFAULT_SENTENCE_PATTERN))); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addStringEntry("headerPattern", escapePattern(options.getOptional("headerPattern", escapePattern(PanelConfig.DEFAULT_HEADER_PATTERN)))); //$NON-NLS-1$ //$NON-NLS-2$
		builder.addBooleanEntry("showAlignment", PanelConfig.DEFAULT_TEXT_SHOW_ALIGNMENT); //$NO //$NON-NLS-1$
		// FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		builder.virtual();
		ConfigUtils.buildDefaultFontConfig(builder,
				TextArea.DEFAULT_FONT.getName(),
				TextArea.DEFAULT_FONT.getSize(),
				TextArea.DEFAULT_TEXT_COLOR);
		// END FONT SUBGROUP
		builder.back();

		// END TEXT GROUP
		builder.back();

		if(!options.getBoolean("hidePreview")) { //$NON-NLS-1$
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
		}

		// DETAIL GROUP
		builder.addGroup("detail", true); //$NON-NLS-1$

		builder.addIntegerEntry("wordScope", PanelConfig.DEFAULT_WORD_SCOPE, 0, 5); //$NON-NLS-1$
		builder.addIntegerEntry("leftSyllableExtent", 1, 0, 10); //$NON-NLS-1$
		builder.addIntegerEntry("rightSyllableExtent", 2, 1, 10); //$NON-NLS-1$
		builder.addIntegerEntry("graphHeight", PanelConfig.DEFAULT_GRAPH_HEIGHT, 50, 400); //$NON-NLS-1$
		builder.addIntegerEntry("graphWidth", PanelConfig.DEFAULT_GRAPH_WIDTH, 100, 500); //$NON-NLS-1$
		builder.addIntegerEntry("wordSpacing", PanelConfig.DEFAULT_WORD_SPACING, 0, 50); //$NON-NLS-1$
		builder.addIntegerEntry("graphSpacing", PanelConfig.DEFAULT_GRAPH_SPACING, 0, 50); //$NON-NLS-1$
		builder.addBooleanEntry("paintCompact", PaIntECurve.DEFAULT_PAINT_COMPACT); //$NON-NLS-1$
		builder.addBooleanEntry("clearLabelBackground", PanelConfig.DEFAULT_CLEAR_LABEL_BACKGROUND); //$NON-NLS-1$
		builder.addStringEntry("detailPattern", escapePattern(options.getOptional("detailPattern", PanelConfig.DEFAULT_DETAIL_PATTERN))); //$NON-NLS-1$ //$NON-NLS-2$
		// FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		builder.virtual();
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
		builder.virtual();
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
	}

}
