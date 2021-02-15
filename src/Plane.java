
public class Plane extends Form {
	int[] dimensions = new int[3];
	
	public Plane(int newX, int newY, int newZ, int xl, int zl) {
		coords[0] = newX;
		coords[1] = newY;
		coords[2] = newZ;
		dimensions[0] = xl;
		dimensions[1] = 0;
		dimensions[2] = zl;
		type = 3;
		
	}
	
	public int getType() {
		return type;
	}
	
	public int[] getDimensions() {
		return dimensions;
	}
	
	public void setDimensions(int[] newDimensions) {
		dimensions = newDimensions;
	}
	public int[][] getFace() {
		int[][] face = { {0, 0, 0}, {dimensions[0], 0, 0}, {dimensions[0], 0, dimensions[2]}, {0, 0, dimensions[2]} };
		return face;
	}
}
