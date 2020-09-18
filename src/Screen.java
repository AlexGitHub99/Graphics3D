import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

public class Screen extends JPanel{
	int SHAPE = 1;
	int width;
	int height;
	double[] playerPos = new double[3];
	double playerYaw;
	double playerPitch;
	int playerHeight;
	int FOV;
	ArrayList<Form> forms;
	
	public void paint(Graphics g) {
		playerHeight = 80; //default;
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.red);
		paintGrid(g, 100);
		g.setColor(Color.blue);
//		if(forms != null) {
//			for(int i = 0; i < forms.size(); i++) {
//				paintOutline(g, forms.get(i));
//			}
//		}
		
		for(int f = 0; f < forms.size(); f++)  {
			Form currentForm = forms.get(f);
			if(currentForm.getType() == SHAPE) {
				Shape currentShape = (Shape)currentForm;
				int[][][] faces = currentShape.getFaces();
				for(int s = 0; s < faces.length; s++) {
					int[] xPoints = new int[faces[s].length];
					int[] yPoints = new int[faces[s].length];
					for(int p = 0; p < faces[s].length; p++) {
						int[] xy = calcPoint(faces[s][p][0], faces[s][p][1], faces[s][p][2]);
						xPoints[p] = xy[0];
						yPoints[p] = xy[1];
					}
					g.fillPolygon(xPoints, yPoints, faces[s].length);
				}
			}
		}
	}
	
	public Screen(int newWidth, int newHeight) {
		width = newWidth;
		height = newHeight;
		FOV = 110; //default
	}
	
	public double getPlayerYaw() {
		return playerYaw;
	}
	
	public double getPlayerPitch() {
		return playerPitch;
	}
	
	public void setForms(ArrayList<Form> newForms) {
		forms = newForms;
	}
	
	public void setFOV(int newFOV) {
		FOV = newFOV;
	}
	
	public void setPlayerPos(double newX, double newY, double newZ) {
		playerPos[0] = newX;
		playerPos[1] = newY + playerHeight;
		playerPos[2] = newZ;
	}
	
	public void setPlayerHeight(int newPlayerHeight) {
		playerHeight = newPlayerHeight;
	}
	
	public void setPlayerYaw(int newYaw) {
		playerYaw = newYaw;
	}
	
	public void setPlayerPitch(int newPitch) {
		playerPitch = newPitch;
	}
	
	private void paintOutline(Graphics g, Form obj) {
		int[][] vertices = obj.getPoints();
		int[][] coords2D = new int[2][vertices.length];
		for(int i = 0; i < vertices.length; i ++) {
			double dx = vertices[i][0] - playerPos[0];
			double dy = vertices[i][1] - playerPos[1];
			double dz = vertices[i][2] - playerPos[2];
			double yaw = Math.atan2(dz, dx)/Math.PI*180;
			double pitch = Math.atan2(dy, Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)))/Math.PI*180;
			//Converts the angle difference between the vertex and the edge of the players FOV to 2D coordinates
			double yawDif = (playerYaw - yaw);
			double pitchDif = (playerPitch - pitch);
			if(yawDif > 180) {
				yawDif = yawDif - 360;
			} else if(yawDif < -180) {
				yawDif = yawDif + 360;
			}
			if(pitchDif > 180) {
				pitchDif = pitchDif - 360;
			} else if(pitchDif < -180) {
				pitchDif = pitchDif + 360;
			}
			coords2D[0][i] = (int)((yawDif + FOV/2)/FOV*width); 
			coords2D[1][i] = (int)((pitchDif + FOV/2)/FOV*height);
			g.setColor(Color.blue);
			g.fillOval(coords2D[0][i], coords2D[1][i], 10, 10);
		}
		g.setColor(Color.cyan);
		g.fillPolygon(coords2D[0], coords2D[1], coords2D[0].length);
	}
	
	private void paintGrid(Graphics g, int size) {
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				g.setColor(Color.getHSBColor((float)(i + j)/(float)size, 1.0f, 1.0f));
				int[] xy = calcPoint(i*20, 0, j*20);
				g.fillOval(xy[0], xy[1], 8, 8);
			}
		}
	}
	
	private int[] calcPoint(int x, int y, int z) {
		double px = x - playerPos[0];
		double py = y - playerPos[1];
		double pz = z - playerPos[2];
		double pDistance = Math.sqrt(Math.pow(px, 2) + Math.pow(pz, 2) + Math.pow(py, 2));
		double pXZ = Math.sqrt(Math.pow(px, 2) + Math.pow(pz, 2));
		
		double vx = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.cos(Math.toRadians(playerYaw));
		double vy = pDistance*Math.sin(Math.toRadians(playerPitch));
		double vz = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.sin(Math.toRadians(playerYaw));
		double vXZ = Math.sqrt(Math.pow(vx, 2) + Math.pow(vz, 2));
		
		double pYaw = Math.toDegrees(Math.asin(pz/pXZ));
		
		if(px < 0 && pYaw >= 0) {
			pYaw = 180 - pYaw;
		} else if(px < 0 && pYaw < 0) {
			pYaw = -180 - pYaw;
		}
		
		double yawDif = pYaw - playerYaw;
		
		//choose shortest angle
		if(yawDif > 180) {
			yawDif = yawDif - 360;
		} else if(yawDif < -180) {
			yawDif = yawDif + 360;
		}
		
		double pvXZ = Math.sqrt(Math.pow(Math.cos(Math.toRadians(Math.abs(yawDif)))*pDistance, 2) - Math.pow(py, 2));
		
		double perpXZ = (py*(vXZ*vy) + Math.pow(vXZ, 2)*pvXZ) / (Math.pow(vy, 2) + Math.pow(vXZ, 2));
		double perpY = perpXZ*(vy/vXZ);
		double perpX = Math.cos(Math.toRadians(playerYaw))*perpXZ;
		double perpZ = Math.sin(Math.toRadians(playerYaw))*perpXZ;
		double perpDistance = Math.sqrt(Math.pow(perpX, 2) + Math.pow(perpY, 2) + Math.pow(perpZ, 2));
		
		double viewDifVerticle = Math.sqrt( Math.pow(pvXZ - perpXZ, 2) + Math.pow(py - perpY, 2) );
		double yDifPixels = ((width/2)/Math.tan(Math.toRadians(FOV)/2))*viewDifVerticle/perpDistance;
		
		if(perpXZ > 0) {
			if(py > perpY) {
				yDifPixels *= -1;
			}
		} else if(perpXZ < 0) {
			if(py < perpY) {
				yDifPixels *= -1;
			}
		}
		
		double pvDistance = Math.sqrt(Math.pow(px - perpX, 2) + Math.pow(py - perpY, 2) + Math.pow(pz - perpZ, 2));
		double viewDifHorizontal = Math.sqrt(Math.pow(pvDistance, 2) - Math.pow(viewDifVerticle, 2));
		double xDifPixels = ((width/2)/Math.tan(Math.toRadians(FOV)/2))*viewDifHorizontal/perpDistance;

		if(yawDif > 0) {
			xDifPixels *= -1;
		}
		
		int[] xy2D = new int[2];
		xy2D[0] = (int)((xDifPixels + FOV/2)/FOV*width);
		xy2D[1] = (int)((yDifPixels + FOV/2)/FOV*height);
		return xy2D;
	}
	
}
