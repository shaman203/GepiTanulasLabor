/**
 * Copyright (c) 2010
 * Budapest University of Technology and Economics (BUTE)
 * Department of Measurement and Information Systems (DMIS)
 * All Rights Reserved.
 * 
 * "Cooperative and Learning Systems (VIMIM223)" laboratory
 * "Auctions and Voting" excercise
 * 
 * 				------------
 * 				Bidder agent
 * 				------------
 * 
 * Author:		Dániel László, Kovács (dkovacs@mit.bme.hu)
 * 
 */

package msclab01.votingauction_lab.BidderAgent;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import msclab01.*;
import msclab01.votingauction_lab.DataHandler;

/**
 * JADE agent: BidderAgent
 * A bidder agent for english auctions...
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class HolBidderAgent extends Agent {

	private static final long serialVersionUID = -4427153511109068652L;

	/** The filepath of the configuration file describing the main parameters of the auction*/
	private String configFilePath;
	/** My actual cash*/
	private int myMoney;
	/** My actual utility*/
	private double myUtility = 0;
	/** The actual number of goods bought so far (by me)*/
	private int myBuys = 0;
	/** The total amount of goods sold at the actual auction (default value is 0)*/
	private int totalGoodNumber = 0;
	/** The number of different good types*/
	private int goodTypeNum;
	/** The actual amount of different goods (of given good types)*/
	private HashMap<String, Integer> goodNumber;
	/** The minimal price of different goods (of given good types)*/
	private HashMap<String, Integer> goodPrice;

	/** Put agent initializations here*/
	protected void setup() {

		System.out.println("Bidder agent, " + getLocalName() + ", starting...");

		// Register the bidder service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("bidder");
		sd.setName("bidder");
		dfd.addServices(sd);

		try {

			DFService.register(this, dfd);

		} catch (FIPAException fe) {

			fe.printStackTrace();

		}

		// Get the Auction Configuration file's filePath as a start-up argument...
		Object[] args = getArguments();

		if (args != null && args.length == 1) {

			configFilePath = (String)args[0];

			File configFile = new File(configFilePath);

			if (configFile.isFile()) {

				System.out.println(getLocalName() + " - auction configuration: " + configFilePath);

				// Now read the parameters of the auction from the specified configuration file...
				String[] configuration = DataHandler.readFile2String(configFilePath).split(" ");

				if (configuration.length > 4 && (configuration.length - 1) % 3 == 0) {

					// Read the initial amount of money (fortune)...
					myMoney = Integer.parseInt(configuration[0]);

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

					// Add the behavior for participating in auctions...
					addBehaviour(new ParticipateInAuction());

				} else {

					System.out.println(getLocalName() + " - [CONFIGURATION ERROR] Invalid number of elements in the auction configuration file (must be 1+3k, where k>0). Terminating...");
					doDelete();

				}

			} else {

				System.out.println(getLocalName()+" "+configFilePath + " - [FILE ERROR] The specified file is non existing. Terminating...");
				doDelete();

			}

		} else {

			System.out.println(getLocalName() + " - [INPUT ARGUMENT ERROR] One start-up argument should be specified: the filepath of the auction configuration file! Terminating...");
			doDelete();

		}

	}  // End of BidderAgent.setup() method

	/** Put agent clean-up operations here*/
	protected void takeDown() {

		// Deregister from the yellow pages
		try {

			DFService.deregister(this);

		} catch (FIPAException fe) {

			fe.printStackTrace();

		}

		// Printout a dismissal message
		System.out.println("Bidder agent, " + getAID().getLocalName() + ", terminating...");

	} // End of BidderAgent.takeDown() method

	/**
	 * JADE behaviour: ParticipateInAuction
	 * This is the behavior used by BidderAgent agents to participate in auctions.
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class ParticipateInAuction extends Behaviour {

		private static final long serialVersionUID = 2823366469733824448L;

		/** The name (Agent.getAID().getName() or Agent.getLocalName()) of the latest known leader of the auction (init value is an empty String)*/
		private String leaderName = "";
		/** The latest known ID of the good that was offered for sale (init value is 1)*/
		private int goodCounter = 1;	
		/** The latest known type of the good that was offered for sale (init value is an empty String)*/
		private String goodType = "";
		/** The latest known bid for the offered good (init value is 0)*/
		private int bid = 0;
		/** The latest known round about the offered good (init value is 0)*/
		private int round = 0;
		/** A template to receive messages*/
		private MessageTemplate mt;
		/** The state of this behaviour (init value is 0)*/
		private int step = 0;

		public void action() {

			switch (step) {

			case 0:

				mt = MessageTemplate.and(	MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchContent("start"));

				// Receive an appropriate start message from the Auctioneer
				ACLMessage msg = myAgent.receive(mt);

				if (msg != null) {

					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("");
					send(reply);

					step = 1; 

				} else {

					/* 
					    	 "...we would like to execute the action() method of the ... behaviour only when a
					    	 new message is received. In order to do that we can use the block() method of the
					    	 Behaviour class. This method marks the behaviour as "blocked" so that the agent does
					    	 not schedule it for execution anymore. When a new message is inserted in the agent`s
					    	 message queue all blocked behaviours become available for execution again so that
					    	 they have a chance to process the received message..."
					    	 	- JADE TUTORIAL: JADE PROGRAMMING FOR BEGINNERS (page 17)
					 */  
					block();

				}

				break;

			case 1:

				mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

				// Receive an appropriate message from the Auctioneer
				msg = myAgent.receive(mt);

				if (msg != null) {

					// If the auction is over, then...
					if (msg.getContent().equals("stop")) {

						// Decrease the number of available goods of the given/sold type by 1...
						goodNumber.put(goodType, new Integer(goodNumber.get(goodType).intValue()-1));
						//System.out.println(getLocalName() + " - Good type: " + goodType + " Good number: " + goodNumber.get(goodType).intValue());

						step = 2;

						// ...else this is about the auction of a given good.	
					}else if(msg.getContent().equals("won")){
						// Decrease the amount of my money...
						myMoney -= bid;
						//System.out.println(getLocalName() + " - My money: " + myMoney);

						// Increase my utility...
						myUtility += (goodPrice.get(goodType).doubleValue() + 1) / bid;
						//System.out.println(getLocalName() + " - My uility: " + myUtility);

						// Increment the number of goods I bought...
						myBuys++;
						//System.out.println(getLocalName() + " - My buys: " + myBuys);

						// Decrease the number of available goods of the given/sold type by 1...
						goodNumber.put(goodType, new Integer(goodNumber.get(goodType).intValue()-1));
					} 
					else {

						String[] msgTokens = msg.getContent().split(" ");

						try {

							int actualGoodCounter	= Integer.parseInt(msgTokens[0]);
							String actualGoodType	= msgTokens[1];
							int actualBid			= Integer.parseInt(msgTokens[2]);
							int actualRound			= Integer.parseInt(msgTokens[3]);
							String actualLeaderName = "";

							// If there is an active bid/leader on the given good, then...
							if (msgTokens.length > 4) {

								actualLeaderName	= msgTokens[4];

							}

							//System.out.println(myAgent.getLocalName() + " - Previous good counter: " + goodCounter);
							//System.out.println(myAgent.getLocalName() + " - Actual good counter: " + actualGoodCounter);

							// If the latest good was sold (or left untouched), then...
							if (actualGoodCounter > goodCounter) {

								// If it was sold to me, then...
								if (myAgent.getLocalName().equals(leaderName)) {

									// Decrease the amount of my money...
									myMoney -= bid;
									//System.out.println(getLocalName() + " - My money: " + myMoney);

									// Increase my utility...
									myUtility += (goodPrice.get(goodType).doubleValue() + 1) / bid;
									//System.out.println(getLocalName() + " - My uility: " + myUtility);

									// Increment the number of goods I bought...
									myBuys++;
									//System.out.println(getLocalName() + " - My buys: " + myBuys);

								}

								// Decrease the number of available goods of the given/sold type by 1...
								goodNumber.put(goodType, new Integer(goodNumber.get(goodType).intValue()-1));
								//System.out.println(getLocalName() + " - Good type: " + goodType + " Good number: " + goodNumber.get(goodType).intValue());

							}

							//System.out.println(myAgent.getLocalName() + " - Previous leader name: " + leaderName);
							//System.out.println(myAgent.getLocalName() + " - Actual leader name: " + actualLeaderName);
							//System.out.println(myAgent.getLocalName() + " - Previous bid: " + bid);
							//System.out.println(myAgent.getLocalName() + " - Actual bid: " + actualBid);
							//System.out.println(myAgent.getLocalName() + " - Previous type: " + goodType);
							//System.out.println(myAgent.getLocalName() + " - Actual type: " + actualGoodType);
							//System.out.println(myAgent.getLocalName() + " - Previous round: " + round);
							//System.out.println(myAgent.getLocalName() + " - Actual round: " + actualRound);

							goodCounter	= actualGoodCounter;
							goodType		= actualGoodType;
							bid			= actualBid;
							round			= actualRound;
							leaderName	= actualLeaderName;

							// Primitive, very greedy bidding strategy...
							Random rand = new Random();
							
							if (myMoney - rand.nextInt(2) >= actualBid && actualLeaderName.isEmpty()) {

								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.PROPOSE);
								reply.setContent(Integer.toString(actualBid));
								send(reply);

							}

						} catch (Exception e) {

							e.printStackTrace();

						}

					}

				} else {

					block();

				}

				break;

			} // End of switch 

		} // End of BidderAgent.ParticipateInAuction.action() method

		public boolean done() {

			// If this behaviour's state reached step 2, then we can remove it from the pool
			// of active behaviours (which can be scheduled for execution by the JADE runtime)...
			return (step == 2);

		} // End of BidderAgent.ParticipateInAuction.done() method

	}  // End of BidderAgent.ParticipateInAuction behavour

} // End of BidderAgent class
