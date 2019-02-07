// Created by Tomomasa Uchida
// "Interactive Evolving Ecosystem"

// ----- OSC -----
import oscP5.*;
import netP5.*;
OscP5 oscP5;
int MAX_USER_NUM = 3;
int currentUserNum = 0;
int torsoPosX[] = new int[MAX_USER_NUM];
int torsoPosY[] = new int[MAX_USER_NUM];
int leftHandPosX[] = new int[MAX_USER_NUM];
int leftHandPosY[] = new int[MAX_USER_NUM];
int rightHandPosX[] = new int[MAX_USER_NUM];
int rightHandPosY[] = new int[MAX_USER_NUM];
String depth[] = new String[MAX_USER_NUM];
char c;
float distToUser = 0.0;
// ---------------

int BUTTERFLY_NUM = 70;
int	BIRD_NUM = 70;
//int	FISH_NUM = 200;
//int	FISH_SIZE = 10;
int DRAGON_NUM = 2;
int	CLOUD_NUM = 5;
int FLOWER_NUM = 70;
int FLOWER_SIZE = 7;
int	ROCK_NUM = 25;
int ROCK_SIZE = 40;
int butterflyGenerations = 0;
int birdGenerations = 0;
int dragonGenerations = 0;
float BUTTERFLY_REPRO_PROB = 0.01;
float BIRD_REPRO_PROB = 0.01;
float DRAGON_REPRO_PROB = 0.001; // 0.01%
float FLOWER_APPEARANCE = 0.65; // 65%

int tmpButterflyNum = 0;
int tmpBirdNum = 0;
boolean isStartSexualReproButter = false;
boolean isStartSexualReproBird = false;
boolean bButterflySexualRepro = true;
boolean bButterflyAsexualRepro = false;
boolean bBirdSexualRepro = true;
boolean bBirdAsexualRepro = false;
boolean bPaintDetails = true;
boolean bFlower = true;
boolean bSaveframe = false;

FlockingSystem flockingSystem;
FlowerSystem flowerSystem;
RockSystem rockSystem;
PVector[] rocksAroundIsland = new PVector[36];
Sea sea;
Beach beach;
Island island;

Camera camera;

PVector centerPos;

void setup() {
	fullScreen();
	//size(1920, 1080);
	//size(1440, 900);
	smooth();
	noStroke();
	noCursor();
	frameRate(24);
	centerPos = new PVector(width*0.5, height*0.5);

	// About sea
  	sea = new Sea( new PVector(random(1000), random(1000)), 
  				   new PVector(random(1000), random(1000)), 
  				   centerPos, 
  				   width );

  	// About beach
  	beach = new Beach( new PVector(random(1000), random(1000)), 
  					   new PVector(random(1000), random(1000)), 
  					   centerPos, 
  					   width*0.43 );

  	// About island
  	island = new Island( new PVector(random(1000), random(1000)), 
  						 new PVector(random(1000), random(1000)), 
  						 centerPos, 
  						 width*0.38 );

	// About FlowerSystem
	flowerSystem = new FlowerSystem( FLOWER_NUM );

	// About RockSystem
  	rockSystem = new RockSystem( ROCK_NUM );

  	// About FlockingSystem
	flockingSystem = new FlockingSystem( new PVector(width*0.5, height*0.5) );

	// About Virtual Camera
	camera = new Camera( new PVector(width*0.5, height*0.5) );

	// About OSC
	oscP5 = new OscP5(this, 5555); // 自分のポート番号
	oscP5.plug(this, "getUserNum", "/userNum");
  	oscP5.plug(this, "getTorsoData", "/torso");
  	oscP5.plug(this, "getLeftHandData", "/leftHand");
  	oscP5.plug(this, "getRightHandData", "/rightHand");
  	oscP5.plug(this, "getDepth", "/depth");

  	for ( int i = 0; i < MAX_USER_NUM; i++ ) {
    	depth[i] = "0";
  	}
}

void draw() {
	

	// Virtual Camera movement
	translate(width*0.5-camera.pos.x, height*0.5-camera.pos.y);
  
  noStroke();
  fill(50, 10);
  rect(camera.pos.x-width*0.5, camera.pos.y-height*0.5, 2*width, 2*height);

	// About Background
  if ( !bPaintDetails ) {
    background(0, 200, 40);
  } else {
    //sea.runSea();
    sea.display();
    beach.display();
    island.display();  
  }



	// Display Kinect data
	for (int id = 0; id < currentUserNum; id++) {
		noStroke();

	  	// torso
	  	fill(0, 0, 0);
	  	ellipse(3*torsoPosX[id], 2.5*torsoPosY[id], 50, 50);

	  	// leftHand
	  	fill(0, 0, 255);
	  	ellipse(3*leftHandPosX[id], 2.5*leftHandPosY[id], 50, 50);

	  	// rightHand
	  	fill(255, 0, 0);
	  	ellipse(3*rightHandPosX[id], 2.5*rightHandPosY[id], 50, 50);
	}



	// About FlowerSystem
	if ( bFlower ) {
		flowerSystem.run();
		flowerSystem.display();
	}

  	// About Rock
 	rockSystem.display();

  	
  	// 過生殖防止のため
  	if ( isStartSexualReproButter ) {
  		tmpButterflyNum = flockingSystem.butterflys.size();
  	}

  	if ( isStartSexualReproBird ) {
  		tmpBirdNum = flockingSystem.birds.size();
  	}

  	// Num control
  	if ( flockingSystem.butterflys.size() > tmpButterflyNum + 8 ) {
  		bButterflySexualRepro = false;
  		bButterflyAsexualRepro = false;
  		isStartSexualReproButter = false;
  	}
  	if ( flockingSystem.butterflys.size() > 120 ) bButterflySexualRepro = false;
	if ( flockingSystem.butterflys.size() < 100 ) bButterflySexualRepro = true;
	if ( flockingSystem.butterflys.size() > 50 ) bButterflyAsexualRepro = false;
	if ( flockingSystem.butterflys.size() < 40 ) bButterflyAsexualRepro = true;

  	if ( flockingSystem.birds.size() > tmpBirdNum + 8 ) {
  		bBirdSexualRepro = false;
  		bBirdAsexualRepro = false;
  		isStartSexualReproBird = false;
  	}
  	if ( flockingSystem.birds.size() < 120 ) bBirdSexualRepro = false;
	if ( flockingSystem.birds.size() < 100 ) bBirdSexualRepro = true;
	if ( flockingSystem.birds.size() > 50 ) bBirdAsexualRepro = false;
	if ( flockingSystem.birds.size() < 40 ) bBirdAsexualRepro = true;

  	// About FlockingSystem
	flockingSystem.run();
	flockingSystem.display();
	flockingSystem.runCamera();

	textSize(25);
	fill(0);
	text("Butterfly :  "+flockingSystem.butterflys.size(), camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+30);
	text("Butterfly Generations :  "+butterflyGenerations , camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+60);
	text("Bird :         "+flockingSystem.birds.size(), camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+100);
	text("Bird Generations :         "+birdGenerations , camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+130);
	text("Dragon :     "+flockingSystem.dragons.size(), camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+170);
	text("Dragon Generations :    "+dragonGenerations , camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+200);
  text("currentUserNum :  "+currentUserNum, camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+240);
	
	if ( !bPaintDetails ) {
		
      text("Flower :     "+flowerSystem.flowers.size(), camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+300);
      text("BUTTERFLY_REPRO_PROB :    "+BUTTERFLY_REPRO_PROB, camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+330);
      text("BIRD_REPRO_PROB :       "+BIRD_REPRO_PROB, camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+360);
      text("DRAGON_REPRO_PROB :   "+DRAGON_REPRO_PROB, camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+390);
      text("FLOWER_APPEARANCE :    "+FLOWER_APPEARANCE, camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+420);

      //text("fps : "+frameRate , camera.pos.x-width*0.5+10, camera.pos.y-height*0.5+560);
	}

	// Save frame
	if ( bSaveframe ) {
    	saveFrame("frame/####.tif");
  	}

  	if ( frameCount > 4320 ) exit();


  	// ----- From oF ------
  	interaction();
}

void interaction() {
  	for (int id = 0; id < currentUserNum; id++) {

      	// Kinectとユーザーの距離 : 「0 ~ 1 or 2 ~ 3 or 4」
      	if ( depth[id].length() == 3 ) {
      		distToUser = 0.0; // 0.0

      	} else {
      		c = depth[id].charAt(0);
      		distToUser = (float)c - 48.0; // 1.0 ~ 4.0
      	}
 
 		// Display info
 		textSize(25);
      	fill(0);
      	text("Left Hand", 3*leftHandPosX[id]-50, 2.5*leftHandPosY[id]-80);
      	text("Right Hand", 3*rightHandPosX[id]-50, 2.5*rightHandPosY[id]-80);
      	text("User :"+id, 3*leftHandPosX[id]-50, 2.5*leftHandPosY[id]-110);
      	text("User :"+id, 3*rightHandPosX[id]-50, 2.5*rightHandPosY[id]-110);
      	text("Depth : "+depth[id], 3*leftHandPosX[id]-50, 2.5*leftHandPosY[id]-50);
      	text("Depth : "+depth[id], 3*rightHandPosX[id]-50, 2.5*rightHandPosY[id]-50);

  		// 何秒かおきに(出現しすぎるのを防ぐため) (50は，要調整)
  		if ( frameCount%50 == 0 ) {

  			// 2(m) ~ 3(m) : Butterfly
  			if ( 2.0 <= distToUser && distToUser < 3.0 ) {
  				PVector newButterflyPos;
  				newButterflyPos = new PVector(3*leftHandPosX[id], 2.5*leftHandPosY[id]);
  				newButterfly(newButterflyPos, color(30, 180, 255)); // メス
  				
                newButterflyPos = new PVector(3*rightHandPosX[id], 2.5*rightHandPosY[id]);
  				newButterfly(newButterflyPos, color(200, 240, 240)); // オス


  			// 3(m) ~ 4(m) : Bird
  			} else if ( 3.0 <= distToUser && distToUser < 4.0 ) {
  				PVector newBirdPos;
  				newBirdPos = new PVector(3*leftHandPosX[id], 2.5*leftHandPosY[id]);
  				newBird(newBirdPos, color(255, 100, 0)); // メス
  					
  				newBirdPos = new PVector(3*rightHandPosX[id], 2.5*rightHandPosY[id]);
  				newBird(newBirdPos, color(255, 200, 0)); // オス

  			// 4(m) ~ : Dragon
  			} else if ( 4.0 <= distToUser && distToUser < 9.0 ) {
  				PVector newDragonPos;
  				newDragonPos = new PVector(3*leftHandPosX[id], 2.5*leftHandPosY[id]);
  				newDragon(newDragonPos);

  				newDragonPos = new PVector(3*rightHandPosX[id], 2.5*rightHandPosY[id]);
  				newDragon(newDragonPos);
  			}
  		}
  	}
}

public void getUserNum(int num) {
	currentUserNum = num;
}

public void getTorsoData(int x, int y, int id) {
  	torsoPosX[id] = x;
  	torsoPosY[id] = y;
}

public void getLeftHandData(int x, int y, int id) {
	leftHandPosX[id] = x;
  	leftHandPosY[id] = y; 
}

public void getRightHandData(int x, int y, int id) {
	rightHandPosX[id] = x;
  	rightHandPosY[id] = y;	
}

public void getDepth(String buffer, int id) {
	depth[id] =	buffer;
}

void keyPressed() {
	// Start saveframe
	if (key == 's') {
    	bSaveframe = true;
  	}

  	// Debug
  	if (key == 'a') {
  		if ( bPaintDetails ) bPaintDetails = false;
  		else bPaintDetails = true;
  	}

  	// Add
  	if ( key == 'b') {
  		PVector pos = PVector.random2D();
		pos.mult(random(width*0.4));
		pos.add(centerPos);

		color col;
		if ( random(1) < 0.5 ) col = color(30, 180, 255); // メス
		else 			   	   col = color(200, 240, 240); // オス

  		newButterfly(pos, col);
  	}
  	if ( key == 'c') {
  		PVector pos = PVector.random2D();
		pos.mult(random(width*0.4));
		pos.add(centerPos);

		color col;
		if ( random(1) < 0.5 ) col = color(255, 100, 0); // メス
		else 			    col = color(255, 200, 0); // オス

		newBird(pos, col);
	}

  	if ( key == 'd') {
  		PVector pos = PVector.random2D();
		pos.mult(random(width*0.4));
		pos.add(centerPos);

  		newDragon(pos);
  	}
}

void newButterfly(PVector bornPos, color col) {
	flockingSystem.makeButterfly(bornPos.copy(), col);	
}

void newBird(PVector bornPos, color col) {
	flockingSystem.makeBird(bornPos.copy(), col);
}

void newDragon(PVector bornPos) {
	flockingSystem.makeDragon(bornPos.copy());
}

// For Debug
void mousePressed() {
	PVector pos = new PVector(camera.pos.x-width*0.5+mouseX, camera.pos.y-height*0.5+mouseY);

	color col = color(30, 180, 255); // メス

	newButterfly(pos, col);
}