package main;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

class MyJPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BufferedImage mImage;//image object to be displayed
	private int mSize;//size of panel
	private int[] mRect;
	
	public MyJPanel(int size) {
		mSize = size;
	}
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(mImage!=null)
        {
        	g.drawImage(mImage, 0, 0, mSize, mSize, this);
        }
        if(mRect!=null) {
        	Graphics2D g2d = (Graphics2D) g;
        	g2d.setStroke(new BasicStroke(2));
        	g2d.setColor(Color.BLUE);
        	g2d.drawRect(mRect[0]-(int)(mRect[3]/2), mRect[1]-(int)(mRect[3]/2)-(int)(mRect[2]/2), mRect[3], mRect[3]);
        	g2d.setColor(Color.RED);
        	g2d.drawRect(mRect[0]-(int)(mRect[3]/2), mRect[1]-(int)(mRect[3]/2)+(int)(mRect[2]/2), mRect[3], mRect[3]);
        	
        }
    }
	public void displayImage(BufferedImage i) {
		mImage = i;
		repaint();
	}
	public void displayRectangles(int x, int y, int separation, int size) {
		mRect = new int[]{x,y,separation,size};
		repaint();
	}
}


public class GUI extends JPanel{

	/**
	 * variables
	 */
	private static final long serialVersionUID = 1L;
	
	//constants
	private final int IMG_SIZE = 1024; //Fixed size of GUI
	private final int OFFSET = 60; //Set 20 for Ubuntu-16, set 60 for Windows-10
	private int STEP = 5; //initial step size for coordinate changes
	private final int BIG_STEP = 5; //step size for other text fields
	private final int SMALL_STEP = 1; //finer step size for other text fields
	
	//members
	private double mScaleFactor; //factor that scales image to fit size of GUI
	private Postprocessing mVBPObj; //Single postprocessing object. TODO: make singleton
	private String lastOpenedFile, lastSavedFile; //remember last opened file between loads.
	
	//graphic components
	private JFrame frmVirtualBiprism;
	private JTextField textField_0;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField spinner;
	private JButton btnApply;
	private JCheckBox chckbxNewCheckBox;
	private JCheckBox chckbxObjectStatic;
	private MyJPanel imgPanel;
	private MyJPanel resPanel;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frmVirtualBiprism.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}
	/**
	 * Initialize the contents of the frame.
	 * Contains Action listeners for interaction with menu bar and text fields etc.
	 */
	private void initialize() {
		frmVirtualBiprism = new JFrame();
		frmVirtualBiprism.setTitle("Virtual Biprism");
		frmVirtualBiprism.setResizable(false);
		frmVirtualBiprism.setBounds(100, 100, (int)(IMG_SIZE*1.5), IMG_SIZE+OFFSET);
		frmVirtualBiprism.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		ImageIcon image = new ImageIcon("");
        JLabel imageLabel = new JLabel(image); 
        add(imageLabel);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setToolTipText("sMenu");
		frmVirtualBiprism.setJMenuBar(menuBar);
		
		JMenu mnJmenu = new JMenu("File");
		menuBar.add(mnJmenu);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//on opening the program the standard directory is home, otherwise it is the last successfully opened file
				if(lastOpenedFile==null)
					lastOpenedFile = System.getProperty("user.home").toString();
				
				//Open File dialog
				String s = (String)JOptionPane.showInputDialog(
				                    frmVirtualBiprism,
				                    "Please enter path and filename:",
				                    "Open file",
				                    JOptionPane.PLAIN_MESSAGE,
				                    null,
				                    null,
				                    lastOpenedFile);

				if ((s != null) && (s.length() > 0)) {
						//Assign new Postprocessing Object and load image file
						try {
							if(mVBPObj != null)
								mVBPObj = new Postprocessing(s,mVBPObj.getSettings());
							else
								mVBPObj = new Postprocessing(s);
							System.gc();
							
							if(mVBPObj.getImageSize()[0]<mVBPObj.getImageSize()[1])
								mScaleFactor = (double)IMG_SIZE/(double)mVBPObj.getImageSize()[0];
							else
								mScaleFactor = (double)IMG_SIZE/(double)mVBPObj.getImageSize()[1];
							
							enableEdit();
							recalculate();
							displayRectangles();
							
							//open path:
							lastOpenedFile = s;
						} 
						catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Could not load file (IOException)", "Error", JOptionPane.WARNING_MESSAGE);
						} 
						catch (ImageTooLarge itl) {
							JOptionPane.showMessageDialog(null, "Please choose an input file that is smaller than " + Postprocessing.MAX_IMG_SIZE + " x " +  Postprocessing.MAX_IMG_SIZE + " px!", "Error", JOptionPane.WARNING_MESSAGE);
						}
						catch (ImageFormatNotSupported ifns) {
							JOptionPane.showMessageDialog(null, "Please use a valid file format such as \"" + DataIO.AllowedImageFormat[0] 
									+ "\", \"" + DataIO.AllowedImageFormat[1] + "\", \"" + DataIO.AllowedImageFormat[2] + "\", or the native file format \"" + DataIO.AllowedArrayFormat + "\" !", "Error", JOptionPane.WARNING_MESSAGE);
						}
				    return;
				}
				//If you're here, the return value was null/empty.
			
			}
		});
		mnJmenu.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save Settings");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(mVBPObj!=null){
					String fp = "";
					int i = lastOpenedFile.lastIndexOf('.');
					fp = lastOpenedFile.substring(0,i);
					fp += "_Settings.txt";
					
					
					try{
						PrintWriter out = new PrintWriter(fp);
						
						out.println("Settings for file:" + lastOpenedFile);
						out.println("Saved on " + new java.util.Date());
						out.println();
					    out.println("X: 		" + mVBPObj.getSettings()[0]);
					    out.println("Y: 		" + mVBPObj.getSettings()[1]);
					    out.println("Distance: 	" + mVBPObj.getSettings()[2]);
					    out.println("Size: 		" + mVBPObj.getSettings()[3]);
					    out.println("Angle:		" + mVBPObj.getSettings()[4]);
					    out.println("Cutoff: 	" + mVBPObj.getSettings()[5]);
					    out.println("Offset: 	" + mVBPObj.getSettings()[6]);
					    
					    out.close();
					}
					catch(IOException e) {
					}
				}
			}
		});
		mnJmenu.add(mntmSave);
		
		JMenu mnExport = new JMenu("Export...");
		mnJmenu.add(mnExport);
		
		JMenuItem mntmSourceImage = new JMenuItem("Source Image");
		mntmSourceImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(mVBPObj!=null){
					//if this is the first save, use filename of last opened file without extension
					if(lastSavedFile == null) {
						int i = lastOpenedFile.lastIndexOf('.');
						lastSavedFile = lastOpenedFile.substring(0,i);
					}
					
					String s = (String)JOptionPane.showInputDialog(
		                    frmVirtualBiprism,
		                    "Please enter path and filename:",
		                    "Open file",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    lastSavedFile);
					if ((s != null) && (s.length() > 0)) {
						try {
							mVBPObj.saveSrcImage(s);
							//save path:
							int i = s.lastIndexOf('.');
							lastSavedFile = s.substring(0,i);
						}
						catch(ImageFormatNotSupported ifns) {
							JOptionPane.showMessageDialog(null, "Please choose a valid image format. Supported: bmp, jpg, png ", "Error", JOptionPane.WARNING_MESSAGE); //TODO: generalise
							
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Could not save file (IOException). \n Make sure that the file location exists and that you save in a valid format", "Error", JOptionPane.WARNING_MESSAGE);
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "You need to enter a file location and filename.", "Error", JOptionPane.WARNING_MESSAGE);
						
					}
				}
			}
		});
		mnExport.add(mntmSourceImage);
		
		JMenuItem mntmResultImage = new JMenuItem("Result Image");
		mntmResultImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(mVBPObj!=null){
					
					//if this is the first save, use filename of last opened file without extension
					if(lastSavedFile == null) {
						int i = lastOpenedFile.lastIndexOf('.');
						lastSavedFile = lastOpenedFile.substring(0,i);
					}
					
					String s = (String)JOptionPane.showInputDialog(
		                    frmVirtualBiprism,
		                    "Please enter path and filename:",
		                    "Open file",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    lastSavedFile);
					if ((s != null) && (s.length() > 0)) {
						try {
							mVBPObj.saveImage(s);
							//save path:
							int i = s.lastIndexOf('.');
							lastSavedFile = s.substring(0,i);
						}
						catch(ImageFormatNotSupported ifns) {
							JOptionPane.showMessageDialog(null, "Please choose a valid image format. Supported: bmp, jpg, png ", "Error", JOptionPane.WARNING_MESSAGE); //TODO: generalise
							
						}
						catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Could not save file (IOException). \n Make sure that the file location exists and that you save in a valid format", "Error", JOptionPane.WARNING_MESSAGE);
						}
					}
					else {
						JOptionPane.showMessageDialog(null, "You need to enter a file location and filename.", "Error", JOptionPane.WARNING_MESSAGE);
						
					}
				}
			}
		});
		mnExport.add(mntmResultImage);
		
		JSeparator separator_1 = new JSeparator();
		mnJmenu.add(separator_1);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
			    System.exit(0);
			}
		});
		mnJmenu.add(mntmQuit);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAksQuestion = new JMenuItem("Get help...");
		mntmAksQuestion.addMouseListener(new MouseAdapter() {
			@Override
			//Help
			public void mousePressed(MouseEvent e) {
				JOptionPane.showMessageDialog(null, "Ask Tore!", "Help", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		JMenuItem mntmImageInfo = new JMenuItem("Image Info");
		mnHelp.add(mntmImageInfo);
		mntmImageInfo.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				if(mVBPObj!=null) {
					JOptionPane.showMessageDialog(null, 
							"Image Size: " + mVBPObj.getImageSize()[0] + "x" + mVBPObj.getImageSize()[1] + " px.", 
							"Image Info", JOptionPane.NO_OPTION);
				}
			}
		});
		mnHelp.add(mntmAksQuestion);
		frmVirtualBiprism.getContentPane().setLayout(null);
		
		imgPanel = new MyJPanel(IMG_SIZE);
		imgPanel.setBackground(Color.GRAY);
		imgPanel.setBounds(0, 0, IMG_SIZE, IMG_SIZE);
		frmVirtualBiprism.getContentPane().add(imgPanel);
		
		resPanel = new MyJPanel((int)(IMG_SIZE/2));
		resPanel.setBackground(Color.LIGHT_GRAY);
		resPanel.setBounds(IMG_SIZE, 0, (int)(IMG_SIZE/2), (int)(IMG_SIZE/2));
		frmVirtualBiprism.getContentPane().add(resPanel);
		
		JLabel lblBoxCoordinates = new JLabel("Box Coordinates:");
		lblBoxCoordinates.setBounds(787, 462, 121, 15);
		frmVirtualBiprism.getContentPane().add(lblBoxCoordinates);
		
		JLabel lblBoxSize = new JLabel("Box Distance:");
		lblBoxSize.setBounds(787, 500, 121, 15);
		frmVirtualBiprism.getContentPane().add(lblBoxSize);
		
		JLabel lblBoxDistance = new JLabel("Box Size:");
		lblBoxDistance.setBounds(787, 538, 106, 15);
		frmVirtualBiprism.getContentPane().add(lblBoxDistance);
		
		JLabel lblRotation = new JLabel("Biprism Rotation:");
		lblRotation.setBounds(787, 592, 129, 15);
		frmVirtualBiprism.getContentPane().add(lblRotation);
		
		JLabel lblCutoff = new JLabel("Phase Cutoff:");
		lblCutoff.setBounds(787, 641, 106, 15);
		frmVirtualBiprism.getContentPane().add(lblCutoff);
		
		JLabel lblPhaseOffset = new JLabel("Phase Offset");
		lblPhaseOffset.setBounds(786, 668, 122, 15);
		frmVirtualBiprism.getContentPane().add(lblPhaseOffset);
		
		textField_0 = new JTextField();
		textField_0.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					if(e.getKeyCode()==KeyEvent.VK_LEFT)
						mVBPObj.setCentreX(mVBPObj.getSettings()[0]-STEP);
					else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
						mVBPObj.setCentreX(mVBPObj.getSettings()[0]+STEP);
					else if(e.getKeyCode()==KeyEvent.VK_UP) {
						mVBPObj.setCentreY(mVBPObj.getSettings()[1]-STEP);
						textField_1.requestFocusInWindow();
					}
					else if(e.getKeyCode()==KeyEvent.VK_DOWN) {
						mVBPObj.setCentreY(mVBPObj.getSettings()[1]+STEP);
						textField_1.requestFocusInWindow();
					}
					else
						return;
					textField_0.setText(String.valueOf(mVBPObj.getSettings()[0]));
					textField_1.setText(String.valueOf(mVBPObj.getSettings()[1]));
					displayRectangles();
				}
				catch(OutsideOfDomain a) {
					//do nothing
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		
		textField_0.setEnabled(false);
		textField_0.setBounds(963, 462, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_0);
		textField_0.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					if(e.getKeyCode()==KeyEvent.VK_LEFT) {
						mVBPObj.setCentreX(mVBPObj.getSettings()[0]-STEP);
						textField_0.requestFocusInWindow();
					}
					else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
						mVBPObj.setCentreX(mVBPObj.getSettings()[0]+STEP);
						textField_0.requestFocusInWindow();
					}
					else if(e.getKeyCode()==KeyEvent.VK_UP)
						mVBPObj.setCentreY(mVBPObj.getSettings()[1]-STEP);
					else if(e.getKeyCode()==KeyEvent.VK_DOWN)
						mVBPObj.setCentreY(mVBPObj.getSettings()[1]+STEP);
					else
						return;
					textField_0.setText(String.valueOf(mVBPObj.getSettings()[0]));
					textField_1.setText(String.valueOf(mVBPObj.getSettings()[1]));
					displayRectangles();
				}
				catch(OutsideOfDomain a) {
					//do nothing
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		textField_1.setEnabled(false);
		textField_1.setColumns(10);
		textField_1.setBounds(1036, 462, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_1);
		
		textField_2 = new JTextField();
		textField_2.addKeyListener(new KeyAdapter() {
			@Override
			 public void keyPressed(KeyEvent e) {
				try {
					if(e.getKeyCode()==KeyEvent.VK_LEFT) {
						mVBPObj.setBoxDist(mVBPObj.getSettings()[2]-BIG_STEP);
					}
					else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
						mVBPObj.setBoxDist(mVBPObj.getSettings()[2]+BIG_STEP);
					}
					else if(e.getKeyCode()==KeyEvent.VK_UP)
						mVBPObj.setBoxDist(mVBPObj.getSettings()[2]+SMALL_STEP);
					else if(e.getKeyCode()==KeyEvent.VK_DOWN)
						mVBPObj.setBoxDist(mVBPObj.getSettings()[2]-SMALL_STEP);
					else
						return;
					textField_2.setText(String.valueOf(mVBPObj.getSettings()[2]));
					textField_1.setText(String.valueOf(mVBPObj.getSettings()[1]));
					displayRectangles();
				}
				catch(OutsideOfDomain a) {
					//do nothing
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		textField_2.setEnabled(false);
		textField_2.setBounds(963, 498, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_2);
		textField_2.setColumns(10);
		
		textField_3 = new JTextField();
		textField_3.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					if(e.getKeyCode()==KeyEvent.VK_LEFT) {
						mVBPObj.setBoxSize(mVBPObj.getSettings()[3]-BIG_STEP);
					}
					else if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
						mVBPObj.setBoxSize(mVBPObj.getSettings()[3]+BIG_STEP);
					}
					else if(e.getKeyCode()==KeyEvent.VK_UP)
						mVBPObj.setBoxSize(mVBPObj.getSettings()[3]+SMALL_STEP);
					else if(e.getKeyCode()==KeyEvent.VK_DOWN)
						mVBPObj.setBoxSize(mVBPObj.getSettings()[3]-SMALL_STEP);
					else
						return;
					
					displayRectangles();
					textField_3.setText(String.valueOf(mVBPObj.getSettings()[3]));
				}
				catch(OutsideOfDomain a) {
					//do nothing
				}
				catch(TooSmall b) {
					//do nothing
				}
				catch(ImageTooLarge c) {
					JOptionPane.showMessageDialog(null, "Maximum box size is " + Postprocessing.MAX_BOX_SIZE + "!", "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		textField_3.setEnabled(false);
		textField_3.setColumns(10);
		textField_3.setBounds(963, 536, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_3);
		
		textField_4 = new JTextField();
		textField_4.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_LEFT)
					mVBPObj.setRotAngle(mVBPObj.getSettings()[4]-BIG_STEP);
				else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
					mVBPObj.setRotAngle(mVBPObj.getSettings()[4]+BIG_STEP);
				else if(e.getKeyCode()==KeyEvent.VK_DOWN)
					mVBPObj.setRotAngle(mVBPObj.getSettings()[4]-SMALL_STEP);
				else if(e.getKeyCode()==KeyEvent.VK_UP)
					mVBPObj.setRotAngle(mVBPObj.getSettings()[4]+SMALL_STEP);
				else						
					return;
				textField_4.setText(String.valueOf(mVBPObj.getSettings()[4]));
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		textField_4.setEnabled(false);
		textField_4.setColumns(10);
		textField_4.setBounds(963, 590, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_4);
		
		textField_5 = new JTextField();
		textField_5.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					if(e.getKeyCode()==KeyEvent.VK_LEFT)
						mVBPObj.setCutoff(mVBPObj.getSettings()[5]-BIG_STEP);
					else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
						mVBPObj.setCutoff(mVBPObj.getSettings()[5]+BIG_STEP);
					else if(e.getKeyCode()==KeyEvent.VK_DOWN)
						mVBPObj.setCutoff(mVBPObj.getSettings()[5]-SMALL_STEP);
					else if(e.getKeyCode()==KeyEvent.VK_UP)
						mVBPObj.setCutoff(mVBPObj.getSettings()[5]+SMALL_STEP);
					else						
						return;
					textField_5.setText(String.valueOf(mVBPObj.getSettings()[5]));
				}
				catch(TooSmall a) {
					//do nothing...
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		textField_5.setEnabled(false);
		textField_5.setColumns(10);
		textField_5.setBounds(963, 639, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_5);
		
		textField_6 = new JTextField();
		textField_6.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_LEFT)
					mVBPObj.setOffset(mVBPObj.getSettings()[6]-BIG_STEP);
				else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
					mVBPObj.setOffset(mVBPObj.getSettings()[6]+BIG_STEP);
				else if(e.getKeyCode()==KeyEvent.VK_DOWN)
					mVBPObj.setOffset(mVBPObj.getSettings()[6]-SMALL_STEP);
				else if(e.getKeyCode()==KeyEvent.VK_UP)
					mVBPObj.setOffset(mVBPObj.getSettings()[6]+SMALL_STEP);
				else						
					return;
				textField_6.setText(String.valueOf(mVBPObj.getSettings()[6]));
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				mVBPObj.getResult();
				displayImages();
				displayRectangles();
			}
		});
		textField_6.setEnabled(false);
		textField_6.setColumns(10);
		textField_6.setBounds(963, 666, 61, 19);
		frmVirtualBiprism.getContentPane().add(textField_6);
		
		spinner = new JTextField();
		spinner.setBackground(Color.WHITE);
		spinner.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_LEFT) {
					if(Integer.parseInt(spinner.getText())<1) {
						STEP = 1;
					}
					else {
						STEP--;
					}
					spinner.setText(String.valueOf(STEP));
				}
				else if(e.getKeyCode()==KeyEvent.VK_RIGHT){
						STEP++;
						spinner.setText(String.valueOf(STEP));
				}
				else if(e.getKeyCode()==KeyEvent.VK_DOWN){
						if(Integer.parseInt(spinner.getText())<1) {
							STEP = 1;
						}
						else {
							STEP--;
						}
						spinner.setText(String.valueOf(STEP));
				}
				else if(e.getKeyCode()==KeyEvent.VK_UP){
						STEP++;
						spinner.setText(String.valueOf(STEP));
				}
				else if(e.getKeyCode()==KeyEvent.VK_ENTER||e.getKeyCode()==KeyEvent.VK_TAB){
					textField_0.requestFocusInWindow();
				}
				
				try {
					STEP = Integer.parseInt(spinner.getText());
				}
				catch(NumberFormatException n){
					STEP = BIG_STEP;
				}
				
			}
		});
		spinner.setText("2");
		spinner.setEnabled(false);
		spinner.setBounds(1112, 462, 28, 20);
		frmVirtualBiprism.getContentPane().add(spinner);
		
		
		
		chckbxNewCheckBox = new JCheckBox("normalise");
		chckbxNewCheckBox.setEnabled(false);
		chckbxNewCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				mVBPObj.mNormalize=(arg0.getStateChange()==1)?true:false;
				recalculate();
				textField_5.requestFocusInWindow();
			}
		});
		chckbxNewCheckBox.setBounds(1032, 637, 129, 23);
		//frmVirtualBiprism.getContentPane().add(chckbxNewCheckBox);
		
		chckbxObjectStatic = new JCheckBox("object static");
		chckbxObjectStatic.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				mVBPObj.staticObject=(e.getStateChange()==1)?true:false;
				textField_2.requestFocusInWindow();
			}
		});
		chckbxObjectStatic.setEnabled(false);
		chckbxObjectStatic.setBounds(1032, 496, 129, 23);
		frmVirtualBiprism.getContentPane().add(chckbxObjectStatic);
		
		
		btnApply = new JButton("Apply");
		btnApply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				if(btnApply.isEnabled()) {
					recalculate();
				}
			}
		});
		btnApply.setEnabled(false);
		btnApply.setBounds(1023, 710, 117, 25);
		frmVirtualBiprism.getContentPane().add(btnApply);
		//Press the aplly button when enter is pressed on keyboard:
		frmVirtualBiprism.getRootPane().setDefaultButton(btnApply);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(780, 486, 360, 2);
		frmVirtualBiprism.getContentPane().add(separator);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(780, 619, 360, 2);
		frmVirtualBiprism.getContentPane().add(separator_2);
		
	}
	/**
	 * Update values of the Postprocessing object and get new result
	 */
	
	private void recalculate(){
		assert(mVBPObj!=null);
		try {
			mVBPObj.setCentreX(Integer.parseInt(textField_0.getText()));
			mVBPObj.setCentreY(Integer.parseInt(textField_1.getText()));
			mVBPObj.setBoxDist(Integer.parseInt(textField_2.getText()));
			mVBPObj.setBoxSize(Integer.parseInt(textField_3.getText()));
			mVBPObj.setRotAngle(Integer.parseInt(textField_4.getText()));
			mVBPObj.setCutoff(Integer.parseInt(textField_5.getText()));
			mVBPObj.setOffset(Integer.parseInt(textField_6.getText()));
			mVBPObj.getResult();
		}
		catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Wrong format in one of the boxes! " + nfe.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
		}
		catch(OutsideOfDomain a){
			JOptionPane.showMessageDialog(null, "The last number you entered places one the boxes outside of the domain!" + a.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
		}
		catch(TooSmall i){
			JOptionPane.showMessageDialog(null, "Size and Cutoff must be greater than 0!", "Error", JOptionPane.WARNING_MESSAGE);
		}
		catch(ImageTooLarge c) {
			JOptionPane.showMessageDialog(null, "Maximum box size is " + Postprocessing.MAX_BOX_SIZE + "!", "Error", JOptionPane.WARNING_MESSAGE);
		}
		displayImages();
	}
	/**
	 * Draw the images on the JPanel
	 */
	private void displayImages() {
		imgPanel.displayImage(mVBPObj.getCutImage());
		resPanel.displayImage(mVBPObj.getResImage());
	}
	/**
	 * Draw the boxes onto the JPanel
	 */
	private void displayRectangles() {
		imgPanel.displayRectangles((int)(mScaleFactor*mVBPObj.getSettings()[0]), (int)(mScaleFactor*mVBPObj.getSettings()[1]), 
				(int)(mScaleFactor*mVBPObj.getSettings()[2]), (int)(mScaleFactor*mVBPObj.getSettings()[3]));
	}
	/**
	 * When the file has loaded successfully, enable all interactive elements
	 */
	private void enableEdit() {
		assert(mVBPObj!=null);
		
		textField_0.setText(String.valueOf(mVBPObj.getSettings()[0]));
		textField_1.setText(String.valueOf(mVBPObj.getSettings()[1]));
		textField_2.setText(String.valueOf(mVBPObj.getSettings()[2]));
		textField_3.setText(String.valueOf(mVBPObj.getSettings()[3]));
		textField_4.setText(String.valueOf(mVBPObj.getSettings()[4]));
		textField_5.setText(String.valueOf(mVBPObj.getSettings()[5]));
		textField_6.setText(String.valueOf(mVBPObj.getSettings()[6]));
		textField_0.requestFocusInWindow();
		
		textField_0.setEnabled(true);
		textField_1.setEnabled(true);
		textField_2.setEnabled(true);
		textField_3.setEnabled(true);
		textField_4.setEnabled(true);
		textField_5.setEnabled(true);
		textField_6.setEnabled(true);
		spinner.setEnabled(true);
		btnApply.setEnabled(true);
		chckbxNewCheckBox.setEnabled(true);		
		chckbxObjectStatic.setEnabled(true);
	}
	
}
