
public class Shape extends Form {
	int[][][] faces;

	public Shape(int[] newCoords, int[][][] newFaces) {
		coords = newCoords;
		faces = newFaces;
		type = 1;
	}
	
	public Shape() {
		
	}
	
	public int[] getCoords() {
		return coords;
	}
	
	public void setFaces(int[][][] newFaces) {
		faces = newFaces; 
	}
	
	public int[][][] getFaces() {
		return faces;
	}
}
