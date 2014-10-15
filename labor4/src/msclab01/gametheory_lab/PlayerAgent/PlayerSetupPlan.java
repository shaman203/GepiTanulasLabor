package msclab01.gametheory_lab.PlayerAgent;

import msclab01.gametheory_lab.Game;
import jadex.runtime.*;

/**
 * JADEX player agent's setup plan
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class PlayerSetupPlan extends Plan {

	private static final long serialVersionUID = 1L;

	public PlayerSetupPlan() {
		
		//getLogger().info("Created: "+this);
		
	} // End of PlayerSetupPlan.PlayerSetupPlan() constructor

	public void body() {
		
		// Printout a welcome message
		//System.out.println("Hello! Player-agent " + getAgentName() + " is ready to play the " + G.getName() + " game...");		
		
		// Initialize an appropriate Game object...
		getBeliefbase().getBelief("G").setFact((Game)new Game((String)getBeliefbase().getBelief("gid").getFact()));
		
		// If the GUI is activated
		if (((Boolean)getBeliefbase().getBelief("gui").getFact()).booleanValue()) {
		
			// Create the GUI
			PlayerGui myGui = new PlayerGui(getExternalAccess());
			
			// Show the GUI
			myGui.show();
			
			// Store the GUI
			getBeliefbase().getBelief("myGui").setFact(myGui);
			
		}

	} // End of PlayerSetupPlan.body() method

} // End of PlayerSetupPlan class
