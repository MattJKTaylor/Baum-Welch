package mlap_prog;

public class MLAP {

	public static void main(String[] args)
	{
		String fileName = "task1.dat";
		String task = "1";
		
		if(args.length == 2)
		{
			task = args[0];
			fileName = args[1];
			
			Grid task_grid = new Grid(4, 4, fileName);
			
			System.out.println("\t--- Task " + task + " ---\n");
			if(task.equals("1"))
			{
				task_grid.findVisibleParameters();
				System.out.println("...Any missing parameters are assumed zero...");
			}
			else if(task.equals("2"))
			{
				task_grid.makeGridUniform();
				task_grid.findHiddenParameters();
			}
			else if(task.equals("3"))
			{
				for(int i = 1; i <= 10; i++)
				{
					System.out.println(String.format("\t ******* TASK %s RUN %d/10 *******\n", task, i));	
					task_grid.makeGridRandom();
					task_grid.findHiddenParameters();
					System.out.println(String.format("\t ******* END OF TASK %s RUN %d/10 *******\n", task, i));
				}
			}
			else if(task.equals("4"))
			{
				for(int i = 1; i <= 10; i++)
				{
					System.out.println(String.format("\t ******* TASK %s RUN %d/10 *******\n", task, i));
					
					// Faster algorithm:
					OptimisedGrid optimised_grid = new OptimisedGrid(4, 4, fileName);
					optimised_grid.makeGridWalls();
					optimised_grid.findHiddenParameters();
										
					System.out.println("\n...Any missing parameters are assumed zero...\n");
					
					System.out.println(String.format("\t ******* END OF TASK %s RUN %d/10 *******\n", task, i));
				}
			}
	    }
		else
			throw new IllegalArgumentException("Please specify two arguments: task_number, fileName");	
	}
}
