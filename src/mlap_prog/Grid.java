package mlap_prog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

public class Grid {
	
	// Grid size and cell instances:
	protected static int rows = 0;
	protected static int columns = 0;
	protected static Cell[][] cells;
	
	// Episodes of moves that traverse the grid:
	protected ArrayList<Episode> episodes;
	
	public Grid(int rows, int columns, String episodeFile)
	{
		Grid.rows = rows;
		Grid.columns = columns;
		
		//Define Grid as an 2D array of cells:
		cells = new Cell[rows][columns];
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				cells[i][j] = new Cell(i, j);
			}
		}
		
		try
		{
			// Get a list of episodes from the data file:
			episodes = getEpisodesFromFile(episodeFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Cell getCell(int x, int y)
	{
		return cells[x][y];
	}
	
	public void makeGridUniform()
	{
		double emission = 1.0/3.0;
		double val = 1.0/(rows*columns);
		
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				// Uniform initial
				cells[i][j].setInitialProb(val);
				
				// Uniform emission:
				cells[i][j].setEmissionProb(-1, emission);
				cells[i][j].setEmissionProb(0, emission);
				cells[i][j].setEmissionProb(1, emission);
				
				// Uniform transition:
				for(int k = 0; k < rows; k++)
				{
					for(int l = 0; l < columns; l++)
					{
						cells[i][j].setTransitionProb(cells[k][l], val);
					}
				}
			}
		}
	}
	
	public void makeGridRandom()
	{		
		Random r = new Random();
	
		// Create random initial values:
		int initialTotal = 0;
		int[] initialDist = new int[rows*columns];
		int initialIndex = 0;
		for(int i = 0; i < rows*columns; i++)
		{
			initialDist[i] = r.nextInt(100);
			initialTotal += initialDist[i];
		}
				
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				// Take Random initial value from array and increment current index
				cells[i][j].setInitialProb((double) initialDist[initialIndex]/initialTotal);
				initialIndex++;
				
				// Random emission:
				int e1 = r.nextInt(100);
				int e2 = r.nextInt(100);
				int e3 = r.nextInt(100);
				int eTotal = e1 + e2 + e3;
				
				// Set emission values:
				cells[i][j].setEmissionProb(-1, (double) e1/eTotal);
				cells[i][j].setEmissionProb(0, (double) e2/eTotal);
				cells[i][j].setEmissionProb(1, (double) e3/eTotal);
				
				// Generate random transition values:
				int transitionIndex = 0;
				int transTotal = 0;
				int[] transDist = new int[rows*columns];
				for(int k = 0; k < rows*columns; k++)
				{
					transDist[k] = r.nextInt(100);
					transTotal += transDist[k];
				}
				
				// Set transition values:
				for(int k = 0; k < rows; k++)
				{
					for(int l = 0; l < columns; l++)
					{
						cells[i][j].setTransitionProb(cells[k][l], (double) transDist[transitionIndex]/transTotal);
						transitionIndex++;
					}
				}
			}
		}
	}
	
	public void makeGridWalls()
	{
		// Define the walls in the grid and save to a list:
		ArrayList<Wall> walls = new ArrayList<Wall>();
		walls.add(new Wall(cells[0][2], cells[1][2]));
		walls.add(new Wall(cells[0][1], cells[1][1]));
		walls.add(new Wall(cells[1][3], cells[2][3]));
		walls.add(new Wall(cells[1][2], cells[2][2]));
		walls.add(new Wall(cells[1][0], cells[2][0]));
		walls.add(new Wall(cells[3][2], cells[3][1]));
		
		Random r = new Random();
		// Create random initial values:
		int initialTotal = 0;
		int[] initialDist = new int[rows*columns];
		int initialIndex = 0;
		for (int i = 0; i < rows*columns; i++)
		{
			initialDist[i] = r.nextInt(100);
			initialTotal += initialDist[i];
		}
		
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{
				Cell currentCell = cells[i][j];
				
				// Set initial value
				currentCell.setInitialProb((double) initialDist[initialIndex]/initialTotal);
				initialIndex++;
				
				// Random emission:
				int e1 = r.nextInt(100);
				int e2 = r.nextInt(100);
				int e3 = r.nextInt(100);
				int eTotal = e1 + e2 + e3;
				
				currentCell.setEmissionProb(-1, (double) e1/eTotal);
				currentCell.setEmissionProb(0, (double) e2/eTotal);
				currentCell.setEmissionProb(1, (double) e3/eTotal);
				
				// Get list of adjacent cells to the current cell;
				ArrayList<Cell> adj = getAdjacentCells(currentCell);

				// Get adjacent cells that aren't blocked by walls:
				ArrayList<Cell> validTransitions = new ArrayList<Cell>();
				for(Cell adjCell: adj)
				{
					if(!walls.contains(new Wall(currentCell, adjCell)))
						validTransitions.add(adjCell);
				}
				
				// Generate random probabilities for valid transitions only
				int[] transDist = new int[validTransitions.size()];
				int transTotal = 0;
				for(int k = 0; k < validTransitions.size(); k++)
				{
					transDist[k] = r.nextInt(100);
					transTotal += transDist[k];
				}
				
				// Set transition probabilities:
				int transIndex = 0;
				for(int k = 0; k < rows; k++)
				{
					for(int l = 0; l < columns; l++)
					{
						Cell transCell = cells[k][l];
						if(validTransitions.contains(transCell))
						{
							// Take a value from the transDist array:
							currentCell.setTransitionProb(transCell, (double) transDist[transIndex]/transTotal);
							transIndex++;
						}
						else
						{
							// If the cell is not adjacent, given a 0 probability
							currentCell.setTransitionProb(transCell, 0.0);
						}
					}
				}
			}
		}
	}
	
	public ArrayList<Cell> getAdjacentCells(Cell cell)
	{
		int max_x = rows - 1;
		int max_y = columns - 1;
				
		int x = cell.getX();
		int y = cell.getY();
				
		ArrayList<Cell> adjacent = new ArrayList<Cell>();
		if(x < max_x)
			adjacent.add(cells[x+1][y]);
		if(x > 0)
			adjacent.add(cells[x-1][y]);
		if(y < max_y)
			adjacent.add(cells[x][y+1]);
		if(y > 0)
			adjacent.add(cells[x][y-1]);
		
		return adjacent;
	}
	
	// Finds the transition, initial and emission probabilities for each cell
	public void findVisibleParameters()
	{
		// Generate initial and transition counts for every position in the grid:
		for(Episode ep : episodes)
		{
			ArrayList<Move> moves = ep.getMoves();
								
			// loop through transitions and count the frequencies in each Cell:
			for(int i = 0; i < moves.size() - 1; i++)
			{
				Move currentMove = moves.get(i);
				Move nextMove = moves.get(i + 1);
						
				Cell currentCell = currentMove.getCell();
							
				// First cell in episode
				if(i == 0)
					currentCell.addInitialCount();
					
				// Add outgoing transition and reward:
				currentCell.addTransitionCount(nextMove.getCell());
				currentCell.addEmissionCount(currentMove.getReward());
				currentCell.updateProbFromCount(episodes.size());
			}
					
			// Add emission for final cell in Episode:
			Move finalMove = moves.get(moves.size() - 1);
			Cell finalCell = finalMove.getCell();
			finalCell.addEmissionCount(finalMove.getReward());
			finalCell.updateProbFromCount(episodes.size());
		}
		printParameters();
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
						for(int l = 0; l < rows; l++)
						{
							for(int k = 0; k < columns; k++)
							{
								Cell nextCell = cells[l][k];
								
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
								
							} // joint cell inner loop
						} // joint cell outer loop			
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
	
	// likelihood P(v1, v2,..., vt | params)...
	protected double getLogLikelihood()
	{
		double total = 1.0;
		for(Episode e: episodes)
		{
			ArrayList<Move> moves = e.getMoves();
			Move lastMove = moves.get(moves.size() - 1);
			
			// We sum the last move probabilities for each cell:
			double episodeTotal = 0.0;
			for(int i = 0; i < rows; i++)
			{
				for(int j = 0; j < columns; j++)
				{	
					Cell currentCell = cells[i][j];
					episodeTotal += lastMove.getForwardProb(currentCell);
				}
			}
			
			// We multiply the total as the episode observations are independent:
			total *= episodeTotal;
		}	
		
		return Math.log(total);
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
				for(int l = 0; l < rows; l++)
				{
					for(int k = 0; k < columns; k++)
					{
						Cell c2 = cells[l][k];
						
						forward = currentMove.getForwardProb(c1);
						transProb = c1.getTransitionProb(c2);
						emissionProb = c2.getEmissionProb(nextMove.getReward());
						backward = nextMove.getBackwardProb(c2);
						
						denominator += forward * transProb * emissionProb * backward;
					}
				}
			}
		}
		return numerator/denominator;
	}
	
	// Calculate every forward and backward probability for every cell at each move
	// Stores value within Move instance as key value pairs HashMap<Cell, Double>
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
							for(int l = 0; l < rows; l++)
							{
								for(int k = 0; k < columns; k++)
								{
									Cell transitionFromCell = cells[l][k];
									double prev_forward = prev_move.getForwardProb(transitionFromCell);
									double transProb = transitionFromCell.getTransitionProb(c); // P(Ht+1 = c|Ht = transitionFromCell)
									
									//Sums over all cells using their prev forward and transition prob:
									forwardProb += prev_forward * transProb; // emission comes in later
								}
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
							
							for(int l = 0; l < rows; l++)
							{
								for(int k = 0; k < columns; k++)
								{
									Cell transitionCell = cells[l][k];
									
									double next_backward = next_Move.getBackwardProb(transitionCell);
									double transProb = c.getTransitionProb(transitionCell);
									double emissionProb = transitionCell.getEmissionProb(next_Reward);
									
									//Sums over states using their prev forward and transition prob:
									backwardProb += next_backward * transProb * emissionProb;
								}
							}	
							move.setBackwardProb(c, backwardProb);
						}		
					}
				}
			}	
		}
	}
	
	protected void printParameters()
	{
		System.out.println("\n\t--- Parameters: ---\n");
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < columns; j++)
			{	
				Cell currentCell = cells[i][j];
				currentCell.printCellParamaters();
				System.out.println("");
			}
		}
	}
		
	//Returns an ArrayList of episodes containing the individual moves:
	private static ArrayList<Episode> getEpisodesFromFile(String fileName) throws IOException
	{
		ArrayList<Episode> episodes = new ArrayList<Episode>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try
		{
		    // Regex used to parse string into groups within the brackets (1, 2) 3
		    Pattern visiblePattern = Pattern.compile(".([0-3]),([0-3]). (-?[01])");
		    Pattern hiddenPattern = Pattern.compile("-?[01]");
		    Matcher m = null;
		    
		    // Create new episode to add the moves to
		    Episode ep = new Episode();
		    
		    // read line by line until the end of the document
		    String line = br.readLine();		    
		    while (line != null)
		    {
		    	if(line.isEmpty()) // Empty line = end of episode
		    	{
		    		if(ep.getLength() > 0)
		    		{
		    			// Add episode to list of episodes and create a new one;
		    			episodes.add(ep);
			    		ep = new Episode();
		    		}
		    	}
		    	else
		    	{
		    		//Finds matches for the regex of the current line
		    		m = visiblePattern.matcher(line);
			    	if(m.matches())
			    	{
			    		// Extracts the substrings from () groups
					    int x = Integer.parseInt(m.group(1));
					    int y = Integer.parseInt(m.group(2));
					    int r = Integer.parseInt(m.group(3));
					    	
					    Cell cell = cells[x][y];
					    	
					    // Adds move to current episode
					    ep.addMove(cell, r);
			    	}
			    	else
			    	{
			    		// Check for the hidden pattern of just the reward:
			    		m = hiddenPattern.matcher(line);
			    		if(m.matches())
			    		{
			    			// Add the move with a NULL cell:
			    			// group(0) is the entire string
				    		int r = Integer.parseInt(m.group(0));
				    		ep.addMove(null, r);
			    		}
			    		else
			    		{
			    			throw new IOException("File data is not in the correct format (x, y) r OR r: " + line);
			    		}
			    	}
		    	}
		        line = br.readLine();
		    }
		}
		finally
		{
		    br.close();
		}
		return episodes;
	}
}
