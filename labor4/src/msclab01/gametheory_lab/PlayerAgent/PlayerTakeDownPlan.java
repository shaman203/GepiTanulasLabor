package msclab01.gametheory_lab.PlayerAgent;

import jadex.runtime.*;

/**
 * JADEX player agent's take-down plan
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class PlayerTakeDownPlan extends Plan {

	private static final long serialVersionUID = 1L;
	
	private PlayerGui myGui;
	
	public PlayerTakeDownPlan() {
		
		//getLogger().info("Created: "+this);
		
	} // End of PlayerTakeDownPlan.PlayerTakeDownPlan() constructor

	public void body() {

		// If the GUI was active
		if (((Boolean)getBeliefbase().getBelief("gui").getFact()).booleanValue()) {
		
			// Get the GUI
			myGui = (PlayerGui)getBeliefbase().getBelief("myGui").getFact();
			
			// Close the GUI
			myGui.dispose();
			
		}
	
		// Printout a dismissal message
		//System.out.println("Player-agent " + getAgentName() + " terminating...");

	}  // End of PlayerTakeDownPlan.body() method
	
} // End of PlayerTakeDownPlan class
