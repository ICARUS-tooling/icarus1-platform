package de.ims.icarus.plugins.errormining.ngram_tools;

import java.awt.Color;

import javax.swing.JSplitPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class JfreeTest{
public static void main(String arg[]){
  DefaultCategoryDataset dataset = new DefaultCategoryDataset();
  dataset.setValue(2, "Marks", "Rahul");
  dataset.setValue(7, "Marks", "Vinod");
  dataset.setValue(4, "Marks", "Deepak");
  dataset.setValue(9, "Marks", "Prashant");
  dataset.setValue(6, "Marks", "Chandan");
  JFreeChart chart = ChartFactory.createBarChart
  ("BarChart using JFreeChart","Student", "Marks", dataset, 
   PlotOrientation.VERTICAL, false,true, false);
  chart.setBackgroundPaint(Color.yellow);
  chart.getTitle().setPaint(Color.blue); 
  CategoryPlot p = chart.getCategoryPlot(); 
  p.setRangeGridlinePaint(Color.red); 

  ChartFrame frame1=new ChartFrame("Bar Chart",chart);
  frame1.setVisible(true);
  frame1.setSize(400,350);
  }
}
