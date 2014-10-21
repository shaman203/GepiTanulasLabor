/**
 * Copyright (c) 2010
 * Budapest University of Technology and Economics (BUTE)
 * Department of Measurement and Information Systems (DMIS)
 * All Rights Reserved.
 * 
 * "Cooperative and Learning Systems (VIMIM223)" laboratory
 * "Auctions and Voting" excercise
 * 
 * 				----------
 * 				VoterAgent
 * 				----------
 * 
 * Author:		Dániel László, Kovács (dkovacs@mit.bme.hu)
 * 
 */

package msclab01.votingauction_lab.VoterAgent;

import java.io.File;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import msclab01.votingauction_lab.*;

/**
 * JADE agent: VoterAgent
 * A voter agent for plurality voting...
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class VoterAgent extends Agent {
  
	private static final long serialVersionUID = -4427153511109068652L;
	
	/** The name of the option to vote for in the voting*/
	private String vote;
	/** A random number generator (with a random seed) */
	private Random r = new Random();	// int i = r.nextInt(j); // i is a uniform random number from 0 to j-1
	
	/** Put agent initializations here*/
	protected void setup() {

		System.out.println("VoterAgent " + getLocalName() + " starting...");
		
		// Register the voter service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("voter");
		sd.setName("voter");
		dfd.addServices(sd);

		try {

			DFService.register(this, dfd);

		} catch (FIPAException fe) {

			fe.printStackTrace();

		}

		// Get the Voting Configuration file's filePath as a start-up argument...
		Object[] args = getArguments();
  
		if (args != null && args.length > 0) {

			vote = (String)args[0];
		    
	    	System.out.println(getLocalName() + " is voting for " + vote + "...");

		} else {

			vote = "";
			
			System.out.println(getLocalName() + " is voting for no one...");

		}
		
		// Add the behavior for participating in votings...
		addBehaviour(new ParticipateInVoting());
		
	}  // End of VoterAgent.setup() method

	/** Put agent clean-up operations here*/
	protected void takeDown() {
  	
		// Deregister from the yellow pages
		try {
    	
			DFService.deregister(this);
      
		} catch (FIPAException fe) {
    	
			fe.printStackTrace();
      
		}
	
		// Printout a dismissal message
		System.out.println("VoterAgent " + getAID().getLocalName() + " terminating...");
    
	} // End of VoterAgent.takeDown() method
	
	/**
	 * JADE behaviour: ParticipateInVoting
	 * This is the behavior used by VoterAgent agents to participate in voting.
	 * 
	 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
	 *
	 */
	private class ParticipateInVoting extends Behaviour {

	  private static final long serialVersionUID = 2823366469733824448L;
	  
	  /** The single winner of the voting (initially empty)*/
	  String winner = "";
	  /** A template to receive messages*/
	  private MessageTemplate mt;
	  /** The state of this behaviour (init value is 0)*/
	  private int step = 0;
	
	  public void action() {
		  
		  switch (step) {

			  case 0:
	
				  mt = MessageTemplate.and(	MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						  MessageTemplate.MatchContent("start"));
	
				  // Receive an appropriate start message from the VotingMechanismAgent
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
	
				  // Receive an appropriate message from the VotingMechanismAgent
				  msg = myAgent.receive(mt);
	
				  if (msg != null) {
					  
					  // If the auction is over, then...
					  if (msg.getContent().equals("stop")) {
	
						  step = 2;	// !!!!!!!!!!!!!!!!!!!!!
	
					  // ...else the voting goes on.	
					  } else {
	
						  String[] msgTokens = msg.getContent().split(" ");
	
						  try {
						  
							  int actualRound	= Integer.parseInt(msgTokens[0]);

// ---------------------------------------------------------------------------------------------------							  
//		LENYEGEBEN INNEN INDUL A SZAVAZASERT FELELOS RESZ!!!!!!!!!!!!!!!!!!
// ---------------------------------------------------------------------------------------------------
							  
							  // If there are (still) more than one option, then...
							  if (msgTokens.length > 2 && !vote.isEmpty()) {
								  
								  // Let's be pessimistic, and suppose that the option we want to vote for
								  // isn't available among the options offered by the voting mechanism...
								  boolean doVote = false;
								  
								  // Let's initialize our ballot...
								  String[] ballot = new String[msgTokens.length-1];
								  
								  // Let's fill out the ballot appropriately...
								  for (int i = 0; i < msgTokens.length-1; i++)
									  	if (msgTokens[i+1].equalsIgnoreCase(vote)) {
									  		
									  		doVote = true;
									  		ballot[i] = "1";
									  	
									  	} else ballot[i] = "0";
								  
								  // If the option we wanted to vote for was available, let's vote...
								  if (doVote) {
									  
									// Let's create and send our ballot to the voting mechanism...
									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.REQUEST);
									reply.setContent(CommonMethods.join(ballot, " "));
									send(reply);

								  }

							  } else {
								  
								  // Save the single winner of the voting...
								  winner = msgTokens[2];
								  
							  }
						  
// ---------------------------------------------------------------------------------------------------							  
//		LENYEGEBEN IDAIG TART A SZAVAZASERT FELELOS RESZ!!!!!!!!!!!!!!!!!!
// ---------------------------------------------------------------------------------------------------
							  
						  } catch (Exception e) {
							  
							  e.printStackTrace();
							  
						  }
	
					  }
	
				  } else {
	
					  block();
	
				  }
	
				  break;

		  } // End of switch 
		    
	  } // End of VoterAgent.ParticipateInVoting.action() method

	  public boolean done() {
		  
		  if (step == 2) doDelete();
		  
		  // If this behaviour's state reached step 2, then we can remove it from the pool
		  // of active behaviours (which can be scheduled for execution by the JADE runtime)...
		  return (step == 2);

	  } // End of VoterAgent.ParticipateInVoting.done() method
	  
	}  // End of VoterAgent.ParticipateInVoting behavour
	
} // End of VoterAgent class
