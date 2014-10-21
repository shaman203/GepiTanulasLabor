/**
 * Copyright (c) 2010
 * Budapest University of Technology and Economics (BUTE)
 * Department of Measurement and Information Systems (DMIS)
 * All Rights Reserved.
 * 
 * "Cooperative and Learning Systems (VIMIM223)" laboratory
 * "Auctions and Voting" excercise
 * 
 * 				-----------------------
 * 				VotingMechanismAgentGui
 * 				-----------------------
 * 
 * Author:		Dániel László, Kovács (dkovacs@mit.bme.hu)
 * 
 */

package msclab01.votingauction_lab.VotingMechanismAgent;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * VotingMechanismAgent GUI
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
class VotingMechanismAgentGui extends JFrame {

	private static final long serialVersionUID = 2233952985897694884L;

	private VotingMechanismAgent myAgent;

	public JProgressBar progressBar;
	public JTextField winnerName, winnerVotes;
	public TextArea votingFlow; // This is an AWT TextArea instead of a SWING JTextArea because of autoscrolling. SWING is simply not thread-safe (and now the TextArea.append is called from a different thread: by the agent)...
	public JButton startButton;
	public JButton stopButton;

	VotingMechanismAgentGui(VotingMechanismAgent a) {

		super("VotingMechanismAgent " + a.getLocalName());

		myAgent = a;

		// ---------------------------- NORTHERN PART OF THE FRAME ----------------------------
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		// ------------------------------------ Sub-part 1 ------------------------------------
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 1));

		// Notice the range of the progress bar...
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

		p1.add(progressBar);	

		p1.setBorder(BorderFactory.createTitledBorder("Progress of voting"));

		p.add(p1, BorderLayout.NORTH);

		// ------------------------------------ Sub-part 2 ------------------------------------
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(2, 2));	// Rows * Columns

		winnerName		= new JTextField("");
		winnerVotes		= new JTextField("");

		winnerName.setEditable(false);
		winnerVotes.setEditable(false);

		winnerName.setHorizontalAlignment(JTextField.CENTER);
		winnerVotes.setHorizontalAlignment(JTextField.CENTER);

		JLabel labelWinnerName		= new JLabel("Current winning option");
		JLabel labelWinnerVotes		= new JLabel("Votes for that option");
		
		labelWinnerName.setHorizontalAlignment(SwingConstants.CENTER);
		labelWinnerVotes.setHorizontalAlignment(SwingConstants.CENTER);
		
		p2.add(labelWinnerName);
		p2.add(labelWinnerVotes);
		p2.add(winnerName);
		p2.add(winnerVotes);

		p2.setBorder(BorderFactory.createTitledBorder("Current state of voting"));
		
		p.add(p2, BorderLayout.SOUTH);

		getContentPane().add(p, BorderLayout.NORTH);

		// ---------------------------- CENTRAL PART OF THE FRAME ----------------------------
		p = new JPanel();
		
		votingFlow = new TextArea(30, 80);
		votingFlow.setEditable(false);
		votingFlow.setFont(new Font("Courier New", Font.PLAIN, 12));

		p.add(new JScrollPane(votingFlow), BorderLayout.CENTER);
		
		p.setBorder(BorderFactory.createTitledBorder("Voting events"));
		getContentPane().add(p, BorderLayout.CENTER);

		// ---------------------------- SOUTHERN PART OF THE FRAME ----------------------------
		p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		
		startButton = new JButton("Start");
		startButton.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent ev) {
				
				try {
					
					startButton.setEnabled(false);
					myAgent.callBehaviour("SearchAndNotifyVoters");
					
				} catch (Exception e) {
					
					JOptionPane.showMessageDialog(VotingMechanismAgentGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
					
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
					
					JOptionPane.showMessageDialog(VotingMechanismAgentGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
					
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
		
	} // End of VotingMechanismAgentGui's constructor
	
	/** Set the GUI to be visible*/
	public void show() {
		
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.show();
		
	} // End of method VotingMechanismAgentGui.show()
	
	/** Append some text to the end of the JTextArea*/
	public void appendText(String text) {
		
		votingFlow.append(text);
		
	} // End of method VotingMechanismAgentGui.appendText()
	
} // End of class VotingMechanismAgentGui
