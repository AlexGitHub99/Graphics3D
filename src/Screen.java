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
							int[] xy = calcPoint(faces[s][p][0] + coords[0], faces[s][p][1] + coords[1], faces[s][p][2] + coords[2], true);
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
		labels.add("p2x");
		labels.add("p2y");
		labels.add("p2z");
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
				int[] xy = calcPoint(i*20, 0, j*20, false);
				if(xy != null) {
					g.fillOval(xy[0], xy[1], 8, 8);
				}
			}
		}
	}
	
	//retOffScreen specifies whether the function should give coordinates for points out of FOV for the purpose of drawing lines/shapes that go off screen
	private int[] calcPoint(int x, int y, int z, boolean retOffScreen) {
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
		 *   b |  /									  a | \
		 *     | / pDistance							|  \
		 *     |/                           			 \  \ c
		 * 												 a \ \	
		 * 													 \\
		 * 													   \
		 * 
		 */												    
		
		double absAngle = 2*Math.toDegrees(Math.asin(vpDistance/(2*pDistance)));
		double a = Math.sin(Math.toRadians(absAngle))*pDistance;
		double b = Math.cos(Math.toRadians(absAngle))*pDistance; //this will be used later
		double refPointX = pDistance*Math.cos(Math.toRadians(playerPitch + absAngle))*Math.cos(Math.toRadians(playerYaw)); //+-
	    double refPointY = pDistance*Math.sin(Math.toRadians(playerPitch + absAngle)); //+-
	    double refPointZ = pDistance*Math.cos(Math.toRadians(playerPitch + absAngle))*Math.sin(Math.toRadians(playerYaw)); //+-
	    double c = Math.sqrt(Math.pow(px - refPointX, 2) + Math.pow(py - refPointY, 2) + Math.pow(pz - refPointZ, 2)); //absolute distance
	    double absRotateAngle = 2*Math.toDegrees(Math.asin(c/(2*a)));
	    if(yawDif < 0) { 
	    	absRotateAngle *= -1;
	    } //else it remains positive
	    
//	    double x2D = Math.cos(Math.toRadians(90 + absRotateAngle))*absAngle/FOV*width;
//	    double y2D = Math.sin(Math.toRadians(90 + absRotateAngle))*absAngle/FOV*height;
//	    double x2D = ((Math.cos(Math.toRadians(90 + absRotateAngle))*a)/b)*(width/2)/Math.tan(Math.toRadians(FOV/2));
//	    double y2D = ((Math.sin(Math.toRadians(90 + absRotateAngle))*a)/b)*(width/2)/Math.tan(Math.toRadians(FOV/2));
//	    if(absAngle > 90) { 
//	    	if(retOffScreen == true) {
//	    		absAngle = 89;
//	    		
//	    	} else {
//	    		return null;
//	    	}
//	    }
		
	    //dead code
//		double vpxz = Math.sqrt(Math.pow(px - vx, 2) + Math.pow(pz - vz, 2));
//		double vpx = px - vx;
//		double x1 = Math.cos( Math.toRadians( Math.toDegrees( Math.acos(vpx / vpxz) ) - playerYaw ) )  * vpxz;
//		double xyAngle = Math.toDegrees(Math.atan(y / (x1) )) + playerPitch + 90;
//		double xyDistance1 = Math.sqrt(Math.pow(x1, 2) + Math.pow(y, 2));
//		double x2 = Math.cos(Math.toRadians(xyAngle))*xyDistance1; //actually 2d y
//		double y2 = Math.sin(Math.toRadians(xyAngle))*xyDistance1;
//		double xz2 = Math.sqrt(Math.pow(vpDistance, 2) - Math.pow(y2, 2));
//		double z2 = Math.sin(Math.acos(x2/xz2))*xz2;//actually 2d x
//		
		//////NEW CODE
	    
	    //test values
		double p2x = 0 - playerPos[0];
		double p2y = 50 - playerPos[1];
		double p2z = 0 - playerPos[2];
		
		////first step of translation: move coordinate plane to 0,0
		p2x = p2x - vx;
		p2y = p2y - vy;
		p2z = p2z - vz;
		
		////second step of translation: rotate yaw of coordinate plane to face +z
		double p2xz = Math.sqrt(Math.pow(p2x, 2) + Math.pow(p2z, 2)); //distance formula
		double p2Yaw = Math.toDegrees(Math.atan(p2z/p2x));
		if(p2x < 0) {
			if(p2Yaw < 0) {
				p2Yaw += 180;
			} else {
				p2Yaw -= 180;
			}
		}
		if(Double.isNaN(p2Yaw)) {
			if(p2z > 0) {
				p2Yaw = 90;
			} else {
				p2Yaw = -90;
			}
		}
		
		double newYaw = p2Yaw - (playerYaw - 90);
			
		//Choose newYaw to the smaller angle if it represents the longer ( > 180) angle
		if(newYaw > 180) {
			newYaw = newYaw - 360;
		} else if(newYaw < -180) {
			newYaw = newYaw + 360;
		}
		
		//update x and z to reflect new yaw
		p2x = Math.cos(Math.toRadians(newYaw))*p2xz;
		p2z = Math.sin(Math.toRadians(newYaw))*p2xz;
		
		////third step of translation: rotate pitch of coordinate plane to face +x
		double tiltAngle = playerPitch + 90;
		double p2zy = Math.sqrt(Math.pow(p2z, 2) + Math.pow(p2y, 2)); //distance formula
		double p2Pitch = Math.toDegrees(Math.atan(p2y/p2z));
		if(p2z < 0) {
			if(p2Pitch < 0) {
				p2Pitch += 180;
			} else {
				p2Pitch -= 180;
			}
		}
		if(Double.isNaN(p2Pitch)) {
			if(p2y > 0) {
				p2Pitch = 90;
			} else {
				p2Pitch = -90;
			}
		}
		
		double newPitch = p2Pitch - tiltAngle;
			
		//Choose newPitch to the smaller angle if it represents the longer ( > 180) angle
		if(newPitch > 180) {
			newPitch = newPitch - 360;
		} else if(newPitch < -180) {
			newPitch = newPitch + 360;
		}
		
		//update z and y to reflect new pitch
		p2z = Math.cos(Math.toRadians(newPitch))*p2zy;
		p2y = Math.sin(Math.toRadians(newPitch))*p2zy - (pDistance - b); 
		//(pDistance - b) is the height difference between the view plane and screen plane
		//the x/z coordinates do not need to be adjusted because they're the same for both
		double virtualDistance = (width/2)/Math.tan(Math.toRadians(FOV/2));
		p2x = (virtualDistance/b)*p2x;
		p2y = (virtualDistance/b)*p2y;
		p2z = (virtualDistance/b)*p2z;
		
		//virtualDistance = distance to virtual screen based on width and FOV
	    double x2D = Math.cos(Math.toRadians(90 + absRotateAngle))*Math.tan(Math.toRadians(absAngle))*virtualDistance;    
	    double y2D = Math.sin(Math.toRadians(90 + absRotateAngle))*Math.tan(Math.toRadians(absAngle))*virtualDistance;
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
			labels.set(13, "p2x: " + p2x);
			labels.set(14, "p2y: " + p2y);
			labels.set(15, "p2z: " + p2z);
			
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
