package msclab01.gametheory_lab;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class representing complete, perfect/imperfect information, normal form games
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class Game implements Serializable {

	private static final long serialVersionUID = 5076654959795084395L;

	/** Constant identifying the Hawk-Dove game */
	public static final int HAWK_DOVE = 0;
	/** Constant identifying the Prisoners Dilemma game */
	public static final int PRISONERS_DILEMMA = 1;
	/** Constant identifying the Chicken game */
	public static final int CHICKEN = 2;
	/** Constant identifying the Battle of Sexes game */
	public static final int BATTLE_OF_SEXES = 3;
	/** Constant identifying the Cock of the Walk game */
	public static final int LEADER = 4;
	/** Constant identifying the Matching Pennies game */
	public static final int MATCHING_PENNIES = 5;

	/** The identifier of this game (a Game.XYZ constant, or a filename (without extension) of
	 * a normal form game (.nfg file) made by GAMBIT to \jade\src\msclab01\gametheory_lab\games*/
	private String game_id;
	/** The name of this game */
	private String game_name;
	/** The names of players in this game */
	private Vector<String> player_names;
	/** The names of the strategies in this game */
	private Vector<String[]> strat_names;
	/** The number of pure strategies for every player in this game */
	private int[] strat_nums;
	/** The number of players in this game */
	private int player_num;
	/** Is true for symmetric games, else false */
	private boolean is_symmetric;
	/** Is true for games where there is one cooperative pure strategy for every player, else false */
	private boolean is_cooperative;
	/** The IDs of cooperative pure strategies of every player (if the game is cooperative) */
	public int[] coopStrats;
	/** The HashMap containing every payoff of every player for every outcome*/
	public HashMap<String, double[]> outcomes;
	/** The mixed-strategy solution profiles of a game.
	 *  A double[] describes a mixed strategy of a player.
	 *  Thus a Vector(double[]) is a mixed strategy combination of all the players - a solution.
	 *  ...and a Vector of these, a Vector(Vector(double[])) is a list of such solutions.
	 *  Comment: these are the solutions of the Nash-equilbrium finding method called in solve.bat
	 *  */
	private Vector<Vector<double[]>> solutions;
	/** The expect payoff for the mixed-strategy solution profiles of a game.
	 *  A double[] describes the expected payoffs of players in case of a given mixed-strategy combination,
	 *  and thus Vector(double[]) is a list of such EPs (for every solution profile).
	 *  */
	private Vector<double[]> solutionsEP;
	
	/** Create a predefined game
	 * 
	 * @param id identifier of the game (a filename (without extension) or a Game.* constant)
	 * 
	 */
	public Game(String id) {

		// Set the game-ID...
		game_id = id;
		
		// If the id parameter of the constructor is parseable to an int identifying a predefined game...
		if (new Scanner(id).hasNextInt()) {

			// Now for predefined games we'll just use 2-person 2-2 strategy games...
			player_num			= 2;

			player_names		= new Vector<String>(player_num);
			strat_names			= new Vector<String[]>(player_num);
			
			strat_nums			= new int[player_num];
			strat_nums[0]		= 2;
			strat_nums[1]		= 2;
			
			int sprod	= 1;
			for (int  i = 0; i < strat_nums.length; i++) sprod *= strat_nums[i];
			
			outcomes	= new HashMap<String, double[]>(sprod);
			
			// Switch between predefined games...
			switch(new Scanner(id).nextInt()) {
	
				// Strategy 0 is for HAWK, 1 is for DOVE
				case HAWK_DOVE:
	
					game_name		= "Hawk-Dove";
					player_names.add("1");
					player_names.add("2");
					strat_names.add(new String[]{"Hawk", "Dove"}); // The strategy names of player 1
					strat_names.add(new String[]{"Hawk", "Dove"}); // The strategy names of player 2
					is_symmetric	= true;
					is_cooperative	= true;
					coopStrats 		= new int[]{1, 1};
					
					
					double v = 4.0;				// The value of the resource, that can be gained (>0.0)...
					double c = 2.0;				// The cost of fighting for that resource (>=0.0)...
	
					outcomes.put("0-0", new double[]{(v-c)/2, (v-c)/2});
					outcomes.put("1-0", new double[]{0, v});				// The payoff of p1 is 0, when p1 plays "1" and p2 plays "0"...
					outcomes.put("0-1", new double[]{v, 0});				// The payoff of p2 is 0, when p1 plays "0" and p2 plays "1"...
					outcomes.put("1-1", new double[]{v/2, v/2});
	
					if (v < c) {
						
						solutions = new Vector<Vector<double[]>>(2);
						
						Vector<double[]> solutioncv0	= new Vector<double[]>(2);
						solutioncv0.add(new double[]{1, 0});	// q0 = (1.0*Hawk; 0.0*Dove)
						solutioncv0.add(new double[]{0, 1});	// q1 = (0.0*Hawk; 1.0*Dove)
						
						Vector<double[]> solutioncv1	= new Vector<double[]>(2);
						solutioncv1.add(new double[]{0, 1});	// q0 = (0.0*Hawk; 1.0*Dove)
						solutioncv1.add(new double[]{1, 0});	// q1 = (1.0*Hawk; 0.0*Dove)
						
						solutions.add(solutioncv0);
						solutions.add(solutioncv1);
						
					} else { // if (v >= c)
						
						solutions = new Vector<Vector<double[]>>(1);
						
						Vector<double[]> solutionvc0	= new Vector<double[]>(2);
						solutionvc0.add(new double[]{1, 0});	// q0 = (1.0*Hawk; 0.0*Dove)
						solutionvc0.add(new double[]{1, 0});	// q1 = (1.0*Hawk; 0.0*Dove)
						
						solutions.add(solutionvc0);
						
					}
					
					break;
					
				// Strategy 0 is for TALK, 1 is for SILENT
				case PRISONERS_DILEMMA:
	
					game_name = "Prisoners' Dilemma";
					player_names.add("1");
					player_names.add("2");
					strat_names.add(new String[]{"Talk", "Silent"}); // The strategy names of player 1
					strat_names.add(new String[]{"Talk", "Silent"}); // The strategy names of player 2
					is_symmetric	= true;
					is_cooperative	= true;
					coopStrats 		= new int[]{1, 1};
					
					outcomes.put("0-0", new double[]{-2, -2});
					outcomes.put("1-0", new double[]{-3, 3});
					outcomes.put("0-1", new double[]{3, -3});
					outcomes.put("1-1", new double[]{2, 2});
					
					solutions = new Vector<Vector<double[]>>(1);
					
					Vector<double[]> solutionpd0	= new Vector<double[]>(2);
					solutionpd0.add(new double[]{1, 0});	// q0 = (1.0*Talk; 0.0*Silent)
					solutionpd0.add(new double[]{1, 0});	// q1 = (1.0*Talk; 0.0*Silent)
					
					solutions.add(solutionpd0);
					
					break;
					
				// Strategy 0 is for RECKLESS, 1 is for CHICKEN
				case CHICKEN:
	
					game_name = "Chicken";
					player_names.add("1");
					player_names.add("2");
					strat_names.add(new String[]{"Reckless", "Chicken"}); // The strategy names of player 1
					strat_names.add(new String[]{"Reckless", "Chicken"}); // The strategy names of player 2
					is_symmetric	= true;
					is_cooperative	= true;
					coopStrats 		= new int[]{1, 1};
					
					outcomes.put("0-0", new double[]{-10, -10});
					outcomes.put("1-0", new double[]{-1, 1});
					outcomes.put("0-1", new double[]{1, -1});
					outcomes.put("1-1", new double[]{0, 0});
					
					solutions = new Vector<Vector<double[]>>(2);
					
					Vector<double[]> solutionch0	= new Vector<double[]>(2);
					solutionch0.add(new double[]{1, 0});	// q0 = (1.0*Reckless; 0.0*Chicken)
					solutionch0.add(new double[]{0, 1});	// q1 = (0.0*Reckless; 1.0*Chicken)
					
					Vector<double[]> solutionch1	= new Vector<double[]>(2);
					solutionch1.add(new double[]{0, 1});	// q0 = (0.0*Reckless; 1.0*Chicken)
					solutionch1.add(new double[]{1, 0});	// q1 = (1.0*Reckless; 0.0*Chicken)
					
					solutions.add(solutionch0);
					solutions.add(solutionch1);
					
					break;
					
				// Strategy 0 is for DEFECT, 1 is for COOPERATE
				case BATTLE_OF_SEXES:
	
					game_name = "Battle of Sexes";
					player_names.add("1");
					player_names.add("2");
					strat_names.add(new String[]{"Defect", "Cooperate"}); // The strategy names of player 1
					strat_names.add(new String[]{"Defect", "Cooperate"}); // The strategy names of player 2
					is_symmetric	= true;
					is_cooperative	= true;
					coopStrats 		= new int[]{1, 1};
					
					// SYMMETRIC VERSION				
					outcomes.put("0-0", new double[]{0, 0});
					outcomes.put("1-0", new double[]{1, 2});
					outcomes.put("0-1", new double[]{2, 1});
					outcomes.put("1-1", new double[]{-1, -1});
					
					solutions = new Vector<Vector<double[]>>(2);
					
					Vector<double[]> solutionbs0	= new Vector<double[]>(2);
					solutionbs0.add(new double[]{1, 0});	// q0 = (1.0*Defect; 0.0*Cooperate)
					solutionbs0.add(new double[]{0, 1});	// q1 = (0.0*Defect; 1.0*Cooperate)
					
					Vector<double[]> solutionbs1	= new Vector<double[]>(2);
					solutionbs1.add(new double[]{0, 1});	// q0 = (0.0*Defect; 1.0*Cooperate)
					solutionbs1.add(new double[]{1, 0});	// q1 = (1.0*Defect; 0.0*Cooperate)
					
					solutions.add(solutionbs0);
					solutions.add(solutionbs1);
					
					break;
					
				// Strategy 0 is for GO, 1 is for WAIT
				case LEADER:
	
					game_name = "Leader";
					player_names.add("1");
					player_names.add("2");
					strat_names.add(new String[]{"Go", "Wait"}); // The strategy names of player 1
					strat_names.add(new String[]{"Go", "Wait"}); // The strategy names of player 2
					is_symmetric	= true;
					is_cooperative	= true;
					coopStrats 		= new int[]{1, 1};

					outcomes.put("0-0", new double[]{-1, -1});
					outcomes.put("1-0", new double[]{1, 2});
					outcomes.put("0-1", new double[]{2, 1});
					outcomes.put("1-1", new double[]{0, 0});
					
					solutions = new Vector<Vector<double[]>>(2);
					
					Vector<double[]> solutionld0	= new Vector<double[]>(2);
					solutionld0.add(new double[]{1, 0});	// q0 = (1.0*Go; 0.0*Wait)
					solutionld0.add(new double[]{0, 1});	// q1 = (0.0*Go; 1.0*Wait)
					
					Vector<double[]> solutionld1	= new Vector<double[]>(2);
					solutionld1.add(new double[]{0, 1});	// q0 = (0.0*Go; 1.0*Wait)
					solutionld1.add(new double[]{1, 0});	// q1 = (1.0*Go; 0.0*Wait)
					
					solutions.add(solutionld0);
					solutions.add(solutionld1);
					
					break;
					
				// Strategy 0 is for HEADS, 1 is for TAILS
				case MATCHING_PENNIES:
					
					game_name = "Matching Pennies";
					player_names.add("Equal");
					player_names.add("Different");
					strat_names.add(new String[]{"Heads", "Tails"}); // The strategy names of player 1
					strat_names.add(new String[]{"Heads", "Tails"}); // The strategy names of player 2
					is_symmetric	= false;
					is_cooperative	= false;
					
					outcomes.put("0-0", new double[]{1, -1});
					outcomes.put("1-0", new double[]{-1, 1});
					outcomes.put("0-1", new double[]{-1, 1});
					outcomes.put("1-1", new double[]{1, -1});
					
					solutions = new Vector<Vector<double[]>>(1);
					
					Vector<double[]> solutionmp0	= new Vector<double[]>(2);
					solutionmp0.add(new double[]{0.5, 0.5});	// q0 = (0.5*Heads; 0.5*Tails)
					solutionmp0.add(new double[]{0.5, 0.5});	// q1 = (0.5*Heads; 0.5*Tails)
					
					solutions.add(solutionmp0);
					
					break;
					
			} // SWITCH

		} else { // The id parameter of the constructor wasn't parseable to int...

			// =======================================================
			// READ the .NFO meta-information file of the .NFG game...
			// -------------------------------------------------------
			
			String nfoFilePath	= "sajat_games\\" + game_id + ".nfo";
			
			// IF there is no explicit meta-information about this game (about symmetry and cooperativity), then...
			if (!(new File(nfoFilePath)).exists()) {
			
				is_symmetric	= false;
				is_cooperative	= false;
				
			} else {
				
				String nfoText	 	= DataHandler.readFile2String(nfoFilePath);
				String[] stok 		= nfoText.split("\t");
				
				if (stok[0].equals("1")) is_symmetric	= true;	else is_symmetric	= false;
				if (stok[1].equals("1")) is_cooperative = true; else is_cooperative = false;
				
				// ...and if cooperation can be interpreted in this game in case of every player, then...
				if (is_cooperative) {
					
					// Parse all the IDs of those cooperative pure strategies (for every player)...
					String[] coopStratss	= stok[2].split(" ");
					this.coopStrats			= new int[coopStratss.length];
					for (int i = 0; i < coopStratss.length; i++)
						this.coopStrats[i]	= Integer.parseInt(coopStratss[i]);
					
				}
				
			}
			
			// ==========================================================
			// READ the generated .SOL solution file for the .NFG game...
			// ----------------------------------------------------------

			String nfgFilePath	= "E:\\Workspace\\eclipse workspace\\kooplab\\labor4\\sajat_games\\" + game_id + ".nfg";
			String problem	= DataHandler.readFile2String(nfgFilePath);
					
			String pattern	= "\".[^\\{|\\}]+\"";	// Ezzel ki lehet szedni a jatek, a jatekosok, es a strategiak nevet
			Pattern p		= Pattern.compile(pattern);
			Matcher pm		= p.matcher(problem);
			
			// ---------
			// GAME NAME
			// ---------
			pm.find();
			pattern			= "(?<=\").*(?=\")";
			Pattern pgn		= Pattern.compile(pattern);
			Matcher pgnm	= pgn.matcher(pm.group());
			pgnm.find();
			game_name		= pgnm.group();
			
			// ------------
			// PLAYER NAMES
			// ------------
			pm.find();
			pattern				= "(?<=\")\\w[^\"]+(?=\")";		// Players' name must be non-empty...
			Pattern ppn			= Pattern.compile(pattern);
			Matcher ppnm		= ppn.matcher(pm.group());
			
			player_num		= 0;
			player_names	= new Vector<String>();
			while (ppnm.find()) {
				
				this.player_num++;
				player_names.add(ppnm.group());
				
			}
			
			// --------------------------
			// STRATEGY NAMES AND NUMBERS
			// --------------------------
			pattern				= "(?<=\")\\w[^\"]*(?=\")";		// Players' strategies' name must be non-empty...
			Pattern psn			= Pattern.compile(pattern);
			
			strat_names = new Vector<String[]>();
			while (pm.find()) {
				
				Matcher psnm = psn.matcher(pm.group());	

				psnm.find();
				String snames = psnm.group();	// There must be at least 1 strategy...
							
				while (psnm.find()) snames += "\t" + psnm.group();

				strat_names.add(snames.split("\t"));
				
			}
			
			// Calculate the number of strategies of each player and their product too...
			int sprod		 	= 1;
			String[] sids	 	= new String[player_num];
			strat_nums 			= new int[player_num];
			double[] niloutcome	= new double[player_num];
			
			for (int i = 0; i < player_num; i++) {
				
				strat_nums[i]	= strat_names.elementAt(i).length;
				sprod			*= strat_nums[i];
				sids[i]			= "0"; 	// Meanwhile initialize a 0-0-0-...-0 strategy combination for later use...
				niloutcome[i] 	= 0;	// ...and also initialize the full-zero outcome for potential use...
				
			}

			// --------
			// OUTCOMES
			// --------
			
			// Initialize a vector of outcomes read directly from the NFG file (in order of appearance)
			Vector<double[]> outcomes_unordered = new Vector<double[]>(sprod);

			pattern		= "(?<=\\{).*\"\".*(?=\\})";	// Ezzel ki lehet szedni a payoffokat strat-kombonkent
			p			= Pattern.compile(pattern);
			pm			= p.matcher(problem);
			
			pattern		= "-?\\d+\\.?/?\\d*";				// Regular expression pattern to parse positive or negative (or zero) valued doubles...
			Pattern po	= Pattern.compile(pattern);

			while (pm.find()) {
				
				Matcher pom = po.matcher(pm.group());	
				
				int i = 0;
				double[] outcome = new double[player_num];
				while (pom.find()) {
					
					try {
						
						if (pom.group().contains("/")) {
							
							String[] pomgroup = pom.group().split("/");
							outcome[i] = Double.parseDouble(pomgroup[0]) / Double.parseDouble(pomgroup[1]);
							
						} else {
							
							outcome[i] = Double.parseDouble(pom.group());
					
						}
						
					} catch (Exception e) {
						
						e.printStackTrace();
					
					}
					
					i++;
					
				}
				
				// Collect the outcomes in their order of appearance in the NFG file
				outcomes_unordered.add(outcome);

			} // While outcomes
			
			pattern		= "(?<=\\n)\\d+( \\d+)*";	// Ezzel ki lehet szedni a strat-kombok sorrendjet az NFG vegerol
			Pattern pd	= Pattern.compile(pattern);
			Matcher pdm	= pd.matcher(problem);
			pdm.find();
			
			// The appropriate order of the previously parsed outcomes (outcomes_unordered)
			String[] order = pdm.group().split(" ");
			
			// Initialize the final set of properly ordered outcomes
			outcomes = new HashMap<String, double[]>(sprod);
			
			// Initial strategy combination (0-0-0-...-0) identifying the first outcome...
			String sidss = Game.join(sids, "-");
			
			// Fill the outcomes in the proper order
			for (int i = 0; i < order.length; i++) {
			
				// If the NFG refers to an existing, explicitly given outcome, then...
				if (Integer.parseInt(order[i]) > 0) {
				
					// Associate the appropriate payoffs (outcome) with the appropriate strategy combination (sidss)
					outcomes.put(sidss, outcomes_unordered.get(Integer.parseInt(order[i])-1));
					
				} else { // order[i] == 0
					
					// Associate zero payoffs (niloutcome) to the appropriate strategy combination (sidss)
					outcomes.put(sidss, niloutcome);
					
				}
				
				// Increment the strategy combination (sidss) appropriately to GAMBIT's NFG-file semantics...
				sidss = Game.modifySids(sidss, strat_nums, 1);
			
			} // For outcomes
				
			// ---------
			// SOLUTIONS
			// ---------
			
			// Fill the solutions data structure appropriately...
			solve(this);
			
		} // ELSE IF the id parameter of the Game class' constructor wasn't parseable to int...

		// ----------------
		// EXPECTED PAYOFFS
		// ----------------
		
		// Initialize the data structure holding the expected payoff of every player for every solution...
		solutionsEP = new Vector<double[]>(solutions.size());
		
		// Calculate the expected payoff of every player for every solution...
		for (int k = 0; k < solutions.size(); k++) solutionsEP.add(calcEPs(solutions.get(k)));
		
	} // End of Game(String) constructor
	
	/** Return the payoff a player in case of a strategy combination.
	 *  
	 * @param i ...the ID of the player - goes from 0 to "unbounded finiteness" (Ivan Bach (tm))
	 * @param s the strategy combination where the payoff of player i is important now
	 * @return the payoff of player i in case of strategy combination s
	 * 
	 */
	public double getPayoff(int i, String s) {
		
		return outcomes.get(s)[i];
		
	} // End of Game.getPayoff() method
	
	/** Return the ID of this game.
	 *  
	 * @return the ID of this game
	 * 
	 */
	public String getGame_ID() {
		
		return this.game_id;
		
	} // End of Game.getGame_ID() method

	
	/**
	 * 
	 * Get the name of this game
	 * 
	 * @return the name of this game
	 */
	public String getName() {
		
		return this.game_name;
		
	} // End of Game.getName() method
	
	/**
	 * 
	 * Get the number of players of this game
	 * 
	 * @return number of players of this game
	 */
	public int getPlayerNum() {
		
		return this.player_num;
		
	} // End of Game.getName() method
	
	/**
	 * 
	 * Get the name of a player in this game
	 * 
	 * @param i ...the ID of the player
	 * @return the names of the player in this game
	 */
	public String getPlayerName(int i) {
		
		return player_names.elementAt(i);
		
	} // End of Game.getPlayerName() method
	
	/**
	 * 
	 * Get the number of strategies of a player in this game
	 * 
	 * @param i ...the ID of the player
	 * @return the number of strategies of a player in this game
	 */
	public int getStratNums(int i) {
		
		return strat_nums[i];
		
	} // End of Game.getStratNums() method
	
	/**
	 * 
	 * Get the numbers of all the strategies of every player in this game
	 * 
	 * @return the numbers of all the strategies of every player in this game
	 */
	public int[] getAllStratNums() {
		
		return strat_nums;
		
	} // End of Game.getAllStratNums() method
	
	/**
	 * 
	 * Get the names of the strategies of a player in this game
	 * 
	 * @param i ...the ID of the player
	 * @return the names of the strategies of a player in this game
	 */
	public String[] getStratNames(int i) {
		
		return strat_names.get(i);
		
	} // End of Game.getStratNames() method
	
	/**
	 * 
	 * Get the truth about the game being symmetric
	 * 
	 * @return the truth about the game being symmetric
	 */
	public boolean getSymmetry() {
		
		return this.is_symmetric;
		
	} // End of Game.getSymmetry() method

	/**
	 * 
	 * Get the truth about the game being cooperative
	 * 
	 * @return the truth about the game being cooperative
	 */
	public boolean getCooperative() {
		
		return this.is_cooperative;
		
	} // End of Game.getCooperative() method
	
	/**
	 * 
	 * Get the possible outcomes of the game (payoffs for every player in case of every strategy combination)
	 * 
	 * @return the possible outcomes of the game (payoffs for every player in case of every strategy combination)
	 */
	public HashMap<String, double[]> getOutcomes() {
		
		return this.outcomes;
		
	} // End of Game.getOutcomes() method
	
	/**
	 * 
	 * Get all the solutions (extremal Nash-equiliria) of the game
	 * 
	 * @return all the solutions (extremal Nash-equiliria) of the game
	 */
	public Vector<Vector<double[]>> getSolutions() {
		
		return this.solutions;
		
	} // End of Game.getSolutions() method
	
	/**
	 * 
	 * Get the expected payoff of every player for all solutions (extremal Nash-equiliria) of the game
	 * 
	 * @return the expected payoff of every player for all solutions of the game
	 */
	public Vector<double[]> getSolutionsEP() {
		
		return this.solutionsEP;
		
	} // End of Game.getSolutionsEP() method
	
	/** Increment or decrement a strategy-ID vector (identifying an outcome).
	 *  
	 * @param s the pure strategy-ID vector to be incremented or decremented
	 * @param sNums the number of pure strategies for every player (from 0 to n-1)
	 * @param dir if (dir == 1) increment; else decrement;
	 * 
	 */
	private static String modifySids(String ss, int[] sNums, int dir) {

		boolean carry	= true;
		String[] s		= ss.split("-");
		int pos			= 0;
		int spos;
		
		while (pos < sNums.length && carry) {
			
			spos = Integer.parseInt(s[pos]);
			
			if (dir == 1) {	// increment
				
				if (spos < sNums[pos]-1) {
					
					s[pos] = Integer.toString(++spos);
					carry = false;
					
				} else {
					
					// rotation
					s[pos] = Integer.toString(0);
					
				}
				
			} else {	// decrement
				
				if (spos > 0) {
					
					s[pos] = Integer.toString(--spos);
					carry = false;
					
				} else {
					
					// rotation
					s[pos] = Integer.toString(sNums[pos]-1);
					
				}
				
			}

			pos++;

		}
		
		return Game.join(s, "-");
		
	} // End of Game.modifySids() static method
	
	/** Inverse of String.split()*/
	public static String join(String[] s, String delimiter) {
		
		String out = s[0];
	    for (int i = 1; i < s.length; i++) out += delimiter + s[i];
	    return out;
	    
	} // End of Game.join() static method
	
	/** Calculate (even all) extremal Nash-equilibria of a GAMBIT .NFG game
	 * 
	 * @param g a game based on a GAMBIT .NFG file
	 * @return all the solutions of the Nash-equilbrium finding method called in solve.bat for the GAMBIT .NFG game
	 * */
	public static void solve(Game g) {
		
		String game_id			= g.getGame_ID();
		
		Vector<Vector<double[]>> solutions	= new Vector<Vector<double[]>>();
		
		String solFilePath	= "E:\\Workspace\\eclipse workspace\\kooplab\\labor4\\sajat_games\\" + game_id + ".sol";

		try {
			
			// IF there was no solution generated to this game until now, then...
			if (!(new File(solFilePath)).exists()) {
			
				// ----------------------------------------------------
				// Generate the .SOL solution file for the .NFG game...
				// ----------------------------------------------------
				
				String solveGIDPath	= "E:\\Workspace\\eclipse workspace\\kooplab\\labor4\\gambit\\solve.bat " + game_id;
				Process proc		= Runtime.getRuntime().exec(solveGIDPath);
				proc.waitFor();
				
			}
			
			// ----------------------------------------------------------
			// Read the generated .SOL solution file for the .NFG game...
			// ----------------------------------------------------------

			String solText	 	= DataHandler.readFile2String(solFilePath);
		 	
			// Parse the solution read...
			String pattern	= "(?<=NE,).*";				// Ezzel ki lehet szedni egyenkent a megoldasokat
			Pattern p		= Pattern.compile(pattern);
			Matcher pm		= p.matcher(solText);

			pattern			= "\\d+\\.?/?\\d*";			// Regular expression pattern to parse non-negative doubles...
			Pattern ps		= Pattern.compile(pattern);
			
			// Find solutions 1-by-1...
			while (pm.find()) {
				
				Matcher psm = ps.matcher(pm.group());

				Vector<double[]> solution = new Vector<double[]>(g.getPlayerNum());
				
				// Find solution-elements (mixed-strategy probabilities) 1-by-1...
				for (int i = 0; i < g.getPlayerNum(); i++) {
					
					// A mixed strategy qi of player i
					double[] qi = new double[g.getStratNums(i)];
					
					for (int j = 0; j < g.getStratNums(i); j++) {
						
						psm.find();
						try {
							
							if (psm.group().contains("/")) {
								
								String[] psmgroup = psm.group().split("/");
								qi[j] = Double.parseDouble(psmgroup[0]) / Double.parseDouble(psmgroup[1]);
								
							} else {
								
								qi[j] = Double.parseDouble(psm.group());
						
							}
							
						} catch (Exception e) {
							
							e.printStackTrace();
						
						}
						
					} // For all the pure strategies of a player...
					
					solution.add(qi);
					
				} // For all the players...
				
				solutions.add(solution);
				
			} // While solutions
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		g.solutions = solutions;
	    
	} // End of Game.solve() static method
	
	/** Calculate the expected profit of every player in case of a given mixed-strategy combination
	 * 
	 * @param q a mixed-strategy combination
	 * @return the expected payoff for every player
	 * */
	public double[] calcEPs(Vector<double[]> q) {
		
		// Create some important data structures (for holding the expected profits and pure startegy combinations)...
		double[] eps	= new double[this.player_num];
		String[] sids	= new String[this.player_num];
		
		// Initialize these data structures...
		for (int i = 0; i < this.player_num; i++) {
			
			sids[i] = "0";
			eps[i]	= 0;
			
		}
		
		// The 0-0-0-...-0 strategy combination...
		String sidss = Game.join(sids, "-");
		
		// Calculate the expected payoff of every player by going through all possible pure strategy combinations...
		for (int s = 0; s < this.outcomes.size(); s++) {
			
			// Necessary update (since sidss is incremented below)...
			sids = sidss.split("-");

			// Initial probability of sids...
			double sidsprob = 1;
			
			// Calculate the correct probability of sids assuming q...
			for (int i = 0; i < this.player_num; i++) sidsprob	*= q.get(i)[Integer.parseInt(sids[i])];
			
			// Increment the expected payoff of every player appropriately...
			for (int i = 0; i < this.player_num; i++) eps[i]	+= sidsprob * this.outcomes.get(sidss)[i];

			// Increment the currently examined strategy combination...
			sidss = Game.modifySids(sidss, this.strat_nums, 1);
			
		}
		
		// Return the expected payoff of every player in case of mixed-strategy combination q...
		return eps;
		
	} // End of Game.calcEPs() method
	
	/** Generate a GAMBIT .NFG file for the Tragedy of the Commons game with a given number of players
	 * 
	 * @param pnum the number of players
	 * */
	public static void genTotc(int pnum) {
		
		String target = "/jade/src/msclab01/gametheory_lab/games/totc";
		
		// ----------
		// GAME TITLE
		// ----------
		
		// The prefix of the string holding the complete NFG text...
		String nfg = "NFG 1 R \"Tragedy of the Commons (" + pnum + " players)\" {";
		
		// -------
		// PLAYERS
		// -------
		
		// Add the names of the players...
		for (int i = 0; i < pnum; i++) nfg += " \"Player " + (i+1) + "\"";
		
		// ----------
		// STRATEGIES
		// ----------
		
		// Add the prefix for the list of players strategies...
		nfg += " }\n\n{ ";
		
		// The list of strategies of a player...
		String strats = "{ \"D\" \"C\" }\n";
		
		// Add the list of players' strategies...
		for (int i = 0; i < pnum; i++) nfg += strats;
		
		// --------
		// OUTCOMES
		// --------
		
		// Add the prefix for the list of outcomes...
		nfg += "}\n\"\"\n\n{\n";
		
		// The prefix of an outcome...
		String opr = "{ \"\" ";
		
		// The postfix of an outcome...
		String ops = " }\n";
		
		// The delimiter between payoffs...
		String od = ", ";
		
		// Initialize the initial pure strategy combination and the number of pure strategies of players...
		String[] sids	= new String[pnum];
		int[] snums		= new int[pnum];
		for (int i = 0; i < pnum; i++) {
			
			sids[i]		= "0";
			snums[i]	= 2;
			
		} // FOR players
		
		// The 0-0-0-...-0 strategy combination...
		String sidss = Game.join(sids, "-");
		
		// Add the appropriate string to NFG for every outcome...
		for (int s = 0; s < Math.pow(2, pnum); s++) {
			
			// Initialize the outcome string array...
			String[] o	= new String[pnum];
			
			// Necessary update (since below sidss is incremented)...
			sids		= sidss.split("-");

			// Calculate the number of defective agents in case of the given pure strategy combination (sids)...
			int ds	= 0; for (int i = 0; i < pnum; i++) ds += (1-Integer.parseInt(sids[i]));
			
			// Calculate the appropriate payoffs in case of the given pure strategy combination (sids)...
			for (int i = 0; i < pnum; i++) {
				
				// IF player i is cooperative in case of sids...
				if (sids[i].equals("1")) {
					
					// Outcome of an cooperative agent in case of sids...
					o[i] = Integer.toString(pnum - ds);
					
				} else { // sids[i].equals("0")
					
					// Outcome of a defective agent in case of sids...
					o[i] = Integer.toString(2 * (pnum - ds));
					
				} // IF cooperative
				
			} // FOR players
			
			// Add the appropriate outcome (corresponding to sids) to the NFG string...
			nfg += opr + Game.join(o, od) + ops;
			
			// Increment the pure strategy combination...
			sidss = Game.modifySids(sidss, snums, 1);
			
		} // FOR outcomes
		
		// ----------------
		// OUTCOME ORDERING
		// ----------------
		
		// The prefix for the outcome ordering...
		nfg += "}\n";
		
		// Order the outcomes in a straightforward fashion...
		for (int s = 0; s < Math.pow(2, pnum); s++) nfg += (s+1) + " ";
		
		// A new line at the end of the NFG file...
		nfg += "\n";

		// ------------
		// GENERATE NFG
		// ------------
		
		// Set the GameID...
		String game_id = target + pnum;
		
		// Write the NFG string to the appropriate file...
		DataHandler.writeString2File(game_id + ".nfg", nfg);
		
		// ------------
		// GENERATE NFO
		// ------------
		
		String nfo = "1\t1\t1";
		for (int i = 1; i < pnum; i++) nfo += " 1";
		DataHandler.writeString2File(game_id + ".nfo", nfo);
		
	} // End of Game.genTotc() method
	
	/** Generate a GAMBIT .NFG file for a generalized (N-person) Hawk-Dove game
	 * 
	 * @param pnum the number of players
	 * @param v the value of the resource
	 * @param c the individual cost of fighting over the resource
	 * */
	public static void genHD(int pnum, double v, double c) {
		
		String target = "/jade/src/msclab01/gametheory_lab/games/hd";
		
		// ----------
		// GAME TITLE
		// ----------
		
		// The prefix of the string holding the complete NFG text...
		String nfg = "NFG 1 R \"Hawk-Dove game (" + pnum + " players, V=" + v + ", C=" + c + ")\" {";
		
		// -------
		// PLAYERS
		// -------
		
		// Add the names of the players...
		for (int i = 0; i < pnum; i++) nfg += " \"Player " + (i+1) + "\"";
		
		// ----------
		// STRATEGIES
		// ----------
		
		// Add the prefix for the list of players strategies...
		nfg += " }\n\n{ ";
		
		// The list of strategies of a player...
		String strats = "{ \"Hawk\" \"Dove\" }\n";
		
		// Add the list of players' strategies...
		for (int i = 0; i < pnum; i++) nfg += strats;
		
		// --------
		// OUTCOMES
		// --------
		
		// Add the prefix for the list of outcomes...
		nfg += "}\n\"\"\n\n{\n";
		
		// The prefix of an outcome...
		String opr = "{ \"\" ";
		
		// The postfix of an outcome...
		String ops = " }\n";
		
		// The delimiter between payoffs...
		String od = ", ";
		
		// Initialize the initial pure strategy combination and the number of pure strategies of players...
		String[] sids	= new String[pnum];
		int[] snums		= new int[pnum];
		for (int i = 0; i < pnum; i++) {
			
			sids[i]		= "0";
			snums[i]	= 2;
			
		} // FOR players
		
		// The 0-0-0-...-0 strategy combination...
		String sidss = Game.join(sids, "-");
		
		// Add the appropriate string to NFG for every outcome...
		for (int s = 0; s < Math.pow(2, pnum); s++) {
			
			// Initialize the outcome string array...
			String[] o	= new String[pnum];
			
			// Necessary update (since below sidss is incremented)...
			sids		= sidss.split("-");

			// Calculate the number of Hawks in case of the given pure strategy combination (sids)...
			int hs	= 0; for (int i = 0; i < pnum; i++) hs += (1-Integer.parseInt(sids[i]));
			
			// Calculate the appropriate payoffs in case of the given pure strategy combination (sids)...
			for (int i = 0; i < pnum; i++) {
				
				// IF player i is Dove in case of sids...
				if (sids[i].equals("1")) {
					
					// Outcome of a Dove in case of sids...
					if (hs > 0)
						o[i] = "0";
					else
						o[i] = Double.toString(v/pnum);
					
				} else { // sids[i].equals("0")
					
					// Outcome of a Hawk in case of sids...
					if (hs > 1)
						o[i] = Double.toString((v-c)/hs);
					else // if (hs == 1)
						o[i] = Double.toString(v);
					
				} // IF DOVE
				
			} // FOR players
			
			// Add the appropriate outcome (corresponding to sids) to the NFG string...
			nfg += opr + Game.join(o, od) + ops;
			
			// Increment the pure strategy combination...
			sidss = Game.modifySids(sidss, snums, 1);
			
		} // FOR outcomes
		
		// ----------------
		// OUTCOME ORDERING
		// ----------------
		
		// The prefix for the outcome ordering...
		nfg += "}\n";
		
		// Order the outcomes in a straightforward fashion...
		for (int s = 0; s < Math.pow(2, pnum); s++) nfg += (s+1) + " ";
		
		// A new line at the end of the NFG file...
		nfg += "\n";
		
		// ------------
		// GENERATE NFG
		// ------------
		
		// Set the GameID...
		String game_id = target + pnum + "v" + v + "c" + c;
		
		// Write the NFG string to the appropriate file...
		DataHandler.writeString2File(game_id + ".nfg", nfg);
		
		// ------------
		// GENERATE NFO
		// ------------
		
		String nfo = "1\t1\t1";
		for (int i = 1; i < pnum; i++) nfo += " 1";
		DataHandler.writeString2File(game_id + ".nfo", nfo);
		
	} // End of Game.genHD() method
	
	/** 
	 * 
	 * Create a requested game and then print out the following:
	 * 
	 * (1) name of the game and those of the players
	 * (2) every players' strategies' names
	 * (3) every possible outcome
	 * (4) all the solutions (Nash-equilibria)
	 * (5) expected profit of every player in case of every solution
	 * 
	 * @param args the ID of the requested game (e.g. pure filename without the extension or a Game.XYZ constant)
	 * 
	 * */
	public static void main(String[] args) {
		
		// Create and solve the requested game...
		String game_id;
		if (args.length > 0) game_id = args[0]; else game_id = "pd";
		Game g = new Game(game_id);
		
		// Print out the name of the game and those of the players...
		System.out.println("Name of the game:\t\t\t\t" + g.getName());
		for (int i = 0; i < g.getPlayerNum(); i++) System.out.println("Name of player " + i + ":\t\t\t\t" + g.getPlayerName(i));
		
		// Print out strategy names
		for (int i = 0; i < g.getPlayerNum(); i++)
			for (int k = 0; k < g.getStratNums(i); k++)
				System.out.println("Name of player " + i + "'s pure strategy s(" + i + "," + k + "):\t" + g.getStratNames(i)[k]);

		// Print out the outcomes...
		System.out.println();
		String[] sids = new String[g.getPlayerNum()];
		for (int i = 0; i < g.getPlayerNum(); i++) sids[i] = "0";
		String sidss = Game.join(sids, "-");		// The 0-0-0-...-0 strategy combination...
		for (int s = 0; s < g.getOutcomes().size(); s++) {
			sids = sidss.split("-");				// Necessary update (since below sidss is incremented)...
			String sidstr = sids[0];
			String paystr = Double.toString(g.getOutcomes().get(sidss)[0]);
			for (int i = 1; i < g.getPlayerNum(); i++) {
				sidstr += "; " + sids[i];
				paystr += ",\t" + g.getOutcomes().get(sidss)[i];
			}
			System.out.println("u(" + sidstr + ") =\t(" + paystr  + ")");
			sidss = Game.modifySids(sidss, g.getAllStratNums(), 1);
		}

		// Print out all the solutions of the game and their expected payoff for every player...
		System.out.println();
		for (int s = 0; s < g.getSolutions().size(); s++)		
			for (int i = 0; i < g.getPlayerNum(); i++) {
				
				for (int j = 0; j < g.getStratNums(i); j++)
					System.out.println("Solution(" + s + "):\tq(" + i + ")(s(" + i + ","+ j + ")) = " + g.getSolutions().elementAt(s).elementAt(i)[j]);

				System.out.println("Solution(" + s + "):\tEP(" + i + ") = " + g.getSolutionsEP().elementAt(s)[i]);
				
			}
		
		// -------------------------------
		// GENERATE SOME GAMES (NFG + NFO)
		// -------------------------------
		
		// Generate a TOTC (Tragedy of the Commons) game with a given number of players (but be aware of the stack size setting/thread in the JVM to be able to parse it with regex (for 10 players with greedy quantifiers -Xss321k is just enough))...
		// for (int i = 2; i < 13; i++) genTotc(i);
		
		// Generate a generalized HD (Hawk-Dove) game with a given number of players (but be aware of the stack size setting/thread in the JVM to be able to parse it with regex (for 10 players with greedy quantifiers -Xss321k is just enough))...
		// for (int i = 2; i < 13; i++) {genHD(i, 2*i, i); genHD(i, i, 2*i);}
		
		// ----------------------
		// SOLVE SOME GAMES (SOL)
		// ----------------------
		
		// Solve a TOTC (Tragedy of the Commons) game with a given number of players (but be aware of the stack size setting/thread in the JVM to be able to parse it with regex (for 10 players with greedy quantifiers -Xss321k is just enough))...
		// for (int i = 2; i < 13; i++) {
		// 	String gid = "totc" + i; new Game(gid);
		// }
		
		// Solve a generalized HD (Hawk-Dove) game with a given number of players (but be aware of the stack size setting/thread in the JVM to be able to parse it with regex (for 10 players with greedy quantifiers -Xss321k is just enough))...
		// for (int i = 2; i < 13; i++) {
		// 	double v = 2*i; double c = i; String gid = "hd" + i + "v" + v + "c" + c; new Game(gid);
		// 	v = i; c = 2*i; gid = "hd" + i + "v" + v + "c" + c; new Game(gid);
		// }

	} // End of Game.main() method

} // End of class Game
