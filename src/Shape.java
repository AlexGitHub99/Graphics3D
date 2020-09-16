
public class Shape extends Form {
	int[][] faces;
	
	public Shape(int[][] newPoints, int[][] newFaces) {
		points = newPoints;
		faces = newFaces;
	}
	
	public Shape() {
		
	}
	
	public void setPoints(int[][] newPoints) {
		points = newPoints;
	}
	
	public void setFaces(int[][] newFaces) {
		faces = newFaces; 
	}
	
	public int[][] getFaces() {
		return faces;
	}
	
}
