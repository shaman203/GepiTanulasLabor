/**
 * Copyright (c) 2010
 * Budapest University of Technology and Economics (BUTE)
 * Department of Measurement and Information Systems (DMIS)
 * All Rights Reserved.
 * 
 * "Cooperative and Learning Systems (VIMIM223)" laboratory
 * "Auctions and Voting" excercise
 * 
 * 				----------------
 * 				Auctioneer agent
 * 				----------------
 * 
 * Author:		Dániel László, Kovács (dkovacs@mit.bme.hu)
 * 
 */

package msclab01.votingauction_lab.AuctioneerAgent;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.io.File;
import java.util.*;
import java.text.*;

import msclab01.*;
import msclab01.votingauction_lab.DataHandler;

/**
 * JADE agent: AuctioneerAgent
 * An auctioneer agent for english auctions...
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class AuctioneerAgent extends Agent {
	
	private static final long serialVersionUID = 1733399087955765502L;
	
	//-------------------------- Things that won't change during the auction --------------------------
	/** The UID (Unique Identifier) of the Auction: the timestamp of the moment the Auctioneer came to "life"*/
	private String auid = "";
	/** The beginning and the end of the auction (by System.currentTimeMillis())...*/
	private long startTimeOfAuction;
	/** The filepath of the configuration file describing the main parameters of the auction*/
	private String configFilePath;
	/** The time (in miliseconds) to wait for a proposal*/
	private long waitingtime = 3000;
	/** The GUI by means of which the user can interact with this agent*/
	private AuctioneerAgentGui myGui;
	/** A "pointer" at the instance of the behaviour essentially performing the auction*/
	private AnnounceAndWait awBehaviour;
	/** A random number generator (with a random seed) */
	private Random r = new Random();
	/** The vector of bidder agents' AID's chosen to participate in the auction*/
	private Vector<AID> bidderAgents;
	/** The initial fortune of the bidder agents*/
	private int bidderFortune;
	/** The number of different good types */
	private int goodTypeNum;
	/** The minimal price of different goods (of given good types) */
	private HashMap<String, Integer> goodPrice;
	/** The total amount of goods to sell at the actual auction (default value is 0) */
	public int totalGoodNumber = 0;
	//-------------------------- Things that may change during the auction --------------------------
	/** The actual cash of different bidders */
	private HashMap<AID, Integer> bidderMoney;
	/** The actual utility of different bidders */
	private HashMap<AID, Double> bidderUtility;
	/** The actual number of goods bought by different bidders */
	private HashMap<AID, Integer> bidderBuys;
	/** The amount of remaining goods (of given good types) */
	private HashMap<String, Integer> goodNumber;
	/** The number of the announced goods (init value is 1)*/
	private int goodCounter = 1;
	/** The Agent-IDentifier (AID) of the actual leader of the auction (init value is NULL)*/
	private AID leaderAID = null;
	/** The name (Agent.getAID().getName() or Agent.getLocalName()) of the actual leader of the auction (init value is an empty String)*/
	private String leaderName = "";
	/** The actual type of the good that is being offered for sale (init value is an empty String)*/
	private String goodType = "";
	/** The actual bid for the good being offered  (init value is 0)*/
	private int bid = 0;
	/** The actual round about the offered good (init value is 1)*/
	private int round = 1;
	
	/** Put agent initializations here*/
	protected void setup() {
	
		System.out.println("Auctioneer agent, " + getLocalName() + ", starting...");
		
		// Register the bidder service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("auctioneer");
		sd.setName("auctioneer");
		dfd.addServices(sd);

		try {

			DFService.register(this, dfd);

		} catch (FIPAException fe) {

			fe.printStackTrace();

		}

		// Get the Auction Configuration file's filePath (and maybe the waiting time) as a start-up argument...
		Object[] args = getArguments();
  
		if (args != null && args.length > 0) {

			configFilePath	= (String)args[0];

		    File configFile = new File(configFilePath);

		    if (configFile.isFile()) {

		    	System.out.println(getLocalName() + " - auction configuration: " + configFilePath);

		    	// Now read the parameters of the auction from the specified configuration file...
		    	String[] configuration = DataHandler.readFile2String(configFilePath).split(" ");

		    	if (configuration.length > 4 && (configuration.length - 1) % 3 == 0) {

		    		// Read the initial amount of money (fortune) of the bidders...
		    		bidderFortune = Integer.parseInt(configuration[0]);
    		
		    		// Calculate the number of different good types...
		    		goodTypeNum = (configuration.length - 1)/3;

		    		// Create the data structures holding the information about the actual good types...
		    		goodNumber	= new HashMap<String, Integer>(goodTypeNum);
		    		goodPrice	= new HashMap<String, Integer>(goodTypeNum);

		    		// Fill the data structures with the initial data...
		    		for (int i = 0; i < goodTypeNum; ++i) {

		    			goodNumber.put(configuration[1+3*i], new Integer(configuration[2+3*i]));
		    			goodPrice.put( configuration[1+3*i], new Integer(configuration[3+3*i]));

		    			totalGoodNumber += Integer.parseInt(configuration[2+3*i]);

		    		}

		    		// If there is a second argument, then try to parse and check it...
		    		if (args.length == 2) {

			    		long x = 0;
			    		
			    		try {
			    			
			    			x = Long.parseLong((String)args[1]);
			    			
			    		} catch (NumberFormatException nfe) {
			    			
			    			System.out.println("*** Waiting time argument couldn't be parsed (see the exception below)... Default value: " + waitingtime + " [ms]");
			    			nfe.printStackTrace();
			    			
			    		}
			    		
			    		// 1 milisecond is a very low physical minimum for waiting time, really...
			    		if (x > 0) waitingtime = x;
		    		
		    		}
		    		
		    		// Create and show the GUI 
		    		myGui = new AuctioneerAgentGui(this);
		    		myGui.show();
		
		    	} else {
			  		
				    System.out.println(getLocalName() + " - [CONFIGURATION ERROR] Invalid number of elements in the auction configuration file (must be 1+3k, where k>0). Terminating...");
				    doDelete();

			  	}
		    	
		    } else {
		  		
			    System.out.println(getLocalName() + " " +configFilePath+" - [FILE ERROR] The specified file is non existing. Terminating...");
			    doDelete();

		  	}
		    
		} else {

		    System.out.println(getLocalName() + " - [INPUT ARGUMENT ERROR] One start-up argument should specified: the filepath of the auction configuration file! Terminating...");
		    doDelete();

		}
		
	} // End of AuctioneerAgent.setup() method

  /** Put agent clean-up operations here*/
  protected void takeDown() {
    
	// Deregister from the yellow pages
    try {
    	
      DFService.deregister(this);
      
    } catch (FIPAException fe) {
    	
      fe.printStackTrace();
      
    }
    
    // Printout a dismissal message
    System.out.println("Auctioneer agent, " + getAID().getLocalName() + ", terminating...");

    // Try to do a simple logging ex ante (if the GUI is available), and drop the GUI...
    try {
    	
    	// If GUI was created/instantiated, then...
    	if (myGui.getContentPane().isVisible()) {

    		// If the auction was really started, then there is a point to make a logfile about it...
    		if (!auid.isEmpty()) {
    			
        	    // The filename of the logfile...
        	    String logFileName = "log\\auction_log_" + auid + ".txt";
        		
        		// Write the information about the bidders' performance to an appropriate timestamped file...
        		DataHandler.writeString2File(logFileName, myGui.auctionFlow.getText());

        		// Notify the user about the logfile created...
        		System.out.println("\nLogfile: " + logFileName);
    			
    		}
    		
    		// Close the GUI
    		myGui.dispose();

    	}
	
    } catch (Exception e) {}

  } // End of AuctioneerAgent.takeDown() method
  
  /** Appropriately call (i.e. add or restart) a given behaviour (from the GUI)...*/
  public void callBehaviour(String b) {

	  if (b.equals("SearchAndNotifyBidders")) {

		  addBehaviour(new SearchAndNotifyBidders());

	  } else if (b.equals("AnnounceAndWait")) {

		  awBehaviour.setState(2);
		  awBehaviour.restart();

	  } 

  } // End of AuctioneerAgent.callBehaviour() method
  
  /** Appropriately update the JTextFields on the GUI according to the current state of the auction...*/
  public void updateGuiTexts() {

	  myGui.goodNo.setText(Integer.toString(goodCounter));
	  myGui.goodType.setText(goodType);
	  myGui.initBidAmount.setText(goodPrice.get(goodType).toString()); 
	  myGui.bidAmount.setText(Integer.toString(bid));
	  myGui.leaderName.setText(leaderName);
	  if (leaderName.isEmpty()) {

		  myGui.leaderMoney.setText("");
		  myGui.leaderBuys.setText("");
		  myGui.leaderUtility.setText("");

	  } else {

		  myGui.leaderMoney.setText(bidderMoney.get(leaderAID).toString());
		  myGui.leaderBuys.setText(bidderBuys.get(leaderAID).toString());
		  myGui.leaderUtility.setText(bidderUtility.get(leaderAID).toString());

	  }

  } // End of AuctioneerAgent.updateGuiTexts() method
  
  /** Sort the good prices HashMap...*/
  public LinkedHashMap<String, Integer> sortGoodPrices(HashMap<String, Integer> inMap, boolean ascending) {

	  List<String> mapKeys		= new ArrayList<String>(inMap.keySet());
	  List<Integer> mapValues	= new ArrayList<Integer>(inMap.values());

	  Collections.sort(mapValues);

	  if (!ascending) Collections.reverse(mapValues);

	  LinkedHashMap<String, Integer> outMap	= new LinkedHashMap<String, Integer>();
	  Iterator<Integer> valueIt				= mapValues.iterator();
	  
	  while (valueIt.hasNext()) {
		  
		  Integer val				= valueIt.next();
		  Iterator<String> keyIt	= mapKeys.iterator();
		  
		  while (keyIt.hasNext()) {
			  
			  String key = keyIt.next();
			  
			  if (inMap.get(key).equals(val)) {
				  
				  mapKeys.remove(key);
				  outMap.put(key, val);

				  break;
				  
			  }
			  
		  }
		  
	  }
	  
	  return outMap;
	  
  } // End of AuctioneerAgent.sortGoodPrices() method
  
  /** 
   * Estimate the lower limit of the worst case maximum of the time needed for the auction
   * (i.e. in worst case the length of the auction may be approximately at least as much)...
   **/
  public long estimateMaxAuctionTime() {

	  // Potential (hypothetical) buyer number...
	  int i		= bidderAgents.size();

	  // Number of goods sold so far (hypothetically)...
	  int j		= 0;

	  // The approximate time in milliseconds needed in worst case to sell those goods (hypothetically)...
	  long ms	= 0;
	  
	  // Sort the good prices of given good types in an ascending order...
	  // (...so the good types with lower prices, where you can raise the bid more times, will be in front...)
	  LinkedHashMap<String, Integer> sortedGoodPrices = sortGoodPrices(goodPrice, true);
	  
	  Iterator<String> sgpi = sortedGoodPrices.keySet().iterator();
	  
	  while (sgpi.hasNext()) {

		  String goodTypeKey	= sgpi.next();
		  int goodTypeKeyNum	= goodNumber.get(goodTypeKey);
		  int goodTypePrice		= goodPrice.get(goodTypeKey);
  
		  // If the goods of the given type are enough, then...
		  if (goodTypeKeyNum >= i) {

			  // If this is an affordable good type...
			  if (bidderFortune > goodTypePrice) {
				  
				  // Only i goods of the given type will be sold now (hypothetically)...
				  j += i;
				  
				  // (Agents * NumberOfBids * MaxWaitTime) + TimeLeftToAnnounceEverythingElse...
				  // ...parentheses are for the sake of readability...
				  ms	+=	(i	*	(bidderFortune - goodTypePrice)	*	(3 * waitingtime))
				  				+	((totalGoodNumber - j)			*	(3 * waitingtime));
				  
				  // No hypothetical buyer/bidder agents left (with more than 0 cash)...
				  i		= 0;
				  
				  // Finally break out of the WHILE cycle...
				  break;
				  
			  }
			  
		  // ...else the goods of the given type weren't enough (to cover all remaining hypo-buyers).	  
		  } else {

			  // If this is an affordable good type...
			  if (bidderFortune > goodTypePrice) {
				  
				  // All the goods of the given type will be sold now (hypothetically)...
				  j += goodTypeKeyNum;
				  
				  // (MaxAgentsBuyingTheGivenGoodType * NumberOfBids * MaxWaitTime)
				  // ...parentheses are for the sake of readability...
				  ms	+=	(goodTypeKeyNum	*	(bidderFortune - goodTypePrice)	*	(3 * waitingtime));
				  
				  // The number of hypothetical buyer/bidder agents (with more than 0 cash) decreases... 
				  i		-=	goodTypeKeyNum;
				  
			  }

		  }

	  }
	  
	  // Return the estimated time in milliseconds...
	  return ms;

  } // End of AuctioneerAgent.estimateMaxAuctionTime() method
  
  /** 
   * Convert a time interval given in millisecond to hours, minutes, and seconds...
   **/
  public String[] convertMS2HMS(long ms) {

	  String[] out	= new String[3];

	  long	time	= ms/1000;

	  // Convert the time-interval to hours, minutes, and seconds...
	  out[0]		= Integer.toString((int)(time / 3600));
	  out[1]		= Integer.toString((int)((time % 3600) / 60));
	  out[2]		= Integer.toString((int)(time % 60));  

	  return out;
	  
  } // End of AuctioneerAgent.convertMS2HMS() method
  
	/**
	 * JADE behaviour: SearchAndNotifyBidders
	 * Search for potential bidder agents, and notify them about the start of the auction...
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class SearchAndNotifyBidders extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {

			System.out.println("\nTrying to locate bidders for the auction...");

			// Create the search template for the DF agent...
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd		= new ServiceDescription();
			sd.setType("bidder");
			template.addServices(sd);

			try {

				AID a;

				// Query the DF agent for bidders...
				DFAgentDescription[] result = DFService.search(myAgent, template);

				// Create the data structures describing the bidder agents found...
				bidderAgents	= new Vector<AID>(result.length);
				bidderMoney		= new HashMap<AID, Integer>(result.length);
				bidderUtility	= new HashMap<AID, Double>(result.length);
				bidderBuys		= new HashMap<AID, Integer>(result.length);

    			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("start");
				
	      		// Initialize the data structures...
	      		for (int i = 0; i < result.length; ++i) {
	
	      			// The AID of the i-th bidder agent found...
	      			a = result[i].getName();
	      			      			
	      			System.out.println(a.getLocalName() + " found...");
	      			
	      			// Address the message to the given bidder agent too... 
	    			msg.addReceiver(a);
	      			
	      			// Fill up the list of bidder agents...
	      			bidderAgents.add(a);
	      			
	      			// Define their initial fortune...
	      			bidderMoney.put(a, new Integer(bidderFortune));
	      			
	      			// Define their initial utility (2B zero)...
	      			bidderUtility.put(a, new Double(0));
	      			
	      			// Define the amount of goods they already bought (initially zero)... 
	      			bidderBuys.put(a, new Integer(0));
	      			
	      		} // FOR RESULT

	      		if (result.length > 0) {
	      			
	      			Date presentTime = new Date();
	      			
	    			// Initialize the unique identifier (uid) of the current auction (as the actual start time)...
	    			auid = new SimpleDateFormat("yyyyMMdd-HHmmss").format(presentTime);
	    			
	    			// Get the current time (in milliseconds elapsed since midnight, January 1, 1970 UTC)...
	    			startTimeOfAuction = System.currentTimeMillis();
	    			
		      		// Send the START-OF-AUCTION broadcast message to the discovered bidder agents...
		      		myAgent.send(msg);

		      		// Print the moment in time when the auction began...
		      		System.out.println("\nBeginning of the auction:\t" + new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss").format(presentTime));
		      		
		      		// Estimate the maximum auction time...
		      		String[] time = convertMS2HMS(estimateMaxAuctionTime());
		      		System.out.println("Estimated max. auction time:\t" + time[0] + " hours " + time[1] + " minutes and " + time[2] + " seconds...");

		      		// Update the TextArea on the GUI appropriately...
		      		String eventDescString		= "ANNOUNCE\tstart";
		      		String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");
	
		      		// Choose a random good-type for the following auction...
		      		goodType	= (String)goodNumber.keySet().toArray()[r.nextInt(goodTypeNum)];
		      		
		      		// Fix an initial price for the good of previously chosen type...
		      		bid	= goodPrice.get(goodType).intValue();

		      		// Start executing the auction...
		      		awBehaviour = new AnnounceAndWait();
		      		myAgent.addBehaviour(awBehaviour);
		      		
		      		// Enable the user to stop the auction anytime (from now on) on the GUI...
		      		myGui.stopButton.setEnabled(true);
	      		
	      		} else {
	      			
	      			System.out.println("No bidders found on the platform...");
	      			
	      			// Let the user try to search again...
	      			myGui.startButton.setEnabled(true);
	      			
	      		}
	      		
			} catch (FIPAException fe) {

				fe.printStackTrace();

			}
							
	      } // End of AuctioneerAgent.SearchAndNotifyBidders.action() method

	} // End of AuctioneerAgent.SearchAndNotifyBidders behaviour

	/**
	 * JADE behaviour: AnnounceAndWait
	 * Announce a given good to the bidders and process their proposals (while handling timeouts too)...
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class AnnounceAndWait extends Behaviour {

	  private static final long serialVersionUID = 2823366469733824448L;
	  
	  /** A template to receive messages*/
	  private MessageTemplate mt;
	  /** Content of sent messages*/
	  private String msgContent;
	  /** The behaviour that will realize adaptively timed actions during the auction*/
	  private TimeoutHandler thBehaviour;
	  /** The state of this behaviour (init value is 0)*/
	  private int state = 0;
	  
	  /** Modify the state parameter of this behaviour directly...*/
	  public void setState(int newState) {
		  
		  state = newState;
		  
	  }
	  
	  public void action() {
	  
		  switch (state) {

			  case 0: // Send an appropriate announcement...
	
				  // If the actual good is announced for the 1st time, then...
				  if (leaderName.isEmpty()) {
					  
					  msgContent =	goodCounter + " "
					  				+ goodType + " "
					  				+ bid + " "
					  				+ round;

				  } else {
					  
					  msgContent =	goodCounter + " "
					  				+ goodType + " "
					  				+ bid + " "
					  				+ round + " "
					  				+ leaderName;
					  
				  }
				  
				  // Create the appropriate broadcast message to the bidders...
				  ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				  msg.setContent(msgContent);
				  
				  // Add all the bidders as receivers (in a random, uniformly distributed order)...		  
				  List<AID> bal = Arrays.asList(bidderAgents.toArray(new AID[bidderAgents.size()]));
				  Collections.shuffle(bal, r);					// <--- RANDOMIZATION OF ORDER HERE!
				  Iterator<AID> bali = bal.iterator();
				  while(bali.hasNext()) msg.addReceiver(bali.next());				  
				  //Iterator<AID> bai = bidderAgents.iterator();
				  //while(bai.hasNext()) msg.addReceiver(bai.next());

				  // Send the good-announcement broadcast message to the bidders...
				  myAgent.send(msg);
				  
				  // Update the TextArea on the GUI appropriately...
				  String eventDescString	= "ANNOUNCE\t" + msgContent;
				  String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
				  myGui.appendText(currentTimeString + eventDescString + "\n");

				  // Update the JTextFields on the GUI appropriately...
				  updateGuiTexts();
  
				  // On the next scheduling of this behaviour (by the JADE runtime) we shall execute
				  // the action() method so as to realize the next state of this behaviour (waiting
				  // for proposals)...
				  state = 1;
				  
				  // Add a named TimeoutHandler behaviour instance to the queue of active behaviours...
				  thBehaviour = new TimeoutHandler(myAgent, waitingtime);
				  myAgent.addBehaviour(thBehaviour);
				  
				  break;

			  case 1: // Wait for proposals...

				  mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
	
				  // Receive an appropriate message from a Bidder...
				  msg = myAgent.receive(mt);

				  // If finally a proposal has arrived, then...
				  if (msg != null) {
					  
					  try {
					  
						  // A default pessimistic supposition...
						  int proposal = 0;
						  
						  // Try to parse the content of the received proposal-message...
						  try {
							  
							  // The proposal of the bidder agent...
							  proposal = Integer.parseInt(msg.getContent());
							  
						  } catch (NumberFormatException nfe) {}
						  
						  // If a known bidder is interacting with us appropriately, then...
						  if (bidderMoney.containsKey(msg.getSender()) && proposal > 0) {
						  
							  // If this is a really legitimate proposal, then...
							  // Comment: if the same agent makes a following, higher bid, it is accepted... ;-)
							  if (proposal > bid && bidderMoney.get(msg.getSender()).intValue() >= proposal) {
			
								  // Update the appropriate parameters of the current good-auction
								  // considering the imminent announcement/broadcast...
								  leaderAID		= msg.getSender();
								  leaderName 	= leaderAID.getLocalName();
								  bid			= proposal;
								  round			= 1;
	
								  // Update the TextArea on the GUI appropriately...
								  eventDescString	= "PROPOSAL\t" + leaderName + " " + bid;
								  currentTimeString = "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
								  myGui.appendText(currentTimeString + eventDescString + "\n");
								  
								  // On the next scheduling of this behaviour (by the JADE runtime) we shall execute
								  // the action() method so as to realize the state of this behaviour where the announcement
								  // of the new leader (concerning the announced good) takes place...
								  state = 0;
								  
								  // Remove the TimeoutHandler behaviour from the queue (to ensure that we'll be able to
								  // directly execute state 0 of this awBehaviour next so as to announce the new
								  // highest bid/bidder)... The removal (not reset!) here also ensures, that we'll have
								  // only one TimeoutHandler behaviour instance in the active-queue simultaneously...
								  myAgent.removeBehaviour(thBehaviour);
								  
							  }
							  
						  }
					  
					  } catch (Exception e) {
						  
						  e.printStackTrace();
						  
					  }
	
				  // ...else the message that arrived was not a proposal, and so we block this behaviour
				  // until a new message arrives, even perhaps a proposal.
				  } else {

					  block();

				  }

				  break;

			  case 2: // Send a stop auction announcement to every bidder...
	  
				  // Disable the user to stop the auction on the GUI...
				  myGui.stopButton.setEnabled(false);
				  
				  // Clear the JTextFields on the GUI (since they are irrelevant now (the auction finished))...
				  myGui.goodNo.setText("");
				  myGui.goodType.setText("");
				  myGui.initBidAmount.setText(""); 
				  myGui.bidAmount.setText("");
				  myGui.leaderName.setText("");
				  myGui.leaderMoney.setText("");
				  myGui.leaderBuys.setText("");
				  myGui.leaderUtility.setText("");
				  
				  // Create the appropriate broadcast message to the bidders...
				  msg = new ACLMessage(ACLMessage.INFORM);
				  msg.setContent("stop");

				  // Add all the bidders as receivers...
				  Iterator<AID> bai = bidderAgents.iterator();
				  while(bai.hasNext()) msg.addReceiver(bai.next());

				  // Send the STOP-AUCTION broadcast message to the bidders...
				  myAgent.send(msg);

				  // Update the TextArea on the GUI appropriately...
				  eventDescString		= "ANNOUNCE\tstop";
				  currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
				  myGui.appendText(currentTimeString + eventDescString + "\n");
				  
				  break;

		  } // End of switch 
  
	  } // End of AuctioneerAgent.AnnounceAndWait.action() method

	  public boolean done() {

		  // If this behaviour's state reached 2, then we can remove it from the pool
		  // of active behaviours (which can be scheduled for execution by the JADE runtime)...
		  // Comment: this behaviour can reach state 2 only with exterior "help" (by the TimeoutHandler)...
		  return (state == 2);

	  } // End of AuctioneerAgent.AnnounceAndWait.done() method
	  
	}  // End of AuctioneerAgent.AnnounceAndWait behavour
	
	/**
	 * JADE behaviour: TimeoutHandler
	 * This class wakes up the same way as the hammer of an auctioneer drops...
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class TimeoutHandler extends WakerBehaviour {

		private static final long serialVersionUID = -2313411205396673471L;

		/** Is true when the auction is over (i.e. we tried to sell all the goods)*/
		private boolean auctionIsOver = false;
		/** A "pointer" at the related AnnounceAndWait behaviour (which - by the way - created this behaviour)*/
		//private AnnounceAndWait awBehaviour;
		
		TimeoutHandler(Agent a, long timeout) {//, AnnounceAndWait awb) {

			// Explicit invocation of the super class's (i.e. WakerBehaviour's) appropriate constructor...
			super(a, timeout);
			
			// Let's have a "pointer" at the appropriate AnnounceAndWait behaviour...
			//awBehaviour = awb;

		} // End of constructor

		protected void onWake() {
			
			// If the last round has ended concerning the given good, then (its fate is decided)...
			if (round == 3) {

				// If that last round has ended without any relevant proposals, then (there is no winner)...
				if (leaderName.isEmpty()) {

					// Update the TextArea on the GUI appropriately...
					String eventDescString		= "NOT SOLD\t" + goodCounter + " " + goodType + " " + bid;
					String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");

					// Update the progress bar (the number of finished goods) on the GUI...
					myGui.progressBar.setValue(goodCounter);
					
					// If all the goods were announced (and possibly sold) at the auction, then...
					if (goodCounter == totalGoodNumber) {

						// This should have been the last good (not only of this type)...
						goodNumber.remove(goodType);
					
						// Indicate that the auction is over...
						auctionIsOver = true;
						
						// Finish the AnnounceAndWait behaviour instance (indirectly) now...
						awBehaviour.setState(2);

					// ...else not all the goods were announced (and possibly sold) yet.
					} else {

						// How many goods of this type are on stock? 
						int onStock = goodNumber.get(goodType).intValue();

						// If only 1 good is left on the stock from the given type, then...
						if (onStock == 1) {

							// Finish with this good-type once and for all... :)
							// Comment: so goodNumber won't hold good types with zero goods...
							goodNumber.remove(goodType);

						// ...else there are more than one goods of such type on the stock.
						} else {

							// Now there is one less goods of such type left...
							goodNumber.put(goodType, new Integer(onStock-1));

						}
						
						// Let's move on to the next good...
						goodCounter++;

						// Let's start (again) from the beginning (with that next/new good)...
						round = 1;
						
						// Let's choose its type (randomly from the remaining good-types)...
						goodType = (String)goodNumber.keySet().toArray()[r.nextInt(goodNumber.keySet().size())];

			      		// Fix an initial price for that good (of the chosen type)...
						bid	= goodPrice.get(goodType).intValue();

						// Reset the state of the AnnounceAndWait behaviour instance (indirectly) to
			      		// zero, i.e. "announce", now...
						awBehaviour.setState(0);

					}

				// ...else there is a winner for the given good.
				} else {

					// Update the TextArea on the GUI appropriately...
					String eventDescString		= "SOLD TO\t" + leaderName + " " + goodCounter + " " + goodType + " " + bid;
					String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");

					// Update the progress bar (the number of finished goods) on the GUI...
					myGui.progressBar.setValue(goodCounter);
					
					// If all the goods were announced (and possibly sold) at the auction, then...
					if (goodCounter == totalGoodNumber) {

						// This should have been the last good (not only of this type)...
						goodNumber.remove(goodType);
						
						// Indicate that the auction is over...
						auctionIsOver = true;
						
						// Finish the AnnounceAndWait behaviour instance (indirectly) now...
						awBehaviour.setState(2);

						// Update the amount of the winner agents money...
						bidderMoney.put(leaderAID, new Integer(bidderMoney.get(leaderAID).intValue() - bid));

						// Update its utility...
						bidderUtility.put(leaderAID, new Double(bidderUtility.get(leaderAID).doubleValue() + ((goodPrice.get(goodType).doubleValue() + 1) / bid)));
						
						// Update the number of goods it bought...
						bidderBuys.put(leaderAID, new Integer(bidderBuys.get(leaderAID).intValue() + 1));
						
					// ...else not all the goods were announced (and possibly sold) yet.	
					} else {
			
						// How many goods of this type are on stock? 
						int onStock = goodNumber.get(goodType).intValue();

						// If only 1 good is left on the stock from the given type, then...
						if (onStock == 1) {

							// Finish with this good-type once and for all... :)
							// Comment: so goodNumber won't hold good types with zero goods...
							goodNumber.remove(goodType);

						// ...else there are more than one goods of such type on the stock.
						} else {

							// Now there is one less goods of such type left...
							goodNumber.put(goodType, new Integer(onStock-1));

						}

						// Update the amount of the winner agents money...
						bidderMoney.put(leaderAID, new Integer(bidderMoney.get(leaderAID).intValue() - bid));

						// Update its utility...
						bidderUtility.put(leaderAID, new Double(bidderUtility.get(leaderAID).doubleValue() + ((goodPrice.get(goodType).doubleValue() + 1) / bid)));
						
						// Update the number of goods it bought...
						bidderBuys.put(leaderAID, new Integer(bidderBuys.get(leaderAID).intValue() + 1));

						// Reset leader information...
						leaderAID	= null;
						leaderName	= "";
						
						// Let's move on to the next good...
						goodCounter++;

						// Let's start (again) from the beginning (with that next/new good)...
						round = 1;

						// Let's choose its type (randomly from the remaining good-types)...
						goodType = (String)goodNumber.keySet().toArray()[r.nextInt(goodNumber.keySet().size())];

			      		// Fix an initial price for that good (of the chosen type)...
						bid	= goodPrice.get(goodType).intValue();

						// Reset the state of the AnnounceAndWait behaviour instance (indirectly) to
			      		// zero, i.e. "announce", now...
						awBehaviour.setState(0);

					}
					
				}

			// ...else this is not the last round concerning the given good.
			} else {

				// Let's go to the next round (i.e. drop of auctioneer's hammer)...
				round++;
				
				// Reset the state of the AnnounceAndWait behaviour instance (indirectly) to
	      		// zero, i.e. "announce", now...
				awBehaviour.setState(0);

			}

			// If the auction is over (i.e. we tried to sell all the goods), then...
			if (auctionIsOver) {

				// Calculate and print the elapsed time since the beginning of the auction...
				String[] time = convertMS2HMS(System.currentTimeMillis() - startTimeOfAuction);
				System.out.println("Elapsed auction time:\t\t" + time[0] + " hours " + time[1] + " minutes and " + time[2] + " seconds...\n");

				AID a;
				String aName;

				String stats = "Name\tMoney\tBuys\tUtility\n";	
				Iterator<AID> bai = bidderAgents.iterator();
				while(bai.hasNext()) {

					a		=	bai.next();
					aName	=	a.getLocalName();
					stats	+=	aName + "\t" +
								bidderMoney.get(a).intValue() + "\t" +
								bidderBuys.get(a).intValue() + "\t" +
								bidderUtility.get(a).doubleValue() + "\n";
					
				}

				// Print the information about the bidders' performance to the console...
				System.out.println("RESULTS:\n--------\n" + stats);

				// Write the information about the bidders' performance to an appropriate timestamped file...
				String resFileName = "log\\auction_res_" + auid + ".txt";
				DataHandler.writeString2File(resFileName, stats);

				// Notify the user about the file created...
				System.out.println("Results saved to: " + resFileName + "\n");
				
			}

			// Restart the related and currently blocked AnnounceAndWait behaviour instance...
			// It became blocked because no proposals arrived until this TimeoutHandler behaviour woke up now...
			awBehaviour.restart();

		} // End of AuctioneerAgent.TimeoutHandler.onWake() method

	} // End of AuctioneerAgent.TimeoutHandler behaviour

} // End of AuctioneerAgent class