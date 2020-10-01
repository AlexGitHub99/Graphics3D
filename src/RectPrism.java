
public class RectPrism extends Form {
	int[] dimensions = new int[3];
	int[][][] faces;
	
	public RectPrism(int newX, int newY, int newZ, int xl, int yl, int zl) {
		coords[0] = newX;
		coords[1] = newY;
		coords[2] = newZ;
		dimensions[0] = xl;
		dimensions[1] = yl;
		dimensions[2] = zl;
		type = 2;
		
		int xr = dimensions[0]/2;
		int yr = dimensions[1]/2;
		int zr = dimensions[2]/2;
		faces = new int[12][3][3];
		for(int i = 0; i < 3; i += 2) {
			int mult = 1;
			if(i == 2) {
				mult = -1;
			}
			int[][] face = { {xr*mult, yr, zr}, {xr*mult, -yr, zr}, {xr*mult, -yr, -zr} };
			faces[i] = face;
			int[][] face2 = { {xr*mult, yr, zr}, {xr*mult, yr, -zr}, {xr*mult, -yr, -zr} };
			faces[i + 1] = face2;
		}
		for(int i = 4; i < 7; i += 2) {
			int mult = 1;
			if(i == 6) {
				mult = -1;
			}
			int[][] face = { {xr, yr*mult, zr}, {-xr, yr*mult, zr}, {-xr, yr*mult, -zr} };
			faces[i] = face;
			int[][] face2 = { {xr, yr*mult, zr}, {xr, yr*mult, -zr}, {-xr, yr*mult, -zr} };
			faces[i + 1] = face2;
		}
		for(int i = 8; i < 11; i += 2) {
			int mult = 1;
			if(i == 10) {
				mult = -1;
			}
			int[][] face = { {xr, yr, zr*mult}, {-xr, yr, zr*mult}, {-xr, -yr, zr*mult} };
			faces[i] = face;
			int[][] face2 = { {xr, yr, zr*mult}, {xr, -yr, zr*mult}, {-xr, -yr, zr*mult} };
			faces[i + 1] = face2;
		}
	}
	
	public int getType() {
		return type;
	}
	
	public int[][][] getFaces() {
		return faces;
	}
	
	public void setFaces(int[][][] newFaces) {
		faces = newFaces; 
	}
}
