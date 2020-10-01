import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class Screen extends JPanel{
	ArrayList<String> labels = null;
	
	int SHAPE = 1;
	int RECTPRISM = 2;
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
		
		if(forms != null) {
			for(int f = 0; f < forms.size(); f++)  {
				Form currentForm = forms.get(f);
				if(currentForm.getType() == RECTPRISM) {
					RectPrism currentPrism = (RectPrism)currentForm;
					int[][][] faces = currentPrism.getFaces();
					int[] coords = currentPrism.getCoords();
					for(int s = 0; s < faces.length; s++) {
						int[] xPoints = new int[faces[s].length];
						int[] yPoints = new int[faces[s].length];
						for(int p = 0; p < faces[s].length; p++) {
							int[] xy = calcPoint(faces[s][p][0] + coords[0], faces[s][p][1] + coords[1], faces[s][p][2] + coords[2]);
							if(xy != null) {
								xPoints[p] = xy[0];
								yPoints[p] = xy[1];
							}
						}
						g.drawPolygon(xPoints, yPoints, faces[s].length);
					}
				}
			}
		}
		
		g.setColor(Color.BLACK);
		g.fillRect(width/2 - 1, height/2 - 5, 2, 10);
		g.fillRect(width/2 - 5, height/2 - 1, 10, 2);
		g.setColor(Color.WHITE);
		g.fillRect(width/2, height/2 - 4, 1, 8);
		g.fillRect(width/2 - 4, height/2, 8, 1);
	}
	
	public Screen(int newWidth, int newHeight) {
		width = newWidth;
		height = newHeight;
		FOV = 110; //default
		labels =  new ArrayList<String>();
		labels.add("perpXZ");
		labels.add("vpDistance");
		labels.add("vx: ");
		labels.add("vy: ");
		labels.add("vz: ");
		labels.add("px: ");
		labels.add("py: ");
		labels.add("pz: ");
		labels.add("pDistance");
		labels.add("absAngle");
		labels.add("a");
		labels.add("absRotateAngle");
		labels.add("yawDif");
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
				if(i % 5 == 0 || j % 5 == 0) {
					g.setColor(Color.BLACK);
				} else {
					g.setColor(Color.getHSBColor((float)(i + j)/(float)size, 1.0f, 1.0f));
				}
				int[] xy = calcPoint(i*20, 0, j*20);
				if(xy != null) {
					g.fillOval(xy[0], xy[1], 8, 8);
				}
			}
		}
	}
	
	private int[] calcPoint(int x, int y, int z) {
		//all pitch and yaw go from 180 (all the way left) to -180 (all the way right). Absolute yaw/pitch are relative to +X axis
		//y is verticle direction
		
		double px = x - playerPos[0];
		double py = y - playerPos[1];
		double pz = z - playerPos[2];
		double pDistance = Math.sqrt(Math.pow(px, 2) + Math.pow(py, 2) + Math.pow(pz, 2)); //absolute distance
		double pXZ = Math.sqrt(Math.pow(px, 2) + Math.pow(pz, 2)); //absolute distance
		
		double vx = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.cos(Math.toRadians(playerYaw)); //+-
		double vy = pDistance*Math.sin(Math.toRadians(playerPitch)); //+-
		double vz = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.sin(Math.toRadians(playerYaw)); //+-
		
		//calc point yaw
		double pYaw = Math.toDegrees(Math.asin(pz/pXZ));
		
		if(px < 0 && pYaw >= 0) {
			pYaw = 180 - pYaw;
		} else if(px < 0 && pYaw < 0) {
			pYaw = -180 - pYaw;
		}
		
		//calc difference between point yaw and player yaw
		double yawDif = pYaw - playerYaw;
		
		//Choose yawDif to the smaller angle if it represents the longer ( > 180) angle
		if(yawDif > 180) {
			yawDif = yawDif - 360;
		} else if(yawDif < -180) {
			yawDif = yawDif + 360;
		}
		
		
		
		
		
		
		//absolute distance from view point to point, in three dimensions
		double vpDistance = Math.sqrt(Math.pow(px - vx, 2) + Math.pow(py - vy, 2) + Math.pow(pz - vz, 2));
		
		/***
		 *     Perpendicular Triangle:                  Rotation Triangle:
		 *      ____                                    
		 *     | a /                                    |\
		 *     |  /									  a | \
		 *     | / pDistance							|  \
		 *     |/                           			 \  \ b
		 * 												 a \ \	
		 * 													 \\
		 * 													   \
		 * 
		 */												    
		
		double absAngle = 2*Math.toDegrees(Math.asin(vpDistance/(2*pDistance)));
		double a = Math.sin(Math.toRadians(absAngle))*pDistance;
		double refPointX = pDistance*Math.cos(Math.toRadians(playerPitch + absAngle))*Math.cos(Math.toRadians(playerYaw)); //+-
	    double refPointY = pDistance*Math.sin(Math.toRadians(playerPitch + absAngle)); //+-
	    double refPointZ = pDistance*Math.cos(Math.toRadians(playerPitch + absAngle))*Math.sin(Math.toRadians(playerYaw)); //+-
	    double b = Math.sqrt(Math.pow(px - refPointX, 2) + Math.pow(py - refPointY, 2) + Math.pow(pz - refPointZ, 2)); //absolute distance
	    double absRotateAngle = 2*Math.toDegrees(Math.asin(b/(2*a)));
	    if(yawDif < 0) { 
	    	absRotateAngle *= -1;
	    } //else it remains positive
	    
	    double x2D = Math.cos(Math.toRadians(90 + absRotateAngle))*absAngle/FOV*width;
	    double y2D = Math.sin(Math.toRadians(90 + absRotateAngle))*absAngle/FOV*height;
		if(x == 0 && y == 0 && z == 0) {
			labels.set(1, "vpDistance: " + Math.round(vpDistance));
			labels.set(2, "vx: " + Math.round(vx));
			labels.set(3, "vy: " + Math.round(vy));
			labels.set(4, "vz: " + Math.round(vz));
			labels.set(5, "px: " + Math.round(px));
			labels.set(6, "py: " + Math.round(py));
			labels.set(7, "pz: " + Math.round(pz));
			labels.set(8, "pDistance: " + pDistance);
			labels.set(9, "absAngle: " + Math.round(absAngle));
			labels.set(10, "a: " + a);
			labels.set(11, "absRotateAngle: " + absRotateAngle);
			labels.set(12,  "yawDif: " + yawDif);
		}
		
		int[] xy2D = new int[2];
		xy2D[0] = (int)(x2D + width/2);
		xy2D[1] = (int)(-y2D + height/2);
		return xy2D;
	}
	
	public ArrayList<String> getInfoLabels() {
		return labels;
	}
	
}
