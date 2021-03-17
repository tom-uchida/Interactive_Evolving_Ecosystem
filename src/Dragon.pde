class Dragon extends Boid {
	//float headSize;
	PVector posHead, posRightWing, posLeftWing, posBody, posMed, posTail, posEnd;
	float angHead, angRightWing, angLeftWing, angBody, angMed, angTail;

	color eyeCol = color(255, 255, 0);

	boolean isClose = false;

	Dragon (PVector _pos, PVector _vel, color _col, DNA _dna, boolean _isInit) {
		super( _pos, _vel, _col); // Boidクラスのコンストラクタの呼び出し

		// Genetic Algorithm
		dna = _dna;
		headSize 		= map(dna.genes[0], 0, 1, 10, 6);
		maxFlockSpeed   = map(dna.genes[0], 0, 1, 5.0, 10.0);
		maxFlockForce   = map(dna.genes[1], 0, 1, 3.0, 7.0);
		maxSeekSpeed    = map(dna.genes[0], 0, 1, 15.0, 20.0);
		maxSeekForce    = map(dna.genes[2], 0, 1, 5.0, 10.0);


		posHead = _pos;
		posBody = PVector.add( posHead, new PVector(0, 0.4*headSize) );
		posRightWing = PVector.add( posBody, new PVector(0, 3.0*headSize) );
		posLeftWing = PVector.add( posBody, new PVector(0, 3.0*headSize) );
		posMed = PVector.add( posBody, new PVector(0, 6.0*headSize) );
		posTail = PVector.add( posMed, new PVector(0, 1.8*headSize) );
		posEnd = PVector.add( posTail, new PVector(0, 1.2*headSize) );
		angHead = 0;
		angRightWing = 0;
		angLeftWing = 0;
		angBody = 0;
		angMed = 0;
		angTail = 0;
		shadowCol = color( 50, 30 );
	
		// パラメータ要調整
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

	// 生殖
	// 新しい子を生成する関数
	Dragon reproduce(PVector bornPos) {
		if ( random(1) < DRAGON_REPRO_PROB ) { // a certain probability
			DNA childDNA = dna.copy(); // 親の完全なコピーである子を新しく作る
			childDNA.mutate(0.01); // mutation
			dragonGenerations++;

			// 子が生まれる位置の決定
			//PVector bornPos = new PVector(random(width), random(height*0.7, height));
			bornPos.add(new PVector(0, random(200, 500)));

			return new Dragon(bornPos, vel.copy(), col, childDNA, false);
		
		} else {
			return null;
		}
	}

	// A force to keep it on screen
	void boundaries() {
		desired = null;

		if ( pos.x < width*0.15 ) {
			desired = new PVector(2*maxSeekSpeed, vel.y);
		} else if ( pos.x > width*0.85 ) {
			desired = new PVector(-2*maxSeekSpeed, vel.y);
		}

		if ( pos.y < -height*0.1 ) {
			desired = new PVector(vel.x, 2*maxSeekSpeed);
		} else if ( pos.y > height*1.1 ) {
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

	void update() {
	    super.update();

	    lifePoint -= 1.0;
	    if ( born && lifePoint < 255.0 ) lifePoint += 10.0;
	    if ( lifePoint >= 255.0 ) born = false;

	    //posHead = new PVector(pos.x, pos.y);
	    angHead = atan2(vel.y, vel.x);
	    // atan2(y, x) : 点(vel.ｘ, vel.ｙ)とx軸の角度(θ)を返す。つまり，位置から角度を割り出せる．
    	// 角度は、y を x で割った角度で、-PIからPIの範囲の実数

	    if (isClose) {
	      angHead += 0.5*sin(0.4*frameCount);
	      isClose = false;
	    }
	    
	    posBody = PVector.sub( posHead, new PVector(0.4*headSize*cos(angHead), 0.4*headSize*sin(angHead)) );
	    angBody = atan2( posBody.y-posMed.y, posBody.x-posMed.x );

	    posRightWing = PVector.sub( posBody, new PVector(3.0*headSize*cos(angBody), 3.0*headSize*sin(angBody)) );
	    angRightWing = atan2( posRightWing.y-posMed.y, posRightWing.x-posMed.x );

	    posLeftWing = PVector.sub( posBody, new PVector(3.0*headSize*cos(angBody), 3.0*headSize*sin(angBody)) );
	    angLeftWing = atan2( posLeftWing.y-posMed.y, posLeftWing.x-posMed.x );
	    
	    posMed = PVector.sub( posBody, new PVector(6.0*headSize*cos(angBody), 6.0*headSize*sin(angBody)) );
	    angMed = atan2( posMed.y-posTail.y,posMed.x-posTail.x );
	    
	    posTail = PVector.sub( posMed, new PVector(1.8*headSize*cos(angMed), 1.8*headSize*sin(angMed)) );
	    angTail = atan2( posTail.y-posEnd.y,posTail.x-posEnd.x );
	    
	    posEnd = PVector.sub( posTail, new PVector(1.2*headSize*cos(angTail), 1.2*headSize*sin(angTail)));
  	}
  	
  	void shadow() {
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

	void display() {
		noStroke();
		paintHead(posHead, angHead, col, eyeCol);
		paintRightWing(posRightWing, angRightWing, col);
		paintLeftWing(posLeftWing, angLeftWing, col);
		paintBody(posBody, angBody, col);
		paintMed(posMed, angMed, col);
		paintTail(posTail, angTail, col);
	}

	void paintHead(PVector cen, float ang, color c, color eye) { 
    	fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x,cen.y);
      	rotate(HALF_PI + ang); //時計回りに回転
      	beginShape();
        	curveVertex(0, -3.0*headSize); // 顔の先端
      
      		// 顔の右半分
        	curveVertex(0.6*headSize, -1.5*headSize); 
        	curveVertex(0.95*headSize, -0.3*headSize);
        	curveVertex(1.1*headSize, headSize); // 右耳の先端
        	curveVertex(0.7*headSize, 0.5*headSize);
        	curveVertex(0.5*headSize, 0.2*headSize);
       
	        curveVertex(0, 0); // 頭頂部

	      	// 顔の左半分
	      	curveVertex(-0.5*headSize, 0.2*headSize);
	        curveVertex(-0.7*headSize, 0.5*headSize);
	        curveVertex(-1.1*headSize, headSize); // 左耳の先端
	        curveVertex(-0.95*headSize, -0.3*headSize);
	        curveVertex(-0.6*headSize, -1.5*headSize);      	

	        curveVertex(0, -3.0*headSize); // 顔の先端

	        curveVertex(0.6*headSize,-1.5*headSize);
	        curveVertex(0.95*headSize,-0.3*headSize);
	    endShape();

	     // 目
	     fill(eye); // Yellow
	     beginShape();
	        curveVertex(0.4*headSize,-0.7*headSize);
	        curveVertex(0.65*headSize,-0.4*headSize);
	        curveVertex(0.85*headSize,0.2*headSize);
	        curveVertex(0.6*headSize,-0.1*headSize);
	        curveVertex(0.5*headSize,-0.4*headSize);
	        curveVertex(0.4*headSize,-0.7*headSize);
	        curveVertex(0.65*headSize,-0.4*headSize);
	     endShape();
	     beginShape();
	        curveVertex(-0.4*headSize,-0.7*headSize);
	        curveVertex(-0.65*headSize,-0.4*headSize);
	        curveVertex(-0.85*headSize,0.2*headSize);
	        curveVertex(-0.6*headSize,-0.1*headSize);
	        curveVertex(-0.5*headSize,-0.4*headSize);
	        curveVertex(-0.4*headSize,-0.7*headSize);
	        curveVertex(-0.65*headSize,-0.4*headSize);
	      endShape();
	    popMatrix();
	}

	void paintRightWing(PVector cen, float ang, color c){
		fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x, cen.y);
      	rotate(HALF_PI + ang);
      	beginShape();
      		curveVertex(0.5*headSize, 0);

      		curveVertex(0, -2.0*headSize);
      		curveVertex(1.0*headSize, -1.2*headSize);
      		curveVertex(1.5*headSize, -1.0*headSize);
      		curveVertex(2.0*headSize, -0.8*headSize);
      		curveVertex(2.5*headSize, -0.8*headSize);
      		curveVertex(4.0*headSize, -0.85*headSize);
      		curveVertex(3.5*headSize, -0.9*headSize);
      		curveVertex(4.0*headSize, -0.95*headSize);
      		curveVertex(4.5*headSize, -1.0*headSize);
      		curveVertex(5.5*headSize, -1.2*headSize);
      		curveVertex(6.5*headSize, -1.5*headSize);
      		curveVertex(7.5*headSize, -2.0*headSize);
      		curveVertex(8.5*headSize, -1.5*headSize);
      		curveVertex(9.5*headSize, -0.5*headSize);
      		curveVertex(10.0*headSize, 2.0*headSize); // 最右点
      		curveVertex(8.5*headSize, 0.5*headSize);
      		curveVertex(7.0*headSize, 0.9*headSize);
      		curveVertex(5.5*headSize, 0.4*headSize);
      		curveVertex(4.0*headSize, 0.8*headSize);
      		curveVertex(2.5*headSize, 0.5*headSize);
      		curveVertex(1.0*headSize, 0.3*headSize);
      		curveVertex(0.5*headSize, 1.0*headSize);
      		curveVertex(0.1*headSize, 1.5*headSize);

      		curveVertex(0, -2.0*headSize);
      	endShape();
      	popMatrix();
	}

	void paintLeftWing(PVector cen, float ang, color c) {
		fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x, cen.y);
      	rotate(HALF_PI + ang);
      	beginShape();
      		curveVertex(-0.5*headSize, 0);

      		curveVertex(0, -2.0*headSize);
      		curveVertex(-1.0*headSize, -1.2*headSize);
      		curveVertex(-1.5*headSize, -1.0*headSize);
      		curveVertex(-2.0*headSize, -0.8*headSize);
      		curveVertex(-2.5*headSize, -0.8*headSize);
      		curveVertex(-4.0*headSize, -0.85*headSize);
      		curveVertex(-3.5*headSize, -0.9*headSize);
      		curveVertex(-4.0*headSize, -0.95*headSize);
      		curveVertex(-4.5*headSize, -1.0*headSize);
      		curveVertex(-5.5*headSize, -1.2*headSize);
      		curveVertex(-6.5*headSize, -1.5*headSize);
      		curveVertex(-7.5*headSize, -2.0*headSize);
      		curveVertex(-8.5*headSize, -1.5*headSize);
      		curveVertex(-9.5*headSize, -0.5*headSize);
      		curveVertex(-10.0*headSize, 2.0*headSize); // 最左点
      		curveVertex(-8.5*headSize, 0.5*headSize);
      		curveVertex(-7.0*headSize, 0.9*headSize);
      		curveVertex(-5.5*headSize, 0.4*headSize);
      		curveVertex(-4.0*headSize, 0.8*headSize);
      		curveVertex(-2.5*headSize, 0.5*headSize);
      		curveVertex(-1.0*headSize, 0.3*headSize);
      		curveVertex(-0.5*headSize, 1.0*headSize);
      		curveVertex(-0.1*headSize, 1.5*headSize);

      		curveVertex(0, -2.0*headSize);
      	endShape();
      	popMatrix();
	}

  	void paintBody(PVector cen, float ang, color c) {
    	fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x, cen.y);
      	rotate(HALF_PI + ang);
      	beginShape();
	        curveVertex(0, -0.6*headSize);
	      
	        curveVertex(0.3*headSize, -0.4*headSize);
	        curveVertex(0.4*headSize, 0.0);
	        curveVertex(0.45*headSize, 0.5*headSize);
	        curveVertex(0.5*headSize, 1.0*headSize);
	        curveVertex(0.55*headSize, 1.5*headSize);
	        curveVertex(0.65*headSize, 2.0*headSize);
	        curveVertex(0.7*headSize, 2.5*headSize);
	        curveVertex(0.75*headSize, 3.0*headSize);
	        curveVertex(0.8*headSize, 4.0*headSize);
	        curveVertex(0.85*headSize, 4.5*headSize);
	        curveVertex(0.9*headSize, 5.0*headSize);
	        curveVertex(0.95*headSize, 6.0*headSize);
	        curveVertex(1.0*headSize, 7.0*headSize);
	       
	        curveVertex(0, 8.0*headSize); // 胴体の最下部
	     
	      	curveVertex(-1.0*headSize, 7.0*headSize);
			curveVertex(-0.95*headSize, 6.0*headSize);
			curveVertex(-0.9*headSize, 5.0*headSize);
			curveVertex(-0.85*headSize, 4.5*headSize);
			curveVertex(-0.8*headSize, 4.0*headSize); 
			curveVertex(-0.75*headSize, 3.0*headSize);    
			curveVertex(-0.7*headSize, 2.5*headSize);
			curveVertex(-0.65*headSize, 2.0*headSize);
			curveVertex(-0.55*headSize, 1.5*headSize);
			curveVertex(-0.5*headSize, 1.0*headSize);
			curveVertex(-0.45*headSize, 0.5*headSize);
			curveVertex(-0.4*headSize, 0.0);
			curveVertex(-0.3*headSize, -0.4*headSize);
	      
	        curveVertex(0, -0.6*headSize);

	        curveVertex(0.3*headSize, -0.4*headSize);
	        curveVertex(0.4*headSize, 0.0);
      	endShape();

    	popMatrix();
  }

  	void paintMed(PVector cen, float ang, color c) {
  		fill(c, lifePoint);
    	pushMatrix();
    	translate(cen.x, cen.y);
    	rotate(HALF_PI + ang);
    	beginShape();
	    	curveVertex(0, 0);

	    	curveVertex(0.9*headSize, 0.5*headSize);
	    	curveVertex(1.0*headSize, 0.6*headSize);
	    	curveVertex(1.5*headSize, 0.7*headSize);
	    	curveVertex(1.7*headSize, 0.8*headSize);
	    	curveVertex(3.0*headSize, 1.5*headSize);
	    	curveVertex(3.5*headSize, 3.5*headSize); // 右足先端部
	    	curveVertex(2.7*headSize, 2.2*headSize);
	    	curveVertex(1.5*headSize, 2.0*headSize);
	    	curveVertex(1.0*headSize, 1.8*headSize);
	    	curveVertex(0.9*headSize, 2.5*headSize);
	    	curveVertex(0.8*headSize, 3.0*headSize);

	    	curveVertex(0, 4.0*headSize);

	    	curveVertex(-0.8*headSize, 3.0*headSize);
	    	curveVertex(-0.9*headSize, 2.5*headSize);
	    	curveVertex(-1.0*headSize, 1.8*headSize);
	    	curveVertex(-1.5*headSize, 2.0*headSize);
	    	curveVertex(-2.7*headSize, 2.2*headSize);
	    	curveVertex(-3.5*headSize, 3.5*headSize); // 左足先端部
	    	curveVertex(-3.0*headSize, 1.5*headSize);
	    	curveVertex(-1.7*headSize, 0.8*headSize);
	    	curveVertex(-1.5*headSize, 0.7*headSize);
	    	curveVertex(-1.0*headSize, 0.6*headSize);
	    	curveVertex(-0.9*headSize, 0.5*headSize);

	    	curveVertex(0, 0);

	    	curveVertex(0.9*headSize, 0.5*headSize);
	    	curveVertex(1.0*headSize, 0.6*headSize);
	    endShape();
	    popMatrix();
  }

  void paintTail(PVector cen, float ang, color c){
    	fill(c, lifePoint);
    	pushMatrix();
      	translate(cen.x,cen.y);
      	rotate(HALF_PI + ang); // 時計回りに回転
      	beginShape();
      		curveVertex(0, 0);

      		curveVertex(0.8*headSize, 1.2*headSize);
      		curveVertex(0.75*headSize, 1.5*headSize);
      		curveVertex(0.65*headSize, 2.0*headSize);
      		curveVertex(0.6*headSize, 2.5*headSize);
      		curveVertex(0.35*headSize, 6.0*headSize);
      		curveVertex(0.3*headSize, 6.5*headSize);
      		curveVertex(0.25*headSize, 7.0*headSize);
      		curveVertex(0.2*headSize, 7.5*headSize);
      		curveVertex(0.15*headSize, 7.8*headSize);
      		curveVertex(0.1*headSize, 8.0*headSize);
      		curveVertex(0.05*headSize, 9.0*headSize);

      		curveVertex(0, 10.0*headSize);

      		curveVertex(-0.05*headSize, 9.0*headSize);
      		curveVertex(-0.1*headSize, 8.0*headSize);
      		curveVertex(-0.15*headSize, 7.8*headSize);
      		curveVertex(-0.2*headSize, 7.5*headSize);
      		curveVertex(-0.25*headSize, 7.0*headSize);
      		curveVertex(-0.3*headSize, 6.5*headSize);
      		curveVertex(-0.35*headSize, 6.0*headSize);
      		curveVertex(-0.6*headSize, 2.5*headSize);
      		curveVertex(-0.65*headSize, 2.0*headSize);
      		curveVertex(-0.75*headSize, 1.5*headSize);
      		curveVertex(-0.8*headSize, 1.2*headSize);

      		curveVertex(0, 0);

      	endShape();
      	popMatrix();
  }

  	void paint(){
		noStroke();
		fill(col, lifePoint);

		float ang = atan2(vel.y, vel.x);
		PVector v1 = new PVector( pos.x + 50*cos(ang), pos.y + 50*sin(ang) );
		PVector v2 = new PVector( pos.x + 50*0.5*cos(ang+radians(90+45)), pos.y + 50*0.5*sin(ang+radians(90+45)) );
		PVector v3 = new PVector( pos.x + 50*0.5*cos(ang+radians(270-45)), pos.y + 50*0.5*sin(ang+radians(270-45)) );
		triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
	}

  	void border() {
		PVector diffToCenter = PVector.sub( centerPos, pos );
		float distToCenter = diffToCenter.mag();

		if ( distToCenter >= width*0.55 ) {
			lifePoint -= 10;
			if ( lifePoint < 50 && lifePoint > 40 ) {
				PVector newPos = PVector.random2D();
				newPos.mult(random(width*0.4));
				newPos.add(centerPos);

				flockingSystem.makeDragon(newPos);
			}
			seek( centerPos );
		}
	}
}