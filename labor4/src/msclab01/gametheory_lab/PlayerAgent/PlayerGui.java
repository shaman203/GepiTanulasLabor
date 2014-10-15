package msclab01.gametheory_lab.PlayerAgent;



import jadex.runtime.IExternalAccess;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.GroupLayout.*;

/*import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;*/

/**
 * The GUI of the PlayerAgent.
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
class PlayerGui extends JFrame {

	private static final long serialVersionUID = 1L;
	
	/** constant identifying the compact view of the GUI */
	public static final int COMPACT = 0;
	/** constant identifying the extended view of the GUI */
	public static final int EXTENDED = 1;

	/** A pointer to the agent to whom this GUI belongs (who created it). */
	private IExternalAccess myAgent;
	/** The data model beneath the JTable holding the agent's history. */
	protected DefaultTableModel tadata;
	/** The maximum amount of rows held by the DefaultTableModel of agent history. */
	protected int tadata_maxrownum = 100;
	/** The widget to show the agent history DefaultTableModel data. */
	protected JTable tatable;
	/** The panel holding the JTable widget */
	private JPanel tablePanel;
	/** The data model beneath the JFreeChart showing the agent's utility history. */
	// protected DefaultCategoryDataset chdata;
	/** The widget to show the agent utility history DefaultCategoryDataset data. */
	// protected JFreeChart chchart;
	/** The textfield where the cumulated utility value is written (by the agent's plans). */
	private JTextField valueField;


	/**
	 *  Construct the GUI of the given JADEX agent (player).
	 */
	PlayerGui(final IExternalAccess a) {

		super(a.getAgentName());

		myAgent = a;

		// -------------------------------- NORTH --------------------------------
		JPanel p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		
		valueField = new JTextField(15);
		valueField.setText(((Double)myAgent.getBeliefbase().getBelief("utility").getFact()).toString());
		valueField.setEnabled(false);
		valueField.setBorder(BorderFactory.createTitledBorder("Current utility"));
		
		JCheckBox cb = new JCheckBox("Extended view", false);
		cb.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent ie) {

				if (ie.getStateChange() == ItemEvent.SELECTED) {
		
					changeView(EXTENDED);

				} else {

					changeView(COMPACT);

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
		layout.setVerticalGroup(vGroup);

		getContentPane().add(p, BorderLayout.NORTH);
		
		// -------------------------------- CENTER --------------------------------
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
		p.setPreferredSize(new Dimension(0, 0));
		tablePanel = p;

		getContentPane().add(tablePanel, BorderLayout.CENTER);

		// -------------------------------- SOUTH --------------------------------
		/*chdata = new DefaultCategoryDataset();
		
		// Value - of What - Where
		chdata.addValue(((Double)myAgent.getBeliefbase().getBelief("utility").getFact()).doubleValue(), myAgent.getAgentName(), "0");
		chchart = createChart(chdata);
        ChartPanel chartPanel = new ChartPanel(chchart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Utility over rounds"));
        
        getContentPane().add(chartPanel, BorderLayout.SOUTH);*/
		
		
		// Add a WindowListener to this Window to listen to windowClosing events.
		// When such an event occurs, the corresponding Agent should be terminated.
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				myAgent.killAgent();

			}

		});

		setResizable(false);

	} // End of PlayerGui.PlayerGui(IExternalAccess) constructor

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

	} // End of PlayerGui.show() method

	/**
	 *  Just (re)pack the GUI and make it visible (again) according
	 *  to some panel-changes referenced by the changeID.
	 */
	public void changeView(int changeID) {

		switch (changeID) {
		
			case COMPACT:
				
				// Minimize the JPanel holding the JTable 
				tablePanel.setPreferredSize(new Dimension(0, 0));
				break;
				
			case EXTENDED:
				
				// Maximize the JPanel holding the JTable 
				tablePanel.setPreferredSize(null);
				break;
				
		} // SWITCH CHANGEID
		
		pack();
		super.show();

	} // End of PlayerGui.changeView() method
	
	/**
	 *  Update the utility textField, and refresh the history table.
	 *  @param content The content.
	 */
	public void updatePlayerGui(final String[] content) {
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				
				valueField.setText(content[8]);
				
				tadata.addRow(content);
				tatable.scrollRectToVisible(tatable.getCellRect(tatable.getRowCount()-1, 0, true));

				// If there are too many rows in the data model, then
				if (tadata.getRowCount() > tadata_maxrownum) {
					
					tadata.removeRow(0);
					
				}
				
				//chdata.setValue(new Double(content[8]), myAgent.getAgentName(), content[0]);

			}
			
		});
		
	} // End of PlayerGui.addRow(String[]) method

    /**
     * Creates a chart.
     * 
     * @param dataset  a CategoryDataset
     * 
     * @return The chart.
     */
    /*private JFreeChart createChart(final CategoryDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createLineChart(
            "",       					// chart title
            "Rounds",                	// domain axis label
            "Utility",             		// range axis label
            dataset,                   	// data
            PlotOrientation.VERTICAL,  	// orientation
            false,                      // include legend
            true,                      	// tooltips
            false                      	// urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        //final StandardLegend legend = (StandardLegend) chart.getLegend();
        //legend.setDisplaySeriesShapes(true);
        //legend.setShapeScaleX(1.5);
        //legend.setShapeScaleY(1.5);
        //legend.setDisplaySeriesLines(true);

        //chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.white);

        // customise the range axis...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAutoRangeIncludesZero(true);
       
        // customise the renderer...
        // final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        // renderer.setDrawShapes(true);

        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;
        
    }*/ // End of PlayerGui.createChart() method

} // End of PlayerGui class
