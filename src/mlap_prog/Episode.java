package mlap_prog;

import java.util.ArrayList;

public class Episode {
	
	private ArrayList<Move> moves = new ArrayList<Move>();
	
	public void addMove(Cell cell, int reward)
	{
		moves.add(new Move(cell, reward));
	}
	
	public ArrayList<Move> getMoves()
	{
		return moves;
	}
	
	public int getLength()
	{
		return moves.size();
	}
}
