/**
 * Copyright (c) 2010
 * Budapest University of Technology and Economics (BUTE)
 * Department of Measurement and Information Systems (DMIS)
 * All Rights Reserved.
 * 
 * "Cooperative and Learning Systems (VIMIM223)" laboratory
 * "Auctions and Voting" excercise
 * 
 * 				--------------------
 * 				Auctioneer agent GUI
 * 				--------------------
 * 
 * Author:		Dániel László, Kovács (dkovacs@mit.bme.hu)
 * 
 */

package msclab01.votingauction_lab.AuctioneerAgent;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * AuctioneerAgent GUI
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
class AuctioneerAgentGui extends JFrame {

	private static final long serialVersionUID = 2233952985897694884L;

	private AuctioneerAgent myAgent;

	public JProgressBar progressBar;
	public JTextField goodNo, goodType, initBidAmount, bidAmount, leaderName, leaderMoney, leaderBuys, leaderUtility;
	public TextArea auctionFlow; // This is an AWT TextArea instead of a SWING JTextArea because of autoscrolling. SWING is simply not thread-safe (and now the TextArea.append is called from a different thread: by the agent)...
	public JButton startButton;
	public JButton stopButton;

	AuctioneerAgentGui(AuctioneerAgent a) {

		super("AuctioneerAgent " + a.getLocalName());

		myAgent = a;

		// ---------------------------- NORTHERN PART OF THE FRAME ----------------------------
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		// ------------------------------------ Sub-part 1 ------------------------------------
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 1));

        progressBar = new JProgressBar(0, myAgent.totalGoodNumber);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

		p1.add(progressBar);	

		p1.setBorder(BorderFactory.createTitledBorder("Progress of auction"));

		p.add(p1, BorderLayout.NORTH);

		// ------------------------------------ Sub-part 2 ------------------------------------
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 8));

		goodNo			= new JTextField("");
		goodType		= new JTextField("");
		initBidAmount	= new JTextField("");
		bidAmount		= new JTextField("");
		leaderName		= new JTextField("");
		leaderMoney		= new JTextField("");
		leaderBuys		= new JTextField("");
		leaderUtility	= new JTextField("");

		goodNo.setEditable(false);
		goodType.setEditable(false);
		initBidAmount.setEditable(false);
		bidAmount.setEditable(false);
		leaderName.setEditable(false);
		leaderMoney.setEditable(false);
		leaderBuys.setEditable(false);
		leaderUtility.setEditable(false);

		goodNo.setHorizontalAlignment(JTextField.CENTER);
		goodType.setHorizontalAlignment(JTextField.CENTER);
		initBidAmount.setHorizontalAlignment(JTextField.CENTER);
		bidAmount.setHorizontalAlignment(JTextField.CENTER);
		leaderName.setHorizontalAlignment(JTextField.CENTER);
		leaderMoney.setHorizontalAlignment(JTextField.CENTER);
		leaderBuys.setHorizontalAlignment(JTextField.CENTER);
		leaderUtility.setHorizontalAlignment(JTextField.CENTER);

		JLabel labelGoodNo			= new JLabel("Good No.");
		JLabel labelGoodType		= new JLabel("Good type");
		JLabel labelInitBidAmount	= new JLabel("Initial bid");
		JLabel labelBidAmount		= new JLabel("Actual bid");
		JLabel labelLeaderName		= new JLabel("Bidder");
		JLabel labelLeaderMoney		= new JLabel("Money");
		JLabel labelLeaderBuys		= new JLabel("Buys");
		JLabel labelLeaderUtility	= new JLabel("Utility");
		
		labelGoodNo.setHorizontalAlignment(SwingConstants.CENTER);
		labelGoodType.setHorizontalAlignment(SwingConstants.CENTER);
		labelInitBidAmount.setHorizontalAlignment(SwingConstants.CENTER);
		labelBidAmount.setHorizontalAlignment(SwingConstants.CENTER);
		labelLeaderName.setHorizontalAlignment(SwingConstants.CENTER);
		labelLeaderMoney.setHorizontalAlignment(SwingConstants.CENTER);
		labelLeaderBuys.setHorizontalAlignment(SwingConstants.CENTER);
		labelLeaderUtility.setHorizontalAlignment(SwingConstants.CENTER);
		
		p2.add(labelGoodNo);
		p2.add(labelGoodType);
		p2.add(labelInitBidAmount);
		p2.add(labelBidAmount);
		p2.add(labelLeaderName);
		p2.add(labelLeaderMoney);
		p2.add(labelLeaderBuys);
		p2.add(labelLeaderUtility);
		p2.add(goodNo);
		p2.add(goodType);
		p2.add(initBidAmount);
		p2.add(bidAmount);
		p2.add(leaderName);
		p2.add(leaderMoney);
		p2.add(leaderBuys);
		p2.add(leaderUtility);

		p2.setBorder(BorderFactory.createTitledBorder("Current state of auction"));
		
		p.add(p2, BorderLayout.SOUTH);

		getContentPane().add(p, BorderLayout.NORTH);

		// ---------------------------- CENTRAL PART OF THE FRAME ----------------------------
		p = new JPanel();
		
		auctionFlow = new TextArea(30, 80);
		auctionFlow.setEditable(false);
		auctionFlow.setFont(new Font("Courier New", Font.PLAIN, 12));

		p.add(new JScrollPane(auctionFlow), BorderLayout.CENTER);
		
		p.setBorder(BorderFactory.createTitledBorder("Auction events"));
		getContentPane().add(p, BorderLayout.CENTER);

		// ---------------------------- SOUTHERN PART OF THE FRAME ----------------------------
		p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		
		startButton = new JButton("Start");
		startButton.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				
				try {
					
					startButton.setEnabled(false);
					myAgent.callBehaviour("SearchAndNotifyBidders");
					
				} catch (Exception e) {
					
					JOptionPane.showMessageDialog(AuctioneerAgentGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
					
				}
				
			}
			
		});
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				
				try {
				
					stopButton.setEnabled(false);
					myAgent.callBehaviour("AnnounceAndWait");
					
				} catch (Exception e) {
					
					JOptionPane.showMessageDialog(AuctioneerAgentGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
					
				}
				
			}
			
		});
		stopButton.setEnabled(false);
		
		p.add(startButton);
		p.add(stopButton);
		
		getContentPane().add(p, BorderLayout.SOUTH);
		
		// --------------------------------------------------
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new WindowAdapter() {
			
			public void windowClosing(WindowEvent e) {
				
				myAgent.doDelete();
				
			}
			
		});
		
		setResizable(false);
		
	} // End of AuctioneerAgentGui's constructor
	
	/** Set the GUI to be visible*/
	public void show() {
		
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.show();
		
	} // End of method AuctioneerAgentGui.show()
	
	/** Append some text to the end of the JTextArea*/
	public void appendText(String text) {
		
		auctionFlow.append(text);
		
	} // End of method AuctioneerAgentGui.appendText()
	
} // End of class AuctioneerAgentGui
