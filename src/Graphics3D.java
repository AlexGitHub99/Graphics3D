import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.MouseInputListener;

public class Graphics3D implements KeyListener, MouseInputListener{
	Robot robot = null;
	ArrayList<Form> forms = new ArrayList<Form>();
	JFrame frame = new JFrame("Graphics");
	JFrame info = new JFrame("Info");
	ArrayList<JLabel> labels = new ArrayList<JLabel>();
	int width = 1000;
	int height = 1000;
	Screen screen = new Screen(width, height);
	double[] playerPos = new double[3];
	int playerYaw;
	int playerPitch;
	int playerHeight = 80;
	int speed = 5; // m/s
	int sensitivity = 30;
	double velocity = 0; // m/s
	double g = -9.8; // m/s^2
	boolean forward = false;
	boolean backward = false;
	boolean left = false;
	boolean right = false;
	boolean space = false;
	int mouseX;
	int mouseY;
	int dMouseX = 0;
	int dMouseY = 0;
	boolean playing = true;
	long lastTime = 0;
	
	public Graphics3D() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		info.setSize(500, 500);
		info.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		info.setLocation(width, 0);
		info.setLayout(new GridLayout(10, 0));
		//create all the labels to be displayed in the info window in an ArrayList and add them to the info window
		addLabels(); 
		
		info.setVisible(true);
		
		frame.add(screen);
		frame.setSize(width + 50, height + 50);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(this);
		frame.addMouseMotionListener(this);
		frame.setVisible(true);
		BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		screen.setCursor(blankCursor);
		
		screen.setPlayerHeight(playerHeight);
		screen.setFOV(110);
		playerPos[0] = 539;
		playerPos[1] = 0;
		playerPos[2] = 624;
		updatePlayerPos();
		updatePlayerYaw(172);
		updatePlayerPitch(0);
//		playerPos[0] = 0;
//		playerPos[1] = 0;
//		playerPos[2] = 0;
//		updatePlayerPos();
//		updatePlayerYaw(45);
//		updatePlayerPitch(0);
//		forms.add( new RectPrism(100, 100, 100, 50, 50, 500) );
		forms.add( new Plane(500, 100, 1000, 300, -800));
		screen.setForms(forms);
		screen.repaint();
//		for(int i = -180; i < 150; i++) {
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			playerPos[0] = i;
//			playerPos[1] = i;
//			screen.setPlayerPos(playerPos);
//			screen.repaint();
//		}
		
		start();
	}
	
	public void updatePlayerYaw(int newPlayerYaw) {
		playerYaw = newPlayerYaw;
		screen.setPlayerYaw(playerYaw);
	}
	
	public void updatePlayerPitch(int newPlayerPitch) {
		playerPitch = newPlayerPitch;
		screen.setPlayerPitch(playerPitch);
	}
	
	public void updatePlayerPos() {
		screen.setPlayerPos(playerPos[0], playerPos[1], playerPos[2]);
	}
	
	public static void main(String[] args) {
		new Graphics3D();
	}
	
	public void start() {
		lastTime = java.lang.System.currentTimeMillis();
//		while(true) {
//			if(playing == true) {
//				System.out.println("playing true");
//			}
//		}
		while(true) {
			//this line should do absolutely nothing but for some reason it makes the program work after escaping in and out
//			System.out.println("random print");
			if(playing == true) {
				double time = (java.lang.System.currentTimeMillis() - lastTime)/1000.0;
				lastTime = java.lang.System.currentTimeMillis();
				
				//process mouse input
				int newYaw = playerYaw - dMouseX*sensitivity/100;
				if(newYaw > 180) {
					newYaw = newYaw % 180 - 180;
				} else if(newYaw < -180) {
					newYaw = newYaw % 180 + 180;
				}
				int newPitch = playerPitch - dMouseY*sensitivity/100;
				if(newPitch > 180) {
					newPitch = newPitch % 180 - 180;
				} else if(newPitch < -180){
					newPitch = newPitch % 180 + 180;
				}
				
				if(newPitch > 90) {
					newPitch = 90;
				} else if(newPitch < -90) {
					newPitch = -90;
				}
				
				updatePlayerYaw(newYaw);
				updatePlayerPitch(newPitch);
				dMouseX = 0;
				dMouseY = 0;
				
				//process keyboard input
				if(space == true && playerPos[1] <= 0) {
					velocity += 20;
				}
				
				double pixelChange = speed*time*50;
				if(forward == true && left == true) {
					playerPos[0] += (Math.cos(Math.toRadians(playerYaw))*pixelChange + Math.cos(Math.toRadians(playerYaw + 90))*pixelChange)/2;
					playerPos[2] += (Math.sin(Math.toRadians(playerYaw))*pixelChange + Math.sin(Math.toRadians(playerYaw + 90))*pixelChange)/2;
				} else if(forward == true && right == true) {
					playerPos[0] += (Math.cos(Math.toRadians(playerYaw))*pixelChange + Math.cos(Math.toRadians(playerYaw - 90))*pixelChange)/2;
					playerPos[2] += (Math.sin(Math.toRadians(playerYaw))*pixelChange + Math.sin(Math.toRadians(playerYaw - 90))*pixelChange)/2;
				} else if(backward == true && left == true) {
					playerPos[0] += (-Math.cos(Math.toRadians(playerYaw))*pixelChange + Math.cos(Math.toRadians(playerYaw + 90))*pixelChange)/2;
					playerPos[2] += (-Math.sin(Math.toRadians(playerYaw))*pixelChange + Math.sin(Math.toRadians(playerYaw + 90))*pixelChange)/2;
				} else if(backward == true && right == true) {
					playerPos[0] += (-Math.cos(Math.toRadians(playerYaw))*pixelChange + Math.cos(Math.toRadians(playerYaw - 90))*pixelChange)/2;
					playerPos[2] += (-Math.sin(Math.toRadians(playerYaw))*pixelChange + Math.sin(Math.toRadians(playerYaw - 90))*pixelChange)/2;
				} else if(forward == true) {
					playerPos[0] += Math.cos(Math.toRadians(playerYaw))*pixelChange;
					playerPos[2] += Math.sin(Math.toRadians(playerYaw))*pixelChange;
				} else if(backward == true) {
					playerPos[0] += -Math.cos(Math.toRadians(playerYaw))*pixelChange;
					playerPos[2] += -Math.sin(Math.toRadians(playerYaw))*pixelChange;
				} else if(left == true) {
					playerPos[0] += Math.cos(Math.toRadians(playerYaw + 90))*pixelChange;
					playerPos[2] += Math.sin(Math.toRadians(playerYaw + 90))*pixelChange;
				} else if(right == true) {
					playerPos[0] += Math.cos(Math.toRadians(playerYaw - 90))*pixelChange;
					playerPos[2] += Math.sin(Math.toRadians(playerYaw - 90))*pixelChange;
				}
				
				if(playerPos[1] > 0 || velocity != 0) {
					double distance = 0.5*g*Math.pow(time, 2) + velocity*time; //d = 0.5*a*t^2 + u*t
					velocity += g*time; //v = at
					playerPos[1] += distance*50; //50 pixels per meter
				}
				if(playerPos[1] <= 0) {
					playerPos[1] = 0;
					velocity = 0;
				}
				
				updatePlayerPos();
				
				//REPAINT screen and update info window
				screen.repaint();
				ArrayList<String> labelStrings = screen.getInfoLabels();
				int i = 0;
				if(labelStrings != null) {
					for(i = 0; i < labelStrings.size(); i++) {
						labels.get(i).setText(labelStrings.get(i));
					}
				}
				labels.get(i).setText("Player X: " + Math.round(playerPos[0]));
				labels.get(i + 1).setText("Player Y: " + Math.round(playerPos[1]));
				labels.get(i + 2).setText("Player Z: " + Math.round(playerPos[2]));
				labels.get(i + 3).setText("Player Yaw: " + Math.round(playerYaw));
				labels.get(i + 4).setText("Player Pitch: " + Math.round(playerPitch));
				labels.get(i + 5).setText("Player Verticle Vel: " + Math.round(velocity));
				//delay
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addLabels() {
		ArrayList<String> labelStrings = screen.getInfoLabels();
		if(labelStrings != null) {
			for(int i = 0; i < labelStrings.size(); i++) {
				labels.add(new JLabel(labelStrings.get(i)));
				labels.get(i).setFont(new Font("Consolas", 0, 15));
				info.add(labels.get(i));
			}
		}
		for(int i = 0; i < 6; i++ ) { //iterations is the number of variables set in the main loop
			labels.add(new JLabel());
			labels.get(labels.size() - 1).setFont(new Font("Consolas", 0, 15));
			info.add(labels.get(labels.size() - 1));
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_W) {
			forward = true;
		} else if(e.getKeyCode() == KeyEvent.VK_S) {
			backward = true;
		} else if(e.getKeyCode() == KeyEvent.VK_A) {
			left = true;
		} else if(e.getKeyCode() == KeyEvent.VK_D) {
			right = true;
		} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			space = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode() == KeyEvent.VK_W) {
			forward = false;
		} else if(e.getKeyCode() == KeyEvent.VK_S) {
			backward = false;
		} else if(e.getKeyCode() == KeyEvent.VK_A) {
			left = false;
		} else if(e.getKeyCode() == KeyEvent.VK_D) {
			right = false;
		} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			playing = !playing;
			System.out.println(playing);
		} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			space = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		if(playing == true) {
			dMouseX += e.getX() - mouseX;
			dMouseY += e.getY() - mouseY;
			
			robot.mouseMove(width/2, height/2);
			mouseX = width/2;
			mouseY = height/2;
		}
	}
}
