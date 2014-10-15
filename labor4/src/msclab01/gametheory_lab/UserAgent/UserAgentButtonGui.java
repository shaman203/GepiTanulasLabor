package msclab01.gametheory_lab.UserAgent;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.GroupLayout.*;

import msclab01.gametheory_lab.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * The GUI of the UserAgent.
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
class UserAgentButtonGui extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/** constant identifying the compact view of the GUI */
	public static final int COMPACT = 0;
	/** constant identifying the extended view of the GUI */
	public static final int EXTENDED = 1;

	/** A pointer to the agent to whom this GUI belongs (who created it). */
	private UserAgent myAgent;
	/** The data model beneath the JTable holding the agent's history. */
	protected DefaultTableModel tadata;
	/** The maximum amount of rows held by the DefaultTableModel of agent history. */
	protected int tadata_maxrownum = 100;
	/** The widget to show the agent history DefaultTableModel data. */
	protected JTable tatable;
	/** The panel holding the JTable widget */
	private JPanel tablePanel;
	/** The data beneath the JFreeChart showing the agent's utility history. */
	protected XYSeries as;
	/** The widget to show the agent utility history DefaultCategoryDataset data. */
	protected JFreeChart chchart;
	/** The textfield where the cumulated utility value is written. */
	private JTextField valueField;
	/** The textfield where the messages coming indirectly from GameAgent are written. */
	private JTextField messageField;
	/** The buttons for choosing strategies to play. */
	private JButton[] b;
	/** The game of the corresponding agent. */
	private Game G;


	/**
	 *  Construct the GUI of the given JADEX agent (User).
	 */
	UserAgentButtonGui(UserAgent a, String myPid) {

		super(a.getLocalName() + " control");

		myAgent = a;
		
		G = a.getMyGame();
		
		b = new JButton[G.getStratNums(Integer.parseInt(myPid))];

		// --------------------------------- NORTH -----------------------------------
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 1));
		
		// -------------------------------- NORTH 1/2 --------------------------------
		
		/*JPanel p1 = new JPanel();
		GroupLayout layout = new GroupLayout(p1);
		p1.setLayout(layout);
		
		valueField = new JTextField(15);
		valueField.setText(((Double)myAgent.getUtility()).toString());
		valueField.setEnabled(false);
		valueField.setBorder(BorderFactory.createTitledBorder("Current utility"));
		
		JCheckBox cb = new JCheckBox("Compact view", false);
		cb.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent ie) {

				if (ie.getStateChange() == ItemEvent.SELECTED) {
		
					changeView(COMPACT);

				} else {

					changeView(EXTENDED);

				}

			} // End of overridden ItemListener.itemStateChanged() method
			
		}); // End of the anonym ItemListener class

		// Create a sequential group for the horizontal axis.
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGroup(layout.createParallelGroup().addComponent(valueField));
		hGroup.addGroup(layout.createParallelGroup().addComponent(cb));
		layout.setHorizontalGroup(hGroup);

		// Create a sequential group for the vertical axis.
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(valueField).addComponent(cb));
		layout.setVerticalGroup(vGroup);*/
		
		// ------------------------------- NORTH 2/2 ------------------------------
		
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 1));
		
		messageField = new JTextField();
		messageField.setText("");
		messageField.setEnabled(false);

		p2.add(messageField);
		
		// ------------------------------ NORTH 2/2/1 -----------------------------
		
		JPanel p21 = new JPanel();
		p21.setLayout(new GridLayout(1, G.getStratNums(Integer.parseInt(myPid))));
		
		for (int si = 0; si < G.getStratNums(Integer.parseInt(myPid)); si++) {
		
			final String sis = Integer.toString(si);
			
			b[si] = new JButton("                ");
			b[si].addActionListener( new ActionListener() {

				public void actionPerformed(ActionEvent ev) {
					
					try {
						
						// Clear the message textfield...
						messageField.setText("");
						
						// Disable both choice buttons (the agent will perhaps enable them later)...
						setChoiceButtonsState(false);
						
						// Erase the text-label of both choice buttons (the agent will perhaps (re)write them later)...
						setChoiceButtonsText(new String[]{"", ""});
						
						// Let the agent know the choice of the User Almighty... ;)
						myAgent.setMyStrategy(sis);
						
						myAgent.myButtonGui.dispose();
						
					} catch (Exception e) {
						
						JOptionPane.showMessageDialog(UserAgentButtonGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
						
					}
					
				}
				
			});
			b[si].setEnabled(false);
			
			p21.add(b[si]);
			
		}
		
		// ------------------------------------------------------------------------		
		
		p2.add(p21);
		
		// ------------------------------------------------------------------------
		
		//p.add(p1);
		p.add(p2);
		
		getContentPane().add(p, BorderLayout.CENTER);
		
		// -------------------------------- CENTER --------------------------------
		/*
		p = new JPanel();
		p.setLayout(new GridLayout(1, 1));
		tadata	= new DefaultTableModel(new String[]{"Play", "OppsName", "OppsRole", "MyRole", "OppsStrat", "MyStrat", "OppsPayoff", "MyPayoff", "MyUtility"}, 0);
		tatable = new JTable(tadata) {
			
			private static final long serialVersionUID = 1L;

			// Override this method to paint the columns (really cells) with an alternate color
			public Component prepareRenderer (TableCellRenderer renderer, int index_row, int index_col) {

				Component comp = super.prepareRenderer(renderer, index_row, index_col);

				// If column index is even (for a cell), and it is not selected, then...
				if(index_col % 2 == 0 && !isCellSelected(index_row, index_col)) {

					comp.setBackground(Color.lightGray);
					
				} else {

					comp.setBackground(Color.white);

				}

     			return comp;

			} // End of JTable.prepareRenderer() method

			// Override this method to render cells' value with central alignment
			public TableCellRenderer getCellRenderer(int row, int column) {
				
				// Return this kind of object...
				return new DefaultTableCellRenderer() {

					private static final long serialVersionUID = 1L;

					// ...with this method overridden this way! 
					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

						JLabel renderedLabel = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
						renderedLabel.setHorizontalAlignment(SwingConstants.CENTER);
						return renderedLabel;

					} // End of DefaultTableCellRenderer.getTableCellRendererComponent() method

				}; // End of (returned anonym) DefaultTableCellRenderer class

			} // End of JTable.getCellRenderer() method

		}; // End of JTable class
		tatable.setEnabled(false);
		JScrollPane sp = new JScrollPane(tatable);		
		p.add(sp);
		p.setBorder(BorderFactory.createTitledBorder("History"));
		tablePanel = p;
		
		getContentPane().add(p, BorderLayout.CENTER);

		// ----------------------------- SOUTH ------------------------------- 
		
		// Initialize data model (behind the chart diagram)
		XYSeriesCollection chdata = new XYSeriesCollection();
		as = new XYSeries(a.getLocalName());
		as.setMaximumItemCount(300);	// !!! SHOW ONLY THE LAST 300 SAMPLES !!!
		chdata.addSeries(as);	// Add the new XYSeries 'as' for the newcomer player!!
		
		// Create the chart diagram object, and put it to a panel on the content pane
		chchart = createChart(chdata);
		
		// Let's set the color of this series on the plot according to the user type...
		chchart.getXYPlot().getRenderer().setSeriesPaint(chdata.indexOf(as), PlayerType.getTypeColor(PlayerType.USER));

		// Create the panel holding the chart...
        ChartPanel cp	= new ChartPanel(chchart);
        cp.setPreferredSize(new Dimension(500, 270));
        cp.setBorder(BorderFactory.createTitledBorder("Utility over plays"));

		getContentPane().add(cp, BorderLayout.SOUTH);
		
		// Add a WindowListener to this Window to listen to windowClosing events.
		// When such an event occurs, the corresponding Agent should be terminated.
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				myAgent.doDelete();

			}

		});
*/
		setResizable(false);

	} // End of UserAgentGui.UserGui(IExternalAccess) constructor

	/**
	 *  Position the GUI to the middle of the screen and make it visible (show it).
	 */
	public void show() {

		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.show();

	} // End of UserAgentGui.show() method

	/**
	 *  Just (re)pack the GUI and make it visible (again) according
	 *  to some panel-changes referenced by the changeID.
	 */
	public void changeView(int changeID) {

		switch (changeID) {
		
			case COMPACT:
				
				// Minimize the JPanel holding the JTable and JFreeChart
				tablePanel.setPreferredSize(new Dimension(0, 0));
				break;
				
			case EXTENDED:
				
				// Maximize the JPanel holding the JTable and JFreeChart 
				tablePanel.setPreferredSize(null);
				break;
				
		} // SWITCH CHANGEID
		
		pack();
		super.show();

	} // End of UserAgentGui.changeView() method
	
	/**
	 *  Update the utility textField, and refresh the history table.
	 *  @param content The content.
	 */
	public void updateGui(final String[] content) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				valueField.setText(content[8]);
				
				tadata.addRow(content);
				tatable.scrollRectToVisible(tatable.getCellRect(tatable.getRowCount()-1, 0, true));

				// If there are too many rows in the data model, then
				if (tadata.getRowCount() > tadata_maxrownum) {
					
					tadata.removeRow(0);
					
				}
				
				as.add(new Double(content[0]).doubleValue(), new Double(content[8]).doubleValue());

			}
			
		});
		
	} // End of UserAgentGui.updateGui(String[]) method

	/**
	 *  Update the message textField
	 *  @param content The content.
	 */
	public void updateMessageField(final String content) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				messageField.setText(content);

			}
			
		});
		
	} // End of UserAgentGui.updateMessageField(String) method
	
	/**
	 *  Set the choice buttons' EnabledState according to the input
	 *  @param state The new state of the buttons.
	 */
	public void setChoiceButtonsState(final boolean state) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				for (int bi = 0; bi < b.length; bi++) b[bi].setEnabled(state);

			}
			
		});
		
	} // End of UserAgentGui.setChoiceButtonsState(boolean) method
	
	/**
	 *  Set the choice buttons' text-label according to the input
	 *  @param txt The new text-labrl of the buttons.
	 */
	public void setChoiceButtonsText(final String[] txt) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				for (int bi = 0; bi < txt.length; bi++) b[bi].setText(txt[bi]);

			}
			
		});
		
	} // End of UserAgentGui.setChoiceButtonsText(boolean) method
	
	/**
     * Creates a chart.
     * 
     * @param dataset  a CategoryDataset
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final XYSeriesCollection dataset) {
        
        // Create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart( //.createLineChart(
            "",       					// chart title
            "Plays",                	// domain axis label
            "Utility",             		// range axis label
            dataset,                   	// data
            PlotOrientation.VERTICAL,  	// orientation
            false,                     	// include legend
            false,                     	// tooltips
            false                      	// urls
        );

        // Get a reference to the plot for further customisation...
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
        
    } // End of UserAgentGui.createChart() method

} // End of UserGui class
