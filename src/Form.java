
public class Form {
	
	int[] coords;
	int[][] points;
	int type;
	
	public Form() {
		
	}
	
	public int getType() {
		return type;
	}
	
	public int[][] getPoints() {
		return points;
	}

	public void setCoords(int[] newCoords) {
		coords[0] = newCoords[0];
		coords[1] = newCoords[1];
		coords[2] = newCoords[2];
	}
	
	public int[] getCoords() {
		return coords;
	}
}
