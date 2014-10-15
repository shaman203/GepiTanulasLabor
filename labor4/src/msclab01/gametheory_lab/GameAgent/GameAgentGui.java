package msclab01.gametheory_lab.GameAgent;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import org.jfree.data.xy.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotOrientation;
import jade.core.AID;

import msclab01.gametheory_lab.PlayerType;

/**
 * The GUI of the GameAgent.
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
class GameAgentGui extends JFrame {

	private static final long serialVersionUID = 1L;

	/** Constant identifying a detailed GUI, where every players' individual utility can be traced in time
	 * (rounds) simultaneously. */
	public static final int GUI_PLAYERS_UTIL = 1;
	/** Constant identifying a GUI, where every type's ratio in the whole population can be traced in time
	 * (rounds) simultaneously. */
	public static final int GUI_TYPES_RATIO = 2;
	/** Constant identifying a GUI, where every type's number in the whole population can be traced in time
	 * (rounds) simultaneously. */
	public static final int GUI_TYPES_NUMBER = 3;
	/** Constant identifying a GUI, where every type's average utility, and the average utility of the whole
	 * population can be traced in time (rounds) simultaneously. */
	public static final int GUI_TYPES_AVGUTIL = 4;
	/** Constant identifying a GUI, where every type's maximal utility, and the average utility of the whole
	 * population can be traced in time (rounds) simultaneously. */
	public static final int GUI_TYPES_MAXUTIL = 5;
	/** Constant identifying a GUI, where every type's minimal utility, and the average utility of the whole
	 * population can be traced in time (rounds) simultaneously. */
	public static final int GUI_TYPES_MINUTIL = 6;
	
	/** The agent, to whom this whole GUI belongs. */
	private GameAgent myAgent;
	/** The data model beneath the JFreeChart. */
	protected XYSeriesCollection chdata;
	/** A HashMap for mapping agents to series. These series will make up the XYSeriesCollection data model finally. */
	protected HashMap<AID, XYSeries> chdataseries01;
	/** A HashMap for mapping playerTypes to series. These series will make up the XYSeriesCollection data model finally. */
	protected HashMap<Integer, XYSeries> chdataseries02;
	/** A HashMap for mapping playerTypes to series. These series will make up the XYSeriesCollection data model finally. */
	protected HashMap<Integer, XYSeries> chdataseries03;
	/** A HashMap for mapping playerTypes to series. These series will make up the XYSeriesCollection data model finally. */
	protected HashMap<Integer, XYSeries> chdataseries04;
	/** The widget to visualize the data in the data model. */
	protected JFreeChart chchart;

	/** The type of this GUI. */
	protected int guiType;
	
	/** JRadioButton group for changing the GUI */
	public ButtonGroup rgroup;
	
	/**
	 *  Construct the GUI of the given JADEX agent (player).
	 */
	GameAgentGui(GameAgent a, int guiType) {

		super(a.getLocalName());

		this.myAgent = a;
		this.guiType = guiType;
		
		// ------------------------------------ NORTH ------------------------------------------
		
        JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 1));
		
		rgroup = new ButtonGroup();
		
	    JRadioButton rb1 = new JRadioButton("Util");
	    JRadioButton rb2 = new JRadioButton("Rat");
	    JRadioButton rb3 = new JRadioButton("Num");
	    JRadioButton rb4 = new JRadioButton("Avg");
	    //JRadioButton rb5 = new JRadioButton("Max");
	    //JRadioButton rb6 = new JRadioButton("Min");

	    p.add(rb1);
	    rgroup.add(rb1);
	    p.add(rb2);
	    rgroup.add(rb2);
	    p.add(rb3);
	    rgroup.add(rb3);
	    p.add(rb4);
	    rgroup.add(rb4);
	    //p.add(rb5);
	    //rgroup.add(rb5);
	    //p.add(rb6);
	    //rgroup.add(rb6);
	    
	    switch (guiType) {

			case GUI_PLAYERS_UTIL:
				
				rb1.setSelected(true);
				
				break;
				
			case GUI_TYPES_RATIO:
				
				rb2.setSelected(true);
				
				break;
				
			case GUI_TYPES_NUMBER:
				
				rb3.setSelected(true);
				
				break;
				
			case GUI_TYPES_AVGUTIL:
				
				rb4.setSelected(true);
				
				break;
				
			default:
					
				rb4.setSelected(true);
				
				break;
			
	    } // END SWITCH

	    rb1.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
			        myAgent.renewGui(GameAgentGui.GUI_PLAYERS_UTIL);
			      }
			    });
	    
	    rb2.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  myAgent.renewGui(GameAgentGui.GUI_TYPES_RATIO);
			      }
			    });

	    rb3.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  myAgent.renewGui(GameAgentGui.GUI_TYPES_NUMBER);
			      }
			    });
	    
	    rb4.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  myAgent.renewGui(GameAgentGui.GUI_TYPES_AVGUTIL);
			      }
			    });
	    
	    /*rb5.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  myAgent.renewGui(GameAgentGui.GUI_TYPES_MAXUTIL);
			      }
			    });
	    
	    rb6.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  myAgent.renewGui(GameAgentGui.GUI_TYPES_MINUTIL);
			      }
			    });*/
	    
	    getContentPane().add(p, BorderLayout.NORTH);
	    
	    // ------------------------------------ CENTER ------------------------------------------
	    
		// Initialize data model (behind the chart diagram)
		chdata			= new XYSeriesCollection();

		// Chart diagram object
		ChartPanel chartPanel;
		
		switch (this.guiType) {

			case GUI_PLAYERS_UTIL:

				chdataseries01	= new HashMap<AID, XYSeries>();

				// Create the chart diagram object, and put it to a panel on the content pane
				chchart		= createChart(this.guiType, chdata);	
		        chartPanel	= new ChartPanel(	chchart,
		        											800,	// int width,
		        											600,	// int height,
		        											320,	// int minimumDrawWidth,
		        											240,	// int minimumDrawHeight,
		        											320,	// int maximumDrawWidth,
		        											240,	// int maximumDrawHeight,
		        											true,	// boolean useBuffer,
		        											false,	// boolean properties,
		        											true,	// boolean save,
		        											false,	// boolean print,
		        											false,	// boolean zoom,
		        											false);	// boolean tooltips);

		        chartPanel.setBorder(BorderFactory.createTitledBorder("Utility of Players"));
		        //chartPanel.setPreferredSize(new Dimension(800, 600));

		        getContentPane().add(chartPanel, BorderLayout.CENTER);

		        break;

			case GUI_TYPES_RATIO:

				chdataseries02	= new HashMap<Integer, XYSeries>();

				// Create the chart diagram object, and put it to a panel on the content pane
				chchart		= createChart(this.guiType, chdata);	
		        chartPanel	= new ChartPanel(	chchart,
		        											800,	// int width,
		        											600,	// int height,
		        											320,	// int minimumDrawWidth,
		        											240,	// int minimumDrawHeight,
		        											320,	// int maximumDrawWidth,
		        											240,	// int maximumDrawHeight,
		        											true,	// boolean useBuffer,
		        											false,	// boolean properties,
		        											true,	// boolean save,
		        											false,	// boolean print,
		        											false,	// boolean zoom,
		        											false);	// boolean tooltips);

		        chartPanel.setBorder(BorderFactory.createTitledBorder("Ratio of player types"));
		        //chartPanel.setPreferredSize(new Dimension(800, 600));

		        getContentPane().add(chartPanel, BorderLayout.CENTER);

		        break;
		        
			case GUI_TYPES_NUMBER:

				chdataseries03	= new HashMap<Integer, XYSeries>();

				// Create the chart diagram object, and put it to a panel on the content pane
				chchart		= createChart(this.guiType, chdata);	
		        chartPanel	= new ChartPanel(	chchart,
		        											800,	// int width,
		        											600,	// int height,
		        											320,	// int minimumDrawWidth,
		        											240,	// int minimumDrawHeight,
		        											320,	// int maximumDrawWidth,
		        											240,	// int maximumDrawHeight,
		        											true,	// boolean useBuffer,
		        											false,	// boolean properties,
		        											true,	// boolean save,
		        											false,	// boolean print,
		        											false,	// boolean zoom,
		        											false);	// boolean tooltips);

		        chartPanel.setBorder(BorderFactory.createTitledBorder("Quantity of player types"));
		        //chartPanel.setPreferredSize(new Dimension(800, 600));

		        getContentPane().add(chartPanel, BorderLayout.CENTER);

		        break;

			case GUI_TYPES_AVGUTIL:

				chdataseries04	= new HashMap<Integer, XYSeries>();

				// Create the chart diagram object, and put it to a panel on the content pane
				chchart		= createChart(this.guiType, chdata);	
		        chartPanel	= new ChartPanel(	chchart,
		        											800,	// int width,
		        											600,	// int height,
		        											320,	// int minimumDrawWidth,
		        											240,	// int minimumDrawHeight,
		        											320,	// int maximumDrawWidth,
		        											240,	// int maximumDrawHeight,
		        											true,	// boolean useBuffer,
		        											false,	// boolean properties,
		        											true,	// boolean save,
		        											false,	// boolean print,
		        											false,	// boolean zoom,
		        											false);	// boolean tooltips);

		        chartPanel.setBorder(BorderFactory.createTitledBorder("Average utility of player types"));
		        //chartPanel.setPreferredSize(new Dimension(800, 600));

		        getContentPane().add(chartPanel, BorderLayout.CENTER);

		        break;
		        
			default:	// case GUI_TYPES_AVGUTIL or else...

				chdataseries01	= new HashMap<AID, XYSeries>();

				// Create the chart diagram object, and put it to a panel on the content pane
				chchart		= createChart(this.guiType, chdata);	
		        chartPanel	= new ChartPanel(	chchart,
		        											800,	// int width,
		        											600,	// int height,
		        											320,	// int minimumDrawWidth,
		        											240,	// int minimumDrawHeight,
		        											320,	// int maximumDrawWidth,
		        											240,	// int maximumDrawHeight,
		        											true,	// boolean useBuffer,
		        											false,	// boolean properties,
		        											true,	// boolean save,
		        											false,	// boolean print,
		        											false,	// boolean zoom,
		        											false);	// boolean tooltips);

		        chartPanel.setBorder(BorderFactory.createTitledBorder("Utility of Players"));
		        //chartPanel.setPreferredSize(new Dimension(800, 600));

		        getContentPane().add(chartPanel, BorderLayout.CENTER);

		        break;
		        
		} // SWITCH BY GUITYPE

		// ------------------------------------ SOUTH ------------------------------------------
		
		// JPanel holding PLAY/RESTART, PAUSE, STOP buttons...
		
		// -------------------------------------------------------------------------------------
		
		
		// Add a WindowListener to this Window to listen to windowClosing events.
		// When such an event occurs, the corresponding Agent should be terminated.
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				myAgent.doDelete();

			} // End of overridden WindowAdapter.windowClosing() method

		}); // End of inner anonym WindowAdapter class

		setResizable(false);

	} // End of GameAgentGui.GameAgentGui(GameAgent) constructor

	/**
	 *  Position the GUI to the middle of the screen and make it visible (show it).
	 */
	public void show() {

		pack();
		Dimension screenSize	= Toolkit.getDefaultToolkit().getScreenSize();
		int centerX				= (int)screenSize.getWidth() / 2;
		int centerY				= (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.show();

	} // End of GameAgentGui.show() method

	/**
	 *  Add the aggregated utility values of all the agents in the given round to the chart.
	 *  
	 *  Comment: let's be careful with these final input variables! They are created in a different
	 *  thread (the GameAgent's thread). They hold a reference to objects, and the state of these objects
	 *  may be changed by operations on them, but the variables will always refer to the same objects!!
	 *  This means, that if we manipulate these objects here, in this thread, then the effect will be
	 *  instantly felt by the other thread. For example: we remove an element from this playersUtility
	 *  HashMap here, in this updateChart method, in this thread, while at the same time the other thread
	 *  (e.g. GameAgent's), which gave us the reference to playersUtility, tries to iterate over it. This
	 *  would result in a nice little java.util.ConcurrentModificationException... So be careful!! Final
	 *  variables - even though their value will surely not change, and this can help avoid programming
	 *  errors - can lead to errors in a multi-threaded environment (like ours).
	 *  One solution would be to make all the methods which can access/modify given objects from different
	 *  threads, synchronized. Nonetheless this wouldn't be good here, because we want playersUtility
	 *  here to be independent of anything else. This playersUtility variable is passed to us from
	 *  the GameAgent's thread, by the ExecutePlays behaviour. But there is an other behaviour in that
	 *  thread, called Search4Players, which at some point iterates over this object. So the execution
	 *  of GameAgent's thread's Search4Players, and this thread's updateChart may interleave. To solve
	 *  the issue, we have to make a copy of playersUtility here, and manipulate that copy. Easy... :-)
	 *  For more info look at the following: http://java.sun.com/docs/books/tutorial/essential/concurrency
	 *  
	 *  @param round The number of the round.
	 *  @param playersUtility The HashMap having the utility values of the agents in the given round.
	 */
	public void updateChart(final int guiType, final String round, final HashMap<AID, Double> playersUtility, final HashMap<AID, Integer> playersType) {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

        		AID a;		// An agent to map...
        		Integer t;	// A type to map...
        		
        		// -------------------------------------------------------------------
          		// Let's make a copy of playersUtility for Thread-Safety reasons!!!...
        		HashMap<AID, Double> playersUtility_copy = new HashMap<AID, Double>();
        		Iterator<AID> pu = playersUtility.keySet().iterator();
    			
        		// Do the copying............
        		while(pu.hasNext()) {

    				a = pu.next();	// Is ConcurrentModificationException possible here?...
    				playersUtility_copy.put(a, playersUtility.get(a));

    			} // WHILE HAS NEXT
				
        		Iterator<AID> puc;
        		
				switch (guiType) {

					case GUI_PLAYERS_UTIL:

		          		// Let's make a copy of chdataseries01 for Thread-Safety reasons!!!...
		        		HashMap<AID, XYSeries> chdataseries01_copy = new HashMap<AID, XYSeries>();
		        		Iterator<AID> cd = chdataseries01.keySet().iterator();
		    			
		        		// Do the copying............
		        		while(cd.hasNext()) {
		
		    				a = cd.next();
		    				chdataseries01_copy.put(a, chdataseries01.get(a));
		
		    			} // WHILE HAS NEXT
		        		// -------------------------------------------------------------------
/*		        		
		          		// Let's check if every XYSeries still has its player behind, or is the series irrelevant!
		        		Iterator<AID> cdc = chdataseries01_copy.keySet().iterator();
		        		while(cdc.hasNext()) {
		
							a = cdc.next();
							if (!playersUtility_copy.containsKey(a)) {
		
								XYSeries as = chdataseries01.get(a);
								chdataseries01.remove(a);
								chdata.removeSeries(as);
		
							}
		
		          		} // WHILE CDC
*/		
		          		// Now let's check if every player - whose actual utility is stored (in playersUtility) - has its XYSeries!
		        		puc = playersUtility_copy.keySet().iterator();
		        		while(puc.hasNext()) {
		
		        			// The next agent-player...
		        			a = puc.next();
		        			
							// If player 'a' has its XYSeries, then...
							if (chdataseries01.containsKey(a)) {
		
								XYSeries as = chdataseries01.get(a);
								int maxas	= 1000 / chdataseries01.size();
								as.setMaximumItemCount(maxas);	// !!! SHOW ONLY THE LAST maxas SAMPLES !!!
								as.add(new Double(round).doubleValue(), playersUtility_copy.get(a).doubleValue());
								chdataseries01.put(a, as);

								// It may happen, that adding a point to XYSeries "as" is too slowly translated to "chdata", and so
								// the index of the updated series will be "-1" in "chdata", and we won't be able to (re)paint it... 
								if (chdata.indexOf(as) >= 0) {

									// Let's (re)set the color of this series on the plot according to the given type of the player...
									// ...it may happen, that a players type was unknown (until it was chosen to
									// play, and thus informed us of its type), but becomes known only just now/later.
									chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(playersType.get(a).intValue()));

								}

							} else {

								XYSeries as = new XYSeries(a.getLocalName());
								int maxas	= 1000 / (chdataseries01.size() + 1);
								as.setMaximumItemCount(maxas);	// !!! SHOW ONLY THE LAST maxas SAMPLES !!!
								as.add(new Double(round).doubleValue(), playersUtility_copy.get(a).doubleValue());
								chdata.addSeries(as);	// Add the new XYSeries 'as' for the newcomer player!!
								chdataseries01.put(a, as);

								// Let's set the color of this series on the plot according to the given type of the player...
								chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(playersType.get(a).intValue()));

							} // IF HAS SERIES

						} // WHILE PUC

		        		break;

					case GUI_TYPES_RATIO:

		          		// Let's make a copy of chdataseries02 for Thread-Safety reasons!!!...
		        		HashMap<Integer, XYSeries> chdataseries02_copy = new HashMap<Integer, XYSeries>();
		        		Iterator<Integer> cd02 = chdataseries02.keySet().iterator();

		        		// Do the copying............
		        		while(cd02.hasNext()) {

		    				t = cd02.next();
		    				chdataseries02_copy.put(t, chdataseries02.get(t));
		
		    			} // WHILE HAS NEXT
		        		// -------------------------------------------------------------------
		        		
		        		// Initialize a primitive array for accumulating the number of different player types in this round...
		        		double[] type_ratios = new double[PlayerType.TYPENUM];
		        		for (int i = 0; i < type_ratios.length; i++) type_ratios[i] = 0;
		        		
		        		// Fill the array with values (do the accumulation)...
		        		puc = playersUtility_copy.keySet().iterator();
		        		while(puc.hasNext()) {

		        			// Increment the number of this agent's type by 1...	        			
		        			type_ratios[playersType.get(puc.next())] += 1;

		        		}		        		
/*		        		
		          		// Let's check if every XYSeries still has its type behind, or is the series irrelevant!
		        		Iterator<Integer> cd02c = chdataseries02_copy.keySet().iterator();
		        		while(cd02c.hasNext()) {

							t = cd02c.next();
							if (type_ratios[t.intValue()] == 0) {
		
								XYSeries as = chdataseries02.get(t);
								chdataseries02.remove(t);
								chdata.removeSeries(as);
		
							}

		          		} // WHILE CD02C
*/		
		        		// Now let's check if every type has its XYSeries!
		        		for (int i = 0; i < type_ratios.length; i++) {
		        			
		        			// Normalize this ratio to the [0..1] intervall...
		        			type_ratios[i] = type_ratios[i] / playersUtility_copy.size();
		        			
							// If type 'i' still has its XYSeries, then...
							if (chdataseries02.containsKey(i)) {
		
								XYSeries as = chdataseries02.get(i);
								as.add(new Double(round).doubleValue(), type_ratios[i]);
								chdataseries02.put(i, as);

								// It may happen, that adding a point to XYSeries "as" is too slowly translated to "chdata", and so
								// the index of the updated series will be "-1" in "chdata", and we won't be able to (re)paint it... 
								if (chdata.indexOf(as) >= 0) {

									// Let's (re)set the color of this series on the plot according to the given type of the player...
									// ...it may happen, that a players type was unknown (until it was chosen to
									// play, and thus informed us of its type), and becomes known only just now/later.
									chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(i));

								} // IF INDEXOF(AS) >= 0

							} else {
		
								// Every series must have a unique name!
								XYSeries as = new XYSeries("Type " + i);
								as.add(new Double(round).doubleValue(), type_ratios[i]);
								chdata.addSeries(as);	// Add the new XYSeries 'as' for the newcomer player!!
								chdataseries02.put(i, as);
		
								// Let's set the color of this series on the plot according to the given type of the player...
								chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(i));
		
							} // IF HAS SERIES
		
						} // FOR TYPE_RATIOS
        		
		        		break;
		        		
					case GUI_TYPES_NUMBER:

		          		// Let's make a copy of chdataseries02 for Thread-Safety reasons!!!...
		        		HashMap<Integer, XYSeries> chdataseries03_copy = new HashMap<Integer, XYSeries>();
		        		Iterator<Integer> cd03 = chdataseries03.keySet().iterator();

		        		// Do the copying............
		        		while(cd03.hasNext()) {

		    				t = cd03.next();
		    				chdataseries03_copy.put(t, chdataseries03.get(t));
		
		    			} // WHILE HAS NEXT
		        		// -------------------------------------------------------------------
		        		
		        		// Initialize a primitive array for accumulating the number of different player types in this round...
		        		double[] type_numbers = new double[PlayerType.TYPENUM];
		        		for (int i = 0; i < type_numbers.length; i++) type_numbers[i] = 0;
		        		
		        		// Fill the array with values (do the accumulation)...
		        		puc = playersUtility_copy.keySet().iterator();
		        		while(puc.hasNext()) {

		        			// Increment the number of this agent's type by 1...	        			
		        			type_numbers[playersType.get(puc.next())] += 1;

		        		}		        		
/*		        		
		          		// Let's check if every XYSeries still has its type behind, or is the series irrelevant!
		        		Iterator<Integer> cd03c = chdataseries03_copy.keySet().iterator();
		        		while(cd03c.hasNext()) {

							t = cd03c.next();
							if (type_numbers[t.intValue()] == 0) {
		
								XYSeries as = chdataseries03.get(t);
								chdataseries03.remove(t);
								chdata.removeSeries(as);
		
							}

		          		} // WHILE CD02C
*/		
		        		// Now let's check if every type has its XYSeries!
		        		for (int i = 0; i < type_numbers.length; i++) {
		        			
							// If type 'i' still has its XYSeries, then...
							if (chdataseries03.containsKey(i)) {
		
								XYSeries as = chdataseries03.get(i);
								as.add(new Double(round).doubleValue(), type_numbers[i]);
								chdataseries03.put(i, as);

								// It may happen, that adding a point to XYSeries "as" is too slowly translated to "chdata", and so
								// the index of the updated series will be "-1" in "chdata", and we won't be able to (re)paint it... 
								if (chdata.indexOf(as) >= 0) {

									// Let's (re)set the color of this series on the plot according to the given type of the player...
									// ...it may happen, that a players type was unknown (until it was chosen to
									// play, and thus informed us of its type), and becomes known only just now/later.
									chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(i));

								} // IF INDEXOF(AS) >= 0

							} else {
		
								// Every series must have a unique name!
								XYSeries as = new XYSeries("Type " + i);
								as.add(new Double(round).doubleValue(), type_numbers[i]);
								chdata.addSeries(as);	// Add the new XYSeries 'as' for the newcomer player!!
								chdataseries03.put(i, as);
		
								// Let's set the color of this series on the plot according to the given type of the player...
								chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(i));
		
							} // IF HAS SERIES
		
						} // FOR TYPE_NUMBER
        		
		        		break;
		        		
					case GUI_TYPES_AVGUTIL:

		          		// Let's make a copy of chdataseries02 for Thread-Safety reasons!!!...
		        		HashMap<Integer, XYSeries> chdataseries04_copy = new HashMap<Integer, XYSeries>();
		        		Iterator<Integer> cd04 = chdataseries04.keySet().iterator();

		        		// Do the copying............
		        		while(cd04.hasNext()) {

		    				t = cd04.next();
		    				chdataseries04_copy.put(t, chdataseries04.get(t));

		    			} // WHILE HAS NEXT
		        		// -------------------------------------------------------------------

		        		// Initialize a primitive array for accumulating the number of different player types in this round...
		        		double[] type_number = new double[PlayerType.TYPENUM];

		        		// Initialize a primitive array for accumulating the utility of different player types in this round...
		        		double[] type_utility = new double[PlayerType.TYPENUM];
	        		
		        		// Initialize the arrays...
		        		for (int i = 0; i < type_number.length; i++) {
		        			
		        			type_number[i]	= 0;
		        			type_utility[i]	= 0;
		        			
		        		}
	        		
		        		// Fill the arrays with values (do the accumulation)...
		        		puc = playersUtility_copy.keySet().iterator();
		        		while(puc.hasNext()) {

		        			// Get the next agent...
		        			a = puc.next();
		        			
		        			// Get its type...
		        			t = playersType.get(a);
		        			
		        			// Increment the number of this agent's type by 1...	        			
		        			type_number[t.intValue()] += 1;
		        			
		        			// Increment the aggregated utility of this agent's type by the agent's individual utility...	        			
		        			type_utility[t.intValue()] += playersUtility_copy.get(a).doubleValue();

		        			// Increment the total utility of players...
		        			type_utility[PlayerType.EVERYONE] += playersUtility_copy.get(a).doubleValue();
		        			
		        		}
		        		
		        		// The total number of players (in this round)...
		        		type_number[PlayerType.EVERYONE] = playersUtility_copy.size();
/*		        		
		          		// Let's check if every XYSeries still has its type behind, or is the series irrelevant!
		        		Iterator<Integer> cd04c = chdataseries04_copy.keySet().iterator();
		        		while(cd04c.hasNext()) {

							t = cd04c.next();
							if (type_number[t.intValue()] == 0) {
		
								XYSeries as = chdataseries04.get(t);
								chdataseries04.remove(t);
								chdata.removeSeries(as);
		
							}

		          		} // WHILE CD02C
*/		
		        		// Now let's check if every type has its XYSeries!
		        		for (int i = 0; i < type_number.length; i++) {
		        			
		        			// Get the average utility for type i...
		        			type_utility[i] = type_utility[i] / type_number[i];
		        			
							// If type 'i' still has its XYSeries, then...
							if (chdataseries04.containsKey(i)) {
		
								XYSeries as = chdataseries04.get(i);
								as.add(new Double(round).doubleValue(), type_utility[i]);
								chdataseries04.put(i, as);

								// It may happen, that adding a point to XYSeries "as" is too slowly translated to "chdata", and so
								// the index of the updated series will be "-1" in "chdata", and we won't be able to (re)paint it... 
								if (chdata.indexOf(as) >= 0) {

									// Let's (re)set the color of this series on the plot according to the given type of the player...
									// ...it may happen, that a players type was unknown (until it was chosen to
									// play, and thus informed us of its type), and becomes known only just now/later.
									chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(i));

								} // IF INDEXOF(AS) >= 0

							} else {
		
								// Every series must have a unique name!
								XYSeries as = new XYSeries("Type " + i);
								as.add(new Double(round).doubleValue(), type_utility[i]);
								chdata.addSeries(as);	// Add the new XYSeries 'as' for the newcomer player!!
								chdataseries04.put(i, as);
		
								// Let's set the color of this series on the plot according to the given type of the player...
								chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(i));
		
							} // IF HAS SERIES
		
						} // FOR TYPE_RATIOS
        		
		        		break;
		        		
				} // SWITCH GUITYPE

			} // End of Runnable.run() method

		}); // End of anonym Runnable class

	} // End of GameAgentGui.updateChart() method

    /**
     * Creates a chart.
     * 
     * @param dataset  a CategoryDataset
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final int guiType, final XYSeriesCollection dataset) {
        
    	final JFreeChart chart;
    	
    	switch (guiType) {
    	
	    	case GUI_PLAYERS_UTIL:
	    	
		        // Create the chart...
		        chart = ChartFactory.createXYLineChart( //.createLineChart(
		            "",       					// chart title
		            "Rounds",                	// domain axis label
		            "Utility",             		// range axis label
		            dataset,                   	// data
		            PlotOrientation.VERTICAL,  	// orientation
		            false,                     	// include legend
		            false,                     	// tooltips
		            false                      	// urls
		        );
		        
		        break;
		        
	    	case GUI_TYPES_RATIO:
	        	
		        // Create the chart...
		        chart = ChartFactory.createXYLineChart( //.createLineChart(
		            "",       					// chart title
		            "Rounds",                	// domain axis label
		            "Ratio",             		// range axis label
		            dataset,                   	// data
		            PlotOrientation.VERTICAL,  	// orientation
		            false,                     	// include legend
		            false,                     	// tooltips
		            false                      	// urls
		        );
		        
		        break;
		        
	    	case GUI_TYPES_NUMBER:
	        	
		        // Create the chart...
		        chart = ChartFactory.createXYLineChart( //.createLineChart(
		            "",       					// chart title
		            "Rounds",                	// domain axis label
		            "Quantity",            		// range axis label
		            dataset,                   	// data
		            PlotOrientation.VERTICAL,  	// orientation
		            false,                     	// include legend
		            false,                     	// tooltips
		            false                      	// urls
		        );
		        
		        break; 
	
	    	case GUI_TYPES_AVGUTIL:
	        	
		        // Create the chart...
		        chart = ChartFactory.createXYLineChart( //.createLineChart(
		            "",       					// chart title
		            "Rounds",                	// domain axis label
		            "Average utility",     		// range axis label
		            dataset,                   	// data
		            PlotOrientation.VERTICAL,  	// orientation
		            false,                     	// include legend
		            false,                     	// tooltips
		            false                      	// urls
		        );
		        
		        break;  
		        
	    	default: // GUI_TYPES_AVGUTIL or something else...
	        	
		        // Create the chart...
		        chart = ChartFactory.createXYLineChart( //.createLineChart(
		            "",       					// chart title
		            "Rounds",                	// domain axis label
		            "Average utility",     		// range axis label
		            dataset,                   	// data
		            PlotOrientation.VERTICAL,  	// orientation
		            false,                     	// include legend
		            false,                     	// tooltips
		            false                      	// urls
		        );
		        
		        break;  
	        
    	} // SWITCH BY GUITYPE

        // Get a reference to the plot for further customization...
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        // plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        /*final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesFilled(1, false);
        plot.setRenderer(renderer);*/

        // change the auto tick unit selection to integer units only...
        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
 
        return chart;
        
    } // End of GameAgentGui.createChart() method
    
    /**
     * Enables/disables the radio buttons that can change the type of the chart
     * 
     * @param en enables (true) or disables (false) the radio buttons on the GUI
     */
    public void setRadioButtons(boolean en) {
    	
    	// Re-enable radio buttons...
	    Enumeration<AbstractButton> rbe = rgroup.getElements();
	    while (rbe.hasMoreElements()) rbe.nextElement().setEnabled(en);
    	
    } // End of GameAgentGui.enableRadioButtons() method
    
} // End of GameAgentGui class