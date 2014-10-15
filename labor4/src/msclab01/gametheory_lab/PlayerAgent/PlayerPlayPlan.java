package msclab01.gametheory_lab.PlayerAgent;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import jadex.runtime.*;
import jadex.util.Tuple;
import msclab01.gametheory_lab.*;

/**
 * JADEX player agent's play plan
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class PlayerPlayPlan extends Plan {

	private static final long serialVersionUID = 1L;

	/** A random number generator (with a random seed) */
	private Random r = new Random();

	public PlayerPlayPlan() {

		//getLogger().info("Created: "+this);

	} // End of PlayerPlayPlan.PlayerPlayPlan() constructor

	public void body() {
		
		// Get the contents of the message(event) - REQUEST from GameAgent - that caused the execution of this plan...
		StringTokenizer stok = new StringTokenizer(	(String)((IMessageEvent)getInitialEvent()).getContent(), "\t");
		
		// If the structure of the content is right, then...
		if(stok.countTokens() == 3) {

			// Get the description of the current game...
			Game G			= (Game)getBeliefbase().getBelief("G").getFact();
			
			// Parse my PlayerID...
			int pid			= Integer.parseInt(stok.nextToken());
			
			// Parse the names of the opponent agents...
			String[] opps	= stok.nextToken().split(" ");
			
			// Parse the actual role of those opponent agents in the current play of the game...
			String[] oppr	= stok.nextToken().split(" ");
			
			// Get my TypeID
			int tid			= ((Integer)getBeliefbase().getBelief("myType").getFact()).intValue();

			// Reply with an INFORM message (about the chosen strategy, actual utility, and type)...
			sendMessage(((IMessageEvent)getInitialEvent()).createReply("inform", (chooseStrategy(G, pid, tid, opps, oppr) + " " + getBeliefbase().getBelief("utility").getFact() + " " + tid)));

		} // IF 3 STRING TOKENS

	}  // End of PlayerPlayPlan.body() method

	/** The program of the agent-plan for strategy choice
	 * 
	 * @param	G:		the game the player thinks he/she is playing
	 * @param	pid: 	the ID of the player/role we must assume in the game
	 * @param	tid: 	the ID of our type (e.g. pure hawk, tit4tat), i.e. the ID of our strategy selection mechanism/algorithm/program
	 * @param	opps:	the local names of the opponent-player-agents (just to know with whom we are dealing with (again?))
	 * @param	oppr:	the actual role of these opponent-player-agents in the current play of the game (just to know in what role they are (again?))	 
	 * @return	the String corresponding to the int-value of the chosen PURE strategy
	 */
	private String chooseStrategy (Game G, int pid, int tid, String[] opps, String[] oppr) {

		// Set a default value to return...
		String returnval = "0";
		
		// Switch by player type...
		switch(tid) {

			// ------------------------------------------------------------------------------------
			case PlayerType.RANDOM:

				// Return a String of the int-value of a randomly (according to uniform distribution) chosen PURE strategy of mine
				returnval = Integer.toString(r.nextInt(G.getStratNums(pid)));

				break;

			// ------------------------------------------------------------------------------------
			case PlayerType.DOMINANT:

				/*if (pid == 0) {

					// If strategy 1 is dominant, then...
					if ((myPayoffs[1][0] >= myPayoffs[0][0]) && (myPayoffs[1][1] >= myPayoffs[0][1])) {

						returnval = "1";

					} else {

						returnval = "0";

					}

				} else { // PID == 1

					// If strategy 1 is dominant, then...
					if ((myPayoffs[0][1] >= myPayoffs[0][0]) && (myPayoffs[1][1] >= myPayoffs[1][0])) {

						returnval = "1";

					} else {

						returnval = "0";

					}			

				}*/

				break;

			// ------------------------------------------------------------------------------------
			case PlayerType.NASH:

				// returnval = "the strategy prescribed by a - possibly maximal - Nash-equilibrium of the game";
				
				// Get the expected payoff of every player for every solution of this game...
				Vector<double[]> eps = G.getSolutionsEP();
				
				// Let's suppose that the expected payoff of the 1st solution is maximal for me...
				int maxk = 0; double maxep = eps.get(maxk)[pid];
				
				// Select the first solution whose expected payoff is maximal for me...
				for (int k = 1; k < eps.size(); k++)
					if (eps.get(k)[pid] > maxep) {
						
						maxep	= eps.get(k)[pid];
						maxk	= k;
						
					}
						
				// Return a pure strategy ID chosen accordingly to the mixed strategy prescribed to me by the maximal solution...
				returnval = Integer.toString(playMixedStrategy(G.getSolutions().get(maxk).get(pid)));
				
				break;

			// ------------------------------------------------------------------------------------
			case PlayerType.PARETO:

				// returnval = "the strategy prescribed by a - possibly maximal - Pareto-optimum of the game";

				break;

			// ------------------------------------------------------------------------------------		
			case PlayerType.TIT4TAT:

				// IF cooperation can be interpreted in this game (for every player)...
				if (G.getCooperative()) {
					
					// =========================================
					// 1/2: MAKING A DECISION PER EVERY OPPONENT
					// -----------------------------------------
				
					// Is the game symmetric?
					boolean is_symmetric = G.getSymmetry();
					
					// Initialize an array holding our strategic responses against every of our opponents...
					String[] rets = new String[opps.length];
					
					// Our cooperative strategy at the moment...
					String our_coop_strat = Integer.toString(G.coopStrats[pid]);
					
					// Create an OQL (Object Query Language) expression to retrieve memories about opponents from the BeliefBase...
					IExpression query_memory = getExpression("query_memory");
	
					for (int i = 0; i < opps.length; i++) {
						
						// VOTEi=COOPERATIVE (let's be positive at the beginning...:)
						rets[i] = our_coop_strat;
						
						// Let's consider considering memories about this opponent...
						Tuple memory_about_opponent = (Tuple)query_memory.execute("$opponent", opps[i]);
		
						// If we've met this opponent before, then...
						if (memory_about_opponent != null) {
		
							// Get the second element of the Tuple: the action history (with me) of the player...
							Vector<String[]> opponents_action_history = (Vector<String[]>)memory_about_opponent.get(1);
		
							// --------------------------------------------------------------
							// The opponents_action_history is a Vector of observations about
							// a given opponent in chronological order. An observation is a
							// String[], which describes a memory of a play with the given
							// opponent. It consists of the following elements:
							// --------------------------------------------------------------
							// [0]	opponent_pid		The role of the opponent
							// [1]	opponent_sid		The strategy he/she played
							// [2]	opponent_payoff		The payoff he/she got
							// [3]	my_pid				My role
							// [4]	my_sid				My strategy
							// [5]	my_payoff			My payoff
							// --------------------------------------------------------------

							// If the game is symmetric, then...
							if (is_symmetric) {
								
								// VOTEi=REFLECTION (return his/her previous action - 2nd element of the last observation...)
								rets[i] = opponents_action_history.lastElement()[1];
								
							} else {
								
								String[] opp_obs;
								boolean found	= false;
								int opp_obs_num = opponents_action_history.size();
								
								// While not found the appropriate observation, and there are obs. left, then...
								while (!found && opp_obs_num > 0) {
									
									opp_obs_num--;
									opp_obs = opponents_action_history.elementAt(opp_obs_num);
									
									// If the opponent played the same role as now, then...
									if (opp_obs[0].equals(oppr[i])) {
									
										// We played against the i-th opponent who was in the same role as now...
										found	= true;
										
										// If the i-th opponent didn't play his cooperative strategy last time we met him/her in the actual role, then...
										if (!opp_obs[1].equals(Integer.toString(G.coopStrats[Integer.parseInt(oppr[i])]))) {
											
											// VOTEi=RANDOM (overwrite the initially assumed cooperative strategy with a random choice...)
											rets[i]	= Integer.toString(r.nextInt(G.getStratNums(pid)));
											
										} // ...ELSE remain by cooperating with the i-th opponent...
										
									} // IF OPP. WAS IN THE SOME ROLE AS NOW
																
								} // WHILE NOT FOUND
								
							} // IF IS SYMMETRIC
							
						} // IF MEMORY ABOUT OPP. IS NOT NULL
						
					} // FOR OPPONENTS

					// =========================================
					// 2/2: AGGREGATING OUR INDIVIDUAL DECISIONS
					// -----------------------------------------
					
					// Initialize the number of cooperative decisions...
					double coops = 0;
					
					// Calculate the actual number of cooperative decisions...
					for (int i = 0; i < rets.length; i++)
						if (rets[i].equals(our_coop_strat)) coops++;
					
					// If we wouldn't play cooperatively with the majority of our opponents, then...
					if (coops < rets.length/2.0) {
						
						// Lets create an int-array to hold the number of those times when we chose a given strategy above in rets[]...
						int[] stratchoices = new int[G.getStratNums(pid)];
						
						// Lets initialize that array...
						for (int j = 0; j < G.getStratNums(pid); j++)
							stratchoices[j] = 0;

						// Lets calculate the number of times a given strategy was chosen above...
						for (int i = 0; i < rets.length; i++)
							stratchoices[Integer.parseInt(rets[i])]++;
						
						// Lets suppose that the 0-th strategy got the most votes...
						int maxsci = 0;
						
						// ...and now find the real maximum!
						for (int j = 1; j < G.getStratNums(pid); j++)
							if (stratchoices[maxsci] < stratchoices[j])
								maxsci = j;
						
						// ---------- MOST VOTED ------------
						returnval = Integer.toString(maxsci);
						
					} else {
						
						// ----- COOPERATIVE ------
						returnval = our_coop_strat;
						
					} // ELSE IF (coops >= rets.length/2.0)
					
				} else {
					
					// ----------------------- RANDOM ---------------------------
					returnval = Integer.toString(r.nextInt(G.getStratNums(pid)));
					
				} // ELSE IF cooperation can't be interpreted in the current game (for every player)...
				
				break;

			// ------------------------------------------------------------------------------------	
			case PlayerType.HAWK:
				
				returnval = "0";
				
				break;
			
			// ------------------------------------------------------------------------------------
			case PlayerType.DOVE:
				
				returnval = "1";
				
				break;

			// ------------------------------------------------------------------------------------				
			case PlayerType.TALK:
				
				returnval = "0";
				
				break;

			// ------------------------------------------------------------------------------------
			case PlayerType.SILENT:
				
				returnval = "1";
				
				break;
				
			// ------------------------------------------------------------------------------------
			case PlayerType.RECKLESS:
				
				returnval = "0";
				
				break;
			// ------------------------------------------------------------------------------------	
			case PlayerType.CHICKEN:
				
				returnval = "1";
				
				break;
			
			// ------------------------------------------------------------------------------------
			case PlayerType.OPERA:
				
				returnval = "0";
				
				break;
				
			// ------------------------------------------------------------------------------------	
			case PlayerType.FOOTBALL:
				
				returnval = "1";
				
				break;

			// ------------------------------------------------------------------------------------
			case PlayerType.DEFECT:
				
				returnval = "0";
				
				break;
				
			// ------------------------------------------------------------------------------------	
			case PlayerType.COOPERATE:
				
				returnval = "1";
				
				break;
				
			// ------------------------------------------------------------------------------------
			case PlayerType.GO:
				
				returnval = "0";
				
				break;
				
			// ------------------------------------------------------------------------------------
			case PlayerType.WAIT:
				
				returnval = "1";
				
				break;
				
			// ------------------------------------------------------------------------------------
			case PlayerType.HEADS:
				
				returnval = "0";
				
				break;
				
			// ------------------------------------------------------------------------------------
			case PlayerType.TAILS:
				
				returnval = "1";
				
				break;
				
		} // END OF SWITCH
		
		return returnval;

	} // End of PlayerPlayPlan.chooseStrategy() method
	
	/** The program of the agent-plan for strategy choice
	 * 
	 * @param	qi:		the game the player thinks he/she is playing
	 * @return	the int-value of the chosen PURE strategy
	 */
	private int playMixedStrategy (double[] qi) {
		
		// Initialize the return value (the pure strategy ID)...
		int si		= 0;
		
		// Initialize the sum of probabilities...
		double sum	= qi[0];
		
		// Generate a pseudo-random number (according to approx. uniform distribution) in [0,1) ...
		double rand = r.nextDouble();
		
		while (sum <= rand && si < qi.length-1) {
			
			si++;
			sum += qi[si];

		}
		
		return si;
		
	} // End of PlayerPlayPlan.playMixedStrategy() method

} // End of PlayerPlayPlan class
