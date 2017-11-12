# Baum-Welch Implementation
## The Task

Consider an agent who moves around between the 16 ‘cells’. At any point in time, the agent either moves to an adjacent cell or stays where it is. No diagonal moves are allowed. So, for example, if at time point t the agent were in cell (0, 2) then at time point t + 1 it could only be in one of the following cells: (0, 2), (1, 2), (0, 1) or (0, 3). The agent moves probabilistically according to transition probabilities. So, for example, there is a transition probability P(ht+1 = (1, 2)| ht = (0, 2)) which, for any time point t, is the probability that the agent arrives in cell (1, 2) at time t + 1 given that it was in cell (0, 2) at time t. In addition to the transition probabilities there is also an initial probability for each cell which is the probability that the agent starts in that cell. For example, there is a probability P(h1 = (2, 3)). At each time point, the agent receives a ‘reward’. There are three levels of reward: -1, 0 or 1. The reward the agent receives depends on a probability distribution that is specific to the cell the agent is currently visiting. So, for example, there are 3 emission probabilities P(vt = −1| ht = (0, 2)), P(vt = 0 | ht = (0, 2)) and P(vt = 1 | ht = (0, 2)). These three probabilities will sum to one and are independent of t.

If the cells the agent visits are hidden from us then this process is clearly a Hidden Markov Model (HMM). In the following tasks you are required to estimate the initial, transition and emission probabilities using data.

## Executing project

You should run the program from the command line as follows:
**mlap_prog 2 task2.dat**

- The first command line argument refers to the task. It can be either 1, 2, 3, or 4.
- The second command line argument should be the data file which is to be used.
- Only task1.dat should be used for task 1 and task2.dat for the remaining tasks.

## Description of Tasks

### Task 1
Compute the maximum likelihood estimates for model parameters in task1.dat

This is data for 7 episodes. In any particular episode the agent moves around and receives rewards according to the unknown initial, transition and emission probabilities. Each episode ends after a number of time steps. In the data file, episodes are separated by a blank line. Note that the episodes are of varying lengths. For each time point you are given the cell that the agent is in and the reward the agent received in that cell.

### Task 2

Only the emission data is given in task2.dat

This task finds the emission and transition probabilities for each state given uniform initial probabilities and the emission data. Use uniform distributions as the starting point and do not adapt the algorithm to take adjacent cell transition probabilities into consideration. Terminate the algorithm once the log-likelihood is less than 0.01 compared to the previous iteration.

### Task 3
This task finds the initial, transition, and emission probabilities for each state given the emission data and starting with random parameters.

### Task 4
This task finds the initial, transition, and emission probabilities for each state given the emission data. In this task certain transitions between states are disallowed via the use of walls between cells in the grid.
