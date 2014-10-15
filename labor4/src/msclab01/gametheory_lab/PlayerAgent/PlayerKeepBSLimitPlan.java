package msclab01.gametheory_lab.PlayerAgent;

import jadex.runtime.Plan;

/**
 * JADEX player agent's Beliefset limiting plan
 * 
 * @author Dániel László, Kovács (dkovacs@mit.bme.hu)
 *
 */
public class PlayerKeepBSLimitPlan extends Plan {

	private static final long serialVersionUID = 1L;
	
	/** The beliefset's name. */
	protected String beliefsetname;

	public PlayerKeepBSLimitPlan(String bsn) {
		
		//getLogger().info("Created: "+this);
		beliefsetname = bsn;
		
	} // End of PlayerKeepBSLimitPlan.PlayerKeepBSLimitPlan() constructor

	public void body() {
		
		Object[] facts = getBeliefbase().getBeliefSet(beliefsetname).getFacts();
		getBeliefbase().getBeliefSet(beliefsetname).removeFact(facts[0]);
		//getLogger().info("Success, removed: "+facts[0]);
		
	} // End of PlayerKeepBSLimitPlan.body() method

} // End of PlayerKeepBSLimitPlan class
