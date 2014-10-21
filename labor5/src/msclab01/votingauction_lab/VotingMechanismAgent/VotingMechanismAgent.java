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
 * 				VotingMechanismAgent
 * 				--------------------
 * 
 * Author:		Dániel László, Kovács (dkovacs@mit.bme.hu)
 * 
 */

package msclab01.votingauction_lab.VotingMechanismAgent;

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
import msclab01.votingauction_lab.*;

/**
 * JADE agent: VotingMechanismAgent
 * A voting mechanism agent for plurality voting...
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class VotingMechanismAgent extends Agent {

	private static final long serialVersionUID = 1733399087955765502L;

	//-------------------------- Things that won't change during the voting --------------------------
	/** The UID (Unique Identifier) of the Voting: the timestamp of the moment the VotingMechanismAgent came to "life"*/
	private String uid = "";
	/** The beginning and the end of the voting (by System.currentTimeMillis())...*/
	private long startTimeOfVoting;
	/** The filepath of the configuration file describing the main parameters of the voting*/
	private String configFilePath;

	/** The maximal time (in miliseconds [ms]) to wait for a vote*/
	private long waitingtime = 3000;		// !!! FONTOS KEZDETI BEALLITAS !!!

	/** The GUI by means of which the user can interact with this agent*/
	private VotingMechanismAgentGui myGui;
	/** A "pointer" at the instance of the behaviour essentially performing the voting*/
	private AnnounceAndWait awBehaviour;
	/** The vector of voter agents' AID's chosen to participate in the voting*/
	private Vector<AID> voterAgents;
	/** The actual options than can be chosen during the voting*/
	private String[] options;

	/** The maximal number of voting rounds*/
	private int maxRounds = 2;				// !!! FONTOS KEZDETI BEALLITAS !!!

	//-------------------------- Things that may change during the voting --------------------------
	/** The votes according to the ballots that arrived*/
	private int[] votes;
	/** The HashMap of voter' latest ballots in the voting*/
	private HashMap<AID, String[]> voterBallots;
	/** The binary array indicating if a voter voted*/
	private boolean[] voted;
	/** The id number of the winning option*/
	private int winnerId;
	/** The name of the winning option*/
	private String winnerName;
	/** The actual round (init value is 1)*/
	private int round = 1;
	/** The number of votes in the actual round (init value is 0)*/
	private int numberOfVotesInRound = 0;

	/** Put agent initializations here*/
	protected void setup() {

		System.out.println("VotingMechanismAgent " + getLocalName() + " starting...");

		// Register the voter service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("votingmechanism");
		sd.setName("votingmechanism");
		dfd.addServices(sd);

		try {

			DFService.register(this, dfd);

		} catch (FIPAException fe) {

			fe.printStackTrace();

		}

		// Get the voting Configuration file's filePath (and maybe the waiting time) as a start-up argument...
		Object[] args = getArguments();

		if (args != null && args.length > 0) {

			configFilePath	= (String)args[0];

			File configFile = new File(configFilePath);

			if (configFile.isFile()) {

				System.out.println(getLocalName() + " - voting configuration: " + configFilePath);

				// Now read the parameters of the voting from the specified configuration file...
				options = DataHandler.readFile2String(configFilePath).split(" ");

				if (options.length > 1) {

					votes = new int[options.length];
					for (int i = 0; i < votes.length; i++) votes[i] = 0;

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
					myGui = new VotingMechanismAgentGui(this);
					myGui.show();

				} else {

					System.out.println(getLocalName() + " - [CONFIGURATION ERROR] Too few options in the voting configuration (must be at least 2). Terminating...");
					doDelete();

				}

			} else {

				System.out.println(getLocalName() + " - [FILE ERROR] The specified file is non existing. Terminating...");
				doDelete();

			}

		} else {

			System.out.println(getLocalName() + " - [INPUT ARGUMENT ERROR] At least 1 start-up argument should specified: the filepath of the voting configuration file! Terminating...");
			doDelete();

		}

	} // End of VotingMechanismAgent.setup() method

	/** Put agent clean-up operations here*/
	protected void takeDown() {

		// Deregister from the yellow pages
		try {

			DFService.deregister(this);

		} catch (FIPAException fe) {

			fe.printStackTrace();

		}

		// Printout a dismissal message
		System.out.println("VotingMechanism agent " + getAID().getLocalName() + " terminating...");

		// Try to do a simple logging ex ante (if the GUI is available), and drop the GUI...
		try {

			// If GUI was created/instantiated, then...
			if (myGui.getContentPane().isVisible()) {

				// If the voting was really started, then there is a point to make a logfile about it...
				if (!uid.isEmpty()) {

					// The filename of the logfile...
					String logFileName = "log\\voting_log_" + uid + ".txt";

					// Write the information about the voters' performance to an appropriate timestamped file...
					DataHandler.writeString2File(logFileName, myGui.votingFlow.getText());

					// Notify the user about the logfile created...
					System.out.println("\nLogfile: " + logFileName);

				}

				// Close the GUI
				myGui.dispose();

			}

		} catch (Exception e) {}

	} // End of VotingMechanismAgent.takeDown() method

	/** Appropriately call (i.e. add or restart) a given behaviour (from the GUI)...*/
	public void callBehaviour(String b) {

		if (b.equals("SearchAndNotifyVoters")) {

			addBehaviour(new SearchAndNotifyVoters());

		} else if (b.equals("AnnounceAndWait")) {

			awBehaviour.setState(2);
			awBehaviour.restart();

		}

	} // End of VotingMechanismAgent.callBehaviour() method

	/** Appropriately update the JTextFields on the GUI according to the current state of the voting...*/
	public void updateGuiTexts() {

		myGui.winnerName.setText(winnerName);
		if (winnerName.isEmpty()) {

			myGui.winnerVotes.setText("");

		} else {

			myGui.winnerVotes.setText("" + votes[winnerId]);

		}

	} // End of VotingMechanismAgent.updateGuiTexts() method

	/** 
	 * Estimate the lower limit of the worst case maximum of the time needed for the voting
	 * (i.e. in worst case the length of the voting may be approximately at least as much)...
	 **/
	public long estimateMaxVotingTime() {

		// Potential (hypothetical) voter number...
		int i		= voterAgents.size();

		// The approximate time in milliseconds needed in worst case to receive all the votes (hypothetically)...
		long ms	= i * waitingtime;

		// Return the estimated time in milliseconds...
		return ms;

	} // End of VotingMechanismAgent.estimateMaxVotingTime() method

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

	} // End of VotingMechanismAgent.convertMS2HMS() method

	/**
	 * JADE behaviour: SearchAndNotifyVoters
	 * Search for potential voter agents, and notify them about the start of the voting...
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class SearchAndNotifyVoters extends OneShotBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {

			System.out.println("\nTrying to locate voters for the voting...");

			// Create the search template for the DF agent...
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd		= new ServiceDescription();
			sd.setType("voter");
			template.addServices(sd);

			try {

				AID a;

				// Query the DF agent for voters...
				DFAgentDescription[] result = DFService.search(myAgent, template);

				// Create the data structures describing the voter agents found...
				voterAgents	= new Vector<AID>(result.length);

				// Initialize the data holding the latest ballot of voters...
				voterBallots = new HashMap<AID, String[]>(result.length);

				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("start");

				// Initialize the data structures...
				for (int i = 0; i < result.length; ++i) {

					// The AID of the i-th voter agent found...
					a = result[i].getName();

					System.out.println(a.getLocalName() + " found...");

					// Address the message to the given voter agent too... 
					msg.addReceiver(a);

					// Fill up the list of voter agents...
					voterAgents.add(a);

					// Initialize voters ballots...
					voterBallots.put(a, new String[]{});

				} // FOR RESULT

				if (result.length > 0) {

					// Initialize the binary array indicating if a voter agent voted...
					voted = new boolean[result.length];
					for (int i = 0; i < result.length; i++) voted[i] = false;

					Date presentTime = new Date();

					// Initialize the unique identifier (uid) of the current voting (as the actual start time)...
					uid = new SimpleDateFormat("yyyyMMdd-HHmmss").format(presentTime);

					// Get the current time (in milliseconds elapsed since midnight, January 1, 1970 UTC)...
					startTimeOfVoting = System.currentTimeMillis();

					// Send the START-OF-VOTING broadcast message to the discovered voter agents...
					myAgent.send(msg);

					// Print the moment in time when the voting began...
					System.out.println("\nBeginning of the voting:\t" + new SimpleDateFormat("yyyy.MM.dd. HH:mm:ss").format(presentTime));

					// Estimate the maximum voting time...
					String[] time = convertMS2HMS(estimateMaxVotingTime());
					System.out.println("Estimated max. voting time:\t" + time[0] + " hours " + time[1] + " minutes and " + time[2] + " seconds...");

					// Update the TextArea on the GUI appropriately...
					String eventDescString		= "ANNOUNCE\tstart";
					String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");

					// Start executing the voting...
					awBehaviour = new AnnounceAndWait();
					myAgent.addBehaviour(awBehaviour);

					// Enable the user to stop the voting anytime (from now on) on the GUI...
					myGui.stopButton.setEnabled(true);

				} else {

					System.out.println("No voters found on the platform...");

					// Let the user try to search again...
					myGui.startButton.setEnabled(true);

				}

			} catch (FIPAException fe) {

				fe.printStackTrace();

			}

		} // End of VotingMechanismAgent.SearchAndNotifyVoters.action() method

	} // End of VotingMechanismAgent.SearchAndNotifyVoters behaviour

	/**
	 * JADE behaviour: AnnounceAndWait
	 * Announce given options to the voters and process their ballots (while handling timeouts too)...
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
		/** The behaviour that will realize adaptively timed actions during the voting*/
		private TimeoutHandler thBehaviour;
		/** The state of this behaviour (init value is 0)*/
		private int state = 0;

		/** Modify the state parameter of this behaviour directly...*/
		public void setState(int newState) {

			state = newState;

		}

		public void action() {

			switch (state) {

			case 0: // Send an appropriate announcement of options to the voters...

				msgContent =	round + " " + CommonMethods.join(options, " ");

				// Create the appropriate broadcast message to the voters...
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent(msgContent);

				// Add all the voters as receivers...		  	  
				Iterator<AID> vai = voterAgents.iterator();
				while(vai.hasNext()) msg.addReceiver(vai.next());

				// Send the broadcast announcement message of options to the voters...
				myAgent.send(msg);

				// Update the TextArea on the GUI appropriately...
				String eventDescString	= "ANNOUNCE\t" + msgContent;
				String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
				myGui.appendText(currentTimeString + eventDescString + "\n");

				// On the next scheduling of this behaviour (by the JADE runtime) we shall execute
				// the action() method so as to realize the next state of this behaviour (waiting
				// for proposals)...
				state = 1;

				// Add a named TimeoutHandler behaviour instance to the queue of active behaviours...
				thBehaviour = new TimeoutHandler(myAgent, waitingtime);
				myAgent.addBehaviour(thBehaviour);

				break;

			case 1: // Wait for ballots...

				mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

				// Receive an appropriate message from a voter...
				msg = myAgent.receive(mt);

				// ---------------------------------------------------------------------------------------------------							  
				//		LENYEGEBEN INNEN INDUL EGY-EGY BEJOVO SZAVAZAT FELDOLGOZASART FELELOS RESZ!!!!!!!!!!!!!!!!
				// ---------------------------------------------------------------------------------------------------

				// If finally a ballot has arrived from a voter who hasn't yet voted in this round, then...
				if (msg != null && !voted[voterAgents.indexOf(msg.getSender())]) {

					try {

						// The waiting ended: remove the current TimeoutHandler behaviour from the queue...
						// The removal (not reset!) ensures, that we'll have only one TimeoutHandler
						// behaviour instance in the active-queue simultaneously...
						myAgent.removeBehaviour(thBehaviour);

						//Let's get the votes of this voter...
						String[] ballot = msg.getContent().split(" ");

						// Let's check and process the ballot...
						for (int i = 0; i < ballot.length; i++)
						{
							//if (ballot[i].equalsIgnoreCase("1")) 
							//{
								//votes[i]++; 
								//break;
							//}	// !!! FONTOS !!!
							Integer value = Integer.parseInt(ballot[i]);
							votes[i]+=value;
						}
						// Update the TextArea on the GUI appropriately...
						eventDescString		= "VOTE\t\t" + msg.getSender().getLocalName() + "\t" + msg.getContent();
						currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
						myGui.appendText(currentTimeString + eventDescString + "\n");

						// Remember that this voter has voted in this round...
						voted[voterAgents.indexOf(msg.getSender())] = true;

						// Remember his/her last ballot...
						voterBallots.put(msg.getSender(), ballot);

						// Increment the number of votes in this round...
						numberOfVotesInRound++;

						// Update the progress bar on the GUI...
						double progress = 100 * ((((double)round - 1.0) / (double)maxRounds) + ((1.0 / (double)maxRounds) * ((double)numberOfVotesInRound / (double)voterAgents.size())));
						myGui.progressBar.setValue((int)progress);

						// If there are no more voters to wait for in this round...
						if (numberOfVotesInRound == voterAgents.size()) {

							// If the outcome is not ambiguous (i.e. there is only 1 option with a maximum number of ballots), then the voting has ended, that is...
							//if ((CommonMethods.maxNum(votes) == 1) && (votes[CommonMethods.maxIdx(votes)] >= voterAgents.size() /2))
								if (CommonMethods.maxNum(votes) == 1){		// !!! FONTOS !!!


								winnerId		= CommonMethods.maxIdx(votes);
								winnerName	= options[winnerId];

								// On the next scheduling of this behaviour (by the JADE runtime) we shall execute
								// the action() method so as to realize the set state of this behaviour...
								state = 2;

								// Update the TextArea on the GUI appropriately...
								eventDescString	= "WINNER\t" + winnerName + " " + votes[winnerId];
								currentTimeString = "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
								myGui.appendText(currentTimeString + eventDescString + "\n");

								// ...else the outcome is (still) ambiguous.
							} else {

								// If there are no more rounds, then...
								if (round == maxRounds) {

									// There is no definite winner...
									winnerName	= "";

									state = 2;

									// Update the TextArea on the GUI appropriately...
									eventDescString	= "UNDECIDED";
									currentTimeString = "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
									myGui.appendText(currentTimeString + eventDescString + "\n");

									// ...else there is possibility to continue the voting with a new round and new options...
								} else {

									// The next round comes...
									round++;

									// There will be no definite winner at the beginning of that round...
									winnerName	= "";

									// Re-initialize the appropriate data for the next round...
									Vector<Integer> newOptionsIdxVector = CommonMethods.allMaxes(votes);

									String[] newoptions = new String[newOptionsIdxVector.size()];
									for (int i = 0; i < newOptionsIdxVector.size(); i++)
										newoptions[i] = options[newOptionsIdxVector.get(i)];

									// ...the new options for the new round
									options = newoptions;

									// ...the initial number of votes (for the new options)
									votes = new int[options.length];
									for (int i = 0; i < votes.length; i++) votes[i] = 0;

									// ...the sum of these votes
									numberOfVotesInRound = 0;

									// ...and the indication about who voted in the new round (no one)
									for (int i = 0; i < voted.length; i++) voted[i] = false;

									// Set the state to announce the new options next...
									state = 0;

								}

							}

							// ...else there are still some voters left who haven't yet voted (so we can wait for their votes).
						} else {

							// If actually the winner is definite...
							if (CommonMethods.maxNum(votes) == 1) {

								winnerId		= CommonMethods.maxIdx(votes);
								winnerName	= options[winnerId];

								// ...else the winner is actually not definite.
							} else {

								winnerName	= "";

							}

							// Start a new period of time limited waiting for an appropriate vote (in state==1)...
							myAgent.addBehaviour(thBehaviour);

						}

						// Update the JTextFields on the GUI appropriately...
						updateGuiTexts();

						// ---------------------------------------------------------------------------------------------------							  
						//		LENYEGEBEN IDAIG TART EGY-EGY BEJOVO SZAVAZAT FELDOLGOZASART FELELOS RESZ!!!!!!!!!!!!!!!!
						// ---------------------------------------------------------------------------------------------------

					} catch (Exception e) {

						e.printStackTrace();

					}

					// ...else the message that arrived was not an appropriate vote, and so we block this behaviour
					// until a new message arrives, even perhaps a correct vote.
				} else {

					block();

				}

				break;

			case 2: // Send a stop voting announcement to every voter...

				// ...just for the done() method (to return true).
				state = 3;

				// Update the progress bar on the GUI...
				myGui.progressBar.setValue(100);

				// Disable the user from stopping the voting on the GUI...
				myGui.stopButton.setEnabled(false);

				// Create the appropriate broadcast message to the voters...
				msg = new ACLMessage(ACLMessage.INFORM);
				msg.setContent("stop");

				// Add all the voters as receivers to the STOP-VOTING message, and create the statistics...
				String stats = "Name\tWon\n----\t---\n";
				for (int i = 0; i < voterAgents.size(); i++) {

					AID a 		= voterAgents.get(i);
					String aName	= a.getLocalName();
					String[] b	= voterBallots.get(a);

					// Let's find out if voter "a" voted for "b" the last time (i.e. if "a" won)...
					String won = "no";;
					if (!winnerName.isEmpty() && b.length == options.length && b[winnerId].equalsIgnoreCase("1")) won = "yes";

					// Let's construct the statistics for this voter...
					stats	+= 	aName + "\t" +
							won + "\n";

					// ...and prepare him/her a STOP-VOTING message...
					msg.addReceiver(a);

				}

				// Send the STOP-VOTING broadcast message to the voters...
				myAgent.send(msg);

				// Update the TextArea on the GUI appropriately...
				eventDescString		= "ANNOUNCE\tstop";
				currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
				myGui.appendText(currentTimeString + eventDescString + "\n");

				// Calculate and print the elapsed time since the beginning of the voting...
				String[] time = convertMS2HMS(System.currentTimeMillis() - startTimeOfVoting);
				System.out.println("Elapsed time:\t\t\t" + time[0] + " hours " + time[1] + " minutes and " + time[2] + " seconds...\n");

				// Print the information about the voters' performance to the console...
				System.out.println("========\nRESULTS:\n========\n\n" + stats);

				// Write the information about the voters' performance to an appropriate timestamped file...
				String resFileName = "log\\voting_res_" + uid + ".txt";
				DataHandler.writeString2File(resFileName, stats);

				// Notify the user about the file created...
				System.out.println("Results saved to: " + resFileName + "\n");

				break;

			} // End of switch 

		} // End of VotingMechanismAgent.AnnounceAndWait.action() method

		public boolean done() {

			// If this behaviour's state reached 2, then we can remove it from the pool
			// of active behaviours (which can be scheduled for execution by the JADE runtime)...
			// Comment: this behaviour can reach state 2 only with exterior "help" (by the TimeoutHandler)...
			return (state == 3);

		} // End of VotingMechanismAgent.AnnounceAndWait.done() method

	}  // End of VotingMechanismAgent.AnnounceAndWait behavour

	/**
	 * JADE behaviour: TimeoutHandler
	 * This behaviour wakes up (becomes active) when a given time (timeout) passes since its creation...
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class TimeoutHandler extends WakerBehaviour {

		private static final long serialVersionUID = -2313411205396673471L;

		TimeoutHandler(Agent a, long timeout) {//, AnnounceAndWait awb) {

			// Explicit invocation of the super class's (i.e. WakerBehaviour's) appropriate constructor...
			super(a, timeout);

		} // End of constructor

		protected void onWake() {

			// If the last round of the voting has ended (without all the votes arriving), then...
			if (round == maxRounds) {

				// Reset the state of the AnnounceAndWait behaviour instance (indirectly) to STOP...
				awBehaviour.setState(2);

				// If this last round has ended without bringing a final decision, then (there is no winner)...
				if (winnerName.isEmpty()) {

					// Update the TextArea on the GUI appropriately...
					String eventDescString		= "UNDECIDED";
					String currentTimeString	= "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");

					// ...else there is a winner.
				} else {

					// Update the TextArea on the GUI appropriately...
					String eventDescString	= "WINNER\t" + winnerName + " " + votes[winnerId];
					String currentTimeString = "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");

				}

				// ...else this is not the last round.
			} else {

				// If this round has ended without bringing a final decision, then (there is still a next round to decide)...
				if (winnerName.isEmpty()) {

					// The next round comes...
					round++;

					// Re-initialize the appropriate data for the next round...
					Vector<Integer> newOptionsIdxVector = CommonMethods.allMaxes(votes);

					String[] newoptions = new String[newOptionsIdxVector.size()];
					for (int i = 0; i < newOptionsIdxVector.size(); i++)
						newoptions[i] = options[newOptionsIdxVector.get(i)];

					// ...the options
					options = newoptions;

					// ...the votes (for the new options)
					votes = new int[options.length];
					for (int i = 0; i < votes.length; i++) votes[i] = 0;

					// ...the sum of these votes
					numberOfVotesInRound = 0;

					// ...and the indication about who voted in the new round (no one)
					for (int i = 0; i < voted.length; i++) voted[i] = false;

					// Reset the state of the AnnounceAndWait behaviour instance (indirectly) to ANNOUNCE...
					awBehaviour.setState(0);

					// Update the progress bar on the GUI...
					double progress = 100 * ((((double)round - 1.0) / (double)maxRounds) + ((1.0 / (double)maxRounds) * ((double)numberOfVotesInRound / (double)voterAgents.size())));
					myGui.progressBar.setValue((int)progress);

					// ...else there is a winner.
				} else {

					// Reset the state of the AnnounceAndWait behaviour instance (indirectly) to STOP...
					awBehaviour.setState(2);

					// Update the TextArea on the GUI appropriately...
					String eventDescString	= "WINNER\t" + winnerName + " " + votes[winnerId];
					String currentTimeString = "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()) + "]\t";
					myGui.appendText(currentTimeString + eventDescString + "\n");

				}

			}

			// Restart the related and currently blocked AnnounceAndWait behaviour instance...
			// It became blocked because no votes arrived until this TimeoutHandler behaviour woke up now...
			awBehaviour.restart();

		} // End of VotingMechanismAgent.TimeoutHandler.onWake() method

	} // End of VotingMechanismAgent.TimeoutHandler behaviour

} // End of VotingMechanismAgent class