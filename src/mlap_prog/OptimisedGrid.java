package mlap_prog;

import java.util.ArrayList;
import java.util.HashMap;

public class OptimisedGrid extends Grid{

	public OptimisedGrid(int rows, int columns, String episodeFile)
	{
		super(rows, columns, episodeFile);
	}
	
	/*
	 All overridden methods in this class make use of getAdjacentCells in the for loops
	 rather than looping through every possible cell, this increases performance dramatically
	 */

	// Override calcForwardBack 
	private void calcForwardBack()
	{
		for(Episode ep : episodes)
		{
			ArrayList<Move> moves = ep.getMoves();	

			// Forward probability loop
			for(int observation = 0; observation < moves.size(); observation++)
			{
				Move move = moves.get(observation);
				int r = move.getReward();

				// Loop through every cell
				for(int i = 0; i < rows; i++)
				{
					for(int j = 0; j < columns; j++)
					{
						Cell c = cells[i][j];

						double forwardProb = 0.0;
						if(observation == 0)
						{
							// Use initial probability for the first move in the episode:
							forwardProb = c.getInitialProb() * c.getEmissionProb(r);							
							move.setFowardProb(c, forwardProb);
						}
						else // Take transition probabilities into account:
						{
							// Get the previous move in the sequence:
							Move prev_move = moves.get(observation - 1);

							// Loop through every possible previous cell transition
							for(Cell transitionFromCell : getAdjacentCells(c))
							{
								double prev_forward = prev_move.getForwardProb(transitionFromCell);
								double transProb = transitionFromCell.getTransitionProb(c); // P(Ht+1 = c|Ht = transitionFromCell)

								//Sums over all cells using their prev forward and transition prob:
								forwardProb += prev_forward * transProb; // emission comes in later
							}

							// currentEmission is constant throughout equation so we multiply at the end:
							double currentEmission = c.getEmissionProb(r);
							forwardProb = forwardProb * currentEmission;
							move.setFowardProb(c, forwardProb);
						}
					}
				}
			}

			// backward probability loop
			// Loop backwards from last observation to first:
			for(int observation = moves.size() - 1; observation >= 0; observation--)
			{
				Move move = moves.get(observation);		

				// Loop every cell
				for(int i = 0; i < rows; i++)
				{
					for(int j = 0; j < columns; j++)
					{
						Cell c = cells[i][j];

						double backwardProb = 0.0;			
						if(observation == moves.size() - 1)
						{
							backwardProb = 1.0;
							move.setBackwardProb(c, backwardProb);
						}
						else
						{
							Move next_Move = moves.get(observation + 1);
							int next_Reward = next_Move.getReward();

							for(Cell transitionCell : getAdjacentCells(cells[i][j]))
							{
								double next_backward = next_Move.getBackwardProb(transitionCell);
								double transProb = c.getTransitionProb(transitionCell);
								double emissionProb = transitionCell.getEmissionProb(next_Reward);

								//Sums over states using their prev forward and transition prob:
								backwardProb += next_backward * transProb * emissionProb;
							}	
							move.setBackwardProb(c, backwardProb);
						}		
					}
				}
			}	
		}
	}

	// The EM Algorithm
	public void findHiddenParameters()
	{
		int iteration = 0; // Counts the number of EM iterations
		double prevLikelihood = 0.0; // Stores the previous log likelihood so we can terminate the algorithm

		// Measure time to convergence:
		long startTime = System.currentTimeMillis();

		while(true)
		{
			iteration++;

			// Calculate forward and backward probabilities:
			calcForwardBack();	

			// Calculate log likelihood:
			double currentLikelihood = getLogLikelihood();
			System.out.println(String.format("EM iteration %d, Log likelihood = %f, (Diff: %.3f)", iteration, currentLikelihood,  currentLikelihood - prevLikelihood));

			// Compare the old likelihood with the new value or check if undefined:
			if(Double.isNaN(currentLikelihood))
			{
				System.out.println("\n**The parameters generated for this run iteration have become undefined so the run has been terminated. Run the task again for new randomly generated starting parameters**\n");
				return;
			}
			else if(Math.abs(currentLikelihood - prevLikelihood) < 0.01)
			{
				printParameters();

				long endTime = System.currentTimeMillis();
				System.out.println("Took "+ (endTime - startTime) + " ms");

				return;
			}
			prevLikelihood = currentLikelihood;


			for(int i = 0; i < rows; i++)
			{
				for(int j = 0; j < columns; j++)
				{	
					Cell currentCell = cells[i][j];

					double initialProbSum = 0.0; // P(h1 = currentCell) for t=1
					double marginalProbSum_T = 0.0; // prob P(ht = currentCell) for t = 1 to T
					double marginalProbSum_T_minus_1 = 0.0; // prob P(ht = currentCell) for t = 1 to T-1

					HashMap<Cell, Double> jointProbSum = new HashMap<Cell, Double>(); //Joint prob P(ht+1 = nextCell, ht = currentCell)
					HashMap<Integer, Double> emissionProbSum = new HashMap<Integer, Double>(); //Joint prob P(Vt = reward, ht = currentCell)

					for(Episode ep : episodes)
					{
						ArrayList<Move> moves = ep.getMoves();

						Move firstMove = moves.get(0);
						initialProbSum += firstMove.getMarginalCellProb(currentCell);

						// Sum cell probability over all moves
						for(Move move : moves)
						{
							// Marginal probability for that cell at current move:
							double cellProb = move.getMarginalCellProb(currentCell);
							marginalProbSum_T += cellProb; // total over every move

							// All moves except the last
							// Allows us to calculate the transition probability as the joint prob only goes to t-1
							if(moves.indexOf(move) < moves.size() - 1)
								marginalProbSum_T_minus_1 += cellProb;

							// Emission probability sum:
							int rewardType = move.getReward();
							double emissionTotal = 0.0;
							if(emissionProbSum.containsKey(rewardType))
								emissionTotal = emissionProbSum.get(rewardType);

							emissionProbSum.put(rewardType, emissionTotal + cellProb);
						}

						// Calculate joint probabilities for each cell P(Ht+1 = nextCell, Ht = currentCell)
						for(Cell nextCell : getAdjacentCells(cells[i][j]))
						{
							double jointTotal = 0.0;
							if(jointProbSum.containsKey(nextCell))
								jointTotal = jointProbSum.get(nextCell);

							for(int observation = 0; observation < moves.size() - 1; observation++)
							{
								Move move = moves.get(observation);
								Move nextMove = moves.get(observation + 1);
								jointTotal += calcJointProb(move, nextMove, currentCell, nextCell);
							}
							jointProbSum.put(nextCell, jointTotal);

						}		
					} // Episode loop

					// Save the new parameters back to temporary variables within cell
					// Uses temporary variables to prevent the new values effect the next cell iteration

					// New initial prob:
					double newInitial = (double) initialProbSum/episodes.size();
					currentCell.setTempInitialProb(newInitial);

					// New emission prob:
					for(int em : emissionProbSum.keySet())
					{
						double newEmission = emissionProbSum.get(em)/marginalProbSum_T;
						currentCell.setTempEmissionProb(em, newEmission);
					}

					// New transition prob:
					for(Cell c : jointProbSum.keySet())
					{
						double newTrans = jointProbSum.get(c)/marginalProbSum_T_minus_1;
						currentCell.setTempTransitionProb(c, newTrans);
					}

				} // Cell inner loop
			} // Cell outer loop

			// Overwrite the parameter probabilities with values stored in temporary variables
			for(int i = 0; i < rows; i++)
			{
				for(int j = 0; j < columns; j++)
				{
					Cell c = cells[i][j];
					c.copyTempToActual();
				}
			}

		} // While loop - repeat the process again until convergence	
	}

	// Joint probability P(ht+1 = nextCell, ht = currentCell)
	protected double calcJointProb(Move currentMove, Move nextMove, Cell currentCell, Cell nextCell)
	{
		double forward = currentMove.getForwardProb(currentCell);
		double transProb = currentCell.getTransitionProb(nextCell);
		double emissionProb = nextCell.getEmissionProb(nextMove.getReward());
		double backward = nextMove.getBackwardProb(nextCell);

		// Transition we're interested in:
		double numerator = forward * transProb * emissionProb * backward;

		// Sum over all cell combinations:
		double denominator = 0.0;
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				Cell c1 = cells[i][j];
				for(Cell c2 : getAdjacentCells(c1))
				{
					forward = currentMove.getForwardProb(c1);
					transProb = c1.getTransitionProb(c2);
					emissionProb = c2.getEmissionProb(nextMove.getReward());
					backward = nextMove.getBackwardProb(c2);

					denominator += forward * transProb * emissionProb * backward;
				}
			}
		}
		return numerator/denominator;
	}
}
