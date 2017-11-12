package mlap_prog;

import java.util.HashMap;

public class Move {
	
	private int reward;
	private Cell cell;

	private HashMap<Cell, Double> forwardProb, backwardProb;
	
	public Move(Cell cell, int reward)
	{
		this.reward = reward;
		this.cell = cell;
		forwardProb = new HashMap<Cell, Double>();
		backwardProb = new HashMap<Cell, Double>();
	}
	
	public Cell getCell()
	{
		return cell;
	}
	
	public int getReward()
	{
		return reward;
	}
	
	public void setFowardProb(Cell cell, double val)
	{
		forwardProb.put(cell, val);
	}
	
	public double getForwardProb(Cell cell)
	{
		return forwardProb.get(cell);
	}
	
	public void setBackwardProb(Cell cell, double val)
	{
		backwardProb.put(cell, val);
	}
	
	public double getBackwardProb(Cell cell)
	{
		return backwardProb.get(cell);
	}
	
	// P(Cell = cell) at this move in the series:
	public double getMarginalCellProb(Cell cell)
	{
		double numerator = forwardProb.get(cell) * backwardProb.get(cell);
		double denominator = 0.0;	
		
		for(Cell c : forwardProb.keySet())
		{
			denominator += forwardProb.get(c) * backwardProb.get(c);
		}
		
		return numerator/denominator;
	}
}
