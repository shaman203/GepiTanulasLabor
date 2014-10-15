package msclab01.gametheory_lab.GameAgent;

import java.util.*;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.lang.sl.SLCodec;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.*;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
//import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.proto.AchieveREInitiator;
import jade.content.onto.basic.Action;

import msclab01.gametheory_lab.*;

/**
 * GameAgent for evolutionary game theoretic simulations
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class GameAgent extends Agent {

	private static final long serialVersionUID = 1L;

	/** The Game object */
	private Game G;
	/** The maximum number of players/round (should be at least 2 !!!) */
	private int max_players;
	/** The type of the GUI (see GameAgentGui class for GUI*.* named constants!) */
	private int guiType;
	/** A counter for the rounds that passed */
	private int rounds = 0;
	
	/** A limit for the number rounds */
	private int max_rounds		= 250;
	/** The time (ms) to wait to begin to search for player agents */
	private int wait_to_start	= 5000;
	
	/** The vector of player agents' AID's chosen to play in a round */
	private Vector<AID> playerAgents;
	/** The N-tuples of player agents' AID's formed to play in N-person games */
	private HashMap<AID, Vector<AID>> playerTuples;
	/** The player ID of the player agents' in a game in round */
	private HashMap<AID, Integer> playersPID;
	/** The strategic choices received from player agents in a round */
	private HashMap<AID, String> playerAnswers;
	/** The utility of player agents (aggregating over the rounds) */
	private HashMap<AID, Double> playersUtility;
	/** The memory of player agents' utility (aggregating over the rounds) */
	private Vector<HashMap<AID, Double>> playersUtilities = new Vector<HashMap<AID, Double>>();
	/** The type of all the player agents ever alive (on our platform) */
	private HashMap<AID, Integer> playersType = new HashMap<AID, Integer>();
	/** The memory of player agents' types */
	private Vector<HashMap<AID, Integer>> playersTypes = new Vector<HashMap<AID, Integer>>();
	/** The dead agents */
	private Vector<AID> deadAgents = new Vector<AID>();
	/** A random number generator (with a random seed) */
	private Random r = new Random();
  	/** The GUI showing the progress of the agent population */
  	private GameAgentGui myGui;

  	/**
  	 * 
  	 * Setup the agent: get the start-up arguments, and initialize some variables.
  	 * Then start looking for potential player agents.
  	 * 
  	 */
	protected void setup() {

		// Register to (understand) some languages/codecs and ontologies...
		Codec codec			= new SLCodec();
		Ontology ontology	= JADEManagementOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		// If at 1 or 2 start-up arguments were given on the input, then...
		Object[] args = getArguments();
		if (args != null && args.length == 1) {

			G 			= new Game((String)args[0]);
			max_players = 500; // A default value...
			guiType		= GameAgentGui.GUI_PLAYERS_UTIL;

		} else if (args != null && args.length == 2) {

			G 			= new Game((String)args[0]);
			max_players = Integer.parseInt((String)args[1]);
			guiType		= GameAgentGui.GUI_PLAYERS_UTIL;
			
		} else if (args != null && args.length >= 3) {

			G 			= new Game((String)args[0]);
			max_players = Integer.parseInt((String)args[1]);
			guiType		= Integer.parseInt((String)args[2]);

		} else { // args == null || args.length == 0...:)

			// Set up some default values...
			G 			= new Game(Integer.toString(Game.HAWK_DOVE));
			max_players = 500;
			guiType		= GameAgentGui.GUI_PLAYERS_UTIL;

		} // IF ARGS.LENGTH > 2

		// Initialize the Map of player-utilities to be an empty HashMap<AID, Double>...
		playersUtility	= new HashMap<AID, Double>();
		
		System.out.println("Hello! Game-agent " + getAID().getName() + " is ready to play the " + G.getName() + " game...");

		// Create and show the GUI...
		myGui = new GameAgentGui(this, guiType);
		myGui.setRadioButtons(false);
		myGui.show();

		// Try to wait a few seconds before starting to search for players...
		// This time shall be enough for any initial player-population to start completely
		// after this game agent is born...
		addBehaviour(new WakerBehaviour(this, wait_to_start) {

			private static final long serialVersionUID = -8726861432928954325L;

			protected void onWake() {
				
				// Let's start looking for player agents...
				addBehaviour(new Search4Players());
				
			} // End of WakerBehaviour.onWake()
			
		}); // End of adding a WakerBehaviour to add a Search4Players behaviour...

	} // End of GameAgent.setup()


	/**
	 * 
	 * Print a goodbye message, dispose the GUI, and let the JADE framework do the rest...
	 * 
	 */
	protected void takeDown() {

		// Printout a dismissal message
		System.out.println("Game-agent " + getAID().getName() + " terminating...");

	    // We don't have to deregister from the yellow pages, since we haven't registered... Invisible agent:)
	    /*try {
	    	
	      DFService.deregister(this);
	      
	    } catch (FIPAException fe) {
	    	
	      fe.printStackTrace();
	      
	    }*/
		
	  	// Close the GUI
	  	myGui.dispose();
		
	} // End of GameAgent.takeDown()

	/**
	 * JADE behaviour: search for potential player agents for a round...
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class Search4Players extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {
			
			System.out.println("Trying to locate players for round " + rounds + "...");

			// Create the search template for the DF agent...
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd		= new ServiceDescription();
			sd.setType("player");
			template.addServices(sd);

			// Create some search constraints...
			//SearchConstraints sc		= new SearchConstraints();
			//sc.setMaxResults(new Long(200)); // The limit on the number of search-results...
			
			try {

				// (Re)initialize a vector for players to be chosen in this round...
        		playerAgents = new Vector<AID>();

        		AID a;

        		// Lets generate a random int between N and max_players: the number of players/round!
        		// We expect max_players to be at least N............................................
        		int max_round_players = r.nextInt(max_players-1) + G.getPlayerNum();

				// Query the DF agent for players...
				DFAgentDescription[] result = DFService.search(myAgent, template);
				//DFAgentDescription[] result = DFService.search(myAgent, template, sc);

				// Initialize a vector for all the players found in this round...
				Vector<AID> allPlayerAgents = new Vector<AID>(result.length);

				// Convert the result array to a vector...
				for (int i = 0; i < result.length; ++i) {
					
					allPlayerAgents.add(result[i].getName());
					
				} // FOR RESULT
				
				// Let's make a copy of playersUtility for Thread-Safety reasons!!!...
        		HashMap<AID, Double> playersUtility_copy = new HashMap<AID, Double>();
        		Iterator<AID> pu = playersUtility.keySet().iterator();
    			
        		// Do the copying............
        		while(pu.hasNext()) {

    				a = pu.next();
    				playersUtility_copy.put(a, playersUtility.get(a));

    			} // WHILE HAS NEXT

    			// Now let's check if every agent - whose utility is stored (in playersUtility) - is still alive!
          		Iterator<AID> puc = playersUtility_copy.keySet().iterator();

          		while(puc.hasNext()) {

    				a = puc.next();
    				
    				// If the agent disappeared (e.g. died), then...
    				if (!allPlayerAgents.contains(a)) {

    					playersUtility.remove(a);

    				} // IF NOT CONTAINS
	
    			} // WHILE HAS NEXT
     		
        		// Choose players for this round...
        		for (int i = 0; i < result.length; ++i) {

        			// Get the AID of the found agent...
        			a = result[i].getName();
        			
        			// If the agent isn't on the deadlist, then...
        			// -------------------------------------------------------------------------------
        			// It may happen, that a player, after being previously notified of his below-zero
        			// utility, and thus starts to terminate, does it "too slowly", and so we find him/her
        			// still registered to the platform, when querying the DF for potential players for the
        			// next round. This could cause a problem, because we could handle this agent as a
        			// player, and thus send him/her an action-request, although he/she is terminating
        			// (or is terminated), and certainly won't answer - kind of a deadlock...
        			// We could implement timeout's (waiting here and there), but this would decrease
        			// the overall system performance significantly. Besides, this is the reason behind
        			// letting players terminate themselves instead of asking the AMS to terminate them.
        			// 1 more thing: this approach excludes resurrection... ;-) That is: if an agent, who
        			// is known to be dead, is found when querying the DF (i.e. when a new agent with the
        			// same local name was started (maybe by a user) after the previous one terminated),
        			// then we won't handle such an agent as a potential player.
        			// ---------------------------------------------------------
        			if (!deadAgents.contains(a)) {

	        			// If we meet this agent for the first time, then...
	        			if(!playersUtility.containsKey(a)/* && playersUtility.size() < max_players*//*max_round_players*/) { // !!!!!!

	        				// If the agent has services defined...
	        				Iterator<ServiceDescription> asdi = result[i].getAllServices();
	            			if (asdi.hasNext()) {

	            				// The first service-name of the agent of service-type "player" is the "typeID initialUtility" string...
	            				StringTokenizer stok = new StringTokenizer(((ServiceDescription)asdi.next()).getName(), " ");
	            				
	            				// Let's properly initialize the typeID of this agent-player...
	            				playersType.put(a, new Integer(stok.nextToken()));

	            				// Let's properly initialize the utility of this agent-player...
	            				playersUtility.put(a, new Double(stok.nextToken()));
	            				
	            			} else { // Not proper registration...
	            				
	            				// Let's give it a default type! This way, for example, when the GUI considers
	    	        			// the type of this player for choosing a color for him/her on the chart, then
	    	        			// those players, who are in playersUtility, but never played before, will have
	    	        			// a GRAY color by default. Sometime later, when they perhaps become chosen to
	    	        			// play, and inform us of their type, the GUI will be updated accordingly...
	            				playersType.put(a, PlayerType.UNKNOWN);
	            				
	            				// ...an initial utility (which may not be true)...
	            				playersUtility.put(a, new Double(0));
	            				
	            			} // IF SERVICEDESCRIPTION.HASNEXT
	            			
	        			} // IF PLAYERSUTILITY.CONTAINSKEY && PU.SIZE < MAX

	        			// Every possible/allowed player shall play in this round... 
	        			if (playersUtility.containsKey(a) /*r.nextInt(2) == 1 && */) {

	        				// ...choose this agent to play in this round!
	        				playerAgents.add(a);

	        			} // IF SELECTED (this could be modified to a rulette wheel, maybe)
	        			
        			} // IF NOT DEAD

        		} // FOR RESULT

        		System.out.println("- " + playerAgents.size() + "/" + playersUtility.size() + "/" + result.length + " players chosen to play in this round...");
        		// If the number of chosen players is odd, then - because of pairing - one will be left out.

			} catch (FIPAException fe) {

				fe.printStackTrace();

			}
			
			if (playersUtility.size() <= max_players && rounds < max_rounds) { // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				
				// If at least N players were chosen to play in this round, then...
				if (playerAgents.size() > G.getPlayerNum()-1) {
	
	        		// Initialize the Map of players' answers in this round...
	        		playerAnswers = new HashMap<AID, String>(playerAgents.size());
	
	        		// Combine the agents (give value to playerTuples)...
					makeTuples();
					
					// If the Search4Players behaviour is called first time before a play-execution...
					if (rounds == 0) {
						
						// Let's make a copy of playersUtility for Thread-Safety reasons!!!...
		        		HashMap<AID, Double> playersUtility_copy = new HashMap<AID, Double>();
		        		Iterator<AID> pu = playersUtility.keySet().iterator();
		    			
		        		// Do the copying............
		        		while(pu.hasNext()) {

		        			AID a = pu.next();	// Is ConcurrentModificationException possible here?...
		    				playersUtility_copy.put(a, playersUtility.get(a));

		    			} // WHILE HAS NEXT
						
						// Put everything into the archive...
						playersUtilities.add(playersUtility_copy); playersTypes.add(playersType);
						
						// Update the GUI with the initial utilities of the players...
						myGui.updateChart(guiType, Integer.toString(rounds), playersUtility, playersType);
						
					}
	
					// Let's execute a round...
					myAgent.addBehaviour(new ExecutePlays());
	
				} else {
	
					// Let's start over, and look for potential players for a round...
					myAgent.addBehaviour(new Search4Players());
	
				} // IF >= N PLAYERS WERE CHOSEN TO PLAY IN THIS ROUND

			} else {
				
				System.out.println("Finished...");
				
				// Enables the radio buttons on the GUI allowing to change the type of the data-chart diagram...
				myGui.setRadioButtons(true);

			} // IF ROUNDS < MAX_ROUNDS
				
	      } // End of GameAgent.Search4Players.action() method

	} // End of GameAgent.Search4Players behaviour

	/**
	 * JADE behaviour: execute a round, i.e. ask players to act, then wait for their reply (their action),
	 * calculate their outcome, and finally notify them about this.
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class ExecutePlays extends Behaviour {

		private static final long serialVersionUID = 1L;
		
		// The counter of replies receiver in this round from the current player agents. Initially zero...
		private int repliesCnt = 0;
		
		// The initial state of this behaviour...
		private int step = 0;			

		public void action() {
			
			switch (step) {

		    	case 0:

		    		// Send an appropriate action-request to all (at least N) player agents, who play in this round (i.e. are associated)
		    		for (int i = 0; i < playerAgents.size(); ++i) {

		    			// Let's get the i-th agent (say player i) playing in this round...
		    			AID pi = playerAgents.get(i);
		    			
		    			// Let's try to get the opponents of player agent i...
		    			Vector<AID> pjs = playerTuples.get(pi);

		    			// If player agent i has opponents, then...
		    			if (pjs != null) {

		    				// 3 things are sent to the agents:
			    			// --------------------------------
			    			// (1)	The number of the player/role they (pi) assume in the game
			    			// (2)  The name of the opponents against whom they'll play
		    				// (3)	The roles those opponents assume in the current game/play
		    				
		    				// Construct an ACTION-REQUEST message to player i...
			    			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			    			msg.addReceiver(pi);
		    				
			    			String pjss		= pjs.get(0).getLocalName();
			    			String pjsps	= playersPID.get(pjs.get(0)).toString();
			    			
			    			for (int j = 1; j < pjs.size(); j++) {
			    				
			    				AID pj	= pjs.get(j);
			    				
			    				pjss 	+= " " + pj.getLocalName();
			    				pjsps	+= " " + playersPID.get(pj).toString();
			    				
			    			}
			    			
			    			msg.setContent(playersPID.get(pi).toString() + "\t" + pjss  + "\t" + pjsps);
				    		msg.setConversationId("action-info");

				    		// Send the ACTION-REQUEST message to player i...
				    		send(msg);

		    			} // IF Pi HAS PAIR(S)

		    		} // FOR CHOSEN PLAYERS

		    		// Let's continue to the next step/state: wait for players' reply...
			   		step = 1;

		    		break;

		    	case 1:

		    		// Receive a message (describing the current actions of the player agents)
		    		ACLMessage reply = myAgent.receive(MessageTemplate.MatchConversationId("action-info"));

		    		// If a message was received, then...
		    		if (reply != null) {

		    			AID pi = reply.getSender();
		    			StringTokenizer stok = new StringTokenizer(reply.getContent(), " ");
		    			
		    			// If an appropriate INFORM message was received from one of the players of the current round, then...
		    			if (reply.getPerformative() == ACLMessage.INFORM && stok.countTokens() == 3 && playerAgents.contains(pi)) {

		    				// An appropriate reply was received
		    				repliesCnt++;
		    				
		    				// Player i says that - in this round - he/she wants to do...
		    				String piAction = stok.nextToken();
		    				playerAnswers.put(pi, piAction);
		    				
		    				// Player i says that - at the beginning of this round - his/her utility is...
		    				playersUtility.put(pi, new Double(stok.nextToken()));
		    				
		    				// If the typeID of this agent-player was unknown until yet, then...
		    				if (playersType.get(pi).intValue() == PlayerType.UNKNOWN)
		    							playersType.put(pi, new Integer(stok.nextToken()));

		    				// Let's try to get the opponents of player i...
		    				Vector<AID> pjs = playerTuples.get(pi);

		    				// If player i has opponents, then...
		    				if (pjs != null) {

		    					int jj = 0;
		    					boolean allsent = true; // Let's be optimistic:) ...mindenki szent...
		    					while (jj < pjs.size() && allsent) {

		    						if (playerAnswers.get(pjs.get(jj)) == null) allsent = false;
		    						jj++;
		    						
		    					}

		    					// If every opponent of pi had told us already, what he/she wants to do in this round, then...
		    					if (allsent) {	// If every opponent of pi chose a strategy

		    						// ----------------------------------------------
					    			// Six things are sent back to each player-agent:
					    			// ----------------------------------------------
					    			// (1)  The names of the opponents against whom they've played
					    			// (2)	The number of the player/role the opponents assumed in the play
					    			// (3)	The number of the strategies the opponents played in the play
		    						// (4)	The number of the player/role they have assumed in the play
					    			// (5)	The number of the strategy they have played during the play
					    			// (6)	Their respective payoff after the play

		    						// Let's make a copy of pjs (to be able to modify the copy without modifying the real pjs)
		    						Vector<AID> pjs_copy = new Vector<AID>(pjs.size()+1);
			    		    			
		    		        		// Do the copying............
		    						for (int j = 0; j < pjs.size(); j++) {

		    							pjs_copy.add(pjs.get(j));

		    		    			}
		    						
		    						// Let us add ourselves to these players...
		    						pjs_copy.add(pi);
		    						
		    						// Let's construct the strategy combination defining the outcome...
		    						String[] sids	= new String[pjs_copy.size()];
		    						for (int i = 0; i < pjs_copy.size(); i++) {
			    						AID p_i = pjs_copy.get(i);
			    						sids[playersPID.get(p_i).intValue()] = playerAnswers.get(p_i);
			    					}
		    						
		    						String sidss = Game.join(sids, "-");
		    						
	    							// Can the players reproduce?
	    							String reprok;
	    							
	    							// Don't forget, that when there is a little bit less, than the
	    							// maximum of players, and we allow them to reproduce, there may
	    							// happen to be more agents created on the platform, than the maximum...
	    							if (playersUtility.size() >= max_players) reprok = "0"; else reprok = "1";
	    							
	    							// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	    							// reprok = "0"; // NO REPRODUCTION NOW!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	    							// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		    						
		    						for (int i = 0; i < pjs_copy.size(); i++) {
		    							
		    							// A player
			    						AID p_i = pjs_copy.get(i);
		    						
		    							// The role of the player agent
		    							int p_iRole = playersPID.get(p_i).intValue();
		    							
		    							// Let's get the names, roles, and actions of the opponents of the player...
		    		    				Vector<AID> p_js	= playerTuples.get(p_i);
		    			    			String p_jNames		= p_js.get(0).getLocalName();
		    			    			String p_jRoles		= playersPID.get(p_js.get(0)).toString();
		    			    			String p_jActions	= playerAnswers.get(p_js.get(0)).toString();
		    			    			for (int j = 1; j < p_js.size(); ++j) {
		    			    				
		    			    				p_jNames	+= " " + p_js.get(j).getLocalName();
		    			    				p_jRoles	+= " " + playersPID.get(p_js.get(j)).toString();
			    			    			p_jActions	+= " " + playerAnswers.get(p_js.get(j)).toString();
			    			    			
		    			    			}
		    			    			
		    							// The action of the player agent
		    							String p_iAction = playerAnswers.get(p_i);
			    						
			    						// Let's calculate the payoffs of players in case of that strat. comb.
			    						double p_iPayoff = G.getOutcomes().get(sidss)[p_iRole];
			    						
			    						// Let's get the actual utility of the players...
			    						double p_iUtility = playersUtility.get(p_i).doubleValue();
			    						
			    						// Let's increment the aggregated utility of the players respective to their current payoff...
			    						p_iUtility += p_iPayoff;
			    						
			    						// If the new utility of players went below zero, then...
			    						if (p_iUtility < 0) deadAgents.add(p_i);
			    						
		    							// Let's store the new utility values...
		    							playersUtility.put(p_i, new Double(p_iUtility));
		    							
			    						// Let's construct the replies to the players...
			    						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			    						msg.addReceiver(p_i);
			    						msg.setContent(p_jNames + "\t" + p_jRoles + "\t" + p_jActions + "\t" + p_iRole + "\t" + p_iAction + "\t" + p_iPayoff + "\t" + reprok);
		
			    						// Let's notify the players of the outcome of the play...
							    		send(msg);
							    		
		    						}    		

		    					} // IF PjsACTIONs != NULL

		    				} // IF Pjs != NULL

		    				// If every member of every game had sent us his/her reply, then...
		    				if (repliesCnt >= playerTuples.size()) {

		    					// We've received all replies
		    					step = 2;

		    				} // IF REPLIESCNT >= LENGTH

		    			} // IF PERFORMATIVE == INFORM && CONTENT_SYNTAX_OK && PLAYER IS NOT A "HACKER" :)

		    		} else {

	    				block();

	    			} // IF REPLY == NULL
	     
		    		break;

	    } // End of SWITCH       

	  } // End of GameAgent.ExecutePlays.action()

	  public boolean done() {
	
		  if (step == 2) {

			  	// Increment the round-number counter...
			  	rounds++;
			
			  	// Let's make a copy of playersUtility for Thread-Safety reasons!!!...
        		HashMap<AID, Double> playersUtility_copy = new HashMap<AID, Double>();
        		Iterator<AID> pu = playersUtility.keySet().iterator();
    			
        		// Do the copying............
        		while(pu.hasNext()) {

        			AID a = pu.next();	// Is ConcurrentModificationException possible here?...
    				playersUtility_copy.put(a, playersUtility.get(a));

    			} // WHILE HAS NEXT
			  	
				// Put everything into the archive...
				playersUtilities.add(playersUtility_copy); playersTypes.add(playersType);
			  
				// Update the GUI with the actual utilities of the players in this round...
				myGui.updateChart(guiType, Integer.toString(rounds), playersUtility, playersType);
			
			  	// Let's start over, and look for potential players for the next round...
			  	myAgent.addBehaviour(new Search4Players());
			
			  	// Deactivate this behaviour...
			  	return true;

		  } else {

			  	// Let this behaviour be active...
			  	return false;
 
		  }

	  } // End of GameAgent.ExecutePlays.done()

	} // End of GameAgent.ExecutePlays behaviour


	/** Associate the player agents, and associate a playerID/roleID to every one of them. 
	 *  The roleID defines which player/role the agent will have to assume in the game. */
	private void makeTuples() {

		// At first all of these players are unassociated...
		int unassociated_player_num = playerAgents.size();
		
		// Make a local copy of playerAgents to manipulate it freely...
		Vector<AID> players	= new Vector<AID>(unassociated_player_num);
		
		// Do the copying............
		for (int i = 0; i < unassociated_player_num; i++) players.add(playerAgents.get(i));

		playerTuples	= new HashMap<AID, Vector<AID>>();
		playersPID		= new HashMap<AID, Integer>();

		// While there is still enough agents to associate to plays of the game...
		while (unassociated_player_num > G.getPlayerNum()-1) {
			
			// Put different numbers into the play vector
			Vector<Integer> play = new Vector<Integer>(G.getPlayerNum());	// Chosen player ID's
			for (int j = 0; j < G.getPlayerNum(); j++) {
				
				int i = r.nextInt(unassociated_player_num);
				while (play.contains(new Integer(i)))
					i = r.nextInt(unassociated_player_num);
				
				play.add(new Integer(i));
				
			} // While: choose player-agents for this play...
			
			// Sort player agents ID's playing in this play in descending order
			Collections.sort(play, Collections.reverseOrder());

			// Associate the players in playerTuples
			for (int i = 0; i < G.getPlayerNum(); i++) {
			
				Vector<AID> pjs = new Vector<AID>(G.getPlayerNum()-1);	// Chosen opponents of a player i	
				for (int j = 0; j < G.getPlayerNum(); j++)
					if (j != i)
						pjs.add(players.get(play.get(j).intValue()));
				
				playerTuples.put(players.get(play.get(i).intValue()), pjs);
				
			}
			
			// Put different numbers into the roles vector and associate these roles with the player agents
			Vector<Integer> roles = new Vector<Integer>(G.getPlayerNum());
			for (int i = 0; i < G.getPlayerNum(); i++) {
				
				int j = r.nextInt(G.getPlayerNum());
				while (roles.contains(new Integer(j)))
					j = r.nextInt(G.getPlayerNum());
				
				int rolei = new Integer(j);
				
				roles.add(rolei);
				
				playersPID.put(players.get(play.get(i).intValue()), rolei);
				
				// This won't make a problem here since play is sorted in descended order
				players.remove(play.get(i).intValue());
				
			} // While: generate random roles for the player agents...

			unassociated_player_num -= G.getPlayerNum();

		} // WHILE UPN >= N

	} // End of GameAgent.makeTuples()

	/**
	 * Request the termination of an agent from the AMS
	 * 
	 * @param a The agent to kill.
	 */
	private void kill (AID a) {

		KillAgent ka		= new KillAgent();
		ka.setAgent(a);
		Action actExpr		= new Action(getAMS(), ka);
		ACLMessage request	= new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(getAMS());
		request.setOntology(JADEManagementOntology.getInstance().getName());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		try {

			getContentManager().fillContent(request, actExpr);
			addBehaviour(new AchieveREInitiator(this, request) {

				private static final long serialVersionUID = 1L;

				protected void handleInform(ACLMessage inform) {

					System.out.println("Agent successfully created");

				}

				protected void handleFailure(ACLMessage failure) {

					System.out.println("Error creating agent.");

				}

			}); // End of inner anonym AchieveREInitiator behaviour class

		} catch (Exception e) {

			e.printStackTrace();

		} // END OF TRY-CATCH

	} // End of GameAgent.kill() method

	/**
	 * Reset the type of the GUI
	 * 
	 * @param newtype the ID of the new GUI type (cf. GameAgentGui.GUI*.* constants).
	 */
	public void renewGui (int newtype) {
		
	  	// Close the current GUI...
	  	myGui.dispose();
		
	  	// Refresh the GUI type...
		guiType = newtype;
		
		// Create and show the new GUI...
		myGui = new GameAgentGui(this, guiType);
		myGui.show();
		
		// Update the new GUI with the appropriate data...
		for (int i = 0; i < playersTypes.size(); i++)
			myGui.updateChart(guiType, Integer.toString(i), playersUtilities.elementAt(i), playersTypes.elementAt(i));
		
	}
	
} // End of class GameAgent
