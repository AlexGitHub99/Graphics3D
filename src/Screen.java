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
	int PLANE = 3;
	int SCREEN = 0;
	int EDGE = 1;
	int OFFSCREEN = 2;
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
		g.fillRect(0, 0, width + 50, height + 50);
		g.setColor(Color.red);
		paintGrid(g, 100);
		g.setColor(Color.blue);
		
		if(forms != null) {
			for(int f = 0; f < forms.size(); f++)  {
				Form currentForm = forms.get(f);
				if(currentForm.getType() == RECTPRISM) {
					RectPrism currentPrism = (RectPrism)currentForm;
					int[][][] faces = currentPrism.getFaces();
					for(int s = 0; s < faces.length; s++) {
						paintFace(g, currentPrism.getCoords(), faces[s]);
					}
				} else if(currentForm.getType() == PLANE) {
					Plane currentPlane = (Plane)currentForm;
					int[][] face = currentPlane.getFace();
					paintFace(g, currentPlane.getCoords(), face);
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
	
	private void paintFace(Graphics g, int[] coords, int[][] face) {
		ArrayList<int[]> points = new ArrayList<int[]>();
		int exitArrayNumber = -1;
		int[] xyzFirstOut = null;
		int[] xyzSecondOut = null;
		int p = 0;
		int startedAt = -1;
		while(true) { //connected points are next to each other in array
			if(p == startedAt) {
				break;
			}
			System.out.println("New point, p = " + p);
			//3D points
			int x = face[p][0] + coords[0];
			int y = face[p][1] + coords[1];
			int z = face[p][2] + coords[2];
			//2d point
			int[] xy = calcPoint(x, y, z);
			int nextP;
			if(p == face.length - 1) {
				nextP = 0;
			} else {
				nextP = p + 1;
			}
			
			int[] xyNextP = calcPoint(face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]);
			
			boolean offScreen = false;
			if(xy != null) {
				if(xy[0] < 0 || xy[0] > width || xy[1] < 0 || xy[1] > height) {//current point is off screen
					offScreen = true;
				}
			}
			
			//
			//main logic section
			//
			if(xyNextP == null) { //next point is behind you
				if(xy != null) { //current point is in front of you
					if(offScreen == false) { //current point is on screen
						if(startedAt == -1) {
							startedAt = p;
						}
						points.add(xy);
						points.add(calcLine(x, y, z, face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2])[0]);
						exitArrayNumber = points.size() - 1;
						xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						xyzSecondOut = null; //reset just in case
					} else { //current point is off screen
						
						int[][] intersepts = calcLine(x, y, z, face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]);
						if(intersepts != null) {
							if(startedAt != -1) {
								int[] xyEnter = intersepts[0];
								points.add(xyEnter); //add first intercept
								addCorners(points, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
							} else {
								startedAt = nextP;
							}
							//reset
							exitArrayNumber = -1;
							xyzFirstOut = null;
							
							//add next intercept
							points.add(intersepts[1]);
							exitArrayNumber = points.size() - 1;
							xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							xyzSecondOut = null; //reset just in case
						} else {
							if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
								xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][1] + coords[1]};
							}
						}
					}
				} else {
					if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
						xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][1] + coords[1]};
					}
				}
			} else if(xyNextP[0] < 0 || xyNextP[0] > width || xyNextP[1] < 0 || xyNextP[1] > height) { //next point is off screen
				if(xy != null) { //current point is in front of you
					if(offScreen == false) { //current point is on screen
						if(startedAt == -1) {
							startedAt = p;
						}
						points.add(xy);
						points.add(calc2DIntersect(xy, xyNextP)[0]);
						exitArrayNumber = points.size() - 1;
						xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						xyzSecondOut = null; //reset just in case
					} else { //current point is off screen
						int[][] intersects2D = calc2DIntersect(xy, xyNextP);
						if(intersects2D != null) {
							if(startedAt != -1) {
								int[] xyEnter = intersects2D[0];
								points.add(xyEnter); //add first intercept
								addCorners(points, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
							} else {
								startedAt = nextP;
							}
							//reset
							exitArrayNumber = -1;
							xyzFirstOut = null;
							
							//add point between intercepts
//							int[] xyBetween = {intersects2D[1][0] - intersects2D[0][0], intersects2D[1][1] - intersects2D[0][1]}; //subtracts x and y difference to find middle point of line
//							points.add(xyBetween);
							
							//add next intercept
							points.add(intersects2D[1]);
							exitArrayNumber = points.size() - 1;
							xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							xyzSecondOut = null; //reset just in case
						} else {
							if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
								xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][1] + coords[1]};
							}
						}
					}
				} else { //current point is behind you
					
					int[][] intersepts = calcLine(face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2], x, y, z);
					if(intersepts != null) {
						if(startedAt != -1) {
							int[] xyEnter = intersepts[1];
							points.add(xyEnter); //add first intercept
							addCorners(points, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
						} else {
							startedAt = nextP;
						}
						//reset
						exitArrayNumber = -1;
						xyzFirstOut = null;
						
						//add next intercept
						points.add(intersepts[0]);
						exitArrayNumber = points.size() - 1;
						xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						xyzSecondOut = null; //reset just in case
					} else {
						if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
							xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][1] + coords[1]};
						}
					}
				}
			} else { //next point is on screen
				if(xy == null || offScreen == true) { //current point is either behind you or off screen
					if(startedAt != -1) {
						int[] xyEnter;
						if(xy != null) { //current point is in front of you just off screen
							xyEnter = calc2DIntersect(xyNextP, xy)[0];
						} else { //current point is behind you
							xyEnter = calcLine(face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2], x, y, z)[0];
						}
						points.add(xyEnter);
						
						addCorners(points, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
						
						//reset
						xyzFirstOut = null;
						xyzSecondOut = null;
						exitArrayNumber = -1;
					}
				} else { //current point is on screen
					if(startedAt == -1) {
						startedAt = p;
					}
					points.add(xy);
				}
			}
				
			p++;
			if(p >= face.length) {
				if(startedAt == -1) { //all points were behind you
					break;
				}
				p = 0;
			}
			if(p == startedAt) {
				break;
			}
		}
		int[] xPointsArray = new int[points.size()];
		int[] yPointsArray = new int[points.size()];
		System.out.println("points size: " + points.size());
		for(int i = 0; i < xPointsArray.length; i ++) {
			System.out.println("x: " + points.get(i)[0] + " y: " + points.get(i)[1]);
			xPointsArray[i] = points.get(i)[0];
			yPointsArray[i] = points.get(i)[1];
			g.setColor(Color.red);
			g.fillOval(points.get(i)[0], points.get(i)[1], 5, 5);
		}
		g.setColor(Color.blue);
		g.drawPolygon(xPointsArray, yPointsArray, xPointsArray.length);
	}
	
	private void addCorners(ArrayList<int[]> points, int[] xyzFirstOut, int[] xyzSecondOut, int[] xyExit, int[] xyEnter, int exitArrayNumber) { //points, 2d array entering point, 3d array first point out of screen, 3d array second point out of screen
		System.out.println("Adding corners");
		System.out.print("xyzFirstOut: ");
		printArray(xyzFirstOut);
		if(xyzSecondOut != null) {
			System.out.print("xyzSecondOut: ");
			printArray(xyzSecondOut);
		}
		System.out.print("xyExit: ");
		printArray(xyExit);
		System.out.print("xyEnter: ");
		printArray(xyEnter);
		System.out.println("exitArrayNumber: " + exitArrayNumber);
		
		double px = xyzFirstOut[0] - playerPos[0];
		double py = xyzFirstOut[1] - playerPos[1];
		double pz = xyzFirstOut[2] - playerPos[2];
		double pDistance = Math.sqrt(Math.pow(px, 2) + Math.pow(py, 2) + Math.pow(pz, 2)); //absolute distance
		
		double centerPointx = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.cos(Math.toRadians(playerYaw)) + playerPos[0]; //+-
		double centerPointy = pDistance*Math.sin(Math.toRadians(playerPitch)) + playerPos[1]; //+-
		double centerPointz = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.sin(Math.toRadians(playerYaw)) + playerPos[2]; //+-
		
		int[] intersect1 = calcLine((int)centerPointx, (int)centerPointy, (int)centerPointz, xyzFirstOut[0], xyzFirstOut[1], xyzFirstOut[2])[0]; //only 1 intercept	
		px = xyzFirstOut[0] - playerPos[0];
		py = xyzFirstOut[1] - playerPos[1];
		pz = xyzFirstOut[2] - playerPos[2];
		pDistance = Math.sqrt(Math.pow(px, 2) + Math.pow(py, 2) + Math.pow(pz, 2)); //absolute distance
		
		centerPointx = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.cos(Math.toRadians(playerYaw)) + playerPos[0]; //+-
		centerPointy = pDistance*Math.sin(Math.toRadians(playerPitch)) + playerPos[1]; //+-
		centerPointz = pDistance*Math.cos(Math.toRadians(playerPitch))*Math.sin(Math.toRadians(playerYaw)) + playerPos[2]; //+-
		
		int[] intersect2;
		if(xyzSecondOut != null) {//more than one point outside screen consecutively 
			intersect2 = calcLine((int)centerPointx, (int)centerPointy, (int)centerPointz, xyzSecondOut[0], xyzSecondOut[1], xyzSecondOut[2])[0]; //only 1 intercept
		} else {
			intersect2 = xyEnter;
		}
		intersect2 = xyEnter;
		
		System.out.println("intersect1: ");
		printArray(intersect1);
		System.out.println("intersect2: ");
		printArray(intersect2);
		
//		int[] middle = {width/2, height/2};
//		
//		int[] xyFirstOut = calcPoint(xyzFirstOut[0], xyzFirstOut[1], xyzFirstOut[2]);
//		int[] intersect1 = calc2DIntersect(middle, xyFirstOut)[0];
//		int[] intersect2;
//		if(xyzSecondOut != null) {//more than one point outside screen consecutively 
//			int[] xySecondOut = calcPoint(xyzSecondOut[0], xyzSecondOut[1], xyzSecondOut[2]);
//			intersect2 = calc2DIntersect(middle, xySecondOut)[0];
//		} else {
//			intersect2 = xyEnter;
//		}
		
		int xyExitSide = -1;
		int xyEnterSide = -1;
		if(xyExit[0] == 0) {
			xyExitSide = 0;
		} else if(xyExit[1] == 0) {
			xyExitSide = 1;
		} else if(xyExit[0] == width) {
			xyExitSide = 2;
		} else if(xyExit[1] == height) {
			xyExitSide = 3;
		}
		if(xyEnter[0] == 0) {
			xyEnterSide = 0;
		} else if(xyEnter[1] == 0) {
			xyEnterSide = 1;
		} else if(xyEnter[0] == width) {
			xyEnterSide = 2;
		} else if(xyEnter[1] == height) {
			xyEnterSide = 3;
		}
		
		//
		//Add all corners between exit and entry points
		//
		
		//set perimeter distances (distance clockwise around the rectangle from the top left corner)
		int xyExitPerimeterDistance = xyExit[0] + xyExit[1];
		if(xyExit[0] == width || xyExit[1] == 0) {
			//do nothing
		} else {
			xyExitPerimeterDistance = width*2 + height*2 - xyExitPerimeterDistance;
		}
		
		int xyEnterPerimeterDistance = xyEnter[0] + xyEnter[1];
		if(xyEnter[0] == width || xyEnter[1] == 0) {
			//do nothing
		} else {
			xyEnterPerimeterDistance = width*2 + height*2 - xyEnterPerimeterDistance;
		}
		
		int intersect1PerimeterDistance = intersect1[0] + intersect1[1];
		if(intersect1[0] == width || intersect1[1] == 0) {
			//do nothing
		} else {
			intersect1PerimeterDistance = width*2 + height*2 - intersect1PerimeterDistance;
		}
		
		int intersect2PerimeterDistance = intersect2[0] + intersect2[1];
		if(intersect2[0] == width || intersect2[1] == 0) {
			//do nothing
		} else {
			intersect2PerimeterDistance = width*2 + height*2 - intersect2PerimeterDistance;
		}
		
		//corners are in clockwise order
		int[][] corners = new int[4][2];
		corners[0] = new int[] {0, 0}; //top left
		corners[1] = new int[] {width, 0}; //top right
		corners[2] = new int[] {width, height}; //bottom right
		corners[3] = new int[] {0, height}; //bottom left
		
		int difference = intersect2PerimeterDistance - intersect1PerimeterDistance;
		//reduce difference to shortest distance if needed
		if(difference > width + height) {
			difference = difference - 2*(width + height);
		} else if(difference < -(width + height)) {
			difference = difference + 2*(width + height);
		}
		
		if(difference >= 0) {//go clockwise
			if(xyExitPerimeterDistance < xyEnterPerimeterDistance) { //does not need to loop around
				System.out.println("not looping");
				int j = 0;
				for(int i = xyExitSide; i < xyEnterSide; i++) {
					try {
						points.add(exitArrayNumber + 1 + j, corners[i]);
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println(e);
						System.out.println("exitp[1]: " + exitArrayNumber);
						for(int k = 0; k < points.size(); k++) {
							System.out.println("k " + points.get(k)[0] + " " + points.get(k)[1]);
						}
					}
					j++;
				}
			} else { //does need to loop around
				System.out.println("looping");
				int j = 0;
				for(int i = xyExitSide; i < xyEnterSide + 4; i++) {
					points.add(exitArrayNumber + 1 + j, corners[i % 4]);
					j++;
				}
			}
		} else {//go counter clockwise
			if(xyExitPerimeterDistance > xyEnterPerimeterDistance) { //does not need to loop around
				System.out.println("not looping");
				int j = 0;
				for(int i = xyExitSide - 1; i >= xyEnterSide; i--) {
					points.add(exitArrayNumber + 1 + j, corners[i]);
					j++;
				}
			} else { //does need to loop around
				System.out.println("looping");
				int j = 0;
				for(int i = xyExitSide - 1; i >= xyEnterSide - 4; i--) {
					points.add(exitArrayNumber + 1 + j, corners[(i + 4) % 4]);
					j++;
				}
			}
		}
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
		if(pz/pXZ > 1) { //this should not happen in theory but because of rounding errors by the computer it can happen by a very small amount
			pz = pXZ;
		}
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
	    if(c/(2*a) > 1) { //this should not happen in theory but because of rounding errors by the computer it can happen by a very small amount
	    	c = 2*a;
	    }
	    double absRotateAngle = 2*Math.toDegrees(Math.asin(c/(2*a)));
	    if(yawDif < 0) { 
	    	absRotateAngle *= -1;
	    } //else it remains positive
	    
	    if(absAngle > 90) { //point is behind player
	    	return null;
	    }
		
		double virtualDistance = (width/2)/Math.tan(Math.toRadians(FOV/2));
		
		//virtualDistance = distance to virtual screen based on width and FOV
	    double x2D = Math.cos(Math.toRadians(90 + absRotateAngle))*Math.tan(Math.toRadians(absAngle))*virtualDistance;    
	    double z2D = Math.sin(Math.toRadians(90 + absRotateAngle))*Math.tan(Math.toRadians(absAngle))*virtualDistance;
	    
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
		xy2D[1] = (int)(-z2D + height/2); //z represents y direction on screen
		return xy2D;
	}
	
	private int[][] calcLine(int x, int y, int z, int xo, int yo, int zo) {
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
		if(pz/pXZ > 1) { //this should not happen in theory but because of rounding errors by the computer it can happen by a very small amount
			pz = pXZ;
		}
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
	    if(c/(2*a) > 1) { //this should not happen in theory but because of rounding errors by the computer it can happen by a very small amount
	    	c = 2*a;
	    }
	    double absRotateAngle = 2*Math.toDegrees(Math.asin(c/(2*a)));
	    if(yawDif < 0) { 
	    	absRotateAngle *= -1;
	    } //else it remains positive
	    
	    if(absAngle > 90) { //point is behind player
	    	return null;
	    }
		
		//////NEW CODE
		double p2x = xo - playerPos[0];
		double p2y = yo - playerPos[1];
		double p2z = zo - playerPos[2];
		
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
	    double z2D = Math.sin(Math.toRadians(90 + absRotateAngle))*Math.tan(Math.toRadians(absAngle))*virtualDistance;
	    
	    /***
	     *      ____________________                     
	     *     |\                  /|                   /\
	     *     |   \      4     /   |          \       /  \
	     *     |      \      /      |            \    /    \
	     *     |  1      \/      2  |              \ /      \
	     *     |         /\         |    Point of ->/\       \
	     *     |      /      \      |  intersection/   \      \
	     *     |   /      3     \   |             /      \     \
	     *     |/__________________\|            /_________\____\
	     *        Top down view                      Side view
	     *      
	     *  Triangles are viewed from the side perpendicular to them
	     */
	    
	    //x axis line
	    double mH = p2y/(p2x - x2D); //slope
	    double bH = -mH*x2D; //y-intercept
	    
	    //z axis line
	    double mV = p2y/(p2z -  z2D);
	    double bV = -mV*z2D;
	    
	    double[][] zxe2DArray = {null, null}; //first point in the array is the one closest to the first point passed as input into the function
	    double yofIntersFirst = -1;
	    
	    //Horizontal
	    //check for intersection on triangle 1
	    double m1 = virtualDistance/(width/2); // y-intercept for m1 and m2 is virtualDistance
	    double xofInters = (virtualDistance - bH)/(mH - m1);
	    double yofInters = m1*xofInters + virtualDistance;
	    double zofInters = (yofInters - bV)/mV;
	    if(yofInters >= 0 && yofInters <= virtualDistance && Math.abs(zofInters/xofInters) <= height/width) { //intersection point is on triangle 1
	    	zxe2DArray[0] = new double[] {(zofInters/xofInters)*(-width/2), -width/2};
	    	yofIntersFirst = yofInters;
	    }
	    //check for intersection on triangle 2
    	xofInters = (virtualDistance - bH)/(mH - (-m1)); // other side is -m1
 	    yofInters = -m1*xofInters + virtualDistance;
 	    zofInters = (yofInters - bV)/mV;
 	    if(yofInters >= 0 && yofInters <= virtualDistance && Math.abs(zofInters/xofInters) <= height/width) { //intersection point is on triangle 2
	 	    if(yofIntersFirst == -1) { //first point hasn't been found
	 	    	zxe2DArray[0] = new double[] {(zofInters/xofInters)*(width/2), width/2};
	 	    	yofIntersFirst = yofInters;
 	    	} else {
 	    		if(yofInters > yofIntersFirst) { //second intersect
 	    			zxe2DArray[1] = new double[] {(zofInters/xofInters)*(width/2), width/2};
 	    		} else { //switch positions to first intersect
 	    			double[] temp = zxe2DArray[0];
 	    			zxe2DArray[0] = new double[] {(zofInters/xofInters)*(width/2), width/2};
 	    			zxe2DArray[1] = temp;
 	    		}
 	    	}
 	    }
 	    //Vertical
    	//check for intersection on triangle 3
    	double m2 = virtualDistance/(height/2);
    	zofInters = (virtualDistance - bV)/(mV - m2);
    	yofInters = m2*zofInters + virtualDistance;
    	xofInters = (yofInters - bH)/mH;
    	if(yofInters >= 0 && yofInters <= virtualDistance && Math.abs(xofInters/zofInters) <= width/height) { //intersection point is on triangle 3
    		if(yofIntersFirst == -1) { //first point hasn't been found
	 	    	zxe2DArray[0] = new double[] {-height/2, (xofInters/zofInters)*(-height/2)};
	 	    	yofIntersFirst = yofInters;
 	    	} else {
 	    		if(yofInters > yofIntersFirst) { //second intersect
 	    			zxe2DArray[1] = new double[] {-height/2, (xofInters/zofInters)*(-height/2)};
 	    		} else { //switch positions to first intersect
 	    			double[] temp = zxe2DArray[0];
 	    			zxe2DArray[0] = new double[] {-height/2, (xofInters/zofInters)*(-height/2)};
 	    			zxe2DArray[1] = temp;
 	    		}
 	    	}
    	}
    	//check for intersection on triangle 4
		zofInters = (virtualDistance - bV)/(mV - (-m2));
		yofInters = -m2*zofInters + virtualDistance;
		xofInters = (yofInters - bH)/mH;
		if(yofInters >= 0 && yofInters <= virtualDistance && Math.abs(xofInters/zofInters) <= width/height) { //intersection point is on triangle 3
    		if(yofIntersFirst == -1) { //first point hasn't been found
	 	    	zxe2DArray[0] = new double[] {height/2, (xofInters/zofInters)*(height/2)};
	 	    	yofIntersFirst = yofInters;
 	    	} else {
 	    		if(yofInters > yofIntersFirst) { //second intersect
 	    			zxe2DArray[1] = new double[] {height/2, (xofInters/zofInters)*(height/2)};
 	    		} else { //switch positions to first intersect
 	    			double[] temp = zxe2DArray[0];
 	    			zxe2DArray[0] = new double[] {height/2, (xofInters/zofInters)*(height/2)};
 	    			zxe2DArray[1] = temp;
 	    		}
 	    	}
		}
		
		int[][] xy2D = {null, null};
		
	    if(zxe2DArray[0] != null) {
	    	xy2D[0] = new int[2];
			xy2D[0][0] = (int)(zxe2DArray[0][1] + width/2);
			xy2D[0][1] = (int)(-zxe2DArray[0][0] + height/2); //z represents y direction on screen
	    }
	    if(zxe2DArray[1] != null) {
	    	xy2D[1] = new int[2];
			xy2D[1][0] = (int)(zxe2DArray[1][1] + width/2);
			xy2D[1][1] = (int)(-zxe2DArray[1][0] + height/2); //z represents y direction on screen
	    }
	    
		if(xy2D[0] == null && xy2D[1] == null) {
			xy2D = null;
		}
		return xy2D;
	}
	
	public int[][] calc2DIntersect(int[] xy1, int[] xy2) {
		System.out.println("Calculating 2D intersect");
		double m = ((double)xy2[1] - (double)xy1[1])/((double)xy2[0] - (double)xy1[0]); //xy1 slope
		double b = (double)xy1[1] - m*(double)xy1[0]; //y intercept
		int[][] intersects = {null, null};
		
		if(b >= 0 && b <= height) {
			if(intersects[0] == null) {
				intersects[0] = new int[] {0, (int)(Math.round(b))};
			} else {
				intersects[1] = new int[] {0, (int)(Math.round(b))};
			}
		}
	
		double y = m*width + b;
		if(y >= 0 && y <= height) {
			if(intersects[0] == null) {
				intersects[0] = new int[] {width, (int)(Math.round(y))};
			} else {
				intersects[1] = new int[] {width, (int)(Math.round(y))};
			}
		}
	
		double x = -b/m;
		if(x >= 0 && x <= width) {
			if(intersects[0] == null) {
				intersects[0] = new int[] {(int)(Math.round(x)), 0};
			} else {
				intersects[1] = new int[] {(int)(Math.round(x)), 0};
			}
		}
	
		x = (height - b)/m;
		if(x >= 0 && x <= width) {
			if(intersects[0] == null) {
				intersects[0] = new int[] {(int)(Math.round(x)), height};
			} else {
				intersects[1] = new int[] {(int)(Math.round(x)), height};
			}
		}
		
		if(intersects[0] != null) {
			if(intersects[1] != null) {
				if( (intersects[1][0] > xy1[0] && intersects[1][0] > xy2[0]) || (intersects[1][0] < xy1[0] && intersects[1][0] < xy2[0]) ) {
					intersects[1] = null;
				}
			}
			if( (intersects[0][0] > xy1[0] && intersects[0][0] > xy2[0]) || (intersects[0][0] < xy1[0] && intersects[0][0] < xy2[0]) ) {
				intersects[0] = intersects[1];
				intersects[1] = null;
			}
		}
//		System.out.println("Points: " + xy1[0]  + " " + xy1[1] + "  " + xy2[0] + " " + xy2[1]);
//		if(intersects[0] != null) {
//			System.out.println("Intersects: " + intersects[0][0] + " " + intersects[0][1]);
//		}
//		if(intersects[1] != null) {
//			System.out.println("Intersects: " + intersects[1][0] + " " + intersects[1][1]);
//		}
		
		if(intersects[1] != null) {
			if(Math.abs(intersects[0][0] - xy1[0]) > Math.abs(intersects[1][0] - xy1[0])) { //need to switch points
				int[] temp = intersects[0];
				intersects[0] = intersects[1];
				intersects[1] = temp;
			}
		} else if(intersects[0] == null) {
			return null;
		}
		return intersects;
	}
	
	public ArrayList<String> getInfoLabels() {
		return labels;
	}
	
	private void printArray(int[] array) {
		for(int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.print('\n');
	}
	
}
