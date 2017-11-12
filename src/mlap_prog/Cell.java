package mlap_prog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Cell {
	
	// Holds frequency counts of each cell transition and rewards:
	private int x, y, initial_count;
	private HashMap<Cell, Integer> transition_count; // <TransitionCell, count>
	private HashMap<Integer, Integer> emission_count; //<RewardType (-1, 0, 1), count>
	
	// Holds the actual probability rather than the count itself:
	private double initial_prob;
	private HashMap<Cell, Double> transition_prob;
	private HashMap<Integer, Double> emission_prob;
	
	// Temporary variables so we can store them while iterating the algorithm...
	private double temp_initial_prob;
	private HashMap<Cell, Double> temp_transition_prob;
	private HashMap<Integer, Double> temp_emission_prob;
	
	public Cell(int x, int y)
	{
		this.x = x;
		this.y = y;
		initial_count = 0;
		transition_count = new HashMap<Cell, Integer>();
		emission_count = new HashMap<Integer, Integer>();
		transition_prob = new HashMap<Cell, Double>();
		emission_prob = new HashMap<Integer, Double>();
		temp_transition_prob = new HashMap<Cell, Double>();
		temp_emission_prob = new HashMap<Integer, Double>();
	}
			
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
		
	public void addInitialCount()
	{
		initial_count++;
	}
	
	public void addTransitionCount(Cell to_cell)
	{
		if(transition_count.containsKey(to_cell))
		{
			int count = transition_count.get(to_cell) + 1;
			transition_count.put(to_cell, count);
		}
		else
			transition_count.put(to_cell, 1);
	}
	
	private int getTotalTransitionCount()
	{
		int total = 0;
		for(Integer val : transition_count.values())
			total += val;
		
		return total;
	}
	
	private int getTotalEmissionCount()
	{
		int total = 0;
		for(Integer val : emission_count.values())
			total += val;
		
		return total;
	}
	
	public void addEmissionCount(int rewardType)
	{
		if(emission_count.containsKey(rewardType))
		{
			int count = emission_count.get(rewardType) + 1;
			emission_count.put(rewardType, count);
		}
		else
			emission_count.put(rewardType, 1);
	}
	
	// could move this sort of thing into the individual methods:
	public void updateProbFromCount(int totalInitial)
	{
		int totalTrans = getTotalTransitionCount();
		for(Cell cell : transition_count.keySet())
		{
			transition_prob.put(cell, (double) transition_count.get(cell)/totalTrans);
		}
		
		int totalEmission = getTotalEmissionCount();
		for(int rewardType : emission_count.keySet())
		{
			emission_prob.put(rewardType, (double) emission_count.get(rewardType)/totalEmission);
		}
		
		initial_prob = (double) initial_count/totalInitial;
	}
	
	public void setInitialProb(double val)
	{
		initial_prob = val;
	}
	
	public void setTransitionProb(Cell transitionCell, double val)
	{
		transition_prob.put(transitionCell, val);
	}
	
	public void setEmissionProb(int rewardType, double val)
	{
		emission_prob.put(rewardType, val);
	}
	
	public double getInitialProb()
	{
		return initial_prob;
	}
	
	public double getTransitionProb(Cell transitionTo)
	{
		return transition_prob.get(transitionTo);
	}
	
	public double getEmissionProb(int rewardType)
	{
		return emission_prob.get(rewardType);
	}
	
	// === Used to temporarily hold the new probability during an iteration of EM ===
	
	public void setTempInitialProb(double val)
	{
		temp_initial_prob = val;
	}
	
	public void setTempTransitionProb(Cell transitionCell, double val)
	{
		temp_transition_prob.put(transitionCell, val);
	}
	
	public void setTempEmissionProb(int rewardType, double val)
	{
		temp_emission_prob.put(rewardType, val);
	}
	
	// Copies the temp values back to the original variables:
	public void copyTempToActual()
	{
		// Copy contents of temp variables back to the original and then clear the temp:
		initial_prob = temp_initial_prob;
		transition_prob = new HashMap<Cell, Double>(temp_transition_prob);
		emission_prob = new HashMap<Integer, Double>(temp_emission_prob);
		
		temp_initial_prob = 0.0;
		temp_transition_prob.clear();
		temp_emission_prob.clear();
	}
		
	// Print the parameters of the cell:
	public void printCellParamaters()
	{
		System.out.println(String.format("\t-- Cell(%d, %d) --\n", x, y));
		System.out.println(String.format("P(H1 = (%d,%d)) = %f (%s)", x, y, initial_prob, Double.toString(initial_prob)));	
		
		for(int emission : emission_prob.keySet())
		{
			System.out.println(String.format("P(Vt= %d | Ht = (%d, %d)) = %f (%s)", emission, x, y, emission_prob.get(emission), Double.toString(emission_prob.get(emission))));
		}
		
		// Sorts the cell in hash (0,0), (0,1)... to make the printing a bit more readable:
		ArrayList<Cell> sortedCells = new ArrayList<Cell>(transition_prob.keySet());
		Collections.sort(sortedCells, new Comparator<Cell>(){
			
			@Override
			public int compare(Cell c1, Cell c2)
			{
				if(c1.getX() > c2.getX())
					return 1;
				else if(c1.getX() == c2.getX())
				{
					if(c1.getY() > c2.getY())
						return 1;
					else
						return -1;
				}
				else
					return -1;
			}
		});
		
		for(Cell c : sortedCells)
		{
			System.out.println(String.format("P(Ht+1 = (%d,%d) | Ht = (%d, %d)) = %f (%s)", c.getX(), c.getY(), x, y, getTransitionProb(c), Double.toString(getTransitionProb(c))));
		}	
	}
}
