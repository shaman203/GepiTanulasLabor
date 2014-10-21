package msclab01.votingauction_lab.AuctioneerAgent;

public interface IAuctioneerAgent {

	/** Appropriately call (i.e. add or restart) a given behaviour (from the GUI)...*/
	public abstract void callBehaviour(String b); // End of AuctioneerAgent.callBehaviour() method

	public abstract int getTotalGoodNumber();

	public abstract void doDelete();

	public abstract String getLocalName();

}