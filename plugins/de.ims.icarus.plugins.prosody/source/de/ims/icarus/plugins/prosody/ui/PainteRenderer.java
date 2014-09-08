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
package de.ims.icarus.plugins.prosody.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.Axis.Integer;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEParams;
import de.ims.icarus.ui.UIUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PainteRenderer extends JComponent {

	private static final long serialVersionUID = -6643581977469190903L;

	// "Atomprogramm"
	private static String params = "7.08178|21.5023|13.9449|17.4747	6.19013|4.44748|3.84892|17.3455	0.179764|0.652504|-0.272743|0.627447	3.67927|5.15718|5.63528|2.65378	8.97916|12.5409|18.3659|2.34918	99.7662|95.7711|95.7415|80.7089"; //$NON-NLS-1$

	// Der
//	private static String params = "21.5724	12.0341	1.56951	13.0516	3.96587	168.683"; //$NON-NLS-1$
//	private static String params = "-1	28.6665	0.62857	0	1.25997	173.771"; //$NON-NLS-1$
//	private static String params = "3.19101	-1	1.75124	60.0858	0	182.7"; //$NON-NLS-1$
//	private static String params = "27.5209	2.93244	-0.716191	5.94515	44.8107	202.196"; //$NON-NLS-1$

	// Druck
//	private static String params = "24.8617	5.53876	0.846195	76.012	69.7365	164.771"; //$NON-NLS-1$

	// Einschüchterung
//	private static String params = "-1|-1|6.18677|6.04351	2.13969|3.70559|11.1401|12.4721	-2.01372|-1.20651|1.5806|0.594606	0|0|8.43418|8.48017	97.3366|27.011|7.57803|7.05806	169.822|98.379|78.4055|78.4256"; //$NON-NLS-1$

	public static void main(String[] args) throws Exception {
		PainteRenderer renderer = new PainteRenderer();

//		renderer.add(24.8617F, 5.53876F, 0.846195F, 76.012F, 69.7365F, 164.771F);

//		parseAndAdd(renderer, params);
		loadFile(renderer);
		renderer.calc(-2.0, +2.0, 100);

//		renderer.dump();
		renderer.dumpFormula();

		JScrollPane scrollPane = new JScrollPane(renderer);
		UIUtil.defaultSetUnitIncrement(scrollPane);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(scrollPane);
		frame.pack();
//		UIUtil.resizeComponent(frame, 800, 600);
		frame.setVisible(true);
	}

	private static void loadFile(PainteRenderer renderer) throws Exception {
		Path path = Paths.get("D:\\Tasks\\INF\\test-painte-daten.csv");

		try(BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
			String line = null;

			while((line = reader.readLine()) != null) {
				if(line.startsWith("\"")) {
					continue;
				}

				String[] items = line.split(",");

				float a1 = Float.parseFloat(items[0]);
				float a2 = Float.parseFloat(items[1]);
				float b = Float.parseFloat(items[2]);
				float c1 = Float.parseFloat(items[3]);
				float c2 = Float.parseFloat(items[4]);
				float d = Float.parseFloat(items[5]);

				String syl = items[9].replaceAll("\"", "");
				String word = items[10].replaceAll("\"", "");

				PainteSet s = renderer.add(a1, a2, b, c1, c2, d, syl, word);
			}
		}
	}

	private static void parseAndAdd(PainteRenderer renderer, String s) {
		String[] blocks = s.split("\\s+"); //$NON-NLS-1$
		if(blocks.length!=6)
			throw new IllegalArgumentException(s);

		String[] _a1 = blocks[0].split("\\|"); //$NON-NLS-1$
		String[] _a2 = blocks[1].split("\\|"); //$NON-NLS-1$
		String[] _b = blocks[2].split("\\|"); //$NON-NLS-1$
		String[] _c1 = blocks[3].split("\\|"); //$NON-NLS-1$
		String[] _c2 = blocks[4].split("\\|"); //$NON-NLS-1$
		String[] _d = blocks[5].split("\\|"); //$NON-NLS-1$

		for(int i=0; i<_a1.length; i++) {
			float a1 = Float.parseFloat(_a1[i]);
			float a2 = Float.parseFloat(_a2[i]);
			float b = Float.parseFloat(_b[i]);
			float c1 = Float.parseFloat(_c1[i]);
			float c2 = Float.parseFloat(_c2[i]);
			float d = Float.parseFloat(_d[i]);

			renderer.add(a1, a2, b, c1, c2, d);
		}
	}

	private final List<PainteSet> data = new ArrayList<>();

	PainteRenderer() {

	}

	public PainteSet add(float a1, float a2, float b, float c1, float c2, float d, String syl, String word) {
		PainteSet s = add(a1, a2, b, c1, c2, d);
		s.wordName = word;
		s.sylableName = syl;
		return s;
	}

	public PainteSet add(float a1, float a2, float b, float c1, float c2, float d) {
		PainteSet s = new PainteSet();
		s.a1 = a1;
		s.a2 = a2;
		s.b = b;
		s.c1 = c1;
		s.c2 = c2;
		s.d = d;

		data.add(s);

		return s;
	}

	public void calc(double min, double max, int resolution) {

		final double range = max-min;

		for(PainteSet s : data) {

			s.px = new double[resolution+1];
			s.py = new double[resolution+1];

			for(int i=0; i<=resolution; i++) {
				s.px[i] = min + (range * i/resolution);
				s.py[i] = s.y(s.px[i]);
			}
		}
	}

	private static final int ITEMS_PER_ROW = 5;


	/**
	 * @see java.awt.Component#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(ITEMS_PER_ROW*DEFAULT_WIDTH, data.size()/ITEMS_PER_ROW * DEFAULT_HEIGHT);
	}

	/**
	 * @see java.awt.Component#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * @see java.awt.Component#getMaximumSize()
	 */
	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	private PaIntEParams curve = new PaIntEParams();
	private PaIntEGraph graph = new PaIntEGraph();

	private static final int D_MIN = 50;
	private static final int D_MAX = 200;

	private static final double MIN_X = -2D;
	private static final double MAX_X = +2D;

	public static final int DEFAULT_HEIGHT = 150;
	public static final int DEFAULT_WIDTH = 200;

	/**
	 * @see java.awt.Canvas#paint(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		if(data.isEmpty()) {
			return;
		}

		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		Rectangle area = new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);

		// Boundaries
		double maxD = 0;
		double minD = 1000;
		for(PainteSet s : data) {
			maxD = Math.max(maxD, s.d);
			minD = Math.min(minD, s.d-Math.max(s.c1, s.c2));
		}
//
//		curve.setMinY(minD-10);
//		curve.setMaxY(maxD);
//		curve.setMinX(MIN_X);
//		curve.setMaxX(MAX_X);

		Axis.Integer yAxis = (Integer) graph.getYAxis();
		yAxis.setMinValue((int) minD);
		yAxis.setMaxValue((int) maxD);

//		graph.setPaintGrid(true);
//		graph.setGridStyle(GridStyle.CROSSES);

		for(int i=0; i<data.size(); i++) {
			PainteSet s = data.get(i);

			if(i>0 && i%ITEMS_PER_ROW==0) {
				area.y += area.height;
				area.x = 0;
			}

			// Bounding box
//			g.draw(area);

			// Label
			if(s.sylableName!=null && s.wordName!=null) {

				String label = s.wordName+" "+s.sylableName;
				FontMetrics fm = g.getFontMetrics();
				int sw = fm.stringWidth(label);
				int sh = fm.getHeight();
				int x = area.x + area.width/2 - sw/2;
				int y = area.y + sh +8;

				g.drawString(label, x, y);
			}

			// Curve
			curve.setA1(s.a1);
			curve.setA2(s.a2);
			curve.setB(s.b);
			curve.setC1(s.c1);
			curve.setC2(s.c2);
			curve.setD(s.d);

			graph.paint(g, curve, area);

			area.x += area.width;
		}
	}

//	/**
//	 * @see java.awt.Canvas#paint(java.awt.Graphics)
//	 */
//	@Override
//	public void paint(Graphics graphics) {
//
//		if(data.isEmpty()) {
//			return;
//		}
//
//		Graphics2D g = (Graphics2D) graphics;
//		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//
//		Rectangle area = new Rectangle(0, 0, AREA_WIDTH, AREA_HEIGHT);
//
//		double maxD = 0;
//		for(PainteSet s : data) {
//			maxD = Math.max(maxD, s.d);
//		}
//
//		PainteSet example = data.get(0);
//		int resolution = example.px.length;
//		double rangeX = example.px[resolution-1]-example.px[0];
//		double rangeY = D_MAX-D_MIN;
//
//		System.out.println("range x: "+rangeX);
//		System.out.println("range y: "+rangeY);
//
//		double scaleX = AREA_WIDTH/rangeX;
//		double scaleY = AREA_HEIGHT/rangeY;
//
//		System.out.println("scale x: "+scaleX);
//		System.out.println("scale y: "+scaleY);
//
//		for(int i=0; i<data.size(); i++) {
//			PainteSet s = data.get(i);
//
//			if(i>0 && i%ITEMS_PER_ROW==0) {
//				area.y += area.height;
//				area.x = 0;
//			}
//
//			paintSet(g, s, area, scaleX, scaleY);
//			area.x += area.width;
//		}
//	}

	public void dumpFormula() {
		for(int i=0; i<data.size(); i++) {

			data.get(i).dumpFormula();
		}

	}

	public void dump() {
		for(int i=0; i<data.size(); i++) {
			System.out.println("------------------------");
			System.out.println("   SYLLABLE "+i);
			data.get(i).dump();
			System.out.println();
		}
	}

	private static class PainteSet {
		private float a1, a2, b, c1, c2, d;

		private String sylableName;
		private String wordName;

		// Suggested value: 3.6
		private final float g = 3.6F;

		private double[] px, py;

		public double y(double x) {
			return d - (c1/(1+Math.exp(-a1*(b-x)+g))) - (c2/(1+Math.exp(-a2*(x-b)+g)));
		}

		public void dump() {
			if(wordName!=null && sylableName!=null) {
				System.out.println(wordName+" ("+sylableName+")");
			}

			for(int i=0; i<px.length; i++) {
				System.out.printf("%1.03f / %1.03f\n", px[i], py[i]);
			}
		}

		public void dumpFormula() {
			StringBuilder sb = new StringBuilder();

			sb.append("f(x)=");
			sb.append(d);
			sb.append("-(").append(c1).append("/(1+e^(-").append(a1).append("*(").append(b).append("-x)+").append(g).append(")))");
			sb.append("-(").append(c2).append("/(1+e^(-").append(a2).append("*(x-").append(b).append(")+").append(g).append(")))");

			System.out.println(sb.toString());
		}
	}
}
