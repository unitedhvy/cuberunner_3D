//Uttam Suryadevara
//May 24, 2013
//Game.java
/*

The Final Project - Make Your Own Educational Game

CubeRunner 3D
	The game involves the user navigating through a 3D space, dodging cubes (similar to 
the game CubeRunner, but with an added ability to move up and down in addition to left and 
right - a first the x dimension, then depth, or z, and in CubeRunner 3D, also the y) 
	Questions appear at the top right of the screen, and the user is not required to answer
them, but does recieve a large bonus in points for answering, making question-answering 
more lucrative than navigating cubes.
	Though the game is not divided into distinct levels, the timer implementation allows
the game to incrementally get harder as the user progresses.
	Further instructions are avaliable in-game.
	
*/


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;//Key, Action, Change, Timer events
import java.util.ArrayList;//Used to store the current set of Cubes
import java.util.Iterator;//Iterators to move through the ArrayList
import javax.swing.event.*;//Key, Action, Change, Timer events
import java.awt.image.BufferedImage;//for Resizing
import javax.imageio.*; //for Image, ImageIO

//for files and IO
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class Game {
		
	JFrame frame ; // the frame that contains the entire game
	ArrayList<Cube> cubeList = new ArrayList<Cube>(); //an array of
	//the class cube, later described
	
	JPanel mP, pP, eP;
	
	Cube c = new Cube(50,50,200);//a "cube," displayed on screen, with the
	//xlocation, ylocation, and depth on screen
	
	CardLayout screens;
	
	//play related variables
	//set defaults here(1-100), can be changed in options
	double depthmod = 50; //the depth at which the cubes disappear
	double spawnrate = 10; //the rate at which cubes are initially produced
	int spawnarea = 30; //the area in the 3D space in which the cubes are produced
	int focaldepth = 40;//the user's "depth" from the screen
	int cubeSpeed = 10; //the speed of the cubes
	double moveSpeed = 30; //speed of the user 
	int warningdepth = 99; //the depth at which the cubes change color to
	//warn of close distance
	
	//crash related variables
	int crashsizex = 150; //the width around the user that is considered a crash
	int crashsizey = 30; //the height around the user that is considered a crash
	int crashoriginx = 600; //the center of the crash location
	int crashoriginy = 410; //the center of the crash location
	int crashlimit = 70; //the maximum number of crashes
	int crashes = 0;//number of crashes
	
	//question related variables
	int questionpoints = 0;// number of points earned from questions,
	//+10 per right answer, -5 for wrong answer
	int numofquestions = 22;//the number of questions
	String [][] qanda = new String [2][numofquestions]; //a string array containing
	//[0][x](questions), and [1][x](answers)
	int questionnumber;//the question being asked currently.
	
	char color = 'g';//the current color of the user's ship, or 
	//avatar; 'g' for green, 'r' for red; the avatar turns red for
	//a little while after a crash
	int hiscore = 0;//the hiscore
	boolean w, a, s, d;//booleans for the current key pressed
	int endscore = 0; //the final score
	
	//time variables
	double increment = .1;//the time increment
	double currenttime = 0;//the current time
	double crashtime = 1000000;//the time of the latest crash; will
	//be compared to current time to decide when to turn the avatar
	//back to green, so has initially been set to an arbitrarily 
	//large number
	
	//Text displayed to the user
	JTextField score;//the user's score displayed in a TextField
	JTextField messageline3; //the third message in the end game panel
	JTextArea question; //a text area to display the current question
	JTextArea status; //a text area to display the current status
	JTextArea total; //a text area for the current total points
	
	//I/O declarations
	Image image;//the image that is displayed on the menu screen
	private File inputfile; //File with questions and answers
	private Scanner input; //Scanner for the file
	private File hiscorefile; //File with questions and answers
	private Scanner hiscoreinput; //Scanner for the file
	private PrintWriter hiscoreoutput; //Writer to write out the (possibly) new hiscore
	
	
	
	
	public Game() { //sets up main frame, a CardLayout, 3 "cards", menu,
	//game and exit, also initializes several variables
			frame = new JFrame(); //the frame
			frame.setBounds(10,0,1420,830); //the size and location,
			//slightly larger to compensate for edges
			frame.setDefaultCloseOperation(3);
			frame.setVisible(true); //make visible
			frame.setLayout(new BorderLayout()); //set BorderLayout
			//to integrate the game and questions on same frame
			screens = new CardLayout();
			frame.setLayout(screens);
			
			mP = new menuPanel();
			mP.setFocusable(true);
			mP.requestFocusInWindow();//get focus, so the user's 
			//keystrokes are recorded
			
			GetMyImage();//moves the png into an Image file
			mP.addKeyListener(new spacebarPressedListener());//add a "spacebar listener"
			pP = new playPanel();//the panel that contains the game and questions
			eP = new endPanel();//the ending panel
			
			frame.add (mP, "menuPanel");//a panel with the menu and instructions
			frame.add (pP, "playPanel");//panel with game
			frame.add (eP, "endPanel");//panel with final score
			
			cubeList.add(c);//add the initial cube
			w = a = s = d = false;//set all the keys to "not pressed"                
	}//end public Game
			
	public static void main (String[]args) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					new Game();
				}
			}
		);//makes the program threadsafe
	} //end main
	
	
	class menuPanel extends JPanel { //the initial panel, with instructions and options
		public menuPanel(){
			setLayout(new BorderLayout());
			options oP = new options(); //make a new optionspanel
			add(oP, BorderLayout.SOUTH); //add the options to the bottom
			oP.setSize(800, 100);
		} // end public menuPanel
		
		public void paintComponent ( Graphics g ) {
			super.paintComponent ( g );
			g.drawImage(image, 0, 0, 1450, 700, this);//draw the image
		} //end paintComponent
	}//end menuPanel
	
	public void GetMyImage() { //moves the .png file to a useable Image file
		try {	image = ImageIO.read(new File("MenuPic.png"));	// try to
		//get the image, the % allows the pictures to loop
		} catch (IOException e) {
			System.err.println("Error: File not Found - " + "MenuPic.png");
			System.exit(1); //check for an error
		}				
	} //end GetMyImage
	
	class options extends JPanel { //allow modification of a group of settings via a 
	//menu and a single slider
		public JSlider optionSlider;// a general editing slider
		public JTextArea currentlyEditing;// the option currently being edited			
		public JRadioButtonMenuItem fd, cs, ys, wd, sd, rs, as;//options for each of the
		//different possible settings			

		public options() {
			
			//setBackground(Color.black);

			JMenu adjustMenu = new JMenu("Adjust Options");
			
			//add an option for each setting
			fd = new JRadioButtonMenuItem("Focal Depth");
			cs = new JRadioButtonMenuItem("Cube Speed");
			ys = new JRadioButtonMenuItem("Your Speed");
			wd = new JRadioButtonMenuItem("Warning Depth");
			sd = new JRadioButtonMenuItem("Crash Depth");
			rs = new JRadioButtonMenuItem("Spawn Rate");
			as = new JRadioButtonMenuItem("Spawn Area");

			//add the choices to a buttongroup so only one can be selected at a time
			ButtonGroup group = new ButtonGroup();
			group.add(fd);
			group.add(cs);
			group.add(ys);
			group.add(wd);
			group.add(sd);
			group.add(rs);
			group.add(as);
			
			//add listeners to all to detect changes
			fd.addActionListener(new OptionMenuListener());
			cs.addActionListener(new OptionMenuListener());
			ys.addActionListener(new OptionMenuListener());	
			wd.addActionListener(new OptionMenuListener());
			sd.addActionListener(new OptionMenuListener());
			rs.addActionListener(new OptionMenuListener());
			as.addActionListener(new OptionMenuListener());
			
			//add the buttons to a group menu	
			adjustMenu.add(fd);
			adjustMenu.add(cs);
			adjustMenu.add(ys);
			adjustMenu.add(wd);
			adjustMenu.add(sd);
			adjustMenu.add(rs);
			adjustMenu.add(as);

			JMenuBar menus = new JMenuBar();//a menubar to hold the menu
			menus.add(adjustMenu);//add the menu to the menubar
			add(menus);//add the menubar				
			
			optionSlider = new JSlider (1,100);//set max/min
			optionSlider.setMajorTickSpacing(20);//set ticks
			optionSlider.setPaintTicks(true);//drawt the ticks
			optionSlider.setPaintLabels(true);//draw the labels
			optionSlider.addChangeListener(new OptionSliderListener());
			add(optionSlider);// add the slider
			
			JTextArea aLabel = new JTextArea("        Currently Editing:        ");
			add(aLabel);// add a text area that says "Currently Editing:"
			aLabel.setEditable(false);//non-user editable
			
			currentlyEditing = new JTextArea("Nothing");
			add(currentlyEditing);//add a text area that indicates the currently modified setting
			currentlyEditing.setEditable(false);//non-user editable
								
		}//end public options
				
		@SuppressWarnings("unchecked")//delete unnecessary warnings
		
		class OptionMenuListener implements ActionListener { //the listener for selection in the menu
			public void actionPerformed (ActionEvent e) {
				//set the slider to the value of the selected setting
				//set the "currently editing" box to the selected setting
				if (fd.isSelected()) {
					optionSlider.setValue((int)(focaldepth));
					currentlyEditing.setText("Focal Depth");
				}	
				if (cs.isSelected()) {
					optionSlider.setValue((int)(cubeSpeed));
					currentlyEditing.setText("Cube Speed");
				}	
				if (ys.isSelected()) {
					optionSlider.setValue((int)(moveSpeed));
					currentlyEditing.setText("Your Speed");
				}
				if (wd.isSelected()) {
					optionSlider.setValue((int)(warningdepth));
					currentlyEditing.setText("Warning Depth");
				}
				if (sd.isSelected()) {
					optionSlider.setValue((int)(depthmod));
					currentlyEditing.setText("Crash Depth");
				}
				if (rs.isSelected()) {
					optionSlider.setValue((int)(spawnrate));
					currentlyEditing.setText("Spawn Rate");
				}	
				if (as.isSelected()) {
					optionSlider.setValue((int)(spawnarea*2));
					currentlyEditing.setText("Spawn Area");
				}
				mP.requestFocusInWindow();
			}//end actionPerformed
		} //end OptionMenuListener
	
		class OptionSliderListener implements ChangeListener { //the listener for change in the slider
			public void stateChanged(ChangeEvent e) {
				if (c == null) {
					throw new RuntimeException("D:");
				}//do not continue for non-existant cubes
				
				//change the value of the setting based on the change in the slider
				//also set currentlyEditing to the selected setting, along with current value
				if (fd.isSelected()) {
					focaldepth = optionSlider.getValue();
					currentlyEditing.setText("Focal Depth = " + (int)(optionSlider.getValue()));
				}	
				if (cs.isSelected()) {
					cubeSpeed = (int)(optionSlider.getValue());
					currentlyEditing.setText("Cube Speed = " + (int)(optionSlider.getValue()));
				}	
				if (ys.isSelected()) {
					moveSpeed = optionSlider.getValue();
					currentlyEditing.setText("Your Speed = " + (int)(optionSlider.getValue()));
				}
				if (wd.isSelected()) {
					warningdepth = optionSlider.getValue();
					currentlyEditing.setText("Warning Depth = " + (int)(optionSlider.getValue()));
				}
				if (sd.isSelected()) {
					depthmod = optionSlider.getValue();
					currentlyEditing.setText("Crash Depth = " + (int)(optionSlider.getValue()));
				}
				if (rs.isSelected()) {
					spawnrate = optionSlider.getValue();
					currentlyEditing.setText("Spawn Rate = " + (int)(optionSlider.getValue()));
				}	
				if (as.isSelected()) {
					spawnarea = (int)(optionSlider.getValue()/2);
					currentlyEditing.setText("Spawn Area = " + (int)(optionSlider.getValue()));
				}
				mP.requestFocusInWindow();
			}//end stateChanged	
		}// end OptionSliderListener
		
	}//end options
	
	class spacebarPressedListener implements KeyListener { //listen for spacebar keystrokes
		public void keyPressed (KeyEvent e) { //if keypressed
			char keyIn = e.getKeyChar(); //get the input key
			if (keyIn == ' ') { //if spacebar was pressed
				screens.next(frame.getContentPane());//move to the next screen, the playPanel
				pP.requestFocusInWindow();//get focus, so the user's 
               	//keystrokes are recorded
               	currenttime = 0;//reset currenttime because it has been counting since 
               	//beginning of program
			}
		}
		public void keyReleased (KeyEvent e) {} //rest of KeyListener methods
		public void keyTyped (KeyEvent e) {}	
	}//end spacebarPressedListener
	
			
	class playPanel extends JPanel {//main panel with game and questions
		public playPanel() {
			this.setLayout(new BorderLayout());//set to borderlayout
			//to display questions and game
			gamePanel gP = new gamePanel();//the game's panel
			gP.setBounds(10,10, 1200, 800);//set size, location
			add(gP);//add to pP, center
		
			questPanel qP = new questPanel();//the panel for the questions
			//and the status  		
			add (qP, BorderLayout.EAST);//add to the east
			
			addKeyListener(new Move());//add a listener for wasd movement
			addKeyListener(new answerListener()); //add a listener for 
			//numerical answer input
			//the Listeners are added to the parent panel, playPanel,
			//so that each subpanel does not have to have individual
			//focus at the same time
		}//end public playPanel	
	}//end playPanel
				
	class gamePanel extends JPanel implements ActionListener {    
		public gamePanel() { 
			new Timer(3, this).start();// the master timer, triggers
			//screen refresh, cube spawn, etc
		} //end public gamePanel
			
		public void paintComponent(Graphics pg) {
			super.paintComponent(pg);
			
			BufferedImage buf = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
			Graphics g = buf.getGraphics();
			//both lines create an image that allow the screen to be resized
			
			Iterator<Cube> it2 = cubeList.iterator();// an iterator that goes through the
			//array of cubes
			if (w) 
				while (it2.hasNext()) {
					it2.next().y -= moveSpeed/250.;
				}// move up for w
			if (s) 	
				while (it2.hasNext()) {
					it2.next().y += moveSpeed/250.;
				}//move down for s
			if (a) 						
				while (it2.hasNext()) {
					it2.next().x -= moveSpeed/250.;
				}// move left for a
			if (d) 						
				while (it2.hasNext()) {
					it2.next().x += moveSpeed/250.;
				}//move right for d
			Iterator<Cube> it = cubeList.iterator();// another iterator that
			//goes through the array of cubes
///WHAT IS THIS?
			Cube[] arr = new Cube[cubeList.size()];
			for (int i = arr.length-1; i >= 0 && it.hasNext(); i--) 
				arr[i] = it.next();
			for (Cube c : arr) 
				c.draw(g);//draw all the cubes
			
			g.setColor(Color.blue);//draw the crash rectangle
			g.drawRect(crashoriginx-crashsizex/2,crashoriginy-crashsizey/2,crashsizex,crashsizey);
			g.setColor(Color.red);
			g.drawLine(600,300,600,500);//draw the centering line
			
			setBackground(Color.black); 
			
			for(int i = 0; i <10; i++) {//draw the avatar
				if (color == 'r') { //check if red because of a crash
				g.setColor(Color.red);
				} else g.setColor(Color.green);// otherwise, just draw green
				g.drawLine(600,400+i,680,430);//draws several lines, in the
				g.drawLine(600,400+i,520,430);//shape of 2 triangles
				g.setColor(Color.red);
				g.drawLine(0,799-i/3,1199,799-i/3);//draw the lower bound line	
			}
			if (crashtime+2 < currenttime)
				if (color == 'r') color = 'g';//change back to red a little
				//while after the crash
			
			pg.drawImage(buf,0,0,(int)(this.getWidth()), (int)((this.getWidth())/3*2), null);
			//finally, draw a image that is resized appropriatly
		} //end paintComponent
			
		public void actionPerformed(ActionEvent e) { //the timer action
///INSERT PROPER CRASH CODE
				if (crashes >= crashlimit){
					endscore = (int)(currenttime/3.)+questionpoints;
					getHiScore();
					System.out.println(endscore);
					screens.next(frame.getContentPane());
					score.setText("          "+endscore+"          ");
					messageline3.setText("Please Exit And Try Again. The High Score is "+hiscore);
					//System.exit(1);//if crashlimit is exceeded,
					//end the game
					crashlimit  = 1000;
				}
				
				if (cubeSpeed<90) {
					//if (cubeSpeed%10 == 0) 
						//System.out.println(cubeSpeed);
					cubeSpeed = (int)(currenttime/20)+10;	
				}
				if (spawnrate<90) {
					spawnrate = (int)(currenttime/10)+10;
				}		
				
				currenttime += increment;//add to the currenttime variable
				status.setText("Time: "+(int)currenttime+"\nQ - Points: "+questionpoints+
				"\nCrashes: "+crashes); //write out the time, question points, and crashes
				total.setText("Score: "+((int)(currenttime/3.)+questionpoints)+"\t");
				//write out the total score
				
///ADD TIME BASED DIFFICULTY
				if (Math.random() < spawnrate/100.) {
					cubeList.add(new Cube(
					(int) ((Math.random() * spawnarea) - spawnarea/2),
					(int) ((Math.random() * spawnarea) - spawnarea/2),
					300));//make a new cube, referring to the spawn area limit
					Cube c = cubeList.get(cubeList.size()-1); //add to the array
				}
				
				Iterator<Cube> it = cubeList.iterator();
				while (it.hasNext()) //move all the cubes forward
					it.next().moveForward();
				int i = 0;
				int length = cubeList.size();
				while (i < length) {//go through cube array
					if (cubeList.get(i).depth <= depthmod*2) {
						cubeList.remove(i);//remove cubes outside of depth parameters
						if (cubeList.get(i).incrasharea) {//if the cube hits avata
///NEED AX AY??            
						/*int ax = 600 - (int) Math.round(cubeList.get(i).x*focaldepth/cubeList.get(i).depth);
				int ay = 400 - (int) Math.round(cubeList.get(i).y*focaldepth/cubeList.get(i).depth);
							System.out.println("CRASH!  "+(-600+ax)+','+(400-ay));*/
							crashes ++;//add to the crash count
							color = 'r';//set the avatar to red
							crashtime = currenttime;//save the time of the crash
							}
							length--;//make the length shorter b/c of the removed cube
					} else {
///DOES THIS NOT SKIP THE PREVIOUS CUBE B/C OF THE "length--"?
						i++;//otherwise continue through the array
					}
				}
				repaint();//redraw
		} //end actionPerformed
		
		public void getHiScore() {
			
			hiscorefile = new File ("hiscore.txt"); //sets the input file
			try {
				hiscoreinput = new Scanner (hiscorefile); //sets the scanner
			} catch (FileNotFoundException e) {
				System.err.println("ERROR: Cannot open file questions.txt");
				System.exit(1);
			} //checks for availability of file
			hiscore = hiscoreinput.nextInt(); //get the first #, save it as the hiscore
			if (hiscore < endscore) { //if user's score is better than hiscore
				hiscore = endscore; //replace hiscore with user's score
				try {
					hiscoreoutput = new PrintWriter (hiscorefile); //initialize the output printer
				} catch (IOException e) {
					System.err.println("ERROR: Cannot open file hiscore.txt");
					System.exit(1);
				} //attempt writing out
				hiscoreoutput.print(hiscore); //write out		
				hiscoreoutput.close(); //close the PrintWriter
			}
		}	//end getHiScore
	
	} //end gamePanel
   
	class questPanel extends JPanel {//the panel with the questions and status
		public questPanel(){
			inputfile = new File ("questions.txt"); //sets the input file
			try {
				input = new Scanner (inputfile);
			} catch (FileNotFoundException e) {
				System.err.println("ERROR: Cannot open file questions.txt");
				System.exit(1);
			} //attempt scanning the question file
			for( int c = 0; c < numofquestions; c++){//read the file up to the # of desired q's and a's
				qanda[0][c] = input.nextLine();//get the question
				qanda[1][c] = input.nextLine();//get the answer		
			}
			
			setLayout(new GridLayout(6,1));//set as a vertically divided gridlayout
			setBackground(Color.black);
			
			questionnumber = (int)(Math.random()*numofquestions);//grab a random question
			question = new JTextArea(qanda[0][questionnumber]);//write out to the TextArea
			question.setForeground(Color.red);
			question.setBackground(Color.black);
			question.setLineWrap(true);//wrap the question
			question.setWrapStyleWord(true);//wrap words
			question.setFont(new Font ("Calibri", Font.PLAIN, 40));//set font
			question.setEditable(false);//non - user - editable
			add(question);//add to the grid
			
			status = new JTextArea(" ");//initial set to 0, later changed
			status.setForeground(Color.red);
			status.setBackground(Color.black);
			status.setFont(new Font ("Calibri", Font.PLAIN, 30));//set font
			status.setEditable(false);//non - user - editable
			add(status);//add to 2nd grid slot
			
			total = new JTextArea("Score: 0                ");//initial score = 0
			total.setForeground(Color.red);
			total.setBackground(Color.black);
			total.setFont(new Font ("Calibri", Font.PLAIN, 30));//set font
			total.setEditable(false);//non - user - editable
			add(total);//add to 3rd grid slot
						
		} //end public questPanel
	} //end questPanel
		
	class answerListener implements KeyListener { //listen for the answer an check if right
		
		public void restart() { //reset the question
			questionnumber = (int)(Math.random()*numofquestions); //get a random question
			question.setText(qanda[0][questionnumber]);//set the displayed text to the question
		}
		
		public void keyPressed (KeyEvent e) { //check if the answer is right
			char keyIn = e.getKeyChar(); //get the input key
			if (keyIn == '1' || keyIn == '2' || keyIn == '3' || keyIn == '4' || keyIn == '5' || keyIn == '6') {
			//first check if a number from 1-6 has been pressed
				if (keyIn == qanda[1][questionnumber].charAt(0)) { //then compare to answer; if right
					questionpoints += 10;//add 10 points 
					restart();// reset the question
				} else 		
				questionpoints -= 5;//if the answer was wrong, deduct 5 points	
			}
		}//end keyPressed
		
		public void keyReleased (KeyEvent e) {} //rest of KeyListener methods
		public void keyTyped (KeyEvent e) {}	
	}//end answerListener	
	
	
	class endPanel extends JPanel { //the last panel, shows end score
		public endPanel () {
			setLayout(new FlowLayout());
			JTextField message = new JTextField("You've Crashed 7 Times");
			message.setFont(new Font ("Calibri", Font.PLAIN, 60));//set font
			message.setEditable(false);//non - user - editable
			add(message);//add to the top part of the panel
			message.setHorizontalAlignment(JTextField.CENTER);//center the text
			
			JTextField messageline2 = new JTextField("          Nice Try.Your Final Score Is:          ");
			messageline2.setFont(new Font ("Calibri", Font.PLAIN, 40));//set font
			messageline2.setEditable(false);//non - user - editable
			add(messageline2);//add to the top part of the panel
			messageline2.setHorizontalAlignment(JTextField.CENTER);//center the text
			
			score = new JTextField(" ");//print out the final score
			score.setFont(new Font ("Calibri", Font.BOLD, 400));//set font
			score.setEditable(false);//non - user - editable
			add(score);//add to the bottom
			score.setHorizontalAlignment(JTextField.CENTER);//center text
			
			messageline3 = new JTextField(" ");		
			messageline3.setFont(new Font ("Calibri", Font.PLAIN, 20));//set font
			messageline3.setEditable(false);//non - user - editable
			add(messageline3);//add to the top part of the panel
			messageline3.setHorizontalAlignment(JTextField.CENTER);//center the text
			
		} //end public endPanel
	} //end endPanel
	
		
	class Cube { //the cubes in 3D space, store x, y, and z(depth)
		public double x, y, depth;//the x, y, z(depth) variables
		public boolean incrasharea = false;//whether the cube is lined up to crash into the player
		
		public Cube(double x, double y, double depth) { //set up x, y, depth
				this.x = x; //set equal to x(local)
				this.y = y; //set equal to y(local)
				this.depth = depth; //set equal to depth(local)					
		}//end public Cube
		
		public void moveForward() { //moves the Cube forward
				depth -= cubeSpeed/.3/20.; //decrease the depth by the appropriate increment
		}//end moveForward
		
		public void draw(Graphics g) { //draws the cube
				int size = (int) (Math.round(300 - depth)/2);//establish a size based on depth
				int ax = 600 - (int) Math.round(x*focaldepth*240/depth);//establish a screen x
				//coordinate based on the depth and 3D x coordinate
				int ay = 400 - (int) Math.round(y*focaldepth*240/depth);//establish a screen y
				//coordinate based on the depth and 3D y coordinate
				g.setColor(
					//new Color(200 - (int)depth/2, 0, 100+(int)depth/2)
					new Color(10+(int)depth/2, 10+(int)depth/2, 100+(int)depth/2) 
				);//change the color based on the depth (closer = lighter)
				
				if (depthmod*2+warningdepth>depth) { //if the cube's depth is close to the player
					g.setColor(new Color(250,150,50));//change its color to orange						
					int crashx = (int) Math.round(x*focaldepth*240/(depthmod*2));//calculate the
					//on-screen x coordinate at which the cube will hit/pass the player
					if (Math.abs(crashx-crashoriginx+600)-size/2 < crashsizex/2) {// if the 
					//cube is within horizontal crashing distance of the player
						int crashy = (int) Math.round(y*focaldepth*240/(depthmod*2));//calculate the
						//on-screen y coordinate at which the cube will hit/pass the player
						if (Math.abs(crashy-crashoriginy+400)-size/2 < crashsizey/2) {// if the 
						//cube is within vertical crashing distance of the player
							g.setColor(Color.red);//change the cube to red
							incrasharea = true;//label it as on course to hit the player
						}else incrasharea = false;//otherwise as not on crash course
					}			
				}
					
				g.fillRect(ax - size/2, ay - size/2, size, size);//draw a square of size "size" at
				//the location minus the "size"/2
				g.setColor(Color.black); //switch to black, repeat with an outline
				g.drawRect(ax - size/2, ay - size/2, size, size);
		}//end draw
	}//end Cube
			
	class Move implements KeyListener {
		
		public void keyPressed (KeyEvent e) { //if a key is pressed
			char keyIn = e.getKeyChar();//get the key
			
			switch (keyIn) { //based on the key, make one of the directions true(for pressed)
				case 'w':
					w = true;
					break;
				case 's':						
					s = true;
					break;
				case 'a':						
					a = true;
					break;
				case 'd':						
					d = true;
					break;
			}					
		}//end keyPressed
		
		public void keyReleased (KeyEvent e) {//based on the key, make one of the directions
		//false(for released)
			char keyIn = e.getKeyChar();
			
			switch (keyIn) { //save the new position
				case 'w':
					w = false;
					break;
				case 's':						
					s = false;
					break;
				case 'a':						
					a = false;
					break;
				case 'd':						
					d = false;
					break;
			}					
		}
		
		public void keyTyped (KeyEvent e) {}//final keyListener method
			
	}//end Move		
							
}//end Game
