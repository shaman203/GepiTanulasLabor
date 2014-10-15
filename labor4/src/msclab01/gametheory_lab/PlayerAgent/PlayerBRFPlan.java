package msclab01.gametheory_lab.PlayerAgent;

import java.util.*;

import jade.wrapper.*;
import jadex.runtime.*;
import jadex.util.*;
import msclab01.gametheory_lab.Game;

/**
 * JADEX player agent's Belief Revision Function (BRF) plan
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class PlayerBRFPlan extends Plan {

	private static final long serialVersionUID = 1L;

	/** Query the action history Tuples for an opponent(memory). */
	protected IExpression query_memory;
	
	/** The type of reproduction */
	int reproduction_type = 1;

	public PlayerBRFPlan() {

		//getLogger().info("Created: "+this);

		// Create an OQL (Object Query Language) expression to retrieve memories about opponents from the BeliefBase...
		query_memory = getExpression("query_memory");

	} // End of PlayerBRFPlan.PlayerBRFPlan() constructor

	public void body() {

		// Get the contents of the message(event) - INFORM from GameAgent -  that caused the execution of this plan...
		StringTokenizer stok = new StringTokenizer(	(String)((IMessageEvent)getInitialEvent()).getContent(), "\t");

		// If the structure of the content is right, then...
		if(stok.countTokens() == 7) {

			// ------------------------------------------------------------
			// (1) What opponents was I playing against?
			String opponents = stok.nextToken();

			// (2) What was my opponents' role (playerIDs) during that play?
			String opponents_pid = stok.nextToken();

			// (3) What strategy did they choose?
			String opponents_sid = stok.nextToken();

			// (4) What was my role (playerID) during that play?
			String my_pid = stok.nextToken();

			// (5) What strategy did I choose?
			String my_sid = stok.nextToken();

			// (6) What payoff (reinforcement) did I get for that?
			String my_payoff = stok.nextToken();
			
			// (7) Am I allowed to reproduce?
			String reprok = stok.nextToken();
			// ------------------------------------------------------------

			// Get the number of rounds played until now
			int rounds = ((Integer)(getBeliefbase().getBelief("rounds").getFact())).intValue();

			// Update the number of rounds played
			rounds++;

			// Store the new number of rounds among my beliefs
			getBeliefbase().getBelief("rounds").setFact(rounds);

			// Get my actual utility
			double utility = ((Double)(getBeliefbase().getBelief("utility").getFact())).doubleValue();

			// Get my actual payoff (which may be positive, zero, or even negative)
			double payoff = new Double(my_payoff).doubleValue();

			// Calculate my new utility
			utility += payoff;

			// If my utility is below zero, then...
			if (utility < 0) {

				// Appropriately terminate the JADEX agent...
				killAgent();

			} else { // UTILITY >= 0

				// Let's think about - given our actual beliefs - what payoff could have the other opponent get!
				Game G = (Game)getBeliefbase().getBelief("G").getFact();

				String[] sids	= new String[G.getPlayerNum()];
				
				sids[Integer.parseInt(my_pid)] = my_sid;

				String[] opp	= opponents.split(" ");
				String[] osp	= opponents_pid.split(" ");
				String[] oss	= opponents_sid.split(" ");
				
				for (int j = 0; j < osp.length; j++)
					sids[Integer.parseInt(osp[j])] = oss[j];

				// The pure strategy combination played...
				String sidss = Game.join(sids, "-");
				
				// Let's memorize what our opponents did...
				for (int i = 0; i < opp.length; i++) {

					// Let's summarize the strategic outcome of the play associated with this opponent
					String[] observation = {osp[i], oss[i], new Double(G.getOutcomes().get(sidss)[Integer.parseInt(osp[i])]).toString(), my_pid, my_sid, my_payoff};
	
					// Let's consider considering memories about this opponent...
					Tuple memory_about_opponent = (Tuple)query_memory.execute("$opponent", opp[i]);
	
					// If we've met this opponent before, then...
					if (memory_about_opponent != null) {
	
						// Retrieve the memory about the given opponent...
						getBeliefbase().getBeliefSet("memory").removeFact(memory_about_opponent);
						Vector<String[]> opponents_action_history = (Vector<String[]>)memory_about_opponent.get(1);
						opponents_action_history.add(observation);
	
						// If the new action history (memory) for the given opponent is too long, then...
						if (opponents_action_history.size() > ((Integer)getBeliefbase().getBelief("oppmem_limit").getFact()).intValue()) {
	
							// Remove an element from the vector...
							opponents_action_history.removeElementAt(0);
	
						}
	
						// Update the memory about the given opponent...
						getBeliefbase().getBeliefSet("memory").addFact(new Tuple(opp[i], opponents_action_history));
	
					} else {
	
						// Create a memory about the given opponent...
						Vector<String[]> opponents_action_history = new Vector<String[]>();
						opponents_action_history.add(observation);
						getBeliefbase().getBeliefSet("memory").addFact(new Tuple(opp[i], opponents_action_history));
	
					} // IF OPP.MEM. NOT NULL
				
				} // FOR all the opponents

				// --------------------------------------------------
				// Now comes the proliferation (asexual reproduction)
				// --------------------------------------------------
				
				// First get all the necessary parameters for reproduction...
				int children 				= ((Integer)getBeliefbase().getBelief("children").getFact()).intValue();
				int max_reproduction_num 	= ((Integer)getBeliefbase().getBelief("max_reproduction_num").getFact()).intValue();
				double reproduction_cost	= ((Double)getBeliefbase().getBelief("reproduction_cost").getFact()).doubleValue();

				boolean repro;
				String child_utility;

				// Standard reproduction...
				if (reproduction_type == 1) {
	
					repro = (utility > reproduction_cost && children < max_reproduction_num);
					child_utility = "0";

				// Artificial reproduction...
				} else if (reproduction_type == 2) {

					repro = (utility > reproduction_cost);
					child_utility = Double.toString(utility);

				// No reproduction...
				} else {

					child_utility = "";
					repro = false;

				}

				// If the number of reproductions is below a limit, and my utility is high enough, then...
				if (repro && reprok.equals("1")) {

					try {
		
						//System.out.println("Agent: " + this.getAgentName() + "; Utility: " + utility + "; Repro.cost: " + reproduction_cost);

						// myType is gathered from the BeliefBase to be able to reproduce exactly...
						int my_type			= ((Integer)getBeliefbase().getBelief("myType").getFact()).intValue();

						// Get the limit on the opponent memories number...
						int memlimit 		= ((Integer)getBeliefbase().getBelief("memlimit").getFact()).intValue();

						// Get the limit on the opponent memories length...
						int oppmem_limit 	= ((Integer)getBeliefbase().getBelief("oppmem_limit").getFact()).intValue();

						// Pass the knowledge of the game currently played to the descendants... ;)
						String my_game			= (String)getBeliefbase().getBelief("gid").getFact();

						// A child (with the same parameters) is created...
						AgentContainer cc	= (AgentContainer)getBeliefbase().getBelief("cc").getFact();
						AgentController ac	= cc.createNewAgent((getAgentName() + "." + children),
																"jadex.adapter.jade.JadeAgentAdapter",
																new String[]{"msclab01.gametheory_lab.PlayerAgent.Player",
																"default",
																"utility="+child_utility,
																"gui="+((Boolean)getBeliefbase().getBelief("gui").getFact()).booleanValue(),
																"myType="+my_type,
																"gid=\"" + my_game + "\"",	// When running from Eclipse, then \\\" needed, e.g.: ...gid=\\\"pd\\\"
																"max_reproduction_num="+max_reproduction_num,
																"reproduction_cost="+reproduction_cost,
																"memlimit="+memlimit,
																"oppmem_limit="+oppmem_limit});
						ac.start();

						// Update the number of children
						children++;

						// Store the number of children
						getBeliefbase().getBelief("children").setFact(children);

						// Standard reproduction (lowers the utility of the parent)...
						if (reproduction_type == 1) {
						
							// Reduce my utility with the cost of reproduction
							utility -= reproduction_cost;
						
						// Artificial reproduction (utility remains, repro.cost doubles)...
						} else if (reproduction_type == 2) {
							
							getBeliefbase().getBelief("reproduction_cost").setFact(new Double(2 * reproduction_cost));
							
						}
							
					} catch (StaleProxyException e) {

						e.printStackTrace();
  
					} // TRY TO REPRODUCE

				} // IF ABLE TO REPRODUCE

				// Store the new value of my utility among my beliefs
				getBeliefbase().getBelief("utility").setFact(utility);
				
				// If the GUI is activated
				if (((Boolean)getBeliefbase().getBelief("gui").getFact()).booleanValue()) {
				
					// Get the GUI
					PlayerGui myGui = (PlayerGui)(getBeliefbase().getBelief("myGui").getFact());
	
					// This is what I think the payoff of the opponents was (in a String)...
					// More than that (e.g. their actual utility) I can't say, because I don't know with whom else they played...
					String opponents_playername		= G.getPlayerName(Integer.parseInt(osp[0]));
					String opponents_stratname		= G.getStratNames(Integer.parseInt(osp[0]))[Integer.parseInt(oss[0])];
					String opponents_payoff 		= new Double(G.getOutcomes().get(sidss)[Integer.parseInt(osp[0])]).toString();
					
					for (int j = 1; j < osp.length; j++) {
						
						opponents_playername	+= " " + G.getPlayerName(Integer.parseInt(osp[j]));
						opponents_stratname		+= " " + G.getStratNames(Integer.parseInt(osp[j]))[Integer.parseInt(oss[j])];
						opponents_payoff 		+= " " + new Double(G.getOutcomes().get(sidss)[Integer.parseInt(osp[j])]).toString();
						
					}
						
					// This is what I think my new utility is (in a String)...
					String my_utility = Double.toString(utility);

					// N players are assumed...
					String my_stratname			= G.getStratNames(Integer.parseInt(my_pid))[Integer.parseInt(my_sid)];
					
					// N players are assumed...
					String my_playername		= G.getPlayerName(Integer.parseInt(my_pid));
					
					// Update the GUI (the utility will reflect everything + payoff - repro.cost... Everything (AT THE END OF THIS ROUND!!!)
					myGui.updatePlayerGui(new String[]{Integer.toString(rounds), opponents, opponents_playername, my_playername, opponents_stratname, my_stratname, opponents_payoff, my_payoff, my_utility});				
	
					// Store the GUI
					getBeliefbase().getBelief("myGui").setFact(myGui);
					
				} // IF GUI

			} // IF UTILITY >= 0

		} // IF NUMBER OF STRING TOKENS IS OK

	} // End of PlayerBRFPlan.body() method

} // End of PlayerBRFPlan class