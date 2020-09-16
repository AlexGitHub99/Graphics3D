import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

public class Graphics3D implements KeyListener, MouseInputListener{
	
	Robot robot = null;
	ArrayList<Form> forms = new ArrayList<Form>();
	JFrame frame = new JFrame("Graphics");
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
	boolean playing = true;
	long lastTime = 0;
	
	public Graphics3D() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		frame.add(screen);
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(this);
		frame.addMouseMotionListener(this);
		frame.setVisible(true);
		BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		screen.setCursor(blankCursor);
		
		screen.setPlayerHeight(playerHeight);
		screen.setFOV(150);
		playerPos[0] = 0;
		playerPos[1] = 0;
		playerPos[2] = 0;
		updatePlayerPos();
		updatePlayerYaw(45);
		updatePlayerPitch(0);
		forms.add(new Cube(100, 25, 100, 50)); //adds cube at x100, y0, z100 with size 50
		forms.add(new Cube(100, 25, 300, 50)); //adds cube at x100, y0, z300 with size 50
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
		} else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			space = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void start() {
		lastTime = java.lang.System.currentTimeMillis();
		while(true) {
			double time = (java.lang.System.currentTimeMillis() - lastTime)/1000.0;
			lastTime = java.lang.System.currentTimeMillis();
			
			if(space == true && playerPos[1] <= 0) {
				velocity += 5;
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
			screen.repaint();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
			int dMouseX = e.getX() - mouseX;
			int dMouseY = e.getY() - mouseY;
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
			updatePlayerYaw(newYaw);
			updatePlayerPitch(newPitch);
			screen.repaint();
			robot.mouseMove(width/2, height/2);
			mouseX = width/2;
			mouseY = height/2;
		}
	}
}
