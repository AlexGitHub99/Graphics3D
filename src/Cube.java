
public class Cube extends Form{
	int size;
	int x;
	int y;
	int z;
	
	public Cube(int newX, int newY, int newZ, int newSize) {
		x = newX;
		y = newY;
		z = newZ;
		size = newSize;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public int[][] getPoints() {
		int r = size/2;
		int[][] points = {{x + r, y + r, z + r},
							{x + r, y + r, z - r},
						    {x + r, y - r, z + r},
					  	    {x + r, y - r, z - r},
						    {x - r, y + r, z + r},
						    {x - r, y + r, z - r},
						    {x - r, y - r, z + r},
						    {x - r, y - r, z - r}};
		return points;
	}
}
