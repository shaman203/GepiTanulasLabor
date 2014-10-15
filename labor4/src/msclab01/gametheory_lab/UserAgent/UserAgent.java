package msclab01.gametheory_lab.UserAgent;

import java.util.*;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import msclab01.gametheory_lab.*;

/**
 * UserAgent for evolutionary game theoretic simulations
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class UserAgent extends Agent {

	private static final long serialVersionUID = 1L;

	/** The Game object */
	private Game G;
	/** A counter for the rounds that passed */
	private int rounds = 0;
	/** My most actual strategy */
	private String myStrategy;
	/** My most actual roleID */
	private String myPid;
	/** The utility of mine :) */
	private double utility;
  	/** The GUI showing my plays and payoffs in time */
  	private UserAgentGui myGui;
  	/** The GUI showing my plays and payoffs in time */
  	public UserAgentButtonGui myButtonGui;

  	/**
  	 * 
  	 * Setup the agent: get the start-up arguments, and initialize some variables.
  	 * Then start waiting for for requests and informs.
  	 * 
  	 */
	protected void setup() {

		// If at least 1 start-up arguments were given on the input, then...
		Object[] args = getArguments();
		if (args != null && args.length > 1) {

			// Initialize the game according to the input...
			G = new Game((String)args[0]);

			try {

				// The given start-up utility...
				utility = Double.parseDouble((String)args[1]);

			} catch (NumberFormatException nfe) {

		    	// That's hopefully just the beginning... ;)
		    	utility = 0;

				// If the parsing of the double didn't succeed (like 3,6 was written instead of 3.6, etc)
				nfe.printStackTrace();
				
			} // TRY PARSING DOUBLE UTILITY

		} else {
			
	    	// That's hopefully just the beginning... ;)
	    	utility = 0;
			
			if (args != null && args.length == 1) {

				// Initialize the game according to the input...
				G = new Game((String)args[0]);
				
			} else {
	
				// Set up HAWK_DOVE to be the default game...
				G = new Game(Integer.toString(Game.HAWK_DOVE));

			} // IF ARGS.LENGTH == 1
			
		} // IF ARGS.LENGTH > 1

		// If the start-up utility is at least 0, then...
		if (utility < 0) {

			// Re-define the user-given negative value to zero...
			utility = 0;
			
		}
		
		// Register the book-selling service in the yellow pages
	    DFAgentDescription dfd = new DFAgentDescription();
	    dfd.setName(getAID());
	    ServiceDescription sd = new ServiceDescription();
	    sd.setType("player");
	    sd.setName(Integer.toString(PlayerType.USER) + " " + utility);
	    dfd.addServices(sd);

	    try {

	      DFService.register(this, dfd);

	    } catch (FIPAException fe) {

	      fe.printStackTrace();

	    }

	    System.out.println("Hello! User-agent " + getAID().getName() + " is ready to play the " + G.getName() + " game with a start-up utility of " + utility + "...");

		// Create and show the GUI...
		myGui = new UserAgentGui(this);
		myGui.show();

		// Let's start waiting for play-requests and outcome-informs...
		addBehaviour(new RequestServer());
		addBehaviour(new InformReceiver());

	} // End of UserAgent.setup()


	/**
	 * 
	 * Print a goodbye message, dispose the GUI, and let the JADE framework do the rest...
	 * 
	 */
	protected void takeDown() {

		// Printout a dismissal message
		System.out.println("User-agent " + getAID().getName() + " terminating...");

	    // Deregister from the yellow pages
	    try {
	    	
	      DFService.deregister(this);
	      
	    } catch (FIPAException fe) {
	    	
	      fe.printStackTrace();
	      
	    }
	
	  	// Close the GUI
	  	myGui.dispose();
		
	} // End of UserAgent.takeDown()

	/**
	 * 
	 * RequestServer
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class RequestServer extends CyclicBehaviour {
		
		private static final long serialVersionUID = 423911331345878036L;

		// The state of this behaviour (0 or 1)...
		private int step = 0;
		
		// The shortly received message to which we then must construct a reply...
		private ACLMessage msg;
		
		public void action() {
		  	
			// If we are waiting to receive a REQUEST...
			if (step == 0) {
			
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				msg = myAgent.receive(mt);
			  
				// If we have received a REQUEST...
				if (msg != null) {
		    	
					// Get the contents of the message(event) - REQUEST from GameAgent - that caused the execution of this plan...
					StringTokenizer stok = new StringTokenizer(msg.getContent(), "\t");
				  
					// If the structure of the content is right, then...
					if(stok.countTokens() == 3) {
	
						// Get my role...
						int pid		= Integer.parseInt(stok.nextToken());
						myPid		= Integer.toString(pid);
						myButtonGui	= new UserAgentButtonGui((UserAgent)myAgent, myPid);
						myButtonGui.show();
						
						// Get the name and role of the other players...
						String opponents			= stok.nextToken();
						String opproles				= stok.nextToken();
						StringTokenizer oppsstok	= new StringTokenizer(opponents, " ");
						StringTokenizer opprstok	= new StringTokenizer(opproles, " ");
						String oppstr				= oppsstok.nextToken() + " (" + G.getPlayerName(Integer.parseInt(opprstok.nextToken())) + ")";
						
						for (int i = 0; i < opprstok.countTokens(); i++)
							oppstr += ", " + oppsstok.nextToken() + " (" + G.getPlayerName(Integer.parseInt(opprstok.nextToken())) + ")";
						
						// Update the GUI accordingly...
						myGui.updateMessageField("Game:\t" + G.getName() + "\nRole:\t" + G.getPlayerName(pid) + "\nOpponents:\t" + oppstr);
						myButtonGui.setChoiceButtonsText(G.getStratNames(pid));	// Set GUI's choice buttons' text
						myButtonGui.setChoiceButtonsState(true);

						// State := REPLY
						step = 1;
						
					} // IF 3 STRING TOKENS
	
				} else {
		    	
					block();
			    
				} // IF MSG != NULL
			  
			} else { // ELSE STEP == 1
				
				// If my actual strategy was set from the GUI (and so the GUI disabled itself), then...
				if (myStrategy != null) {
					
					// Construct the reply to 
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(myStrategy + " " + utility + " " + PlayerType.USER);
					
					// Set my actual strategy back to null again...
					myStrategy	= null;
					myPid		= null;
					
					// State := RECEIVE
					step = 0;
					
					// Reply with an INFORM message (about the chosen strategy, actual utility, and type) to the GameAgent...
					myAgent.send(reply);
					
				} else {

					// Wait for 1/5 second, and then check again, whether the myStrategy was chosen...
					block(200);

				} // IF MYSTRATEGY IS SET

			} // IF STEP == 0
			  
		  } // End of UserAgent.RequestServer.action() method
	  
	}  // End of inner behaviour class UserAgent.RequestServer
	
	/**
	 * 
	 * InformReceiver
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class InformReceiver extends CyclicBehaviour {

		private static final long serialVersionUID = -3437670509278926205L;

		public void action() {
		  	
			  MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			  ACLMessage msg = myAgent.receive(mt);
			  
			  if (msg != null) {
		    	
					// Get the contents of the message - INFORM from GameAgent -  that caused the execution of this behaviour...
				  	StringTokenizer stok = new StringTokenizer(msg.getContent(), "\t");
				  
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
						//String reprok = stok.nextToken();
						
						// ------------------------------------------------------------

						// Update the number of rounds played
						rounds++;
						
						// Get my actual payoff (which may be positive, zero, or even negative)
						double payoff = new Double(my_payoff).doubleValue();

						// Calculate my new utility
						utility += payoff;

						// If my utility is below zero, then...
						if (utility < 0) {

							// Appropriately terminate the agent...
							doDelete();

						} else { // UTILITY >= 0

							String[] sids	= new String[G.getPlayerNum()];
							
							sids[Integer.parseInt(my_pid)] = my_sid;

							String[] osp	= opponents_pid.split(" ");
							String[] oss	= opponents_sid.split(" ");
							
							for (int j = 0; j < osp.length; j++)
								sids[Integer.parseInt(osp[j])] = oss[j];
							
							String sidss = Game.join(sids, "-");
							
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
							
							// Update the GUI
							myGui.updateGui(new String[]{Integer.toString(rounds), opponents, opponents_playername, my_playername, opponents_stratname, my_stratname, opponents_payoff, my_payoff, my_utility});				

						} // IF UTILITY >= 0						
						
					} // IF NUMBER OF STRING TOKENS IS OK
	
			  } else {
		    	
				  block();
			    
			  }
			  
		  } // End of UserAgent.InformReceiver.action() method
	  
	}  // End of inner behaviour class UserAgent.InformReceiver
	
	/**
	 * 
	 * Returns the actual utility of the agent
	 * 
	 * @return the actual utility of the agent
	 */
	public double getUtility() {
		
		return this.utility;
		
	} // End of UserAgent.getUtility() method

	/**
	 * 
	 * Returns the actual utility of the agent
	 * 
	 * @return the actual strategy of the agent
	 */
	public String getMyStrategy() {

		return this.myStrategy;

	} // End of UserAgent.getUtility() method

	/**
	 * 
	 * Sets the actual strategy of this agent if it is null.
	 * Used by the GUI. The agent will set it again to null.
	 * 
	 * @param s the actual strategy of the agent
	 */
	public void setMyStrategy(String s) {

		if (myStrategy == null) this.myStrategy = s;
		
	} // End of UserAgent.getUtility() method

	/**
	 * 
	 * Return my most actual role
	 * 
	 */
	public String getMyPid() {

		return this.myPid;

	} // End of UserAgent.getUtility() method
	
	/**
	 * 
	 * Returns the actual game of the agent
	 * 
	 * @return the actual game of the agent
	 */
	public Game getMyGame() {

		return this.G;

	} // End of UserAgent.getMyGame() method
	
} // End of class UserAgent
