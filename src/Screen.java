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
	int POSITIVE = 1;
	int NEGATIVE = 2;
	int UNSET = 9000;
	int ZERO = 0;
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
//					RectPrism currentPrism = (RectPrism)currentForm;
//					int[][][] faces = currentPrism.getFaces();
//					for(int s = 0; s < faces.length; s++) {
//						paintFace(g, currentPrism.getCoords(), faces[s], false);
//					}
				} else if(currentForm.getType() == PLANE) {
					Plane currentPlane = (Plane)currentForm;
					int[][] face = currentPlane.getFace();
					paintFace(g, currentPlane.getCoords(), face, true);
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
	
	private void paintFace(Graphics g, int[] coords, int[][] face, boolean fill) {
		ArrayList<int[]> points = new ArrayList<int[]>();
		int exitArrayNumber = -1;
		int[] xyzFirstOut = null;
		int[] xyzSecondOut = null;
		double exitAngle = UNSET;
		double enterAngle = UNSET; 
		int p = 0;
		int startedAt = -1;
		boolean dontBreak = false;
		int[] lastOut = null;
		//centerPoint is the middle of the face and is used for adding corners
		int[] xyzCenterPoint = {0, 0, 0};
		for(int i = 0; i < face.length; i++) {
			xyzCenterPoint[0] += face[i][0];
			xyzCenterPoint[1] += face[i][1];
			xyzCenterPoint[2] += face[i][2];
		}
		xyzCenterPoint[0] = xyzCenterPoint[0]/face.length + coords[0];
		xyzCenterPoint[1] = xyzCenterPoint[1]/face.length + coords[1];
		xyzCenterPoint[2] = xyzCenterPoint[2]/face.length + coords[2];
		
		int[] xyCenterPointTest = calcPoint(xyzCenterPoint[0], xyzCenterPoint[1], xyzCenterPoint[2]);
		if(xyCenterPointTest != null) {
			g.setColor(Color.RED);
			g.fillOval(xyCenterPointTest[0], xyCenterPointTest[1], 5, 5);
			g.setColor(Color.BLUE);
		}
		int[] xyzOnScreen = null;
		while(true) { //connected points are next to each other in array
			if(p == startedAt) {
				if(dontBreak == true) {
					dontBreak = false;
				} else {
					break;
				}
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
			
			int xNext = face[nextP][0] + coords[0];
			int yNext = face[nextP][1] + coords[1];
			int zNext = face[nextP][2] + coords[2];
			int[] xyNextP = calcPoint(xNext, yNext, zNext);
			
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
						int[] intercept = calcLine(x, y, z, face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2], false)[0];
						points.add(xy);
						points.add(intercept);
						exitArrayNumber = points.size() - 1;
						exitAngle = calcAngle(xy, intercept);
						
						xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						xyzSecondOut = null; //reset just in case
						lastOut = new int[] {x, y, z};
					} else { //current point is off screen
						
						int[][] intersepts = calcLine(x, y, z, face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2], true);//calcline between current point and next point
						if(intersepts != null) {
							if(startedAt != -1) {
								int[] xyEnter = intersepts[0];
								points.add(xyEnter); //add first intercept
								enterAngle = calcAngle(intersepts[1], intersepts[0]);
								
								lastOut = new int[] {x, y, z};
								int[] xyzOffScreen = null;
								int[] xyCenterPoint = calcPoint(xyzCenterPoint[0], xyzCenterPoint[1], xyzCenterPoint[2]);
								if(xyCenterPoint == null || xyCenterPoint[0] < 0 || xyCenterPoint[0] > width || xyCenterPoint[1] < 0 || xyCenterPoint[1] > height) { //center point is off screen
									xyzOnScreen = intersepts[2]; //middle point
									xyzOffScreen = xyzCenterPoint;
								} else {
									xyzOnScreen = xyzCenterPoint;
									xyzOffScreen = new int[] {x, y, z};
								}
								if(calcPoint(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2]) == null) {
									System.out.println("Oh no");
								}
								double middleAngle = averageAngle(exitAngle, enterAngle);
								addCorners(points, xyzOffScreen, xyzOnScreen, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
							} else {
								startedAt = nextP;
								dontBreak = true;
							}
							
							//add next intercept
							points.add(intersepts[1]);
							exitArrayNumber = points.size() - 1;
							exitAngle = calcAngle(intersepts[0], intersepts[1]);
							
							xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							xyzSecondOut = null; //reset just in case
							
						} else {
							if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
								xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							}
						}
					}
				} else {
					if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
						xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
					}
				}
			} else if(xyNextP[0] < 0 || xyNextP[0] > width || xyNextP[1] < 0 || xyNextP[1] > height) { //next point is in front but off screen
				if(xy != null) { //current point is in front of you
					if(offScreen == false) { //current point is on screen
						if(startedAt == -1) {
							startedAt = p;
						}
						int[] intercept = calcLine(x, y, z, xNext, yNext, zNext, false)[0];
						points.add(xy);
						points.add(intercept);
						exitArrayNumber = points.size() - 1;
						exitAngle = calcAngle(xy, intercept);
						
						xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						xyzSecondOut = null; //reset just in case
						lastOut = new int[] {x, y, z};
						xyzOnScreen = new int[] {x, y, z};
					} else { //current point is off screen
						int[][] intersepts = calcLine(x, y, z, xNext, yNext, zNext, true);
						if(intersepts != null) {
							if(startedAt != -1) {
								int[] xyEnter = intersepts[0];
								points.add(xyEnter); //add first intercept
								enterAngle = calcAngle(intersepts[1], intersepts[0]);
								
								lastOut = new int[] {x, y, z};
								int[] xyzOffScreen = null;
								int[] xyCenterPoint = calcPoint(xyzCenterPoint[0], xyzCenterPoint[1], xyzCenterPoint[2]);
								if(xyCenterPoint == null || xyCenterPoint[0] < 0 || xyCenterPoint[0] > width || xyCenterPoint[1] < 0 || xyCenterPoint[1] > height) { //center point is off screen
									xyzOnScreen = intersepts[2]; //middle point
									int[] test = calcPoint(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2]);
									g.fillOval(test[0], test[1], 5, 5);
									xyzOffScreen = xyzCenterPoint;
								} else { //center point on screen
									xyzOnScreen = xyzCenterPoint;
									xyzOffScreen = new int[] {x, y, z};
								}
								if(calcPoint(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2]) == null) {
									System.out.println("Oh no");
								}
								addCorners(points, xyzOffScreen, xyzOnScreen, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
							} else {
								startedAt = nextP;
								dontBreak = true;
								//add next intercept
								
							}
							
							points.add(intersepts[1]);
							exitArrayNumber = points.size() - 1;
							exitAngle = calcAngle(intersepts[0], intersepts[1]);
							
							xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							xyzSecondOut = null; //reset just in case
							//add point between intercepts
//							int[] xyBetween = {intersects2D[1][0] - intersects2D[0][0], intersects2D[1][1] - intersects2D[0][1]}; //subtracts x and y difference to find middle point of line
//							points.add(xyBetween);
							
						} else {
							if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
								xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							}
						}
					}
				} else { //current point is behind you
					
					int[][] intersepts = calcLine(face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2], x, y, z, true);
					if(intersepts != null) {
						if(startedAt != -1) {
							int[] xyEnter = intersepts[1];
							points.add(xyEnter); //add first intercept
							enterAngle = calcAngle(intersepts[0], intersepts[1]);
							
							lastOut = new int[] {x, y, z};
							int[] xyzOffScreen = null;
							int[] xyCenterPoint = calcPoint(xyzCenterPoint[0], xyzCenterPoint[1], xyzCenterPoint[2]);
							if(xyCenterPoint == null || xyCenterPoint[0] < 0 || xyCenterPoint[0] > width || xyCenterPoint[1] < 0 || xyCenterPoint[1] > height) { //center point is off screen
								xyzOnScreen = intersepts[2]; //middle point
								xyzOffScreen = xyzCenterPoint;
							} else {
								xyzOnScreen = xyzCenterPoint;
								xyzOffScreen = new int[] {x, y, z};
							}
							if(calcPoint(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2]) == null) {
								System.out.println("Oh no");
							}
							addCorners(points, xyzOffScreen, xyzOnScreen, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
						} else {
							startedAt = nextP;
							dontBreak = true;
						}
						
						//add next intercept
						points.add(intersepts[0]);
						exitArrayNumber = points.size() - 1;
						exitAngle = calcAngle(intersepts[1], intersepts[0]);
						
						xyzFirstOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						xyzSecondOut = null; //reset just in case
					} else {
						if(xyzSecondOut == null) { //if xyzSecondOut is null then the current point must be the first consecutive point outside the screen
							xyzSecondOut = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
						}
					}
				}
			} else { //next point is on screen
				if(xy == null || offScreen == true) { //current point is either behind you or off screen
					if(startedAt != -1) {
						int[] xyEnter;
//						if(xy != null) { //current point is in front of you just off screen
//							xyEnter = calcLine(xNext, yNext, zNext, x, y, z, false)[0];
////							xyEnter = calc2DIntersect(xyNextP, xy)[0];
//						} else { //current point is behind you
//							xyEnter = calcLine(xNext, yNext, zNext, x, y, z, false)[0];
//						}
						int[] intersept = calcLine(xNext, yNext, zNext, x, y, z, false)[0];
						xyEnter = intersept;
						
						points.add(xyEnter);
						enterAngle = calcAngle(xyNextP, intersept);
						
						assert(lastOut != null);
						int[] xyzOffScreen = null;
						int[] xyCenterPoint = calcPoint(xyzCenterPoint[0], xyzCenterPoint[1], xyzCenterPoint[2]);
						if(xyCenterPoint == null || xyCenterPoint[0] < 0 || xyCenterPoint[0] > width || xyCenterPoint[1] < 0 || xyCenterPoint[1] > height) { //center point is off screen
							xyzOnScreen = new int[] {face[nextP][0] + coords[0], face[nextP][1] + coords[1], face[nextP][2] + coords[2]};
							xyzOffScreen = xyzCenterPoint;
						} else {
							xyzOnScreen = xyzCenterPoint;
							xyzOffScreen = new int[] {x, y, z};
						}
						if(calcPoint(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2]) == null) {
							System.out.println("Oh no");
						}
						addCorners(points, xyzOffScreen, xyzOnScreen, xyzFirstOut, xyzSecondOut, points.get(exitArrayNumber), xyEnter, exitArrayNumber);
						
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
		}
		int[] xPointsArray = new int[points.size()];
		int[] yPointsArray = new int[points.size()];
		System.out.println("points size: " + points.size());
		for(int i = 0; i < xPointsArray.length; i ++) {
			System.out.println("x: " + points.get(i)[0] + " y: " + points.get(i)[1]);
			xPointsArray[i] = points.get(i)[0];
			yPointsArray[i] = points.get(i)[1];
			//for debug
//			g.setColor(Color.red);
//			g.fillOval(points.get(i)[0], points.get(i)[1], 5, 5);
//			g.drawString(i + "", points.get(i)[0], points.get(i)[1]);
		}
		g.setColor(Color.blue);
		if(fill == true) {
//			int[][] 
			if(xPointsArray.length == 0) {
//				int x = face[p][0] + coords[0];
//				int y = face[p][1] + coords[1];
//				int z = face[p][2] + coords[2];
//				//2d point
//				int[] xy = calcPoint(x, y, z);
			} else {
				g.fillPolygon(xPointsArray, yPointsArray, xPointsArray.length);
			}
		} else { //fill == false
			g.drawPolygon(xPointsArray, yPointsArray, xPointsArray.length);
		}
		if(xyCenterPointTest != null) {
			g.setColor(Color.RED);
			g.fillOval(xyCenterPointTest[0], xyCenterPointTest[1], 5, 5);
			g.setColor(Color.BLUE);
		}
	}
	
	private void addCorners(ArrayList<int[]> points, int[] xyzOffScreen, int[] xyzOnScreen, int[] xyzFirstOut, int[] xyzSecondOut, int[] xyExit, int[] xyEnter, int exitArrayNumber) { //points, 2d array entering point, 3d array first point out of screen, 3d array second point out of screen
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
		
		int[] intersects = calcPoint(xyzOffScreen[0], xyzOffScreen[1], xyzOffScreen[2]);
		if(intersects != null) {
			System.out.println("Shit");
		}
		intersects = calcPoint(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2]);
		if(intersects == null) {
			System.out.println("Shit");
		}
		if(playerPitch == -90) {
			System.out.println("blah");
		}
//		int[] intersect = {1, 2};
//		try {
		int[] intersect = calcLine(xyzOnScreen[0], xyzOnScreen[1], xyzOnScreen[2], xyzOffScreen[0], xyzOffScreen[1], xyzOffScreen[2], false)[0]; //only 1 intercept
//		} catch(Exception e) {
//			System.out.println("error");
//		}
		
		int xyExitSide = -1;
		int xyEnterSide = -1;
		if(xyExit[0] == 0) {
			xyExitSide = 3;
		} else if(xyExit[1] == 0) {
			xyExitSide = 0;
		} else if(xyExit[0] == width) {
			xyExitSide = 1;
		} else if(xyExit[1] == height) {
			xyExitSide = 2;
		}
		if(xyEnter[0] == 0) {
			xyEnterSide = 3;
		} else if(xyEnter[1] == 0) {
			xyEnterSide = 0;
		} else if(xyEnter[0] == width) {
			xyEnterSide = 1;
		} else if(xyEnter[1] == height) {
			xyEnterSide = 2;
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
		
		int intersectPerimeterDistance = intersect[0] + intersect[1];
		if(intersect[0] == width || intersect[1] == 0) {
			//do nothing
		} else {
			intersectPerimeterDistance = width*2 + height*2 - intersectPerimeterDistance;
		}
		
		//corners are in clockwise order
		int[][] corners = new int[4][2];
		corners[0] = new int[] {width, 0}; //top right
		corners[1] = new int[] {width, height}; //bottom right
		corners[2] = new int[] {0, height}; //bottom left
		corners[3] = new int[] {0, 0}; //top left
		
		
		boolean clockwise = true;
		if(intersectPerimeterDistance < xyEnterPerimeterDistance) {
			if(intersectPerimeterDistance < xyExitPerimeterDistance) {
				if(xyExitPerimeterDistance < xyEnterPerimeterDistance) {
					clockwise = true;
				} else { //xyExitPerimeterDistance > xyEnterPerimeterDistance
					clockwise = false;
				}
			} else { //intersectPerimeterDistance > xyExitPerimeterDistance
				clockwise = false;
			}
		} else { //intersectPerimeterDistance > xyEnterPerimeterDistance
			if(intersectPerimeterDistance < xyExitPerimeterDistance) {
				clockwise = true;
			} else { //intersectPerimetDistance > xyExitPerimetDistance
				if(xyExitPerimeterDistance > xyEnterPerimeterDistance) {
					clockwise = false;
				} else { //xyExitPerimeterDistance > xyEnterPerimeterDistance
					clockwise = true;
				}
			}
		}
		
		if(clockwise) {
			int i = xyEnterSide;
			while(i != xyExitSide) {
				points.add(exitArrayNumber + 1, corners[i]);
				
				i++;
				if(i == 4) {
					i = 0;
				}
			}
		} else { //counter-clockwise
			int i = xyEnterSide;
			while(i != xyExitSide) {
				i--;
				if(i == -1) {
					i = 3;
				}
				points.add(exitArrayNumber + 1, corners[i]);
				
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
	
	private int[][] calcLine(int x, int y, int z, int xo, int yo, int zo, boolean returnMiddle) {
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
	    
	    double[] xyzFirstInters = null;
	    double[] xyzSecondInters = null;
	    
	    //Horizontal
	    //check for intersection on triangle 1
	    double m1 = virtualDistance/(width/2); // y-intercept for m1 and m2 is virtualDistance
	    double xofInters = (virtualDistance - bH)/(mH - m1);
	    double yofInters = m1*xofInters + virtualDistance;
	    double zofInters = (p2z -  z2D)/(p2x - x2D)*(xofInters - x2D) + z2D;//(yofInters - bV)/mV;
	    if(((xofInters > x2D && xofInters < p2x) || (xofInters < x2D && xofInters > p2x) || (zofInters > z2D && zofInters < p2z) || (zofInters < z2D && zofInters > p2z)) && yofInters <= virtualDistance && Math.abs(zofInters/xofInters) <= height/width) { //intersection point is on triangle 1
	    	zxe2DArray[0] = new double[] {(zofInters/xofInters)*(-width/2), -width/2};
	    	xyzFirstInters = new double[] {xofInters, yofInters, zofInters};
	    }
	    //check for intersection on triangle 2
    	xofInters = (virtualDistance - bH)/(mH - (-m1)); // other side is -m1
 	    yofInters = -m1*xofInters + virtualDistance;
 	    zofInters = (p2z -  z2D)/(p2x - x2D)*(xofInters - x2D) + z2D;//(yofInters - bV)/mV;
 	    if(((xofInters > x2D && xofInters < p2x) || (xofInters < x2D && xofInters > p2x) || (zofInters > z2D && zofInters < p2z) || (zofInters < z2D && zofInters > p2z)) && yofInters <= virtualDistance && Math.abs(zofInters/xofInters) <= height/width) { //intersection point is on triangle 2
	 	    if(zxe2DArray[0] == null) { //first point hasn't been found
	 	    	zxe2DArray[0] = new double[] {(zofInters/xofInters)*(width/2), width/2};
	 	    	xyzFirstInters = new double[] {xofInters, yofInters, zofInters};
 	    	} else {
    			zxe2DArray[1] = new double[] {(zofInters/xofInters)*(width/2), width/2};
    			xyzSecondInters = new double[] {xofInters, yofInters, zofInters};
 	    	}
 	    }
 	    //Vertical
    	//check for intersection on triangle 3
    	double m2 = virtualDistance/(height/2);
    	zofInters = (virtualDistance - bV)/(mV - m2);
    	yofInters = m2*zofInters + virtualDistance;
    	xofInters = (p2x - x2D)/(p2z -  z2D)*(zofInters - z2D) + x2D;//(yofInters - bH)/mH;
    	if(((xofInters > x2D && xofInters < p2x) || (xofInters < x2D && xofInters > p2x) || (zofInters > z2D && zofInters < p2z) || (zofInters < z2D && zofInters > p2z)) && yofInters <= virtualDistance && Math.abs(xofInters/zofInters) <= width/height) { //intersection point is on triangle 3
    		if(zxe2DArray[0] == null) { //first point hasn't been found
	 	    	zxe2DArray[0] = new double[] {-height/2, (xofInters/zofInters)*(-height/2)};
	 	    	xyzFirstInters = new double[] {xofInters, yofInters, zofInters};
 	    	} else {
    			zxe2DArray[1] = new double[] {-height/2, (xofInters/zofInters)*(-height/2)};
    			xyzSecondInters = new double[] {xofInters, yofInters, zofInters};
 	    	}
    	}
    	//check for intersection on triangle 4
		zofInters = (virtualDistance - bV)/(mV - (-m2));
		yofInters = -m2*zofInters + virtualDistance;
		xofInters = (p2x - x2D)/(p2z -  z2D)*(zofInters - z2D) + x2D;//(yofInters - bH)/mH;
		if(((xofInters > x2D && xofInters < p2x) || (xofInters < x2D && xofInters > p2x) || (zofInters > z2D && zofInters < p2z) || (zofInters < z2D && zofInters > p2z)) && yofInters <= virtualDistance && Math.abs(xofInters/zofInters) <= width/height) { //intersection point is on triangle 3
    		if(zxe2DArray[0] == null) { //first point hasn't been found
	 	    	zxe2DArray[0] = new double[] {height/2, (xofInters/zofInters)*(height/2)};
	 	    	xyzFirstInters = new double[] {xofInters, yofInters, zofInters};
 	    	} else {
    			zxe2DArray[1] = new double[] {height/2, (xofInters/zofInters)*(height/2)};
    			xyzSecondInters = new double[] {xofInters, yofInters, zofInters};
 	    	}
		}
		
		int[] xyzMiddle = null;
		if(zxe2DArray[1] != null) {
			//check if points need to be switched in order so the closest intercept is first
			boolean shouldSwitch = false;
			if(xyzFirstInters[0] < x2D && xyzSecondInters[0] > xyzFirstInters[0]) {
				shouldSwitch = true;
			} else if(xyzFirstInters[0] > x2D && xyzSecondInters[0] < xyzFirstInters[0]) {
				shouldSwitch = true;
			}
			if(xyzFirstInters[2] < z2D && xyzSecondInters[2] > xyzFirstInters[2]) {
				shouldSwitch = true;
			} else if(xyzFirstInters[2] > z2D && xyzSecondInters[2] < xyzFirstInters[2]) {
				shouldSwitch = true;
			}
			
			if(shouldSwitch) {
				double[] temp = zxe2DArray[0];
				zxe2DArray[0] = zxe2DArray[1];
				zxe2DArray[1] = temp;
				temp = xyzFirstInters;
				xyzFirstInters = xyzSecondInters;
				xyzSecondInters = temp;
			}
			
			if(returnMiddle) {
				double mainDistance = Math.sqrt(Math.pow(p2x - x2D, 2) + Math.pow(p2y - 0, 2) + Math.pow(p2z - z2D, 2));
				System.out.println("Two point distance: " + mainDistance);
				double inters1p2 = Math.sqrt(Math.pow(p2x - xyzFirstInters[0], 2) + Math.pow(p2y - xyzFirstInters[1], 2) + Math.pow(p2z - xyzFirstInters[2], 2));
				double inters2p2 = Math.sqrt(Math.pow(p2x - xyzSecondInters[0], 2) + Math.pow(p2y - xyzSecondInters[1], 2) + Math.pow(p2z - xyzSecondInters[2], 2));
				double middle = (inters1p2 + inters2p2)/2;
				double middleRatio = middle/mainDistance;
				xyzMiddle = new int[3];
				xyzMiddle[0] = (int) (middleRatio*(x - xo) + xo);
				xyzMiddle[1] = (int) (middleRatio*(y - yo) + yo);
				xyzMiddle[2] = (int) (middleRatio*(z - zo) + zo);
			}
		}
		
		
		
		
		int[][] xy2D = null;
		if(xyzMiddle != null) {
			xy2D = new int[][] {null, null, xyzMiddle};
		} else {
			xy2D = new int[][] {null, null};
		}
		
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
		
		if(xy2D == null) {
//			System.out.println("Inside point: " + calcPoint(x, y, z)[0] + " " + calcPoint(x, y, z)[1]);
//			System.out.println("Outside point: " + calcPoint(xo, yo, zo)[0] + " " + calcPoint(xo, yo, zo)[1]);
		}
		return xy2D;
	}
	
	public int[][] calcLine2D(int[] xy1, int[] xy2) {
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
//		for(int i = 0; i < array.length; i++) {
//			System.out.print(array[i] + " ");
//		}
		System.out.print('\n');
	}
	
	public double calcAngle(int[] xy1, int[] xy2) {
		double xDif = xy2[0] - xy1[0];
		double yDif = xy2[1] - xy1[1];
		double angle = Math.asin(yDif/xDif);
		if(xDif < 0) {
			angle += Math.PI;
		}
		if(xDif == 0) {
			if(yDif > 0) {
				angle = Math.PI/2;
			} else if(yDif < 0) {
				angle = -Math.PI/2;
			}
		}
		return Math.toDegrees(angle);
	}
	
	public double averageAngle(double angle1, double angle2) {
		double average = (angle1 + angle2)/2;
		assert(angle2 - angle1 != 180);
		if(Math.abs(angle2 - angle1) > 180) {
			average -= 180;
		}
		return average;
	}
	
	public int[] calcLine2DWithAngle(int[] xy, double angle) {
		double m = Math.sin(Math.toRadians(angle)); //slope
		double b = (double)xy[1] - m*(double)xy[0]; //y intercept
		int[] intersect = null;
		
		int xSign;
		int ySign;
		
		if(angle > -90 && angle < 90) {
			xSign = POSITIVE;
		} else if((angle > 90 && angle < 270) || (angle < -90 && angle > -270)) {
			xSign = NEGATIVE;
		} else {
			xSign = ZERO;
		}
		
		if((angle > 0 && angle < 180) || (angle < -180 && angle > -360)) {
			ySign = POSITIVE;
		} else if((angle < 0 && angle > -180) || (angle > 180 && angle < 360)) {
			ySign = NEGATIVE;
		} else {
			ySign = ZERO;
		}
			
		if(b >= 0 && b <= height) {
			intersect = new int[] {0, (int)(Math.round(b))};
			if( (xSign == POSITIVE && intersect[0] > xy[0]) || 
				(xSign == NEGATIVE && intersect[0] < xy[0]) || 
				(ySign == POSITIVE && intersect[1] > xy[1]) || 
				(ySign == NEGATIVE && intersect[1] < xy[1])) {
				return intersect;
			}
		}
	
		double y = m*width + b;
		if(y >= 0 && y <= height) {
			intersect = new int[] {width, (int)(Math.round(y))};
			if( (xSign == POSITIVE && intersect[0] > xy[0]) || 
					(xSign == NEGATIVE && intersect[0] < xy[0]) || 
					(ySign == POSITIVE && intersect[1] > xy[1]) || 
					(ySign == NEGATIVE && intersect[1] < xy[1])) {
					return intersect;
				}
		}
	
		double x = -b/m;
		if(x >= 0 && x <= width) {
			intersect = new int[] {(int)(Math.round(x)), 0};
			if( (xSign == POSITIVE && intersect[0] > xy[0]) || 
					(xSign == NEGATIVE && intersect[0] < xy[0]) || 
					(ySign == POSITIVE && intersect[1] > xy[1]) || 
					(ySign == NEGATIVE && intersect[1] < xy[1])) {
					return intersect;
				}
		}
	
		x = (height - b)/m;
		if(x >= 0 && x <= width) {
			intersect = new int[] {(int)(Math.round(x)), height};
			if( (xSign == POSITIVE && intersect[0] > xy[0]) || 
					(xSign == NEGATIVE && intersect[0] < xy[0]) || 
					(ySign == POSITIVE && intersect[1] > xy[1]) || 
					(ySign == NEGATIVE && intersect[1] < xy[1])) {
					return intersect;
				}
		}
		
		return null;
	}
	
	public int[] calcMiddlePoint(int[] xy1, int[] xy2) {
		int[] middleXY = new int[2];
		middleXY[0] = (int)Math.round(((double)xy1[0] + (double)xy2[0])/2);
		middleXY[1] = (int)Math.round(((double)xy1[1] + (double)xy2[1])/2);
		return middleXY;
	}
}

