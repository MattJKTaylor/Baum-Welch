package mlap_prog;

public class Wall {
	
	Cell c1, c2;
	
	public Wall(Cell c1, Cell c2)
	{
		this.c1 = c1;
		this.c2 = c2;
	}
	
	public Cell getFirstCell()
	{
		return c1;
	}
	
	public Cell getSecondCell()
	{
		return c2;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Wall)
		{
			Wall w = (Wall) o;
			if(w.getFirstCell().equals(c1) && w.getSecondCell().equals(c2) || w.getFirstCell().equals(c2) && w.getSecondCell().equals(c1))
				return true;
			else
				return false;
		}
		else
			return false;
	}
}
