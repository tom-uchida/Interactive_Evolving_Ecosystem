import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import oscP5.*; 
import netP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class InteractiveEvolvingEcosystem extends PApplet {

// Created by Tomomasa Uchida
// "Interactive Evolving Ecosystem"

// ----- OSC -----


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
float distToUser = 0.0f;
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
float BUTTERFLY_REPRO_PROB = 0.01f;
float BIRD_REPRO_PROB = 0.01f;
float DRAGON_REPRO_PROB = 0.001f; // 0.01%
float FLOWER_APPEARANCE = 0.65f; // 65%

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

public void setup() {
	
	//size(1920, 1080);
	//size(1440, 900);
	
	noStroke();
	noCursor();
	frameRate(24);
	centerPos = new PVector(width*0.5f, height*0.5f);

	// About sea
  	sea = new Sea( new PVector(random(1000), random(1000)), 
  				   new PVector(random(1000), random(1000)), 
  				   centerPos, 
  				   width );

  	// About beach
  	beach = new Beach( new PVector(random(1000), random(1000)), 
  					   new PVector(random(1000), random(1000)), 
  					   centerPos, 
  					   width*0.43f );

  	// About island
  	island = new Island( new PVector(random(1000), random(1000)), 
  						 new PVector(random(1000), random(1000)), 
  						 centerPos, 
  						 width*0.38f );

	// About FlowerSystem
	flowerSystem = new FlowerSystem( FLOWER_NUM );

	// About RockSystem
  	rockSystem = new RockSystem( ROCK_NUM );

  	// About FlockingSystem
	flockingSystem = new FlockingSystem( new PVector(width*0.5f, height*0.5f) );

	// About Virtual Camera
	camera = new Camera( new PVector(width*0.5f, height*0.5f) );

	// About OSC
	oscP5 = new OscP5(this, 5555); // \u81ea\u5206\u306e\u30dd\u30fc\u30c8\u756a\u53f7
	oscP5.plug(this, "getUserNum", "/userNum");
  	oscP5.plug(this, "getTorsoData", "/torso");
  	oscP5.plug(this, "getLeftHandData", "/leftHand");
  	oscP5.plug(this, "getRightHandData", "/rightHand");
  	oscP5.plug(this, "getDepth", "/depth");

  	for ( int i = 0; i < MAX_USER_NUM; i++ ) {
    	depth[i] = "0";
  	}
}

public void draw() {
	

	// Virtual Camera movement
	translate(width*0.5f-camera.pos.x, height*0.5f-camera.pos.y);
  
  noStroke();
  fill(50, 10);
  rect(camera.pos.x-width*0.5f, camera.pos.y-height*0.5f, 2*width, 2*height);

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
	  	ellipse(3*torsoPosX[id], 2.5f*torsoPosY[id], 50, 50);

	  	// leftHand
	  	fill(0, 0, 255);
	  	ellipse(3*leftHandPosX[id], 2.5f*leftHandPosY[id], 50, 50);

	  	// rightHand
	  	fill(255, 0, 0);
	  	ellipse(3*rightHandPosX[id], 2.5f*rightHandPosY[id], 50, 50);
	}



	// About FlowerSystem
	if ( bFlower ) {
		flowerSystem.run();
		flowerSystem.display();
	}

  	// About Rock
 	rockSystem.display();

  	
  	// \u904e\u751f\u6b96\u9632\u6b62\u306e\u305f\u3081
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
	text("Butterfly :  "+flockingSystem.butterflys.size(), camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+30);
	text("Butterfly Generations :  "+butterflyGenerations , camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+60);
	text("Bird :         "+flockingSystem.birds.size(), camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+100);
	text("Bird Generations :         "+birdGenerations , camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+130);
	text("Dragon :     "+flockingSystem.dragons.size(), camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+170);
	text("Dragon Generations :    "+dragonGenerations , camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+200);
  text("currentUserNum :  "+currentUserNum, camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+240);
	
	if ( !bPaintDetails ) {
		
      text("Flower :     "+flowerSystem.flowers.size(), camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+300);
      text("BUTTERFLY_REPRO_PROB :    "+BUTTERFLY_REPRO_PROB, camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+330);
      text("BIRD_REPRO_PROB :       "+BIRD_REPRO_PROB, camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+360);
      text("DRAGON_REPRO_PROB :   "+DRAGON_REPRO_PROB, camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+390);
      text("FLOWER_APPEARANCE :    "+FLOWER_APPEARANCE, camera.pos.x-width*0.5f+10, camera.pos.y-height*0.5f+420);

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

public void interaction() {
  	for (int id = 0; id < currentUserNum; id++) {

      	// Kinect\u3068\u30e6\u30fc\u30b6\u30fc\u306e\u8ddd\u96e2 : \u300c0 ~ 1 or 2 ~ 3 or 4\u300d
      	if ( depth[id].length() == 3 ) {
      		distToUser = 0.0f; // 0.0

      	} else {
      		c = depth[id].charAt(0);
      		distToUser = (float)c - 48.0f; // 1.0 ~ 4.0
      	}
 
 		// Display info
 		textSize(25);
      	fill(0);
      	text("Left Hand", 3*leftHandPosX[id]-50, 2.5f*leftHandPosY[id]-80);
      	text("Right Hand", 3*rightHandPosX[id]-50, 2.5f*rightHandPosY[id]-80);
      	text("User :"+id, 3*leftHandPosX[id]-50, 2.5f*leftHandPosY[id]-110);
      	text("User :"+id, 3*rightHandPosX[id]-50, 2.5f*rightHandPosY[id]-110);
      	text("Depth : "+depth[id], 3*leftHandPosX[id]-50, 2.5f*leftHandPosY[id]-50);
      	text("Depth : "+depth[id], 3*rightHandPosX[id]-50, 2.5f*rightHandPosY[id]-50);

  		// \u4f55\u79d2\u304b\u304a\u304d\u306b(\u51fa\u73fe\u3057\u3059\u304e\u308b\u306e\u3092\u9632\u3050\u305f\u3081) (50\u306f\uff0c\u8981\u8abf\u6574)
  		if ( frameCount%50 == 0 ) {

  			// 2(m) ~ 3(m) : Butterfly
  			if ( 2.0f <= distToUser && distToUser < 3.0f ) {
  				PVector newButterflyPos;
  				newButterflyPos = new PVector(3*leftHandPosX[id], 2.5f*leftHandPosY[id]);
  				newButterfly(newButterflyPos, color(30, 180, 255)); // \u30e1\u30b9
  				
                newButterflyPos = new PVector(3*rightHandPosX[id], 2.5f*rightHandPosY[id]);
  				newButterfly(newButterflyPos, color(200, 240, 240)); // \u30aa\u30b9


  			// 3(m) ~ 4(m) : Bird
  			} else if ( 3.0f <= distToUser && distToUser < 4.0f ) {
  				PVector newBirdPos;
  				newBirdPos = new PVector(3*leftHandPosX[id], 2.5f*leftHandPosY[id]);
  				newBird(newBirdPos, color(255, 100, 0)); // \u30e1\u30b9
  					
  				newBirdPos = new PVector(3*rightHandPosX[id], 2.5f*rightHandPosY[id]);
  				newBird(newBirdPos, color(255, 200, 0)); // \u30aa\u30b9

  			// 4(m) ~ : Dragon
  			} else if ( 4.0f <= distToUser && distToUser < 9.0f ) {
  				PVector newDragonPos;
  				newDragonPos = new PVector(3*leftHandPosX[id], 2.5f*leftHandPosY[id]);
  				newDragon(newDragonPos);

  				newDragonPos = new PVector(3*rightHandPosX[id], 2.5f*rightHandPosY[id]);
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

public void keyPressed() {
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
		pos.mult(random(width*0.4f));
		pos.add(centerPos);

		int col;
		if ( random(1) < 0.5f ) col = color(30, 180, 255); // \u30e1\u30b9
		else 			   	   col = color(200, 240, 240); // \u30aa\u30b9

  		newButterfly(pos, col);
  	}
  	if ( key == 'c') {
  		PVector pos = PVector.random2D();
		pos.mult(random(width*0.4f));
		pos.add(centerPos);

		int col;
		if ( random(1) < 0.5f ) col = color(255, 100, 0); // \u30e1\u30b9
		else 			    col = color(255, 200, 0); // \u30aa\u30b9

		newBird(pos, col);
	}

  	if ( key == 'd') {
  		PVector pos = PVector.random2D();
		pos.mult(random(width*0.4f));
		pos.add(centerPos);

  		newDragon(pos);
  	}
}

public void newButterfly(PVector bornPos, int col) {
	flockingSystem.makeButterfly(bornPos.copy(), col);	
}

public void newBird(PVector bornPos, int col) {
	flockingSystem.makeBird(bornPos.copy(), col);
}

public void newDragon(PVector bornPos) {
	flockingSystem.makeDragon(bornPos.copy());
}

// For Debug
public void mousePressed() {
	PVector pos = new PVector(camera.pos.x-width*0.5f+mouseX, camera.pos.y-height*0.5f+mouseY);

	int col = color(30, 180, 255); // \u30e1\u30b9

	newButterfly(pos, col);
}
class Beach {
	PVector pos;
	FloatList vertX;
	FloatList vertY;

	PVector offset1, offset2;
	int beachCol = color( 200, 160, 110, 255 );
	float radian;
	float size;
	float scale = 0.04f;
	float x, y;

	Beach (PVector _offset1, PVector _offset2, PVector _pos, float _size) {
		offset1 = _offset1;
		offset2 = _offset2;
		pos = _pos;
		size = _size;
		vertX = new FloatList();
		vertY = new FloatList();

		setVertex();
	}

	public void setVertex() {
    	pushMatrix();
    	translate( pos.x, pos.y );
    
    	for ( float radius = size; radius > 0; radius -= (int)size ) {
      		for ( float angle = 0; angle < 360; angle+=15 ) {
          		radian = radians(angle);  
          		x = radius * cos(radian);
          		y = radius * sin(radian);

          		// \u9802\u70b9\u60c5\u5831
          		vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y),   0, 1,   -80, 80) );
          		vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y),   0, 1,   -80, 80) );
      		}
    	}

    	popMatrix();
  	}

  	public void display() {
  		// \u30ed\u30fc\u30ab\u30eb\u5ea7\u6a19\u7cfb\u306b\u79fb\u52d5\u3057\u3066\u304b\u3089\u63cf\u753b
  		pushMatrix();
    	translate( pos.x, pos.y );

  		int i = 0;
  		for ( float radius = size; radius > 0; radius-=(int)size ) {
  			fill(beachCol);

  			beginShape();
      		for ( float angle = 0; angle < 360; angle+=15 ) {
          		vertex( vertX.get(i), vertY.get(i) );
          		i++;
      		}
      		endShape(CLOSE);
    	}

    	popMatrix();
  	}
}

// Boid\u30af\u30e9\u30b9\u3092\u7d99\u627f\u3057\u305f\u30af\u30e9\u30b9
class Bird extends Boid {

	// \u30b3\u30f3\u30b9\u30c8\u30e9\u30af\u30bf
	Bird (PVector _pos, PVector _vel, int _col, DNA _dna, boolean _isInit) {
		super(_pos, _vel, _col); // Boid Class constructor
		shadowCol = color(50, 50);

		// Genetic Algorithm
		dna = _dna;
		//headSize        = map(dna.genes[0], 0, 1,  40,  15);

		headSize = 50;


		maxFlockSpeed   = map(dna.genes[0], 0, 1, 3.0f, 8.0f);
		maxFlockForce   = map(dna.genes[1], 0, 1, 2.0f, 6.0f);
		maxSeekSpeed    = map(dna.genes[0], 0, 1, 3.0f, 8.0f);
		maxSeekForce    = map(dna.genes[2], 0, 1, 2.0f, 5.0f);
		maxRunawaySpeed = map(dna.genes[0], 0, 1, 5.0f, 12.0f);
		maxRunawayForce = map(dna.genes[3], 0, 1, 2.5f, 10.0f);

		// \u5f37\u5316\u5b66\u7fd2
		flowerWeight     = map(dna.genes[4], 0, 1, -2, 2);
		rockWeight     	 = map(dna.genes[5], 0, 1, -2, 2);
		flowerPerception = map(dna.genes[6], 0, 1, 0, 100);
		rockPerception   = map(dna.genes[7], 0, 1, 0, 100);

		// \u30d1\u30e9\u30e1\u30fc\u30bf\u8981\u8abf\u6574
		distanceForSeparation = 2*r + 20;
		distanceForCohesion = 2*r + 200;
		distanceForAlignment = 2*r + 200;

		if ( _isInit ) {
			lifePoint = 255;
		} else {
			lifePoint = 30;
			born = true;
		}
    }

    // ----- \u4ee5\u4e0b\u3001\u30e1\u30f3\u30d0\u95a2\u6570 -----

	// \u9069\u5fdc\u5ea6\u8a08\u7b97 ( \u6355\u98df\u8005\u3068\u9060\u3044\u3000\u304b\u3064\u3000\u82b1\u306b\u8fd1\u3044\u3000\u3068\u3000\u9069\u5fdc\u5ea6\u304c\u9ad8\u304f\u306a\u308b\u3088\u3046\u306b\u8a2d\u5b9a )
	public void fitness(PVector nearestEnemy, PVector nearestFlower) {
		float distEnemy = dist(pos.x, pos.y, nearestEnemy.x, nearestEnemy.y);
		float distFlower = dist(pos.x, pos.y, nearestFlower.x, nearestFlower.y);
    	// \u4e0b\u9650\u3068\u4e0a\u9650\u306e\u5236\u5fa1
    	if ( distEnemy > 2000 ) 	 distEnemy = 2000;
    	else if ( distEnemy < 1.0f )  distEnemy = 1.0f;
    	if ( distFlower < 1.0f ) 	 distFlower = 1.0f;
    	else if ( distFlower > 1000 ) distFlower = 1000;

    	float fitnessForEnemy  = pow( distEnemy / 2000, 2 );  // \u6307\u6570\u95a2\u6570\u7684
    	float fitnessForFlower = pow( 1.0f /  distFlower, 2 ); // \u6307\u6570\u95a2\u6570\u7684

    	fitness = fitnessForEnemy*0.5f + fitnessForFlower*0.5f;
    }

	// \u7121\u6027\u751f\u6b96\u306e\u5834\u5408\u306e\u307f\u4f7f\u7528
	public Bird asexualReproduce() {
		if ( random(1) < BIRD_REPRO_PROB ) { // a certain probability
			DNA childDNA = dna.copy(); // \u89aa\u306e\u5b8c\u5168\u306a\u30b3\u30d4\u30fc\u3067\u3042\u308b\u5b50\u3092\u65b0\u3057\u304f\u4f5c\u308b
			childDNA.mutate(0.01f); // mutation
			birdGenerations++;

			// \u5b50\u304c\u751f\u307e\u308c\u308b\u4f4d\u7f6e\u306e\u6c7a\u5b9a
			PVector bornPos = PVector.random2D();
			bornPos.mult(random(width*0.40f));
			bornPos.add(new PVector(width*0.5f, height*0.5f));

			return new Bird(bornPos, vel.copy(), col, childDNA, false);

		} else {
			return null;
		}
	}

	// \u4f4d\u7f6e\u306e\u66f4\u65b0
	public void update() {
		super.update();

		lifePoint -= 0.75f;

		if ( born && dead ) dead = true;
		if ( lifePoint >= 255.0f ) born = false;
		if ( born && lifePoint < 255.0f ) lifePoint += 10.0f;
    	//if ( dead ) lifePoint -= 20.0;
    }

	// \u5883\u754c
	public void boundaries() {
		desired = null;

		if ( !bPaintDetails ) {
			stroke(255);
			strokeWeight(10);
			line(width*0.15f, -height*0.1f, width*0.15f, height*1.1f);
			line(width*0.85f, -height*0.1f, width*0.85f, height*1.1f);
			line(width*0.15f, -height*0.1f, width*0.85f, -height*0.1f);
			line(width*0.15f, height*1.1f, width*0.85f, height*1.1f);
		}

		if ( pos.x < width*0.15f ) {
			desired = new PVector(maxSeekSpeed, vel.y);
		} else if ( pos.x > width*0.85f ) {
			desired = new PVector(-maxSeekSpeed, vel.y);
		}

		if ( pos.y < -height*0.1f ) {
			desired = new PVector(vel.x, maxSeekSpeed);
		} else if ( pos.y > height*1.1f ) {
			desired = new PVector(vel.x, -maxSeekSpeed);
		}

		if ( desired != null ) {
			desired.normalize();
			desired.mult(maxSeekSpeed);
			PVector steer = PVector.sub(desired, vel);
			steer.limit(maxSeekForce);

			applyForce(steer);
			lifePoint -= 20;
		}
	}

	// \u9ce5\u306e\u8868\u793a
	public void display() {
		noStroke();
		fill(col, lifePoint);

		pushMatrix();
		translate(pos.x, pos.y);
		rotate(HALF_PI + atan2(vel.y, vel.x));
		beginShape();
		curveVertex(0, -0.5f*headSize);
		curveVertex(0.15f*headSize, 0);
			curveVertex(1.0f*headSize, 0); // \u7fbd\u306e\u6700\u53f3\u70b9
			curveVertex(0.8f*headSize, 0.15f*headSize);
			curveVertex(0.2f*headSize, 0.25f*headSize); //\u4ed8\u3051\u6839
			curveVertex(0.3f*headSize, 0.55f*headSize);
			curveVertex(0.1f*headSize, 0.4f*headSize);

			curveVertex(0, 0.7f*headSize); // \u6700\u4e0b\u70b9

			curveVertex(-0.1f*headSize, 0.4f*headSize);
			curveVertex(-0.3f*headSize, 0.55f*headSize);
			curveVertex(-0.2f*headSize, 0.25f*headSize);
			curveVertex(-0.8f*headSize, 0.15f*headSize);
			curveVertex(-1.0f*headSize, 0);
			curveVertex(-0.15f*headSize, 0);

			curveVertex(0, -0.5f*headSize);
			curveVertex(0.1f*headSize, 0);
			endShape();
			popMatrix();
	}

	// \u5f71\u306e\u8868\u793a
	public void shadow() {
		headSize += 5;
		pos.x += 30;
		noStroke();
		float alpha = map(lifePoint, 0, 200, 0, 100);
		fill(shadowCol, alpha);

		pushMatrix();
		translate(pos.x, pos.y);
		rotate(HALF_PI + atan2(vel.y, vel.x));
		beginShape();
		curveVertex(0, -0.5f*headSize);
		curveVertex(0.15f*headSize, 0);
			curveVertex(1.0f*headSize, 0); // \u7fbd\u306e\u6700\u53f3\u70b9
			curveVertex(0.8f*headSize, 0.15f*headSize);
			curveVertex(0.2f*headSize, 0.25f*headSize); //\u4ed8\u3051\u6839
			curveVertex(0.3f*headSize, 0.55f*headSize);
			curveVertex(0.1f*headSize, 0.4f*headSize);

			curveVertex(0, 0.7f*headSize); // \u6700\u4e0b\u70b9

			curveVertex(-0.1f*headSize, 0.4f*headSize);
			curveVertex(-0.3f*headSize, 0.55f*headSize);
			curveVertex(-0.2f*headSize, 0.25f*headSize);
			curveVertex(-0.8f*headSize, 0.15f*headSize);
			curveVertex(-1.0f*headSize, 0);
			curveVertex(-0.15f*headSize, 0);

			curveVertex(0, -0.5f*headSize);
			curveVertex(0.1f*headSize, 0);
			endShape();
			popMatrix();
			pos.x -= 30;
			headSize -= 5;
	}
}


// Boid\u30af\u30e9\u30b9
class Boid {
	DNA dna;
	float fitness;

	PVector pos;
	PVector vel;
	PVector acc;

	PVector sep;
	PVector coh;
	PVector ali;

	float r;
	float mass;

	// \u30e9\u30a4\u30d5\u30dd\u30a4\u30f3\u30c8
	float lifePoint;

	// \u751f\u7269\u306e\u30b5\u30a4\u30ba
	float headSize;

	float maxFlockSpeed;
	float maxFlockForce;
	float maxSeekSpeed;
	float maxSeekForce;
	float maxRunawaySpeed;
	float maxRunawayForce;

	float flowerWeight;
	float rockWeight;
	float flowerPerception;
	float rockPerception;

	float distanceForSeparation;
	float distanceForCohesion;
	float distanceForAlignment;

	int col;
	int shadowCol;

	boolean born;
	boolean dead;

	// \u9ad8\u901f\u5316(\u30eb\u30fc\u30d7\u306e\u5916\u3067\u5b9a\u7fa9)
	PVector steerGood;
	PVector steerBad;
	PVector steer;
	PVector diff;
	PVector desired;
	PVector nearestPos;
	float d;
	float minD2;
	float record;
	float dist;
	int count = 0;

	// \u30b3\u30f3\u30b9\u30c8\u30e9\u30af\u30bf
	Boid (PVector _pos, PVector _vel, int _col) {
		pos = _pos;
		vel = _vel;
		acc = new PVector(0, 0);

		r = random(2.0f, 25);
		mass = 2*r;

		col = _col;
	}

	// \u751f\u7269\u304c\u6b7b\u3093\u3067\u3044\u308b\u304b\u78ba\u8a8d\u3059\u308b\u95a2\u6570
  	public boolean isDead() {
  		if ( lifePoint <= 20.0f ) {
      		return true;

    	} else {
      		return false;
    	}
	}

	public void update() {
		vel.add(acc);
		vel.limit(maxFlockSpeed);
		pos.add(vel);
		acc.mult(0);
	}

	public void applyForce(PVector force) {
		acc.add(force.div(mass));
	}

	public void flock(ArrayList<Boid> boids) {
		sep = separate(boids);
		coh = cohesion(boids);
		ali = align(boids);

		sep.mult(5.0f);
		coh.mult(1.0f);
		ali.mult(1.0f);

		applyForce(sep);
		applyForce(coh);
		applyForce(ali);
	}

	public PVector separate(ArrayList<Boid> boids) {
		steer = new PVector(0,0);
		count = 0;

		for ( Boid other : boids ) {
			d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForSeparation ) {
				diff = PVector.sub(pos, other.pos); //\u6700\u521d\u306b\u5f15\u304d\u7b97\u3057\u3066\u304a\u304f
				diff.normalize();
				diff.div(d); // Weight by distance \u8abf\u7bc0
				steer.add(diff);
				count++;
			}
		} 

		if ( count > 0 ) {
			steer.div(count);

			steer.normalize();
			steer.mult(maxFlockSpeed);
			steer.sub(vel);
			steer.limit(maxFlockForce);
		}

		return steer;
	}

	public PVector cohesion(ArrayList<Boid> boids) {
		steer = new PVector(0,0);
		count = 0;

		for ( Boid other : boids ) {
			d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForCohesion ) {
				diff = PVector.sub(other.pos, pos); //\u6700\u521d\u306b\u5f15\u304d\u7b97\u3057\u3066\u304a\u304f\u3000\u5f15\u304d\u7b97\u9006
				diff.normalize();
				diff.div(d); // Weight by distance \u8abf\u7bc0
				steer.add(diff);
				count++;
			}
		} 

		if ( count > 0 ) {
			steer.div(count);

			steer.normalize();
			steer.mult(maxFlockSpeed);
			steer.sub(vel);
			steer.limit(maxFlockForce);
		}

		return steer;
	}

	public PVector align(ArrayList<Boid> boids) {
		steer = new PVector(0,0);

		for ( Boid other : boids ) {
			d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForAlignment ) {
				steer.add(other.vel);
				count++;
			}
		}

		if ( count > 0 ) {
			steer.div(count);

			steer.normalize();
			steer.mult(maxFlockSpeed);
			steer.sub(vel);
			steer.limit(maxFlockForce);
		}

		return steer;
	}

	public PVector searchNearestTarget(ArrayList<Boid> boids, Boid hunter, boolean bHunt) {
		nearestPos = centerPos; // random large value
		minD2 = 1.0e10f; // random large value
		float tmpMinD2 = 0.0f;

		Boid target;
		float d2;
		for ( int i = boids.size()-1; i >= 0; i-- ) {
			target = boids.get(i);

			diff = PVector.sub(target.pos, pos);
			d2 = diff.magSq();

			if ( d2 < minD2 ) {
				tmpMinD2 = minD2;
				//minD2 = d2; // update

				// \u8996\u91ce\u306e\u6982\u5ff5(\u5185\u7a4d)
				PVector tmpDiff = diff.copy();
				PVector tmpVel = vel.copy();
				tmpDiff.normalize();
				tmpVel.normalize();
				float dot = tmpVel.dot(tmpDiff); // \u5185\u7a4d\u5024:cos

				// \u73fe\u5728\u306e\u4eee\u306e\u6700\u77edpos\u304c\uff0c\u8996\u754c\u5185\u304b\u3064
				// \u30a8\u30ea\u30a2\u5185\u3067\u3042\u308c\u3070\uff0cnearestPos\u3092update
				if ( dot > 0 ) { // > 0.5\u3067\u8996\u754c120\u00b0  > 0\u3067180\u00b0
					if ( height*0.6f > PVector.dist(centerPos, target.pos) ) {
						minD2 = d2; 
						nearestPos = target.pos.copy(); // update
					}

				// \u73fe\u5728\u306e\u4eee\u306e\u6700\u77edpos\u304c\uff0c\u8996\u754c\u5916\u3067\u3042\u308c\u3070\uff0cpos\u3092\u66f4\u65b0\u3057\u306a\u3044
				} else {
					minD2 = tmpMinD2; // not update
					continue;
				}

				// Hunt
				if ( minD2 < 1000 && bHunt ) {
					boids.remove(i);

					hunter.pos.add(vel.mult(1.01f)); // \u52a0\u901f
					lifePoint += random(50, 100);
				}
			}
		}

		return nearestPos.copy(); // Not nearestPos;
	}

	// G.A.
	public Boid searchNearestBoid(ArrayList<Boid> boids) {
		Boid nearestBoid = new Boid(new PVector(0,0), new PVector(0, 0), color(0) );
		nearestPos = new PVector(2000, 0);
		minD2 = 1.0e10f;

		float d2;
		for ( Boid b : boids) {
			diff = PVector.sub(b.pos, pos);
			d2 = diff.magSq();

			if (d2 < minD2 && d2 != 0.0f) { // \u81ea\u5206\u81ea\u8eab\u306f\u9664\u304f
				// update
				minD2 = d2; 
				nearestPos = b.pos.copy();
				nearestBoid = b;
			}
		}

		return nearestBoid;
		//return nearestPos.copy(); // Not nearestPos;
	}

	public PVector searchNearestFlower(ArrayList<Boid> boids, ArrayList<Flower> flowers) {
		// random large value
		nearestPos = new PVector(2000, 0);
		minD2 = 1.0e10f;
		boolean isInit = true;

		Flower f;
		float d2;
		//for ( Flower f : flowers ) {
		for (int i = flowers.size()-1 ; i >= 0 ; i--) {
			f = flowers.get(i);    

			diff = PVector.sub(f.pos, pos);
			d2 = diff.magSq();

			if ( d2 < minD2 ) {
				minD2 = d2;
				nearestPos = f.pos.copy();

				// too close to flowers
				if ( minD2 < 1000 && isInit ) {
					isInit = false;
        			lifePoint += random(50, 120); // recover lifePoint
        			f.dead = true; // gradually disappear
        		}
			}
		}

		return nearestPos.copy(); // Not nearestPos;
	}

	public void eatButterfly(ArrayList<Boid> butterflys) {
        record = 10000;
        nearestPos = null;

        Boid b;
        for ( int i = butterflys.size()-1; i >= 0; i-- ) {
        	b = butterflys.get(i);
            dist = pos.dist(b.pos);

            // \u63a5\u89e6
            if ( dist < 3*maxSeekSpeed ) {
            	butterflys.remove(i);
                lifePoint += b.lifePoint; // \u76f8\u624b\u306elifePoint\u5206\u56de\u5fa9

            } else {
                if ( dist < record ) {
                    record = dist;
                    nearestPos = b.pos.copy();
                }
            }
        }

        if ( nearestPos != null ) seek(nearestPos);
    }

	// Reinforcement Learning
	public void behaviors(ArrayList<Flower> good, ArrayList<Rock> bad) {
        steerGood = eatFlower( good, random(30, 100), flowerPerception);
        steerBad = eatRock( bad, random(-20, -10), rockPerception);

        steerGood.mult(flowerWeight);
        steerBad.mult(rockWeight);

        applyForce(steerGood);
        applyForce(steerBad);
    }

    public PVector eatFlower(ArrayList<Flower> flowers, float nutrition, float perception) {
        record = 10000;
        nearestPos = null;

        for ( int i = flowers.size()-1; i >= 0; i-- ) {
        	Flower f = flowers.get(i);
            dist = pos.dist(f.pos);

            // \u63a5\u89e6
            if ( dist < 3*maxSeekSpeed ) {
            	f.dead = true; // gradually disappear
                lifePoint += nutrition; // \u6442\u53d6

                // Flower\u306e\u8a55\u4fa1\u5024\uff08\u91cd\u307f\uff09\u4e0a\u3052\u308b
                flowerWeight += 0.05f;
                flowerWeight = constrain(flowerWeight, -2.0f, 2.0f);

            } else {
                // \u77e5\u899a\u7bc4\u56f2\u5185
                if ( dist < record && dist < perception ) {
                    record = dist;
                    nearestPos = f.pos.copy();

                    if ( !bPaintDetails ) {
                    	strokeWeight(10);
                        stroke(f.col);
                        line(pos.x, pos.y, nearestPos.x, nearestPos.y);
                    }
                }
            }
        }

        if ( nearestPos != null ) return brainSeek(nearestPos);

        return new PVector(0, 0);
    }

    public PVector eatRock(ArrayList<Rock> rocks, float nutrition, float perception) {
        record = 10000;
        nearestPos = null;

        for ( int i = rocks.size()-1; i >= 0; i-- ) {
        	Rock r = rocks.get(i);
            dist = pos.dist(r.pos);

            // \u63a5\u89e6
            if ( dist < 0.9f*r.size ) {
            	// fill(0);
            	// ellipse(pos.x, pos.y, 30, 30);

                lifePoint += nutrition;

                // Rock\u306e\u8a55\u4fa1\u5024\uff08\u91cd\u307f\uff09\u4e0b\u3052\u308b
                rockWeight -= 0.1f;
                rockWeight = constrain(rockWeight, -2.0f, 2.0f);

            } else {
                // \u77e5\u899a\u7bc4\u56f2\u5185
                if ( dist < record && dist < perception ) {
                    record = dist;
                    nearestPos = r.pos.copy();

                    if ( !bPaintDetails ) {
                    	strokeWeight(10);
                        stroke(50);
                        line(pos.x, pos.y, nearestPos.x, nearestPos.y);
                    }
                }
            }
        }

        if ( nearestPos != null ) return brainSeek(nearestPos);

        return new PVector(0, 0);
    }

    public PVector brainSeek(PVector targetPos) {
        desired = PVector.sub(targetPos, pos);
        desired.setMag(maxSeekSpeed);

        steer = PVector.sub(desired, vel);
        steer.limit(maxSeekForce);

        return steer;
    }

	public void seek(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);

		desired.normalize();
		desired.mult(maxSeekSpeed);

		steer = PVector.sub(desired, vel);
		steer.limit(maxSeekForce);

		applyForce(steer);
	}

	public PVector brainRunAway(Rock targetRock) {
		diff = PVector.sub(targetRock.pos, pos);
		float distance = diff.mag();

		if ( distance < 2*targetRock.size ) {
			stroke(0);
			line(pos.x, pos.y, targetRock.pos.x, targetRock.pos.y);

			diff.normalize();
			diff.mult(maxRunawaySpeed);

			steer = PVector.sub(diff, vel);
			steer.mult(-5.0f*maxRunawayForce);
			//steer.mult(-1.0); // Run away

			return steer;
		}

		return new PVector(0, 0);
	}

	public void flee(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);
		float distance = desired.mag();

		if ( distance < 100 ) {
			desired.normalize();
			desired.mult(maxRunawaySpeed);

			steer = PVector.sub(desired, vel);
			steer.limit(5.0f*maxRunawayForce);
			steer.mult(-1.0f); // Run away

			applyForce(steer);
		}
	}

	public void fleeFromHand(PVector handPos) {
		desired = PVector.sub(handPos, pos);
		float distance = desired.mag();

		if ( distance < 300 ) {
			desired.normalize();
			desired.mult(maxRunawaySpeed);

			steer = PVector.sub(desired, vel);
			steer.limit(5.0f*maxRunawayForce);
			steer.mult(-3.0f); // Run away \u8981\u8abf\u6574

			applyForce(steer);
		}
	}

	public void arrive(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);
		float distance = desired.mag();

		if ( distance < 500 ) {
			float m = map(distance, 0, 500, 0, maxFlockSpeed);
			desired.setMag(m);
		} else {
			desired.setMag(maxFlockSpeed); // seek\u3068\u540c\u3058\u3053\u3068
		}

		steer = PVector.sub(desired, vel);
		steer.limit(maxFlockForce);

		applyForce(steer);
	}

	public void avoid(ArrayList<Boid> boids) {
		float desiredseparation = 30.0f; // \u57fa\u6e96\u306e\u8ddd\u96e2

		steer = new PVector(0, 0);
		count = 0;

		float distance;
		for ( Boid b : boids ) {
			distance = PVector.dist(pos, b.pos);

			distance -= 100; // Obstacle\u306e\u8868\u9762\u3068\u306e\u8ddd\u96e2\u306b\u3059\u308b\u305f\u3081

			if ( (distance > 0) && (distance < desiredseparation) ) {
				// \u91cd\u8981
				diff = PVector.sub(b.pos, pos);

				float angle = PVector.angleBetween(diff, vel); //\u5185\u7a4d
				if ( angle > HALF_PI ) continue; //\u6b21\u306e\u30eb\u30fc\u30d7\u3078

				PVector crss = vel.cross(diff); //\u5916\u7a4d\u3092\u7528\u3044\u3066\u907f\u3051\u308b\u5411\u304d\u3092\u5224\u5b9a

				int dir = 1;
				if ( crss.z > 0 ) dir = -1;

				diff.rotate(dir * HALF_PI);

				diff.normalize();
				diff.div(distance);

				steer.add(diff);
				count++;
			}
		}

		// \u5e73\u5747
		if ( count > 0 ) {
			steer.div((float)count);
		}

		if ( steer.mag() > 0 ) {
			steer.normalize();
			steer.mult(5.5f*maxFlockSpeed); // 5.5\u306f\u8abf\u6574
			steer.sub(vel);
			steer.limit(5.5f*maxFlockForce);
		}

		applyForce(steer);
	}

	public PVector avoidObs(ArrayList<Rock> rocks) {
		float desiredSeparation = 30.0f; // \u57fa\u6e96\u306e\u8ddd\u96e2

		steer = new PVector(0, 0);
		count = 0;

		float distance;
		for ( Rock r : rocks ) {
			distance = PVector.dist(pos, r.pos);

			distance -= 3*r.size; // Rock\u306e\u8868\u9762\u3068\u306e\u8ddd\u96e2\u306b\u3059\u308b\u305f\u3081

			if ( distance > 0 && distance < desiredSeparation ) {
				// \u91cd\u8981
				diff = PVector.sub(r.pos, pos);

				float angle = PVector.angleBetween(diff, vel); //\u5185\u7a4d
				if ( angle > HALF_PI ) continue; //\u6b21\u306e\u30eb\u30fc\u30d7\u3078

				PVector crss = vel.cross(diff); //\u5916\u7a4d\u3092\u7528\u3044\u3066\u907f\u3051\u308b\u5411\u304d\u3092\u5224\u5b9a

				int dir = 1;
				if ( crss.z > 0 ) dir = -1;

				diff.rotate(dir * PI/3);

				diff.normalize();
				diff.div(distance);

				steer.add(diff);
				count++;

				// \u30c7\u30d0\u30c3\u30b0
				// if ( !bPaintDetails ) {
				// 	strokeWeight(7);
				// 	stroke(255, 255, 0);
				// 	line(f.pos.x, f.pos.y, pos.x, pos.y);
				// }
			}
		}

		// \u5e73\u5747
		if ( count > 0 ) {
			steer.div((float)count);
		}

		if ( steer.mag() > 0 ) {
			steer.normalize();
			steer.mult(7.5f*maxFlockSpeed); // 5.5\u306f\u8abf\u6574
			steer.sub(vel);
			steer.limit(7.5f*maxFlockForce);
		}

		//applyForce(steer);
		return steer;
	}

	public void paint(){
		noStroke();
		fill(col, lifePoint);

		float ang = atan2(vel.y, vel.x);
		PVector v1 = new PVector( pos.x + 0.8f*mass*cos(ang), pos.y + mass*sin(ang) );
		PVector v2 = new PVector( pos.x + 0.8f*mass*0.5f*cos(ang+radians(90+45)), pos.y + 0.8f*mass*0.5f*sin(ang+radians(90+45)) );
		PVector v3 = new PVector( pos.x + 0.8f*mass*0.5f*cos(ang+radians(270-45)), pos.y + 0.8f*mass*0.5f*sin(ang+radians(270-45)) );
		triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
	}
}
class Butterfly extends Boid {
	
	Butterfly (PVector _pos, PVector _vel, int _col, DNA _dna, boolean _isInit) {
		// Boid Class constructor
		super(_pos, _vel, _col);
		shadowCol = color(50, 20);

		// Genetic Algorithm
		dna = _dna;
		headSize        = map(dna.genes[0], 0, 1,  35,  18);
		maxFlockSpeed   = map(dna.genes[0], 0, 1, 3.0f, 10.0f);
		maxFlockForce   = map(dna.genes[1], 0, 1, 2.0f, 7.0f);
		maxSeekSpeed    = map(dna.genes[0], 0, 1, 3.0f, 10.0f);
		maxSeekForce    = map(dna.genes[2], 0, 1, 2.5f, 8.0f);
		maxRunawaySpeed = map(dna.genes[0], 0, 1, 5.0f, 20.0f);
		maxRunawayForce = map(dna.genes[3], 0, 1, 2.0f, 10.0f);

		// \u5f37\u5316\u5b66\u7fd2
		flowerWeight     = map(dna.genes[4], 0, 1, -2, 2);
        rockWeight     	 = map(dna.genes[5], 0, 1, -2, 0);
        flowerPerception = map(dna.genes[6], 0, 1, 0, 100);
        rockPerception   = map(dna.genes[7], 0, 1, 0, 100);

		// \u30d1\u30e9\u30e1\u30fc\u30bf\u8981\u8abf\u6574
		distanceForSeparation = 2*r + headSize*0.5f;
		distanceForCohesion = 2*r + 200;
		distanceForAlignment = 2*r + 200;

		if ( _isInit ) {
      		lifePoint = 255;
    	} else {
      		lifePoint = 30;
      		born = true;
    	}
	}

	// \u9069\u5fdc\u5ea6\u8a08\u7b97
	// \u6575\u3068\u9060\u3044\u3000\u304b\u3064\u3000\u82b1\u306b\u8fd1\u3044\u3000\u3068\u3000\u9069\u5fdc\u5ea6\u304c\u9ad8\u304f\u306a\u308b\u3088\u3046\u306b\u8a2d\u5b9a
  	public void fitness(PVector nearestEnemy, PVector nearestFlower) {
    	float distEnemy = dist(pos.x, pos.y, nearestEnemy.x, nearestEnemy.y);
    	float distFlower = dist(pos.x, pos.y, nearestFlower.x, nearestFlower.y);

    	// \u4e0b\u9650\u3068\u4e0a\u9650\u306e\u5236\u5fa1
    	if ( distEnemy > 2000 ) 	  distEnemy = 2000;
    	else if ( distEnemy < 1.0f )   distEnemy = 1.0f;
    	if ( distFlower < 1.0f ) 	  distFlower = 1.0f;
    	else if ( distFlower > 1000 ) distFlower = 1000;

    	float fitnessForEnemy  = pow( distEnemy/2000, 2 ); // \u6307\u6570\u95a2\u6570\u7684
    	float fitnessForFlower = pow( 1.0f/distFlower, 2 ); // \u6307\u6570\u95a2\u6570\u7684

    	fitness = fitnessForEnemy*0.5f + fitnessForFlower*0.5f;
  	}

	// \u7121\u6027\u751f\u6b96\u306e\u5834\u5408\u306e\u307f\u4f7f\u7528
	public Butterfly asexualReproduce() {
		if ( random(1) < BUTTERFLY_REPRO_PROB ) { // a certain probability
			DNA childDNA = dna.copy(); // \u89aa\u306e\u5b8c\u5168\u306a\u30b3\u30d4\u30fc\u3067\u3042\u308b\u5b50\u3092\u65b0\u3057\u304f\u4f5c\u308b
			childDNA.mutate(0.01f); // mutation
			butterflyGenerations++;

			// \u5b50\u304c\u751f\u307e\u308c\u308b\u4f4d\u7f6e\u306e\u6c7a\u5b9a
			PVector bornPos = PVector.random2D();
			bornPos.mult(random(width*0.40f));
			bornPos.add(new PVector(width*0.5f, height*0.5f));

			return new Butterfly(bornPos, vel.copy(), col, childDNA, false);
		
		} else {
			return null;
		}
	}

	public void update() {
		super.update();

		lifePoint -= 0.5f;

		if ( born && dead ) dead = true;
		if ( lifePoint >= 255.0f ) born = false;
		if ( born && lifePoint < 255.0f ) lifePoint += 10.0f;
    	//if ( dead ) lifePoint -= 20.0;
	}

	public void border() {
		PVector diffToCenter = PVector.sub( centerPos, pos );
		float distToCenter = diffToCenter.mag();

		if ( distToCenter >= width*0.48f ) {
			lifePoint -= 5;
			fitness *= 0.5f; // \u30da\u30ca\u30eb\u30c6\u30a3
			seek( centerPos );
		}
	}

	public void boundaries() {
		desired = null;

		if ( pos.x < width*0.15f ) {
			desired = new PVector(maxSeekSpeed, vel.y);
		} else if ( pos.x > width*0.85f ) {
			desired = new PVector(-maxSeekSpeed, vel.y);
		}

		if ( pos.y < -height*0.1f ) {
			desired = new PVector(vel.x, maxSeekSpeed);
		} else if ( pos.y > height*1.1f ) {
			desired = new PVector(vel.x, -maxSeekSpeed);
		}

		if ( desired != null ) {
			desired.normalize();
			desired.mult(2*maxSeekSpeed);
			steer = PVector.sub(desired, vel);
			steer.limit(maxSeekForce);

			applyForce(steer);
			lifePoint -= 10;
		}
	}

	public void display() {
		noStroke();
		fill(col, lifePoint);

		pushMatrix();
		translate(pos.x, pos.y);
		rotate(HALF_PI + atan2(vel.y, vel.x));
		beginShape();
			curveVertex(0, 0*headSize);
			curveVertex(0.2f*headSize, -0.4f*headSize);
			curveVertex(0.1f*headSize, 0);
			curveVertex(0.45f*headSize, -0.33f*headSize);
			curveVertex(0.7f*headSize, -0.35f*headSize); // \u6700\u53f3\u70b9
			curveVertex(0.5f*headSize, 0.05f*headSize);
			curveVertex(0.2f*headSize, 0.15f*headSize); // \u5207\u308a\u8fbc\u307f
			curveVertex(0.4f*headSize, 0.25f*headSize); // \u518d\u958b
			curveVertex(0.35f*headSize, 0.45f*headSize);
			curveVertex(0.25f*headSize, 0.55f*headSize);
			curveVertex(0.05f*headSize, 0.3f*headSize);

			curveVertex(0, 0.5f*headSize); // \u6700\u4e0b\u70b9

			curveVertex(-0.05f*headSize, 0.3f*headSize);
			curveVertex(-0.2f*headSize, 0.55f*headSize);
			curveVertex(-0.35f*headSize, 0.45f*headSize);
			curveVertex(-0.4f*headSize, 0.25f*headSize);
			curveVertex(-0.2f*headSize, 0.15f*headSize);
			curveVertex(-0.5f*headSize, 0.05f*headSize);
			curveVertex(-0.7f*headSize, -0.35f*headSize);
			curveVertex(-0.45f*headSize, -0.33f*headSize);
			curveVertex(-0.1f*headSize, 0);
			curveVertex(-0.25f*headSize, -0.4f*headSize);

			curveVertex(0, 0*headSize);
			curveVertex(0.1f*headSize, 0);
		endShape();
		popMatrix();
	}

	public void shadow() {
		headSize += 3;
		pos.x += 20;
		noStroke();
		float alpha = map(lifePoint, 0, 200, 0, 100);
		fill(shadowCol, alpha);

		pushMatrix();
		translate(pos.x, pos.y);
		rotate(HALF_PI + atan2(vel.y, vel.x));
		beginShape();
			curveVertex(0, 0*headSize);
			curveVertex(0.2f*headSize, -0.4f*headSize);
			curveVertex(0.1f*headSize, 0);
			curveVertex(0.45f*headSize, -0.33f*headSize);
			curveVertex(0.7f*headSize, -0.35f*headSize); // \u6700\u53f3\u70b9
			curveVertex(0.5f*headSize, 0.05f*headSize);
			curveVertex(0.2f*headSize, 0.15f*headSize); // \u5207\u308a\u8fbc\u307f
			curveVertex(0.4f*headSize, 0.25f*headSize); // \u518d\u958b
			curveVertex(0.35f*headSize, 0.45f*headSize);
			curveVertex(0.25f*headSize, 0.55f*headSize);
			curveVertex(0.05f*headSize, 0.3f*headSize);

			curveVertex(0, 0.5f*headSize); // \u6700\u4e0b\u70b9

			curveVertex(-0.05f*headSize, 0.3f*headSize);
			curveVertex(-0.2f*headSize, 0.55f*headSize);
			curveVertex(-0.35f*headSize, 0.45f*headSize);
			curveVertex(-0.4f*headSize, 0.25f*headSize);
			curveVertex(-0.2f*headSize, 0.15f*headSize);
			curveVertex(-0.5f*headSize, 0.05f*headSize);
			curveVertex(-0.7f*headSize, -0.35f*headSize);
			curveVertex(-0.45f*headSize, -0.33f*headSize);
			curveVertex(-0.1f*headSize, 0);
			curveVertex(-0.25f*headSize, -0.4f*headSize);

			curveVertex(0, 0*headSize);
			curveVertex(0.1f*headSize, 0);
		endShape();
		popMatrix();
		pos.x -= 20;
		headSize -= 3;
	}
}
class Camera {
	PVector pos;
	PVector vel;
	PVector acc;
	float maxSpeed;
	float maxForce;

	float centerPosX, centerPosY;

	PVector desired;
	PVector steer;

	Camera (PVector _pos) {
		pos = _pos;
		vel = new PVector(0, 0);
		acc = new PVector(0, 0);

		// \u8981\u8abf\u7bc0
		maxSpeed = 5.0f;
		maxForce = 0.1f;
	}

	public void update() {
		vel.add(acc);
		vel.limit(maxSpeed);
		pos.add(vel);
		acc.mult(0);
	}

	public void applyForce(PVector force) {
		acc.add(force);
	}

	// \u91cd\u5fc3\u8a08\u7b97
	public PVector searchCenterPos(ArrayList<Boid> butterflys, ArrayList<Boid> birds, ArrayList<Boid> dragons) {
		centerPosX = 0;
		centerPosY = 0;

		for ( Boid b : butterflys ) {
			centerPosX += 0.5f*b.pos.x;
			centerPosY += 0.5f*b.pos.y;
		}

		for ( Boid b : birds ) {
			centerPosX += b.pos.x;
			centerPosY += b.pos.y;
		}

		for ( Boid b : dragons ) {
			// Dragon\u306b\u91cd\u307f\u3092\u4ed8\u3051\u308b
			centerPosX += 20*b.pos.x;
			centerPosY += 20*b.pos.y;
		}

		centerPosX = centerPosX / ( 0.5f*butterflys.size() + birds.size() + 20*dragons.size() );
		centerPosY = centerPosY / ( 0.5f*butterflys.size() + birds.size() + 20*dragons.size());

		PVector centerPos = new PVector(centerPosX, centerPosY);
		return centerPos;
	}

	// targetPos is centerPos
	public void seek(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);

		desired.normalize();
		desired.mult(maxSpeed);

		steer = PVector.sub(desired, vel);
		steer.mult(maxForce);

		applyForce(steer);
	}

	public void arrive(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);
		float distance = desired.mag();

		if ( distance < 300 ) {
			float m = map(distance, 0, 300, 0, maxSpeed);
			desired.setMag(m);
		} else {
			desired.setMag(maxSpeed); // seek\u3068\u540c\u3058\u3053\u3068
		}

		steer = PVector.sub(desired, vel);
		steer.limit(maxForce);

		applyForce(steer);
	}

	public void display() {
		strokeWeight(3);
		stroke(0);
		line(width*0.5f, -height, width*0.5f, 2*height);
		line(-width, height*0.5f, 2*width, height*0.5f);

		noFill();
		ellipse(pos.x, pos.y, 20, 20);


		textSize(40);
		fill(0);
		text("G", pos.x+15, pos.y-15);
	}
}
class Cloud extends Boid {
    float[] dim;
    float[] vertX;
    float[] vertY;
    int mindim = 10;
    int maxdim = 100;
    int num;
    PVector desired;
    PVector steer;
  
    Cloud ( PVector _pos, PVector _vel, int _col) {
        super(_pos, _vel, _col);
        makeCloud();

        maxFlockSpeed = 2.0f;
        maxFlockForce = 0.5f;

        // \u30d1\u30e9\u30e1\u30fc\u30bf\u8981\u8abf\u6574
        distanceForSeparation = 2*r + 400;
        distanceForCohesion = 2*r + width*0.5f;
        distanceForAlignment = 2*r + 500;
    }
  
    public void makeCloud() {
        num = (int)random(20, 25);
        dim = new float[num];
        vertX = new float[num];
        vertY = new float[num];

        for ( int i = 0; i < num; i++ ) {
            dim[i] = random(mindim, maxdim);
            float x = 0.8f*cos(TWO_PI/num * i);
            float y = 0.8f*sin(TWO_PI/num * i);
            vertX[i] = x * dim[i];
            vertY[i] = y * dim[i];
        }
    }

    // \u4e2d\u5fc3\u304b\u3089\u4e00\u5b9a\u8ddd\u96e2\u96e2\u308c\u308b\u3068\uff0c\u4e2d\u5fc3\u3092\u30bf\u30fc\u30b2\u30c3\u30c8\u3068\u3059\u308bseek()\u3092\u547c\u3073\u51fa\u3059
    public void border() {
        PVector diffToCenter = PVector.sub( centerPos, pos );
        float distToCenter = diffToCenter.mag();

        if ( distToCenter >= width*0.35f ) {
            seek( centerPos );
            println("test\n");
        }
    }

    // A force to keep it on screen
  public void boundaries() {
        desired = null;

        if ( pos.x < width*0.15f ) {
            desired = new PVector(maxSeekSpeed, vel.y);
        } else if ( pos.x > width*0.85f ) {
            desired = new PVector(-maxSeekSpeed, vel.y);
        }

        if ( pos.y < -height*0.1f ) {
            desired = new PVector(vel.x, maxSeekSpeed);
        } else if ( pos.y > height*1.1f ) {
            desired = new PVector(vel.x, -maxSeekSpeed);
        }

        if ( desired != null ) {
            desired.normalize();
            desired.mult(maxSeekSpeed);
            PVector steer = PVector.sub(desired, vel);
            steer.limit(maxSeekForce);
            
            applyForce(steer);
        }
    }
  
    public void display() {
        pushMatrix();
        translate(pos.x, pos.y);
    
        fill(col);
        noStroke();
        for ( int i = 0; i < num; i++ ) {
            ellipse(vertX[i], vertY[i], 2*dim[i], 2*dim[i]);
        }

        popMatrix();
    }

    public void shadow() {
        pushMatrix();
        translate(pos.x+100, pos.y);
    
        fill(100, 6);
        noStroke();
        for ( int i = 0; i < num; i++ ) {
            ellipse(vertX[i], vertY[i], 3*dim[i], 3*dim[i]);
        }

        popMatrix();
    }
}
class DNA {
	float[] genes; 

	DNA (int _num) {
		genes = new float[_num];
		for (int i = 0; i < genes.length; i++) {
			genes[i] = random(0, 1);
		}
	}

	DNA (float[] newGenes) {
        genes = newGenes;
    }

    // \u4ea4\u53c9(\u6709\u6027\u751f\u6b96)
    public DNA crossover(DNA partner) {
        float[] childGenes = new float[genes.length];
        int midPoint = PApplet.parseInt(random(genes.length)); // \u533a\u5207\u308a\u3068\u306a\u308b\u70b9\u3092\u6c7a\u3081\u308b
        // Take "half" from one and "half" from the other
        for (int i = 0; i < genes.length; i++) {
            if (i > midPoint)  childGenes[i] = genes[i];
            else               childGenes[i] = partner.genes[i];
        }
        DNA newGenes = new DNA(childGenes);
      
        return newGenes;
    }

    // \u7121\u6027\u751f\u6b96\u306e\u5834\u5408\u306e\u307f\u4f7f\u7528
	// Instead of crossover
	public DNA copy() {
		float[] newGenes = new float[genes.length];
        arrayCopy(genes, newGenes);
   
        return new DNA(newGenes);
    }

	// Mutation
    public void mutate(float mutateRate) {
        for (int i = 0; i < genes.length; i++) {
            if ( random(1) < mutateRate ) {
                genes[i] = random(0, 1);
            }
        }
    }
}
class Dragon extends Boid {
	//float headSize;
	PVector posHead, posRightWing, posLeftWing, posBody, posMed, posTail, posEnd;
	float angHead, angRightWing, angLeftWing, angBody, angMed, angTail;

	int eyeCol = color(255, 255, 0);

	boolean isClose = false;

	Dragon (PVector _pos, PVector _vel, int _col, DNA _dna, boolean _isInit) {
		super( _pos, _vel, _col); // Boid\u30af\u30e9\u30b9\u306e\u30b3\u30f3\u30b9\u30c8\u30e9\u30af\u30bf\u306e\u547c\u3073\u51fa\u3057

		// Genetic Algorithm
		dna = _dna;
		headSize 		= map(dna.genes[0], 0, 1, 10, 6);
		maxFlockSpeed   = map(dna.genes[0], 0, 1, 5.0f, 10.0f);
		maxFlockForce   = map(dna.genes[1], 0, 1, 3.0f, 7.0f);
		maxSeekSpeed    = map(dna.genes[0], 0, 1, 15.0f, 20.0f);
		maxSeekForce    = map(dna.genes[2], 0, 1, 5.0f, 10.0f);


		posHead = _pos;
		posBody = PVector.add( posHead, new PVector(0, 0.4f*headSize) );
		posRightWing = PVector.add( posBody, new PVector(0, 3.0f*headSize) );
		posLeftWing = PVector.add( posBody, new PVector(0, 3.0f*headSize) );
		posMed = PVector.add( posBody, new PVector(0, 6.0f*headSize) );
		posTail = PVector.add( posMed, new PVector(0, 1.8f*headSize) );
		posEnd = PVector.add( posTail, new PVector(0, 1.2f*headSize) );
		angHead = 0;
		angRightWing = 0;
		angLeftWing = 0;
		angBody = 0;
		angMed = 0;
		angTail = 0;
		shadowCol = color( 50, 30 );
	
		// \u30d1\u30e9\u30e1\u30fc\u30bf\u8981\u8abf\u6574
		distanceForSeparation = 2*r + 300;
		distanceForCohesion = 2*r + 300;
		distanceForAlignment = 2*r + 300;
		
		if ( _isInit ) {
			lifePoint = 255;
		} else {
			lifePoint = 30;
			born = true;
		}
	}

	// \u751f\u6b96
	// \u65b0\u3057\u3044\u5b50\u3092\u751f\u6210\u3059\u308b\u95a2\u6570
	public Dragon reproduce(PVector bornPos) {
		if ( random(1) < DRAGON_REPRO_PROB ) { // a certain probability
			DNA childDNA = dna.copy(); // \u89aa\u306e\u5b8c\u5168\u306a\u30b3\u30d4\u30fc\u3067\u3042\u308b\u5b50\u3092\u65b0\u3057\u304f\u4f5c\u308b
			childDNA.mutate(0.01f); // mutation
			dragonGenerations++;

			// \u5b50\u304c\u751f\u307e\u308c\u308b\u4f4d\u7f6e\u306e\u6c7a\u5b9a
			//PVector bornPos = new PVector(random(width), random(height*0.7, height));
			bornPos.add(new PVector(0, random(200, 500)));

			return new Dragon(bornPos, vel.copy(), col, childDNA, false);
		
		} else {
			return null;
		}
	}

	// A force to keep it on screen
	public void boundaries() {
		desired = null;

		if ( pos.x < width*0.15f ) {
			desired = new PVector(2*maxSeekSpeed, vel.y);
		} else if ( pos.x > width*0.85f ) {
			desired = new PVector(-2*maxSeekSpeed, vel.y);
		}

		if ( pos.y < -height*0.1f ) {
			desired = new PVector(vel.x, 2*maxSeekSpeed);
		} else if ( pos.y > height*1.1f ) {
			desired = new PVector(vel.x, -2*maxSeekSpeed);
		}

		if ( desired != null ) {
			desired.normalize();
			desired.mult(2*maxSeekSpeed);
			steer = PVector.sub(desired, vel);
			steer.limit(maxSeekForce);

			applyForce(steer);
			lifePoint -= 3;
		}
	}

	public void update() {
	    super.update();

	    lifePoint -= 1.0f;
	    if ( born && lifePoint < 255.0f ) lifePoint += 10.0f;
	    if ( lifePoint >= 255.0f ) born = false;

	    //posHead = new PVector(pos.x, pos.y);
	    angHead = atan2(vel.y, vel.x);
	    // atan2(y, x) : \u70b9(vel.\uff58, vel.\uff59)\u3068x\u8ef8\u306e\u89d2\u5ea6(\u03b8)\u3092\u8fd4\u3059\u3002\u3064\u307e\u308a\uff0c\u4f4d\u7f6e\u304b\u3089\u89d2\u5ea6\u3092\u5272\u308a\u51fa\u305b\u308b\uff0e
    	// \u89d2\u5ea6\u306f\u3001y \u3092 x \u3067\u5272\u3063\u305f\u89d2\u5ea6\u3067\u3001-PI\u304b\u3089PI\u306e\u7bc4\u56f2\u306e\u5b9f\u6570

	    if (isClose) {
	      angHead += 0.5f*sin(0.4f*frameCount);
	      isClose = false;
	    }
	    
	    posBody = PVector.sub( posHead, new PVector(0.4f*headSize*cos(angHead), 0.4f*headSize*sin(angHead)) );
	    angBody = atan2( posBody.y-posMed.y, posBody.x-posMed.x );

	    posRightWing = PVector.sub( posBody, new PVector(3.0f*headSize*cos(angBody), 3.0f*headSize*sin(angBody)) );
	    angRightWing = atan2( posRightWing.y-posMed.y, posRightWing.x-posMed.x );

	    posLeftWing = PVector.sub( posBody, new PVector(3.0f*headSize*cos(angBody), 3.0f*headSize*sin(angBody)) );
	    angLeftWing = atan2( posLeftWing.y-posMed.y, posLeftWing.x-posMed.x );
	    
	    posMed = PVector.sub( posBody, new PVector(6.0f*headSize*cos(angBody), 6.0f*headSize*sin(angBody)) );
	    angMed = atan2( posMed.y-posTail.y,posMed.x-posTail.x );
	    
	    posTail = PVector.sub( posMed, new PVector(1.8f*headSize*cos(angMed), 1.8f*headSize*sin(angMed)) );
	    angTail = atan2( posTail.y-posEnd.y,posTail.x-posEnd.x );
	    
	    posEnd = PVector.sub( posTail, new PVector(1.2f*headSize*cos(angTail), 1.2f*headSize*sin(angTail)));
  	}
  	
  	public void shadow() {
  		headSize += 2;
  		posHead.x += 50;
		posRightWing.x += 50;
  		posLeftWing.x += 50;
  		posBody.x += 50;
  		posMed.x += 50;
  		posTail.x += 50;
  		noStroke();
		paintHead(posHead, angHead, shadowCol, shadowCol);
		paintRightWing(posRightWing, angRightWing, shadowCol);
		paintLeftWing(posLeftWing, angLeftWing, shadowCol);
		paintBody(posBody, angBody, shadowCol);
		paintMed(posMed, angMed, shadowCol);
		paintTail(posTail, angTail, shadowCol);
		posHead.x -= 50;
  		posRightWing.x -= 50;
  		posLeftWing.x -= 50;
  		posBody.x -= 50;
  		posMed.x -= 50;
  		posTail.x -= 50;
  		headSize -= 2;
  	}

	public void display() {
		noStroke();
		paintHead(posHead, angHead, col, eyeCol);
		paintRightWing(posRightWing, angRightWing, col);
		paintLeftWing(posLeftWing, angLeftWing, col);
		paintBody(posBody, angBody, col);
		paintMed(posMed, angMed, col);
		paintTail(posTail, angTail, col);
	}

	public void paintHead(PVector cen, float ang, int c, int eye) { 
    	fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x,cen.y);
      	rotate(HALF_PI + ang); //\u6642\u8a08\u56de\u308a\u306b\u56de\u8ee2
      	beginShape();
        	curveVertex(0, -3.0f*headSize); // \u9854\u306e\u5148\u7aef
      
      		// \u9854\u306e\u53f3\u534a\u5206
        	curveVertex(0.6f*headSize, -1.5f*headSize); 
        	curveVertex(0.95f*headSize, -0.3f*headSize);
        	curveVertex(1.1f*headSize, headSize); // \u53f3\u8033\u306e\u5148\u7aef
        	curveVertex(0.7f*headSize, 0.5f*headSize);
        	curveVertex(0.5f*headSize, 0.2f*headSize);
       
	        curveVertex(0, 0); // \u982d\u9802\u90e8

	      	// \u9854\u306e\u5de6\u534a\u5206
	      	curveVertex(-0.5f*headSize, 0.2f*headSize);
	        curveVertex(-0.7f*headSize, 0.5f*headSize);
	        curveVertex(-1.1f*headSize, headSize); // \u5de6\u8033\u306e\u5148\u7aef
	        curveVertex(-0.95f*headSize, -0.3f*headSize);
	        curveVertex(-0.6f*headSize, -1.5f*headSize);      	

	        curveVertex(0, -3.0f*headSize); // \u9854\u306e\u5148\u7aef

	        curveVertex(0.6f*headSize,-1.5f*headSize);
	        curveVertex(0.95f*headSize,-0.3f*headSize);
	    endShape();

	     // \u76ee
	     fill(eye); // Yellow
	     beginShape();
	        curveVertex(0.4f*headSize,-0.7f*headSize);
	        curveVertex(0.65f*headSize,-0.4f*headSize);
	        curveVertex(0.85f*headSize,0.2f*headSize);
	        curveVertex(0.6f*headSize,-0.1f*headSize);
	        curveVertex(0.5f*headSize,-0.4f*headSize);
	        curveVertex(0.4f*headSize,-0.7f*headSize);
	        curveVertex(0.65f*headSize,-0.4f*headSize);
	     endShape();
	     beginShape();
	        curveVertex(-0.4f*headSize,-0.7f*headSize);
	        curveVertex(-0.65f*headSize,-0.4f*headSize);
	        curveVertex(-0.85f*headSize,0.2f*headSize);
	        curveVertex(-0.6f*headSize,-0.1f*headSize);
	        curveVertex(-0.5f*headSize,-0.4f*headSize);
	        curveVertex(-0.4f*headSize,-0.7f*headSize);
	        curveVertex(-0.65f*headSize,-0.4f*headSize);
	      endShape();
	    popMatrix();
	}

	public void paintRightWing(PVector cen, float ang, int c){
		fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x, cen.y);
      	rotate(HALF_PI + ang);
      	beginShape();
      		curveVertex(0.5f*headSize, 0);

      		curveVertex(0, -2.0f*headSize);
      		curveVertex(1.0f*headSize, -1.2f*headSize);
      		curveVertex(1.5f*headSize, -1.0f*headSize);
      		curveVertex(2.0f*headSize, -0.8f*headSize);
      		curveVertex(2.5f*headSize, -0.8f*headSize);
      		curveVertex(4.0f*headSize, -0.85f*headSize);
      		curveVertex(3.5f*headSize, -0.9f*headSize);
      		curveVertex(4.0f*headSize, -0.95f*headSize);
      		curveVertex(4.5f*headSize, -1.0f*headSize);
      		curveVertex(5.5f*headSize, -1.2f*headSize);
      		curveVertex(6.5f*headSize, -1.5f*headSize);
      		curveVertex(7.5f*headSize, -2.0f*headSize);
      		curveVertex(8.5f*headSize, -1.5f*headSize);
      		curveVertex(9.5f*headSize, -0.5f*headSize);
      		curveVertex(10.0f*headSize, 2.0f*headSize); // \u6700\u53f3\u70b9
      		curveVertex(8.5f*headSize, 0.5f*headSize);
      		curveVertex(7.0f*headSize, 0.9f*headSize);
      		curveVertex(5.5f*headSize, 0.4f*headSize);
      		curveVertex(4.0f*headSize, 0.8f*headSize);
      		curveVertex(2.5f*headSize, 0.5f*headSize);
      		curveVertex(1.0f*headSize, 0.3f*headSize);
      		curveVertex(0.5f*headSize, 1.0f*headSize);
      		curveVertex(0.1f*headSize, 1.5f*headSize);

      		curveVertex(0, -2.0f*headSize);
      	endShape();
      	popMatrix();
	}

	public void paintLeftWing(PVector cen, float ang, int c) {
		fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x, cen.y);
      	rotate(HALF_PI + ang);
      	beginShape();
      		curveVertex(-0.5f*headSize, 0);

      		curveVertex(0, -2.0f*headSize);
      		curveVertex(-1.0f*headSize, -1.2f*headSize);
      		curveVertex(-1.5f*headSize, -1.0f*headSize);
      		curveVertex(-2.0f*headSize, -0.8f*headSize);
      		curveVertex(-2.5f*headSize, -0.8f*headSize);
      		curveVertex(-4.0f*headSize, -0.85f*headSize);
      		curveVertex(-3.5f*headSize, -0.9f*headSize);
      		curveVertex(-4.0f*headSize, -0.95f*headSize);
      		curveVertex(-4.5f*headSize, -1.0f*headSize);
      		curveVertex(-5.5f*headSize, -1.2f*headSize);
      		curveVertex(-6.5f*headSize, -1.5f*headSize);
      		curveVertex(-7.5f*headSize, -2.0f*headSize);
      		curveVertex(-8.5f*headSize, -1.5f*headSize);
      		curveVertex(-9.5f*headSize, -0.5f*headSize);
      		curveVertex(-10.0f*headSize, 2.0f*headSize); // \u6700\u5de6\u70b9
      		curveVertex(-8.5f*headSize, 0.5f*headSize);
      		curveVertex(-7.0f*headSize, 0.9f*headSize);
      		curveVertex(-5.5f*headSize, 0.4f*headSize);
      		curveVertex(-4.0f*headSize, 0.8f*headSize);
      		curveVertex(-2.5f*headSize, 0.5f*headSize);
      		curveVertex(-1.0f*headSize, 0.3f*headSize);
      		curveVertex(-0.5f*headSize, 1.0f*headSize);
      		curveVertex(-0.1f*headSize, 1.5f*headSize);

      		curveVertex(0, -2.0f*headSize);
      	endShape();
      	popMatrix();
	}

  	public void paintBody(PVector cen, float ang, int c) {
    	fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x, cen.y);
      	rotate(HALF_PI + ang);
      	beginShape();
	        curveVertex(0, -0.6f*headSize);
	      
	        curveVertex(0.3f*headSize, -0.4f*headSize);
	        curveVertex(0.4f*headSize, 0.0f);
	        curveVertex(0.45f*headSize, 0.5f*headSize);
	        curveVertex(0.5f*headSize, 1.0f*headSize);
	        curveVertex(0.55f*headSize, 1.5f*headSize);
	        curveVertex(0.65f*headSize, 2.0f*headSize);
	        curveVertex(0.7f*headSize, 2.5f*headSize);
	        curveVertex(0.75f*headSize, 3.0f*headSize);
	        curveVertex(0.8f*headSize, 4.0f*headSize);
	        curveVertex(0.85f*headSize, 4.5f*headSize);
	        curveVertex(0.9f*headSize, 5.0f*headSize);
	        curveVertex(0.95f*headSize, 6.0f*headSize);
	        curveVertex(1.0f*headSize, 7.0f*headSize);
	       
	        curveVertex(0, 8.0f*headSize); // \u80f4\u4f53\u306e\u6700\u4e0b\u90e8
	     
	      	curveVertex(-1.0f*headSize, 7.0f*headSize);
			curveVertex(-0.95f*headSize, 6.0f*headSize);
			curveVertex(-0.9f*headSize, 5.0f*headSize);
			curveVertex(-0.85f*headSize, 4.5f*headSize);
			curveVertex(-0.8f*headSize, 4.0f*headSize); 
			curveVertex(-0.75f*headSize, 3.0f*headSize);    
			curveVertex(-0.7f*headSize, 2.5f*headSize);
			curveVertex(-0.65f*headSize, 2.0f*headSize);
			curveVertex(-0.55f*headSize, 1.5f*headSize);
			curveVertex(-0.5f*headSize, 1.0f*headSize);
			curveVertex(-0.45f*headSize, 0.5f*headSize);
			curveVertex(-0.4f*headSize, 0.0f);
			curveVertex(-0.3f*headSize, -0.4f*headSize);
	      
	        curveVertex(0, -0.6f*headSize);

	        curveVertex(0.3f*headSize, -0.4f*headSize);
	        curveVertex(0.4f*headSize, 0.0f);
      	endShape();

    	popMatrix();
  }

  	public void paintMed(PVector cen, float ang, int c) {
  		fill(c, lifePoint);
    	pushMatrix();
    	translate(cen.x, cen.y);
    	rotate(HALF_PI + ang);
    	beginShape();
	    	curveVertex(0, 0);

	    	curveVertex(0.9f*headSize, 0.5f*headSize);
	    	curveVertex(1.0f*headSize, 0.6f*headSize);
	    	curveVertex(1.5f*headSize, 0.7f*headSize);
	    	curveVertex(1.7f*headSize, 0.8f*headSize);
	    	curveVertex(3.0f*headSize, 1.5f*headSize);
	    	curveVertex(3.5f*headSize, 3.5f*headSize); // \u53f3\u8db3\u5148\u7aef\u90e8
	    	curveVertex(2.7f*headSize, 2.2f*headSize);
	    	curveVertex(1.5f*headSize, 2.0f*headSize);
	    	curveVertex(1.0f*headSize, 1.8f*headSize);
	    	curveVertex(0.9f*headSize, 2.5f*headSize);
	    	curveVertex(0.8f*headSize, 3.0f*headSize);

	    	curveVertex(0, 4.0f*headSize);

	    	curveVertex(-0.8f*headSize, 3.0f*headSize);
	    	curveVertex(-0.9f*headSize, 2.5f*headSize);
	    	curveVertex(-1.0f*headSize, 1.8f*headSize);
	    	curveVertex(-1.5f*headSize, 2.0f*headSize);
	    	curveVertex(-2.7f*headSize, 2.2f*headSize);
	    	curveVertex(-3.5f*headSize, 3.5f*headSize); // \u5de6\u8db3\u5148\u7aef\u90e8
	    	curveVertex(-3.0f*headSize, 1.5f*headSize);
	    	curveVertex(-1.7f*headSize, 0.8f*headSize);
	    	curveVertex(-1.5f*headSize, 0.7f*headSize);
	    	curveVertex(-1.0f*headSize, 0.6f*headSize);
	    	curveVertex(-0.9f*headSize, 0.5f*headSize);

	    	curveVertex(0, 0);

	    	curveVertex(0.9f*headSize, 0.5f*headSize);
	    	curveVertex(1.0f*headSize, 0.6f*headSize);
	    endShape();
	    popMatrix();
  }

  public void paintTail(PVector cen, float ang, int c){
    	fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x,cen.y);
      	rotate(HALF_PI + ang); // \u6642\u8a08\u56de\u308a\u306b\u56de\u8ee2
      	beginShape();
      		curveVertex(0, 0);

      		curveVertex(0.8f*headSize, 1.2f*headSize);
      		curveVertex(0.75f*headSize, 1.5f*headSize);
      		curveVertex(0.65f*headSize, 2.0f*headSize);
      		curveVertex(0.6f*headSize, 2.5f*headSize);
      		curveVertex(0.35f*headSize, 6.0f*headSize);
      		curveVertex(0.3f*headSize, 6.5f*headSize);
      		curveVertex(0.25f*headSize, 7.0f*headSize);
      		curveVertex(0.2f*headSize, 7.5f*headSize);
      		curveVertex(0.15f*headSize, 7.8f*headSize);
      		curveVertex(0.1f*headSize, 8.0f*headSize);
      		curveVertex(0.05f*headSize, 9.0f*headSize);

      		curveVertex(0, 10.0f*headSize);

      		curveVertex(-0.05f*headSize, 9.0f*headSize);
      		curveVertex(-0.1f*headSize, 8.0f*headSize);
      		curveVertex(-0.15f*headSize, 7.8f*headSize);
      		curveVertex(-0.2f*headSize, 7.5f*headSize);
      		curveVertex(-0.25f*headSize, 7.0f*headSize);
      		curveVertex(-0.3f*headSize, 6.5f*headSize);
      		curveVertex(-0.35f*headSize, 6.0f*headSize);
      		curveVertex(-0.6f*headSize, 2.5f*headSize);
      		curveVertex(-0.65f*headSize, 2.0f*headSize);
      		curveVertex(-0.75f*headSize, 1.5f*headSize);
      		curveVertex(-0.8f*headSize, 1.2f*headSize);

      		curveVertex(0, 0);

      	endShape();
      	popMatrix();
  }

  	public void paint(){
		noStroke();
		fill(col, lifePoint);

		float ang = atan2(vel.y, vel.x);
		PVector v1 = new PVector( pos.x + 50*cos(ang), pos.y + 50*sin(ang) );
		PVector v2 = new PVector( pos.x + 50*0.5f*cos(ang+radians(90+45)), pos.y + 50*0.5f*sin(ang+radians(90+45)) );
		PVector v3 = new PVector( pos.x + 50*0.5f*cos(ang+radians(270-45)), pos.y + 50*0.5f*sin(ang+radians(270-45)) );
		triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
	}

  	public void border() {
		PVector diffToCenter = PVector.sub( centerPos, pos );
		float distToCenter = diffToCenter.mag();

		if ( distToCenter >= width*0.55f ) {
			lifePoint -= 10;
			if ( lifePoint < 50 && lifePoint > 40 ) {
				PVector newPos = PVector.random2D();
				newPos.mult(random(width*0.4f));
				newPos.add(centerPos);

				flockingSystem.makeDragon(newPos);
			}
			seek( centerPos );
		}
	}
}
class FlockingSystem {
	ArrayList<Boid> butterflys;
	ArrayList<Boid> birds;
	ArrayList<Boid> dragons;
	//ArrayList<Boid> fishes;
	//ArrayList<Boid> clouds;

	ArrayList<Boid> matingPool; // \u4ea4\u914d\u30d7\u30fc\u30eb

	// ----- \u30bb\u30eb\u7a7a\u9593\u5206\u5272\u6cd5 -----
	ArrayList<Boid>[][] gridForButterfly;
	ArrayList<Boid>[][] gridForBird;
	ArrayList<Flower>[][] gridForFlower;
	ArrayList<Rock>[][] gridForRock;
	ArrayList<Boid> targetButterflys;
	ArrayList<Boid> targetBirds;
	ArrayList<Flower> targetFlowers;
	ArrayList<Rock> targetRocks;
	int scl = 80; // 1\u30bb\u30eb\u306e\u30b5\u30a4\u30ba(\u3053\u306e\u5024\u3092\u5c0f\u3055\u304f\u3059\u308b\u307b\u3069\u30b5\u30a4\u30af\u30eb\u6570\u304c\u6e1b\u308b)
	int cols, rows;
	int cellX, cellY;
	// -----------------------

	// \u9ad8\u901f\u5316\u306e\u305f\u3081\u306b\u30eb\u30fc\u30d7( run()\u95a2\u6570 )\u306e\u5916\u3067\u5b9a\u7fa9
	Boid b;
	Boid targetBird;
	Boid targetDragon;
	Boid nearestButterfly;
	Boid nearestBird;
	Butterfly butterfly;
	Butterfly childButterfly;
	Bird bird;
	Bird childBird;
	Dragon dragon;
	Dragon childDragon;
	PVector targetButterflyPos;
	PVector targetBirdPos;
	PVector targetFlowerPos;
	PVector bornPos;
	float nearestDist;
	float dButterfly;
	float dBird;
	DNA childDNA;


	FlockingSystem (PVector pos) {
		// About Butterfly
		butterflys = new ArrayList<Boid>();
		addButterflys(pos);

		// About Bird
		birds = new ArrayList<Boid>();
		addBirds(pos);

		// About Dragon
		dragons = new ArrayList<Boid>();
		addDragons(pos);

		// About Fish
		//fishes = new ArrayList<Boid>();
		//addFishes(pos);

		// About Cloud
		// clouds = new ArrayList<Boid>();
		// addClouds();

		// Create matingPool
		matingPool = new ArrayList<Boid>();


		// Calculate cols & rows
		cols = (int) (width*1.5f / scl);
  		rows = (int) (height*1.5f / scl);

		// 2\u6b21\u5143\u914d\u5217\u3068\u3057\u3066grid\u3092\u521d\u671f\u5316
  		gridForButterfly = new ArrayList[cols][rows];
  		gridForBird      = new ArrayList[cols][rows];
  		gridForFlower    = new ArrayList[cols][rows];
  		gridForRock      = new ArrayList[cols][rows];
  		for (int i = 0; i < cols; i++) {
    		for (int j = 0; j < rows; j++) {
      			gridForButterfly[i][j] = new ArrayList<Boid>();
      			gridForBird[i][j]      = new ArrayList<Boid>();
      			gridForFlower[i][j]    = new ArrayList<Flower>();
      			gridForRock[i][j]      = new ArrayList<Rock>();
    		}
  		}

		targetButterflys = new ArrayList<Boid>();
		targetBirds      = new ArrayList<Boid>();
		targetFlowers    = new ArrayList<Flower>();
		targetRocks      = new ArrayList<Rock>();

		// Rock \u30bb\u30eb\u306e\u5272\u308a\u5f53\u3066
		cellAssignmentForRock(rockSystem.rocks, gridForRock);
	}

	public void addButterflys(PVector pos) {
		for ( int i = 0; i < BUTTERFLY_NUM; i++ ) {
			PVector initPos = new PVector(random(width*0.15f, width*0.45f), random(height*0.6f) );
			PVector initVec = PVector.random2D().mult(0);
			int col;
			if ( i < BUTTERFLY_NUM*0.5f ) col = color(30, 180, 255); // \u30e1\u30b9
			else 						 col = color(200, 240, 240); // \u30aa\u30b9
			DNA dna = new DNA(8);
			butterflys.add( new Butterfly(initPos, initVec, col, dna, true) );
		}
	}

	public void addBirds(PVector pos) {
		for ( int i = 0; i < BIRD_NUM; i++ ) {
			PVector initPos = new PVector(random(width*0.55f, width*0.85f), random(height*0.6f) );
			PVector initVec = PVector.random2D().mult(0);
			int col;
			if ( i < BIRD_NUM*0.5f ) col = color(255, 100, 0); // \u30e1\u30b9
			else 					col = color(255, 200, 0); // \u30aa\u30b9
			DNA dna = new DNA(8);
			birds.add( new Bird(initPos, initVec, col, dna, true) );
		}
	}

	public void addDragons(PVector pos) {
		for ( int i = 0; i < DRAGON_NUM; i++ ) {
			PVector initPos = new PVector(random(width*0.2f, width*0.8f), random(height*0.5f, height*0.9f) );
			PVector initVec = PVector.random2D().mult(0);
			int col = color(40, 40, 100);
			DNA dna = new DNA(4);
			dragons.add( new Dragon(initPos, initVec, col, dna, true) );
		}
	}

	// void addFishes(PVector pos) {
	// 	for ( int i = 0; i < FISH_NUM; i++ ) {
	// 		PVector initPos = PVector.random2D();
	// 		initPos.mult(random(width*0.57, width*0.60));
	// 		initPos.add(pos);
	// 		PVector initVec = new PVector(0,0);
	// 		color col = color(10);
	// 		fishes.add( new Fish(FISH_SIZE+(int)random(-3, 12), initPos, initVec, col) );
	// 	}
	// }

	// void addClouds() {
	// 	for ( int i = 0; i < CLOUD_NUM; i++ ) {
	// 		PVector initPos = new PVector( random(width), random(height) );
	// 		initPos.add( new PVector( random(-10,10), random(-10,10) ) );
	// 		PVector initVec = PVector.random2D().mult(0);
	// 		color col = color( random(230, 255), random(50, 100) );
	// 		clouds.add( new Cloud(initPos, initVec, col) );
	// 	}
	// }

	// Make a new Creature
  	public void makeButterfly(PVector bornPos, int col) {
    	PVector vel = new PVector(0, 0);
    	DNA dna = new DNA(8);

    	butterflys.add( new Butterfly(bornPos.copy(), vel, col, dna, false) );

    	// Debug
  		fill(col);
  		ellipse(bornPos.copy().x, bornPos.copy().y, 70, 70);
  	}

  	public void makeBird(PVector bornPos, int col) {
    	PVector vel = new PVector(0, 0);
    	DNA dna = new DNA(8);

    	birds.add( new Bird(bornPos.copy(), vel, col, dna, false) );

    	// Debug
  		fill(col);
  		ellipse(bornPos.copy().x, bornPos.copy().y, 70, 70);
  	}

  	public void makeDragon(PVector bornPos) {
  		PVector vec = PVector.random2D().mult(0);
  		int col = color(40, 40, 100);
  		DNA dna = new DNA(4);

  		dragons.add( new Dragon(bornPos.copy(), vec, col, dna, false) );

  		// Debug
  		fill(col);
  		ellipse(bornPos.copy().x, bornPos.copy().y, 70, 70);
  	}

  	// \u5272\u308a\u5f53\u3066\u305f\u60c5\u5831\u3092\u30ea\u30bb\u30c3\u30c8
	public void resetGridForBoid(ArrayList<Boid> grid[][]) {
	  	for (int i = 0; i < cols; i++)
	    	for (int j = 0; j < rows; j++)
	      		grid[i][j].clear();
	}

	public void resetGridForFlower(ArrayList<Flower> grid[][]) {
	  	for (int i = 0; i < cols; i++)
	    	for (int j = 0; j < rows; j++)
	      		grid[i][j].clear();
	}

	// Boid\u306b\u5bfe\u3057\u3066\uff0c\u4f4d\u7f6e\u306b\u5fdc\u3058\u305f\u9069\u5207\u306a\u30bb\u30eb\u306b\u81ea\u8eab\u3092\u767b\u9332\u3059\u308b
	public void cellAssignmentForBoid(ArrayList<Boid> boids, ArrayList<Boid> grid[][]) {
	  	for (Boid b : boids) {
	    	// \u305d\u306eBoid\u304c\uff0c\u3069\u306e\u30bb\u30eb\u306b\u5c5e\u3059\u308b\u304b\u3092\u6c7a\u3081\u308b
	    	cellX = PApplet.parseInt(b.pos.x) / scl; 
	    	cellY = PApplet.parseInt(b.pos.y) / scl;

	    	if ( cellX >= 0 && cellX < cols )
	    		if ( cellY >= 0 && cellY < rows )
	    			grid[cellX][cellY].add(b);
	  	}
	}

	public void cellAssignmentForFlower(ArrayList<Flower> flowers, ArrayList<Flower> grid[][]) {
	  	for (Flower f : flowers) {
	    	// \u305d\u306eBoid\u304c\uff0c\u3069\u306e\u30bb\u30eb\u306b\u5c5e\u3059\u308b\u304b\u3092\u6c7a\u3081\u308b
	    	cellX = PApplet.parseInt(f.pos.x) / scl; 
	    	cellY = PApplet.parseInt(f.pos.y) / scl;

	    	if ( cellX >= 0 && cellX < cols )
	    		if ( cellY >= 0 && cellY < rows )
	    			grid[cellX][cellY].add(f);
	  	}
	}

	public void cellAssignmentForRock(ArrayList<Rock> rocks, ArrayList<Rock> grid[][]) {
	  	for (Rock r : rocks) {
	    	// \u305d\u306eBoid\u304c\uff0c\u3069\u306e\u30bb\u30eb\u306b\u5c5e\u3059\u308b\u304b\u3092\u6c7a\u3081\u308b
	    	cellX = PApplet.parseInt(r.pos.x) / scl; 
	    	cellY = PApplet.parseInt(r.pos.y) / scl;

	    	if ( cellX >= 0 && cellX < cols )
	    		if ( cellY >= 0 && cellY < rows )
	    			grid[cellX][cellY].add(r);
	  	}
	}

	public void run() {
		// \u30bb\u30eb\u60c5\u5831\u306e\u30ea\u30bb\u30c3\u30c8
		resetGridForBoid(gridForButterfly);
		resetGridForBoid(gridForBird);
		resetGridForFlower(gridForFlower);

		// \u30bb\u30eb\u306e\u5272\u308a\u5f53\u3066
		cellAssignmentForBoid(butterflys, gridForButterfly); 
		cellAssignmentForBoid(birds, gridForBird);
		cellAssignmentForFlower(flowerSystem.flowers, gridForFlower);

		// \u30bb\u30eb\u7a7a\u9593\u5206\u5272\u6cd5(\u30eb\u30fc\u30d7\u306e\u56de\u6570\u304c\u5927\u5e45\u6e1b)
	  	//stroke(255);
	  	for (int c = 0; c < cols; c++) {
	    	//line(c*scl, 0, c*scl, height);
	    	for (int r = 0; r < rows; r++) {
	      		//line(0, r*scl, width, r*scl);

	      		// \u5404\u7a2e\u51e6\u7406\u306e\u5bfe\u8c61\u3068\u306a\u308bBoids\u3092\u66f4\u65b0
	      		targetButterflys.clear();
	      		targetBirds.clear();
	      		targetFlowers.clear();
	      		targetRocks.clear();

			    // \u6ce8\u76ee\u30bb\u30eb\u306e8\u8fd1\u508d\u306b\u5c5e\u3059\u308bBoid\u3092\u767b\u9332
			    for (int n = -1; n <= 1; n++) {
	     	  		for (int m = -1; m <= 1; m++) {
	     	  			if (c+n >= 0 && c+n < cols && r+m >= 0 && r+m< rows) {
	     	  				for ( Boid butter : gridForButterfly[c+n][r+m] ) {
	     	  					targetButterflys.add(butter);
	     	  				}

	     	  				for ( Boid bir : gridForBird[c+n][r+m] ) {
	     	  					targetBirds.add(bir);
	     	  				}

	     	  				for ( Flower flower : gridForFlower[c+n][r+m] ) {
	     	  					targetFlowers.add(flower);
	     	  				}

	     	  				for ( Rock rock : gridForRock[c+n][r+m] ) {
	     	  					targetRocks.add(rock);
	     	  				}
	     	  			}
	     	  		}
	     	  	}

			    // Check every Thing
			    for ( Boid butter : gridForButterfly[c][r] ) {
			    	// \u81ea\u5206\u306e\u5468\u56f2\u306e Butterfly \u3068 Flocking
			    	butter.flock(targetButterflys); // flock\u3059\u308b\u5bfe\u8c61\u3092\u6e21\u3059

			    	// \u81ea\u5206\u306e\u5468\u56f2\u306e Bird \u3060\u3051\u3092\u30c1\u30a7\u30c3\u30af
			    	targetBird = butter.searchNearestBoid(targetBirds);
					butter.flee(targetBird.pos);

					// \u81ea\u5206\u306e\u8fd1\u304f\u306e Flower\uff0cRock \u3060\u3051\u30c1\u30a7\u30c3\u30af
					// Reinforcement Learning
					butter.behaviors( targetFlowers, targetRocks );
			    }

			    for ( Boid bir : gridForBird[c][r] ) {
			    	// \u81ea\u5206\u306e\u5468\u56f2\u306e Bird \u3068 Flocking
			    	bir.flock(targetBirds);

			    	// \u81ea\u5206\u306e\u8fd1\u304f\u306e Flower\uff0cRock \u3060\u3051\u30c1\u30a7\u30c3\u30af
					// Reinforcement Learning
					bir.behaviors( targetFlowers, targetRocks );
			    }
			}
		}
 
		// About Butterfly
		for (int i = butterflys.size()-1; i >= 0 ; i--) {
			b = butterflys.get(i);

			// \u6b7b\u3093\u3067\u3044\u308b\u5834\u5408
	      	if (b.isDead()) butterflys.remove(i);

	      	// Interaction
	      	// 0(m) ~ 2(m) : \u624b\u306e\u4f4d\u7f6e\u304b\u3089\u9003\u3052\u308b
	      	if ( currentUserNum > 0 && 0.0f <= distToUser && distToUser < 2.0f ) {
	      		for (int id = 0; id < currentUserNum; id++) {
	      			b.fleeFromHand( new PVector(3*rightHandPosX[id], 2.5f*rightHandPosY[id]) );
	      			b.fleeFromHand( new PVector(3*leftHandPosX[id], 2.5f*leftHandPosY[id]) );
	      		}
	      	}

	      	// Butterfly\u306f\uff0cDragon\u304b\u3089\u9003\u3052\u308b
			targetDragon = b.searchNearestBoid(dragons);
			b.flee(targetDragon.pos);

			targetFlowerPos = b.searchNearestFlower(butterflys, flowerSystem.flowers);
			// b.seek(targetFlowerPos);

			// poly-morphism
			butterfly = (Butterfly) b;
				// \u9069\u5fdc\u5ea6\u306f\u5e38\u306b\u8a08\u7b97\u3057\u3066\u304a\u304f
				// \u305d\u306e\u500b\u4f53\u304c\u3069\u308c\u3050\u3089\u3044\u74b0\u5883\u306b\u9069\u5fdc\u3057\u3066\u3044\u308b\u304b\u3092\u793a\u3059\u5024
				butterfly.fitness(targetDragon.pos, targetFlowerPos); // step1

	      		/*********************** Genetic Algorithms ***********************/
	      		// \u4ed6\u306e\u8776\u3068\u8ddd\u96e2\u304c\u8fd1\u3044\u5834\u5408\u306e\u307f\uff0c\u751f\u6b96
	      		nearestButterfly = b.searchNearestBoid(butterflys);
	      		nearestDist = PVector.dist(b.pos, nearestButterfly.pos);


	      		// \u6027\u5225\u304c\u9055\u3046\u8776\u540c\u58eb\u306e\u307f\u751f\u6b96\u53ef
	      		if ( nearestDist < 7 && 
	      			 green(b.col) != green(nearestButterfly.col) && 
	      			 bButterflySexualRepro && 
	      			 !isStartSexualReproButter ) {

	      			if ( !bPaintDetails ) {
	      				fill(0);
	      				ellipse(b.pos.x, b.pos.y, 50, 50);
	      			}

	      			isStartSexualReproButter = true;

	      			selection(butterflys); // step2
	      			childDNA = reproduction(); // step3
	      			butterflyGenerations++;
	      
	      			// \u5b50\u304c\u751f\u307e\u308c\u308b\u4f4d\u7f6e\u306e\u6c7a\u5b9a
					bornPos = PVector.random2D();
					bornPos.mult(random(50, 100));
					bornPos.add( b.pos );
					camera.seek(bornPos); // \u30ab\u30e1\u30e9\u30ef\u30fc\u30af

	      			// \u8a95\u751f\u3057\u305f\u5b50\u3092\u96c6\u56e3\u306b\u8ffd\u52a0
	      			// copy()\u91cd\u8981!!
	      			butterflys.add( new Butterfly(bornPos, b.vel.copy(), b.col, childDNA, false) );
	      			isStartSexualReproButter = false;
	      		}

	      		
	      		// \u7121\u6027\u751f\u6b96
	      		if ( bButterflyAsexualRepro ) {
	      			childButterfly = butterfly.asexualReproduce();
      				if ( childButterfly != null ) {
						butterflys.add( childButterfly ); // \u5b50\u304c\u8a95\u751f\u3057\u305f\u5834\u5408\u306f\uff0c\u96c6\u56e3\u306b\u8ffd\u52a0
      				}
	      		}
      			/******************************************************************/

      			butterfly.update();
				//butterfly.border();
				butterfly.boundaries();
		}

		// About Bird
		for (int i = birds.size()-1; i >= 0 ; i--) {
			b = birds.get(i);

			// \u6b7b\u3093\u3067\u3044\u308b\u5834\u5408
			if (b.isDead()) birds.remove(i);

			// Interaction
	      	// 0(m) ~ 2(m) : \u624b\u306e\u4f4d\u7f6e\u304b\u3089\u9003\u3052\u308b
	      	if ( currentUserNum > 0 && 0.0f <= distToUser && distToUser < 2.0f ) {
	      		for (int id = 0; id < currentUserNum; id++) {
	      			b.fleeFromHand( new PVector(3*rightHandPosX[id], 2.5f*rightHandPosY[id]) );
	      			b.fleeFromHand( new PVector(3*leftHandPosX[id], 2.5f*leftHandPosY[id]) );
	      		}
	      	}

			// Bird\u306f\uff0cDragon\u304b\u3089\u9003\u3052\u308b
			targetDragon = b.searchNearestBoid(dragons);
			b.flee(targetDragon.pos);

			// // Bird\u306f\uff0cButterfly\u3092\u98df\u3079\u308b
			b.eatButterfly(butterflys);
			// targetButterflyPos = b.searchNearestTarget(butterflys, b, true);
			// b.seek(targetButterflyPos);

			// // Bird\u306f\uff0cFlower\u3092\u98df\u3079\u308b
			// targetFlowerPos = b.searchNearestFlower(birds, flowerSystem.flowers);
			// b.seek(targetFlowerPos);

			// poly-morphism
			bird = (Bird) b;
				// \u9069\u5fdc\u5ea6\u306f\u5e38\u306b\u8a08\u7b97\u3057\u3066\u304a\u304f
				bird.fitness(targetDragon.pos, targetFlowerPos); // step1

	      		/*********************** Genetic Algorithms ***********************/
	      		// \u4ed6\u306e\u9ce5\u3068\u8ddd\u96e2\u304c\u8fd1\u3044\u5834\u5408\u306e\u307f\uff0c\u751f\u6b96
	      		nearestBird = b.searchNearestBoid(birds);
	      		nearestDist = PVector.dist(b.pos, nearestBird.pos);

	      		// \u6027\u5225\u304c\u9055\u3046\u9ce5\u540c\u58eb\u306e\u307f\u751f\u6b96\u53ef
	      		if ( nearestDist < 7 && 
	      			 green(b.col) != green(nearestBird.col) && 
	      			 bBirdSexualRepro  && 
	      			 !isStartSexualReproBird ) {

	      			if ( !bPaintDetails ) {
	      				fill(0);
	      				ellipse(b.pos.x, b.pos.y, 50, 50);
	      			}
	      			isStartSexualReproBird = true;

	      			selection(birds); // step2
	      			childDNA = reproduction(); // step3
	      			birdGenerations++;
	      
	      			// \u5b50\u304c\u751f\u307e\u308c\u308b\u4f4d\u7f6e\u306e\u6c7a\u5b9a
					bornPos = PVector.random2D();
					bornPos.mult(random(50, 100));
					bornPos.add( b.pos );
					camera.seek(bornPos); // \u30ab\u30e1\u30e9\u30ef\u30fc\u30af

	      			// \u8a95\u751f\u3057\u305f\u5b50\u3092\u96c6\u56e3\u306b\u8ffd\u52a0
	      			// copy()\u91cd\u8981!!
	      			birds.add( new Bird(bornPos, b.vel.copy(), b.col, childDNA, false) );
	      			isStartSexualReproBird = false;
	      		}

	      		
	      		// \u7121\u6027\u751f\u6b96
	      		if ( bBirdAsexualRepro ) {
	      			childBird = bird.asexualReproduce();
      				if ( childBird != null ) {
						birds.add( childBird ); // \u5b50\u304c\u8a95\u751f\u3057\u305f\u5834\u5408\u306f\uff0c\u96c6\u56e3\u306b\u8ffd\u52a0
      				}
	      		}
      			/******************************************************************/

      			bird.update();
				//bird.border();
				bird.boundaries();
		}

		// About Dragon
		for (int i = dragons.size()-1; i >= 0 ; i--) {
			b = dragons.get(i);

			if (b.isDead()) dragons.remove(i);

	      	//b.flock(dragons); // dragon\u540c\u58eb\u3067\u7fa4\u308c

			b.avoidObs(rockSystem.rocks);

			targetButterflyPos = b.searchNearestTarget(butterflys, b, true);
			targetBirdPos = b.searchNearestTarget(birds, b, true);
			dButterfly = PVector.dist(b.pos, targetButterflyPos);
			dBird = PVector.dist(b.pos, targetBirdPos);
			// Butterfly\u3068Bird\uff0c\u3088\u308a\u8fd1\u3044\u65b9\u3092seek
			if ( dButterfly < dBird ) {
				b.seek(targetButterflyPos);
				// Debug
				if ( !bPaintDetails ) {
					strokeWeight(10);
					stroke(0);
					line(targetButterflyPos.x, targetButterflyPos.y, b.pos.x, b.pos.y);
				}
			} else {
				b.seek(targetBirdPos);
				// Debug
				if ( !bPaintDetails ) {
					strokeWeight(10);
					stroke(40, 40, 100);
					line(targetBirdPos.x, targetBirdPos.y, b.pos.x, b.pos.y);
				}
			}


			// poly-morphism
			dragon = (Dragon) b;
				// \u751f\u6b96
	      		childDragon = dragon.reproduce(birds.get( (int)random(birds.size()) ).pos.copy() );
      			if ( childDragon != null ) {
					dragons.add( childDragon );
      			}

				dragon.update();
				dragon.boundaries();
				//dragon.border();
		}

		// About Fish
		// for (int i = 0; i < fishes.size(); i++) {
		// 	Boid b = fishes.get(i);

		// 	b.flock(fishes);
		// 	PVector targetRockPos = b.searchNearestTargetObs(rocks);
		// 	b.flee(targetRockPos);
		// 	b.update();
		// 	if ( b.isDead() ) fishes.remove(i);

		// 	Fish fish = (Fish) b;
		// 		fish.borderForFish();
		// }

		// About Cloud
		// for ( Boid c : clouds ) {
		// 	c.flock(clouds);
		// 	c.update();

		// 	Cloud cloud = (Cloud) c;
		// 		//cloud.border();
		// 		cloud.boundaries();
		// }
	}

	public void display() {
		Butterfly butterfly;
		Bird bird;
		Dragon dragon;
		//Fish fish;
		Cloud cloud;

		// Display Butterfly
		for ( Boid b : butterflys ) {
			// poly-morphism
			butterfly = (Butterfly) b;
			if ( bPaintDetails ) {
				butterfly.shadow();
				butterfly.display();
			} else {
				butterfly.paint();
			}
		}

		// Display Bird
		for ( Boid b : birds ) {
			// poly-morphism
			bird = (Bird) b;
			if ( bPaintDetails ) {
				bird.shadow();
				bird.display();
			} else {
				bird.paint();
			}
		}

		// Display Dragon
		for ( Boid b : dragons ) {
			// poly-morphism
			dragon = (Dragon) b;
			if ( bPaintDetails ) {
				dragon.shadow();
				dragon.display();
			} else {
				dragon.paint();
			}
		}

		// Display Fish
		// for ( Boid b : fishes ) {
		// 	// poly-morphism
		// 	fish = (Fish) b;
		// 		if ( bPaintDetails ) fish.display();
		// 		else fish.paint();
		// }

		// Display Cloud
		// for ( Boid b : clouds ) {
		// 	// poly-morphism
		// 	cloud = (Cloud) b;
		// 	if ( bPaintDetails ) {
		// 		cloud.shadow();
		// 		cloud.display();
		// 	}
		// }
	}

	// Control Camera
	public void runCamera() {
		PVector targetPos = camera.searchCenterPos(butterflys, birds, dragons);
		camera.seek(targetPos);
		camera.arrive(targetPos);
		camera.update();
		if ( !bPaintDetails ) camera.display();
	}

	// \u96c6\u56e3\u306e\u4e2d\u3067\uff0c\u6700\u3082\u9069\u5fdc\u5ea6\u304c\u9ad8\u3044\u500b\u4f53\u3092\u63a2\u3059
  	public float getMaxFitness(ArrayList<Boid> boids) {
    	float maxFitness = 0;
    	for (int i = 0; i < boids.size(); i++) {
      		if ( boids.get(i).fitness > maxFitness ) {
        		maxFitness = boids.get(i).fitness;
      		}	
    	}

    	return maxFitness;
  	}

  	// \u4ea4\u914d\u30d7\u30fc\u30eb\u306e\u4f5c\u6210
  	public void selection(ArrayList<Boid> boids) {
    	matingPool.clear();

    	float maxFitness = getMaxFitness(boids); // \u6700\u3082\u9ad8\u3044\u9069\u5fdc\u5ea6\u306e\u5024

    	// \u96c6\u56e3\u306e\u5168\u3066\u306e\u8981\u7d20\u306e\u9069\u5fdc\u5ea6\u3092\uff0c\u6700\u3082\u9ad8\u3044\u9069\u5fdc\u5ea6\u3067\u6b63\u898f\u5316
    	// \u6b63\u898f\u5316\u3055\u308c\u305f\u9069\u5fdc\u5ea6\u306b\u5f93\u3063\u3066\uff0c\u4ea4\u914d\u30d7\u30fc\u30eb\u306b\u8ffd\u52a0
    	for (int i = 0; i < boids.size(); i++) {
      		float fitnessNormal = map(boids.get(i).fitness, 0, maxFitness, 0, 1);
      		int n = (int) (fitnessNormal * 100);  // 0\u304b\u3089100\u307e\u3067\u306e\u7bc4\u56f2
      		for (int j = 0; j < n; j++) {
        		matingPool.add(boids.get(i)); // \u4ea4\u914d\u30d7\u30fc\u30eb\u306b\u8ffd\u52a0
      		}
    	}
  	}

  	// \u6709\u6027\u751f\u6b96
  	public DNA reproduction() {
      	int a = PApplet.parseInt( random(matingPool.size()) );
      	int b = PApplet.parseInt( random(matingPool.size()) );
      
      	// \u4ea4\u914d\u30d7\u30fc\u30eb\u304b\u3089\uff0c\u89aa\u3092\uff12\u3064\u30d4\u30c3\u30af\u30a2\u30c3\u30d7
      	Boid parentA = matingPool.get(a);
      	Boid parentB = matingPool.get(b);

      	// \u89aa\u306eDNA\u3092\u53d6\u5f97
      	DNA genesA = parentA.dna;
      	DNA genesB = parentB.dna;
      
      	// \u4ea4\u53c9
      	DNA childDNA = genesA.crossover(genesB);
      
      	// \u7a81\u7136\u5909\u7570(1%)
      	childDNA.mutate(0.01f);

		return childDNA;
  	}
}
class Flower {
    int num = 100; //\u9802\u70b9\u306e\u63cf\u753b\u56de\u6570
    float scale; //\u82b1\u306e\u5927\u304d\u3055\u8abf\u6574\u7528

    PVector pos;

    float theta_A = 0;
    float delta_A = 1.0f;
    float theta_B = 0;
    float delta_B;

    float theta_A_goal = theta_A + delta_A;
    float theta_B_goal;

    float angle;
    int col;

    float lifePoint;
    boolean dead;
    boolean born;

    Flower(PVector _initPos, float _delta_B, float _scale, int _col, boolean _isInit) {
        pos = _initPos;

        delta_B = _delta_B; // 0.1 or 0.2 or 0.25
        theta_B_goal = theta_B + delta_B;
        angle = random(PI);

        scale = _scale;
        col = _col;

        if ( _isInit ) {
            lifePoint = 255;
            dead = false;
        } else {
            lifePoint = 0;
            born = true;
        }
    }

    public boolean isDead() {
        if ( lifePoint < 0.0f ) {
            return true;
        } else {
            return false;
        }
    }

    public void update() {
        if ( born && lifePoint < 255.0f && !dead ) lifePoint += 5.0f;
        if ( dead ) lifePoint -= 30.0f;
    }

    public void display() {
        fill(col, lifePoint);

        pushMatrix();
        translate(pos.x, pos.y);
        rotate( angle );
        beginShape();
        for (int i = 0; i < num; i++) {
            float r = sin(theta_A) / 2 + 1;
            float x = scale * r * sin(theta_B);
            float y = scale * r * cos(theta_B);

            theta_A += delta_A;
            theta_B += delta_B;

            curveVertex(x, y);
        }
        theta_A = theta_A_goal;
        theta_B = theta_B_goal;
        endShape();

        // \u82b1\u306e\u4e2d\u5fc3
        noStroke();
        fill(255, 100, 0, lifePoint);
        ellipse(0, 0, scale*0.8f, scale*0.8f);

        popMatrix();
    }

    public void paint() {
        fill(col, lifePoint);
        ellipse(pos.x, pos.y, 3*scale, 3*scale);
    }
}
class FlowerSystem {
	ArrayList<Flower> flowers;

	float flowerType;
	int flowerCol;
	int red = color(255, 30, 30);
	int orange = color(255, 148, 0);
	int yellow = color(255, 240, 30);
	int blue = color(53, 81, 255);
	int purple = color(180, 100, 255);
	int pink = color(236, 83, 157);
	int pink2 = color(255, 157, 202);

	FlowerSystem(int _num) {
		flowers = new ArrayList<Flower>();
		addFlowers(_num);
	}

	public void addFlowers(int flowerNum) {
		PVector initPos;
		float scale;
		for ( int i = 0; i < flowerNum; i++ ) {
			while (true) {
				initPos = PVector.random2D();
				initPos.mult( random(0, width*0.3f) ); // \u521d\u671f\u4f4d\u7f6e\u306e\u6c7a\u5b9a
				initPos.add( centerPos );

				scale = random(50, 100);

				boolean bCollision = false;
				if ( i == 0 ) bCollision = false;
				else {
					// Collision detection
					for( Flower other : flowers ) {
						float distance = PVector.dist(initPos, other.pos);
						if ( (scale + other.scale) >= distance || abs(scale - other.scale) >= distance ) {
							bCollision = true;
							break;
						}
					}
				}

				if ( !bCollision ) {
					flowerType = decideType(flowerType); // \u7a2e\u985e\u306e\u6c7a\u5b9a
					float size = FLOWER_SIZE+random(10); // \u5927\u304d\u3055\u306e\u6c7a\u5b9a
					flowerCol = decideColor(flowerCol); // \u8272\u306e\u6c7a\u5b9a
					flowers.add( new Flower(initPos, flowerType, size, flowerCol, true) );
					break;
				}
			}
		}
	}

	public void run() {
		Flower f;
		for (int i = flowers.size()-1; i >= 0; i--) {
			f = flowers.get(i);

			// \u82b1\u304c\u6b7b\u3093\u3067\u3044\u308b\u5834\u5408
			if (f.isDead()) flowers.remove(i);

			f.update();
		}

		PVector bornPos;
		float size;
		// \u4e00\u5b9a\u306e\u78ba\u7387\u3067\u82b1\u3092\u8ffd\u52a0\u3059\u308b
    	if ( random(1) < FLOWER_APPEARANCE ) {
    		// \u82b1\u304c\u54b2\u304f\u4f4d\u7f6e\u306e\u6c7a\u5b9a
    		bornPos = PVector.random2D();
			bornPos.mult( random(width*0.3f) );
			bornPos.add( centerPos );

    		flowerType = decideType(flowerType); // \u7a2e\u985e\u306e\u6c7a\u5b9a
			size = FLOWER_SIZE+random(5); // \u5927\u304d\u3055\u306e\u6c7a\u5b9a
			flowerCol = decideColor(flowerCol); // \u8272\u306e\u6c7a\u5b9a
			flowers.add( new Flower(bornPos, flowerType, size, flowerCol, false) );
    	}
	}

	public void display() {
		for ( Flower f : flowers ) {
			if ( bPaintDetails ) f.display();
			else f.paint();
		}
	}

	public int decideColor(int col) {
		int tmp = (int)random(0, 7);

		if ( tmp == 0 ) {
			col = red;
		} else if ( tmp == 1 ) {
			col = orange;
		} else if ( tmp == 2 ) {
			col = yellow;
		} else if ( tmp == 3 ) {
			col = blue;
		} else if ( tmp == 4 ) {
			col = purple;
		} else if ( tmp == 5 ) {
			col = pink;
		} else if ( tmp == 6 ) {
			col = pink2;
		}

		return col;
	}

	public float decideType(float t) {
		int tmp = (int)random(0, 3);

		if ( tmp == 0 ) {
			t = 0.1f;
		} else if ( tmp == 1 ) {
			t = 0.2f;
		} else if ( tmp == 2 ) {
			t = 0.25f;
		}

		return t;
	}
}
class Island {
	PVector pos;
	FloatList vertX;
	FloatList vertY;

	PVector offset1, offset2;
	int c1Island = color( 75, 255, 75 );
    int c2Island = color( 0, 50, 10 );
	float radian;
	float size;
	float scale = 0.04f;
	float x, y;

	int j = 0;

	Island(PVector _offset1, PVector _offset2, PVector _pos, float _size) {
		offset1 = _offset1;
		offset2 = _offset2;
		pos = _pos;
		size = _size;
		vertX = new FloatList();
		vertY = new FloatList();

		setVertex();
	}

	public void setVertex() {
    	pushMatrix();
    	translate( pos.x, pos.y );
    
    	for ( float radius = size; radius > 0; radius -= (int)(size*0.25f) ) {
      		for ( float angle = 0; angle < 360; angle+=10 ) {
          		radian = radians(angle);  
          		x = radius * cos(radian);
          		y = radius * sin(radian);

          		// \u9802\u70b9\u60c5\u5831
          		vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y),   0, 1,   -60, 60) );
          		vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y),   0, 1,   -60, 60) );

          		if ( radius == size ) {
         			rocksAroundIsland[j] = new PVector( vertX.get(j)+width*0.5f, vertY.get(j)+height*0.5f );
          			j++;
        		}
      		}
    	}

    	popMatrix();
  	}

  	public void display() {
  		// \u30ed\u30fc\u30ab\u30eb\u5ea7\u6a19\u7cfb\u306b\u79fb\u52d5\u3057\u3066\u304b\u3089\u63cf\u753b
  		pushMatrix();
    	translate( pos.x, pos.y );

  		int i = 0;
  		for ( float radius = size; radius > 0; radius -= (int)(size*0.25f) ) {
  			fill( map(radius, 0, width, red(c1Island),   red(c2Island) ),
            	  map(radius, 0, width, green(c1Island), green(c2Island) ),
            	  map(radius, 0, width, blue(c1Island),  blue(c2Island) ) 
          		);

  			beginShape();
      		for ( float angle = 0; angle < 360; angle += 10 ) {
          		vertex( vertX.get(i), vertY.get(i) );
          		i++;
      		}
      		endShape(CLOSE);
    	}

    	popMatrix();
  	}
}
class Rock {
	PVector pos;
	FloatList vertX;
	FloatList vertY;

	PVector offset1, offset2;
	float c1Rock = random(200, 255);
    float c2Rock = random(30, 100);
	float radian;
	float size;
	float scale = 0.04f;
	float x, y;

	Rock(PVector _offset1, PVector _offset2, PVector _pos, float _size) {
		offset1 = _offset1;
		offset2 = _offset2;
		pos = _pos;
		size = _size;
		vertX = new FloatList();
		vertY = new FloatList();

		setVertex();
	}

	public void setVertex() {
    	pushMatrix();
    	translate( pos.x, pos.y );
    
    	for ( float radius = size; radius > 0; radius -= (int)(size*0.25f) ) {
      		for ( float angle = 0; angle < 360; angle+=15 ) {
          		radian = radians(angle);  
          		x = 1.1f * radius * cos(radian);
          		y = radius * sin(radian);

          		// \u9802\u70b9\u60c5\u5831
          		vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y),   0, 1,   -20, 20) );
          		vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y),   0, 1,   -20, 20) );
      		}
    	}

    	popMatrix();
  	}

  	public void display() {
  		// \u30ed\u30fc\u30ab\u30eb\u5ea7\u6a19\u7cfb\u306b\u79fb\u52d5\u3057\u3066\u304b\u3089\u63cf\u753b
  		pushMatrix();
    	translate( pos.x, pos.y );

  		int i = 0;
  		for ( float radius = size; radius > 0; radius -= (int)(size*0.25f) ) {
  			fill( map(radius, 0, size, c1Rock, c2Rock) );

  			beginShape();
      		for ( float angle = 0; angle < 360; angle += 15 ) {
          		vertex( vertX.get(i), vertY.get(i) );
          		i++;
      		}
      		endShape(CLOSE);
    	}

    	popMatrix();
  	}

  	public void shadow() {
  		pushMatrix();
    	translate( pos.x + size*0.5f, pos.y + size*0.5f);

  		fill( 50, 50 );
  		beginShape();
      	for ( int i = 0; i < 24; i++ ) {
          	vertex( vertX.get(i), vertY.get(i) );
      	}
      	endShape(CLOSE);

    	popMatrix();
  	}

  	public void paint() {
  		rectMode(CENTER);
    	fill(50);
    	rect(pos.x, pos.y, 1.8f*size, 1.8f*size);
  	}
}
class RockSystem {
	ArrayList<Rock> rocks;

	RockSystem(int _num) {
		rocks = new ArrayList<Rock>();
		addCenterRocks(_num);
		addRocksAroundIsland();
	}

	// Center rocks
	public void addCenterRocks(int rockNum) {
		for (int i = 0; i < rockNum; i++) {
			// Obstacle Rocks
			PVector initCenterPos;
			float radius;
			while (true) {
				initCenterPos = PVector.random2D();
				initCenterPos.mult(random(width*0.05f, width*0.33f)); // \u521d\u671f\u4f4d\u7f6e\u306e\u6c7a\u5b9a
				initCenterPos.add( centerPos );
				radius = ROCK_SIZE + random(-10, 25);

				boolean bCollision = false;
				if ( i == 0 ) bCollision = false;
				else {
					// Collision detection
					for( Rock other : rocks ) {
						float distance = PVector.dist(initCenterPos, other.pos);
						if ( 3*radius >= distance ) {
							bCollision = true;
							break;
						}
					}
				}

				if ( !bCollision ) {
					rocks.add( new Rock( new PVector(random(1000), random(1000)), new PVector(random(1000), random(1000)), initCenterPos, radius) );
					break;
				}
			}
		}
	}

	// Rocks around island
	public void addRocksAroundIsland() {
		for ( int i = 0; i < rocksAroundIsland.length; i++ ) {
			rocks.add( new Rock( new PVector(random(1000), random(1000)), 
					   new PVector(random(1000), random(1000)), 
					   rocksAroundIsland[i], 
					   random(50, 80) ) 
					);
		}
	}

	public void display() {
		for (int i = 0; i < rocks.size(); i++) {
			Rock r = rocks.get(i);

			if ( bPaintDetails ) {
				r.shadow();
				r.display();

			} else {
				if (i < ROCK_NUM) r.paint();
			}
		}
	}
}
class Sea {
	PVector offset1, offset2;
	PVector pos;
    FloatList vertX;
    FloatList vertY;
    float size;
	float scale = 0.04f;
	int c1Sea = color( 0, 50, 220 );
    int c2Sea = color( 50, 230, 255 );

    float radian;
    float x, y, nx, ny;

	Sea ( PVector _offset1, PVector _offset2, PVector _centre, float _size) {
		offset1 = _offset1;
		offset2 = _offset2;
		pos = _centre;
        size = _size;
        vertX = new FloatList();
        vertY = new FloatList();

        setVertex();
	}

    public void setVertex() {
        pushMatrix();
        translate( pos.x, pos.y );
    
        for ( float radius = (int)size; radius > (int)size*0.25f; radius -= (int)size*0.1f ) {
            for ( float angle = 0; angle < 360; angle+=20 ) {
                radian = radians(angle);  
                x = radius * cos(radian);
                y = radius * sin(radian);

                // \u9802\u70b9\u60c5\u5831
                vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y, frameCount * 0.01f),   0, 1,   -150, 150) );
                vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y, frameCount * 0.01f),   0, 1,   -150, 150) );
            }
        }

        popMatrix();
    }

    public void display() {
        // \u30ed\u30fc\u30ab\u30eb\u5ea7\u6a19\u7cfb\u306b\u79fb\u52d5\u3057\u3066\u304b\u3089\u63cf\u753b
        pushMatrix();
        translate( pos.x, pos.y );

        int i = 0;
        for ( float radius = (int)size; radius > (int)size*0.25f; radius -= (int)size*0.1f ) {
            fill( map(radius, (int)size*0.3f, size, red(c1Sea), red(c2Sea) ),
                  map(radius, (int)size*0.3f, size, green(c2Sea), green(c1Sea) ),
                  map(radius, (int)size*0.3f, size, blue(c2Sea), blue(c1Sea) ) 
                );

            beginShape();
            for ( float angle = 0; angle < 360; angle += 20 ) {
                vertex( vertX.get(i), vertY.get(i) );
                i++;
            }
            endShape(CLOSE);
        }

        popMatrix();
    }

	public void runSea() {
		pushMatrix();
		translate( pos.x, pos.y );

  	    for ( float radius = (int)size; radius > (int)size*0.3f; radius -= (int)size*0.08f ) {
            fill( map(radius, (int)size*0.3f, size, red(c1Sea), red(c2Sea) ),
                    map(radius, (int)size*0.3f, size, green(c2Sea), green(c1Sea) ),
                    map(radius, (int)size*0.3f, size, blue(c2Sea), blue(c1Sea) ) 
                );
    		
            beginShape();
            for ( float angle = 0; angle < 360; angle += 30 ) {
                radian = radians(angle);  
                x = radius * cos(radian);
                y = radius * sin(radian);
      	        nx = x + map(noise(x * scale + offset1.x, y * scale + offset1.y, frameCount * 0.01f), 	0, 1, 	-150, 150);
      	        ny = y + map(noise(x * scale + offset2.x, y * scale + offset2.y, frameCount * 0.01f), 	0, 1, 	-150, 150);
      	        vertex(nx, ny);
    	    }
    	    endShape(CLOSE);
		}
		popMatrix();
	}
}
  public void settings() { 	fullScreen(); 	smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "InteractiveEvolvingEcosystem" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
