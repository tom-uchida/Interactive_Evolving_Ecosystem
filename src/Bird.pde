
// Boidクラスを継承したクラス
class Bird extends Boid {

	// コンストラクタ
	Bird (PVector _pos, PVector _vel, color _col, DNA _dna, boolean _isInit) {
		super(_pos, _vel, _col); // Boid Class constructor
		shadowCol = color(50, 50);

		// Genetic Algorithm
		dna = _dna;
		//headSize        = map(dna.genes[0], 0, 1,  40,  15);

		headSize = 50;


		maxFlockSpeed   = map(dna.genes[0], 0, 1, 3.0, 8.0);
		maxFlockForce   = map(dna.genes[1], 0, 1, 2.0, 6.0);
		maxSeekSpeed    = map(dna.genes[0], 0, 1, 3.0, 8.0);
		maxSeekForce    = map(dna.genes[2], 0, 1, 2.0, 5.0);
		maxRunawaySpeed = map(dna.genes[0], 0, 1, 5.0, 12.0);
		maxRunawayForce = map(dna.genes[3], 0, 1, 2.5, 10.0);

		// 強化学習
		flowerWeight     = map(dna.genes[4], 0, 1, -2, 2);
		rockWeight     	 = map(dna.genes[5], 0, 1, -2, 2);
		flowerPerception = map(dna.genes[6], 0, 1, 0, 100);
		rockPerception   = map(dna.genes[7], 0, 1, 0, 100);

		// パラメータ要調整
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

    // ----- 以下、メンバ関数 -----

	// 適応度計算 ( 捕食者と遠い　かつ　花に近い　と　適応度が高くなるように設定 )
	void fitness(PVector nearestEnemy, PVector nearestFlower) {
		float distEnemy = dist(pos.x, pos.y, nearestEnemy.x, nearestEnemy.y);
		float distFlower = dist(pos.x, pos.y, nearestFlower.x, nearestFlower.y);
    	// 下限と上限の制御
    	if ( distEnemy > 2000 ) 	 distEnemy = 2000;
    	else if ( distEnemy < 1.0 )  distEnemy = 1.0;
    	if ( distFlower < 1.0 ) 	 distFlower = 1.0;
    	else if ( distFlower > 1000 ) distFlower = 1000;

    	float fitnessForEnemy  = pow( distEnemy / 2000, 2 );  // 指数関数的
    	float fitnessForFlower = pow( 1.0 /  distFlower, 2 ); // 指数関数的

    	fitness = fitnessForEnemy*0.5 + fitnessForFlower*0.5;
    }

	// 無性生殖の場合のみ使用
	Bird asexualReproduce() {
		if ( random(1) < BIRD_REPRO_PROB ) { // a certain probability
			DNA childDNA = dna.copy(); // 親の完全なコピーである子を新しく作る
			childDNA.mutate(0.01); // mutation
			birdGenerations++;

			// 子が生まれる位置の決定
			PVector bornPos = PVector.random2D();
			bornPos.mult(random(width*0.40));
			bornPos.add(new PVector(width*0.5, height*0.5));

			return new Bird(bornPos, vel.copy(), col, childDNA, false);

		} else {
			return null;
		}
	}

	// 位置の更新
	void update() {
		super.update();

		lifePoint -= 0.75;

		if ( born && dead ) dead = true;
		if ( lifePoint >= 255.0 ) born = false;
		if ( born && lifePoint < 255.0 ) lifePoint += 10.0;
    	//if ( dead ) lifePoint -= 20.0;
    }

	// 境界
	void boundaries() {
		desired = null;

		if ( !bPaintDetails ) {
			stroke(255);
			strokeWeight(10);
			line(width*0.15, -height*0.1, width*0.15, height*1.1);
			line(width*0.85, -height*0.1, width*0.85, height*1.1);
			line(width*0.15, -height*0.1, width*0.85, -height*0.1);
			line(width*0.15, height*1.1, width*0.85, height*1.1);
		}

		if ( pos.x < width*0.15 ) {
			desired = new PVector(maxSeekSpeed, vel.y);
		} else if ( pos.x > width*0.85 ) {
			desired = new PVector(-maxSeekSpeed, vel.y);
		}

		if ( pos.y < -height*0.1 ) {
			desired = new PVector(vel.x, maxSeekSpeed);
		} else if ( pos.y > height*1.1 ) {
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

	// 鳥の表示
	void display() {
		noStroke();
		fill(col, lifePoint);

		pushMatrix();
		translate(pos.x, pos.y);
		rotate(HALF_PI + atan2(vel.y, vel.x));
		beginShape();
		curveVertex(0, -0.5*headSize);
		curveVertex(0.15*headSize, 0);
			curveVertex(1.0*headSize, 0); // 羽の最右点
			curveVertex(0.8*headSize, 0.15*headSize);
			curveVertex(0.2*headSize, 0.25*headSize); //付け根
			curveVertex(0.3*headSize, 0.55*headSize);
			curveVertex(0.1*headSize, 0.4*headSize);

			curveVertex(0, 0.7*headSize); // 最下点

			curveVertex(-0.1*headSize, 0.4*headSize);
			curveVertex(-0.3*headSize, 0.55*headSize);
			curveVertex(-0.2*headSize, 0.25*headSize);
			curveVertex(-0.8*headSize, 0.15*headSize);
			curveVertex(-1.0*headSize, 0);
			curveVertex(-0.15*headSize, 0);

			curveVertex(0, -0.5*headSize);
			curveVertex(0.1*headSize, 0);
			endShape();
			popMatrix();
	}

	// 影の表示
	void shadow() {
		headSize += 5;
		pos.x += 30;
		noStroke();
		float alpha = map(lifePoint, 0, 200, 0, 100);
		fill(shadowCol, alpha);

		pushMatrix();
		translate(pos.x, pos.y);
		rotate(HALF_PI + atan2(vel.y, vel.x));
		beginShape();
		curveVertex(0, -0.5*headSize);
		curveVertex(0.15*headSize, 0);
			curveVertex(1.0*headSize, 0); // 羽の最右点
			curveVertex(0.8*headSize, 0.15*headSize);
			curveVertex(0.2*headSize, 0.25*headSize); //付け根
			curveVertex(0.3*headSize, 0.55*headSize);
			curveVertex(0.1*headSize, 0.4*headSize);

			curveVertex(0, 0.7*headSize); // 最下点

			curveVertex(-0.1*headSize, 0.4*headSize);
			curveVertex(-0.3*headSize, 0.55*headSize);
			curveVertex(-0.2*headSize, 0.25*headSize);
			curveVertex(-0.8*headSize, 0.15*headSize);
			curveVertex(-1.0*headSize, 0);
			curveVertex(-0.15*headSize, 0);

			curveVertex(0, -0.5*headSize);
			curveVertex(0.1*headSize, 0);
			endShape();
			popMatrix();
			pos.x -= 30;
			headSize -= 5;
	}
}