
public class Shape extends Form {
	int[][][] faces;

	public Shape(int[] newCoords, int[][][] newFaces) {
		coords[0] = newCoords[0];
		coords[1] = newCoords[1];
		coords[2] = newCoords[2];
		faces = newFaces;
		type = 1;
	}
	
	public Shape() {
		
	}
	
	public void setFaces(int[][][] newFaces) {
		faces = newFaces; 
	}
	
	public int[][][] getFaces() {
		return faces;
	}
}
