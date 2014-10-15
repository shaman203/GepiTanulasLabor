package msclab01.gametheory_lab;

import java.awt.Color;
import java.io.Serializable;

/**
 * Class representing some player-types for some classic 2-person games
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class PlayerType implements Serializable {
	
	private static final long serialVersionUID = 3967235792469164329L;

	/** Constant identifying the UNKNOWN type. */
	public static final int UNKNOWN = 0;
	
	/** Constant identifying the EVERYONE type (i.e. the union of every type, even UNKNOWN). */
	public static final int EVERYONE = 1;
	
	/** Constant identifying the USER type, i.e. the strategy is chosen by a human player.
	 *  Can be used by users in any static/repeated game. */
	public static final int USER = 2;
	
	/** Constant identifying the RANDOM type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the strategy is chosen randomly (according to a uniform distribution over the pure strategies).
	 *  Can be used by agents in any static/repeated game. */
	public static final int RANDOM = 3;
	/** Constant identifying the DOMINANT type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the weakly dominant strategy is chosen (if there is no dominant strategy for the agent in a given role
	 *  in the game, then the choice is unspecified here - can be anything).
	 *  Can be used by agents in any static/repeated game. */
	public static final int DOMINANT = 4;
	/** Constant identifying the NASH type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the strategy prescribed by a pure Nash-equilibrium of the game is chosen (if there is no pure Nash-
	 *  equilibrium in the game, then the choice is unspecified here - can be anything).
	 *  Can be used by agents in any static/repeated game. */
	public static final int NASH = 5;
	/** Constant identifying the PARETO type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the strategy prescribed by a pure Pareto-optimum of the game is chosen.
	 *  Can be used by agents in any static/repeated game. */
	public static final int PARETO = 6;
	
	/** Constant identifying the TIT4TAT type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where at first the "cooperative" strategy of the repeated game is chosen, then the strategy our opponent chose.
	 *  Can be used by agents in any repeated game. */
	public static final int TIT4TAT = 7;
	
	/** Constant identifying the HAWK type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the HAWK (non-cooperate) strategy is chosen.
	 *  Can be used by agents in the static/repeated HAWK_DOVE game. */
	public static final int HAWK = 8;
	/** Constant identifying the DOVE type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the DOVE (cooperative) strategy is chosen.
	 *  Can be used by agents in the static/repeated HAWK_DOVE game. */
	public static final int DOVE = 9;
	
	/** Constant identifying the TALK type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the TALK (non-cooperate) strategy is chosen.
	 *  Can be used by agents in the static/repeated PRISONERS_DILEMMA game. */
	public static final int TALK = 10;
	/** Constant identifying the SILENT type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the SILENT (cooperative) strategy is chosen.
	 *  Can be used by agents in the static/repeated PRISONERS_DILEMMA game. */
	public static final int SILENT = 11;	
	
	/** Constant identifying the RECKLESS type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the RECKLESS (non-cooperate) strategy is chosen.
	 *  Can be used by agents in the static/repeated CHICKEN game. */
	public static final int RECKLESS = 12;
	/** Constant identifying the CHICKEN type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the CHICKEN (cooperative) strategy is chosen.
	 *  Can be used by agents in the static/repeated CHICKEN game. */
	public static final int CHICKEN = 13;	
	
	/** Constant identifying the OPERA type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the OPERA strategy is chosen.
	 *  Can be used by agents in the static/repeated ___asymmetric__ BATTLE_OF_SEXES game. */
	public static final int OPERA = 14;
	/** Constant identifying the FOOTBALL type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the FOOTBALL strategy is chosen.
	 *  Can be used by agents in the static/repeated __asymmetric__ BATTLE_OF_SEXES game. */
	public static final int FOOTBALL = 15;
	
	/** Constant identifying the GO type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the GO (non-cooperate) strategy is chosen.
	 *  Can be used by agents in the static/repeated LEADER game. */
	public static final int GO = 16;
	/** Constant identifying the WAIT type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the WAIT (cooperative) strategy is chosen.
	 *  Can be used by agents in the static/repeated LEADER game. */
	public static final int WAIT = 17;
	
	/** Constant identifying the HEADS type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the HEADS strategy is chosen.
	 *  Can be used by agents in the static/repeated MATCHING_PENNIES game. */
	public static final int HEADS = 18;
	/** Constant identifying the TAILS type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the TAILS strategy is chosen.
	 *  Can be used by agents in the static/repeated MATCHING_PENNIES game. */
	public static final int TAILS = 19;

	/** Constant identifying the DEFECT type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the DEFECT strategy is chosen.
	 *  Can be used by agents in the static/repeated __symmetric__ BATTLE_OF_SEXES game. */
	public static final int DEFECT = 20;
	/** Constant identifying the COOPERATE type (i.e. the strategy selection mechanism/algorithm/program) of the agent,
	 *  where the COOPERATE strategy is chosen.
	 *  Can be used by agents in the static/repeated __symmetric__ BATTLE_OF_SEXES game. */
	public static final int COOPERATE = 21;
	
	/** A constant for the overall number of types. */
	public static final int TYPENUM = 22;
	
	/** The typeID of this PlayerType object */
	private int tid;
	
	/**
	 * Construct a PlayerType of a given typeID
	 * 
	 * @param tid typeID
	 */
	public PlayerType(int tid) {

		this.tid = tid;

	} // End of PlayerType(int) constructor
	
	/**
	 * Get the PlayerType's typeID
	 * 
	 * @return typeID
	 */
	public int getTid() {

		return this.tid;

	} // End of PlayerType.getTid(int) method
	
	/**
	 * Set the PlayerType's typeID
	 * 
	 * @param tid typeID
	 */
	public void setTid(int tid) {

		this.tid = tid;

	} // End of PlayerType.setTid() method
	
	/**
	 * Get a recommended Color for a typeID.
	 * Can be used by applications having a GUI, and
	 * during visualization perhaps wanting to
	 * distinguish between the different types.
	 * 
	 * @param tid typeID
	 * 
	 * @return Color
	 * 
	 */
	public static Color getTypeColor(int tid) {

		/*
		---------------------------
		Some colors to choose from:
		---------------------------

 		Color.BLACK
 		Color.BLUE
 		Color.CYAN
 		Color.DARK_GRAY
 		Color.GRAY
 		Color.GREEN
 		Color.LIGHT_GRAY
 		Color.MAGENTA
 		Color.ORANGE
 		Color.PINK
 		Color.RED
 		Color.WHITE
 		Color.YELLOW
		 */

		// First set the Color to return to a default value...
		Color returnval = Color.GRAY;

		// Let's choose an appropriate color for the type...
		switch(tid) {

			case UNKNOWN:
				
				returnval = Color.GRAY;
				break;

			case EVERYONE:
				
				returnval = Color.CYAN;
				break;
				
			case USER:

				returnval = Color.BLACK;
				break;

			case RANDOM:

				returnval = Color.WHITE;
				break;

			case DOMINANT:

				returnval = Color.YELLOW;
				break;

			case NASH:

				returnval = Color.MAGENTA;
				break;

			case PARETO:

				returnval = Color.PINK;
				break;

			case TIT4TAT:

				returnval = Color.BLUE;
				break;

			case HAWK:

				returnval = Color.RED;
				break;

			case DOVE:

				returnval = Color.GREEN;
				break;

			case TALK:

				returnval = Color.RED;
				break;

			case SILENT:

				returnval = Color.GREEN;
				break;

			case RECKLESS:

				returnval = Color.RED;
				break;

			case CHICKEN:

				returnval = Color.GREEN;
				break;

			case OPERA:

				returnval = Color.RED;
				break;

			case FOOTBALL:

				returnval = Color.GREEN;
				break;

			case GO:

				returnval = Color.RED;
				break;

			case WAIT:

				returnval = Color.GREEN;
				break;

			case HEADS:

				returnval = Color.RED;
				break;

			case TAILS:		

				returnval = Color.GREEN;
				break;

			case DEFECT:

				returnval = Color.RED;
				break;

			case COOPERATE:

				returnval = Color.GREEN;
				break;
				
		} // SWITCH

		return returnval;

	} // End of PlayerType.getTypeColor(int) method

} // End of PlayerType class
