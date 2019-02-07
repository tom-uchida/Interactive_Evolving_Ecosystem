class FlockingSystem {
	ArrayList<Boid> butterflys;
	ArrayList<Boid> birds;
	ArrayList<Boid> dragons;
	//ArrayList<Boid> fishes;
	//ArrayList<Boid> clouds;

	ArrayList<Boid> matingPool; // 交配プール

	// ----- セル空間分割法 -----
	ArrayList<Boid>[][] gridForButterfly;
	ArrayList<Boid>[][] gridForBird;
	ArrayList<Flower>[][] gridForFlower;
	ArrayList<Rock>[][] gridForRock;
	ArrayList<Boid> targetButterflys;
	ArrayList<Boid> targetBirds;
	ArrayList<Flower> targetFlowers;
	ArrayList<Rock> targetRocks;
	int scl = 80; // 1セルのサイズ(この値を小さくするほどサイクル数が減る)
	int cols, rows;
	int cellX, cellY;
	// -----------------------

	// 高速化のためにループ( run()関数 )の外で定義
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
		cols = (int) (width*1.5 / scl);
  		rows = (int) (height*1.5 / scl);

		// 2次元配列としてgridを初期化
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

		// Rock セルの割り当て
		cellAssignmentForRock(rockSystem.rocks, gridForRock);
	}

	void addButterflys(PVector pos) {
		for ( int i = 0; i < BUTTERFLY_NUM; i++ ) {
			PVector initPos = new PVector(random(width*0.15, width*0.45), random(height*0.6) );
			PVector initVec = PVector.random2D().mult(0);
			color col;
			if ( i < BUTTERFLY_NUM*0.5 ) col = color(30, 180, 255); // メス
			else 						 col = color(200, 240, 240); // オス
			DNA dna = new DNA(8);
			butterflys.add( new Butterfly(initPos, initVec, col, dna, true) );
		}
	}

	void addBirds(PVector pos) {
		for ( int i = 0; i < BIRD_NUM; i++ ) {
			PVector initPos = new PVector(random(width*0.55, width*0.85), random(height*0.6) );
			PVector initVec = PVector.random2D().mult(0);
			color col;
			if ( i < BIRD_NUM*0.5 ) col = color(255, 100, 0); // メス
			else 					col = color(255, 200, 0); // オス
			DNA dna = new DNA(8);
			birds.add( new Bird(initPos, initVec, col, dna, true) );
		}
	}

	void addDragons(PVector pos) {
		for ( int i = 0; i < DRAGON_NUM; i++ ) {
			PVector initPos = new PVector(random(width*0.2, width*0.8), random(height*0.5, height*0.9) );
			PVector initVec = PVector.random2D().mult(0);
			color col = color(40, 40, 100);
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
  	void makeButterfly(PVector bornPos, color col) {
    	PVector vel = new PVector(0, 0);
    	DNA dna = new DNA(8);

    	butterflys.add( new Butterfly(bornPos.copy(), vel, col, dna, false) );

    	// Debug
  		fill(col);
  		ellipse(bornPos.copy().x, bornPos.copy().y, 70, 70);
  	}

  	void makeBird(PVector bornPos, color col) {
    	PVector vel = new PVector(0, 0);
    	DNA dna = new DNA(8);

    	birds.add( new Bird(bornPos.copy(), vel, col, dna, false) );

    	// Debug
  		fill(col);
  		ellipse(bornPos.copy().x, bornPos.copy().y, 70, 70);
  	}

  	void makeDragon(PVector bornPos) {
  		PVector vec = PVector.random2D().mult(0);
  		color col = color(40, 40, 100);
  		DNA dna = new DNA(4);

  		dragons.add( new Dragon(bornPos.copy(), vec, col, dna, false) );

  		// Debug
  		fill(col);
  		ellipse(bornPos.copy().x, bornPos.copy().y, 70, 70);
  	}

  	// 割り当てた情報をリセット
	void resetGridForBoid(ArrayList<Boid> grid[][]) {
	  	for (int i = 0; i < cols; i++)
	    	for (int j = 0; j < rows; j++)
	      		grid[i][j].clear();
	}

	void resetGridForFlower(ArrayList<Flower> grid[][]) {
	  	for (int i = 0; i < cols; i++)
	    	for (int j = 0; j < rows; j++)
	      		grid[i][j].clear();
	}

	// Boidに対して，位置に応じた適切なセルに自身を登録する
	void cellAssignmentForBoid(ArrayList<Boid> boids, ArrayList<Boid> grid[][]) {
	  	for (Boid b : boids) {
	    	// そのBoidが，どのセルに属するかを決める
	    	cellX = int(b.pos.x) / scl; 
	    	cellY = int(b.pos.y) / scl;

	    	if ( cellX >= 0 && cellX < cols )
	    		if ( cellY >= 0 && cellY < rows )
	    			grid[cellX][cellY].add(b);
	  	}
	}

	void cellAssignmentForFlower(ArrayList<Flower> flowers, ArrayList<Flower> grid[][]) {
	  	for (Flower f : flowers) {
	    	// そのBoidが，どのセルに属するかを決める
	    	cellX = int(f.pos.x) / scl; 
	    	cellY = int(f.pos.y) / scl;

	    	if ( cellX >= 0 && cellX < cols )
	    		if ( cellY >= 0 && cellY < rows )
	    			grid[cellX][cellY].add(f);
	  	}
	}

	void cellAssignmentForRock(ArrayList<Rock> rocks, ArrayList<Rock> grid[][]) {
	  	for (Rock r : rocks) {
	    	// そのBoidが，どのセルに属するかを決める
	    	cellX = int(r.pos.x) / scl; 
	    	cellY = int(r.pos.y) / scl;

	    	if ( cellX >= 0 && cellX < cols )
	    		if ( cellY >= 0 && cellY < rows )
	    			grid[cellX][cellY].add(r);
	  	}
	}

	void run() {
		// セル情報のリセット
		resetGridForBoid(gridForButterfly);
		resetGridForBoid(gridForBird);
		resetGridForFlower(gridForFlower);

		// セルの割り当て
		cellAssignmentForBoid(butterflys, gridForButterfly); 
		cellAssignmentForBoid(birds, gridForBird);
		cellAssignmentForFlower(flowerSystem.flowers, gridForFlower);

		// セル空間分割法(ループの回数が大幅減)
	  	//stroke(255);
	  	for (int c = 0; c < cols; c++) {
	    	//line(c*scl, 0, c*scl, height);
	    	for (int r = 0; r < rows; r++) {
	      		//line(0, r*scl, width, r*scl);

	      		// 各種処理の対象となるBoidsを更新
	      		targetButterflys.clear();
	      		targetBirds.clear();
	      		targetFlowers.clear();
	      		targetRocks.clear();

			    // 注目セルの8近傍に属するBoidを登録
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
			    	// 自分の周囲の Butterfly と Flocking
			    	butter.flock(targetButterflys); // flockする対象を渡す

			    	// 自分の周囲の Bird だけをチェック
			    	targetBird = butter.searchNearestBoid(targetBirds);
					butter.flee(targetBird.pos);

					// 自分の近くの Flower，Rock だけチェック
					// Reinforcement Learning
					butter.behaviors( targetFlowers, targetRocks );
			    }

			    for ( Boid bir : gridForBird[c][r] ) {
			    	// 自分の周囲の Bird と Flocking
			    	bir.flock(targetBirds);

			    	// 自分の近くの Flower，Rock だけチェック
					// Reinforcement Learning
					bir.behaviors( targetFlowers, targetRocks );
			    }
			}
		}
 
		// About Butterfly
		for (int i = butterflys.size()-1; i >= 0 ; i--) {
			b = butterflys.get(i);

			// 死んでいる場合
	      	if (b.isDead()) butterflys.remove(i);

	      	// Interaction
	      	// 0(m) ~ 2(m) : 手の位置から逃げる
	      	if ( currentUserNum > 0 && 0.0 <= distToUser && distToUser < 2.0 ) {
	      		for (int id = 0; id < currentUserNum; id++) {
	      			b.fleeFromHand( new PVector(3*rightHandPosX[id], 2.5*rightHandPosY[id]) );
	      			b.fleeFromHand( new PVector(3*leftHandPosX[id], 2.5*leftHandPosY[id]) );
	      		}
	      	}

	      	// Butterflyは，Dragonから逃げる
			targetDragon = b.searchNearestBoid(dragons);
			b.flee(targetDragon.pos);

			targetFlowerPos = b.searchNearestFlower(butterflys, flowerSystem.flowers);
			// b.seek(targetFlowerPos);

			// poly-morphism
			butterfly = (Butterfly) b;
				// 適応度は常に計算しておく
				// その個体がどれぐらい環境に適応しているかを示す値
				butterfly.fitness(targetDragon.pos, targetFlowerPos); // step1

	      		/*********************** Genetic Algorithms ***********************/
	      		// 他の蝶と距離が近い場合のみ，生殖
	      		nearestButterfly = b.searchNearestBoid(butterflys);
	      		nearestDist = PVector.dist(b.pos, nearestButterfly.pos);


	      		// 性別が違う蝶同士のみ生殖可
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
	      
	      			// 子が生まれる位置の決定
					bornPos = PVector.random2D();
					bornPos.mult(random(50, 100));
					bornPos.add( b.pos );
					camera.seek(bornPos); // カメラワーク

	      			// 誕生した子を集団に追加
	      			// copy()重要!!
	      			butterflys.add( new Butterfly(bornPos, b.vel.copy(), b.col, childDNA, false) );
	      			isStartSexualReproButter = false;
	      		}

	      		
	      		// 無性生殖
	      		if ( bButterflyAsexualRepro ) {
	      			childButterfly = butterfly.asexualReproduce();
      				if ( childButterfly != null ) {
						butterflys.add( childButterfly ); // 子が誕生した場合は，集団に追加
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

			// 死んでいる場合
			if (b.isDead()) birds.remove(i);

			// Interaction
	      	// 0(m) ~ 2(m) : 手の位置から逃げる
	      	if ( currentUserNum > 0 && 0.0 <= distToUser && distToUser < 2.0 ) {
	      		for (int id = 0; id < currentUserNum; id++) {
	      			b.fleeFromHand( new PVector(3*rightHandPosX[id], 2.5*rightHandPosY[id]) );
	      			b.fleeFromHand( new PVector(3*leftHandPosX[id], 2.5*leftHandPosY[id]) );
	      		}
	      	}

			// Birdは，Dragonから逃げる
			targetDragon = b.searchNearestBoid(dragons);
			b.flee(targetDragon.pos);

			// // Birdは，Butterflyを食べる
			b.eatButterfly(butterflys);
			// targetButterflyPos = b.searchNearestTarget(butterflys, b, true);
			// b.seek(targetButterflyPos);

			// // Birdは，Flowerを食べる
			// targetFlowerPos = b.searchNearestFlower(birds, flowerSystem.flowers);
			// b.seek(targetFlowerPos);

			// poly-morphism
			bird = (Bird) b;
				// 適応度は常に計算しておく
				bird.fitness(targetDragon.pos, targetFlowerPos); // step1

	      		/*********************** Genetic Algorithms ***********************/
	      		// 他の鳥と距離が近い場合のみ，生殖
	      		nearestBird = b.searchNearestBoid(birds);
	      		nearestDist = PVector.dist(b.pos, nearestBird.pos);

	      		// 性別が違う鳥同士のみ生殖可
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
	      
	      			// 子が生まれる位置の決定
					bornPos = PVector.random2D();
					bornPos.mult(random(50, 100));
					bornPos.add( b.pos );
					camera.seek(bornPos); // カメラワーク

	      			// 誕生した子を集団に追加
	      			// copy()重要!!
	      			birds.add( new Bird(bornPos, b.vel.copy(), b.col, childDNA, false) );
	      			isStartSexualReproBird = false;
	      		}

	      		
	      		// 無性生殖
	      		if ( bBirdAsexualRepro ) {
	      			childBird = bird.asexualReproduce();
      				if ( childBird != null ) {
						birds.add( childBird ); // 子が誕生した場合は，集団に追加
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

	      	//b.flock(dragons); // dragon同士で群れ

			b.avoidObs(rockSystem.rocks);

			targetButterflyPos = b.searchNearestTarget(butterflys, b, true);
			targetBirdPos = b.searchNearestTarget(birds, b, true);
			dButterfly = PVector.dist(b.pos, targetButterflyPos);
			dBird = PVector.dist(b.pos, targetBirdPos);
			// ButterflyとBird，より近い方をseek
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
				// 生殖
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

	void display() {
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
	void runCamera() {
		PVector targetPos = camera.searchCenterPos(butterflys, birds, dragons);
		camera.seek(targetPos);
		camera.arrive(targetPos);
		camera.update();
		if ( !bPaintDetails ) camera.display();
	}

	// 集団の中で，最も適応度が高い個体を探す
  	float getMaxFitness(ArrayList<Boid> boids) {
    	float maxFitness = 0;
    	for (int i = 0; i < boids.size(); i++) {
      		if ( boids.get(i).fitness > maxFitness ) {
        		maxFitness = boids.get(i).fitness;
      		}	
    	}

    	return maxFitness;
  	}

  	// 交配プールの作成
  	void selection(ArrayList<Boid> boids) {
    	matingPool.clear();

    	float maxFitness = getMaxFitness(boids); // 最も高い適応度の値

    	// 集団の全ての要素の適応度を，最も高い適応度で正規化
    	// 正規化された適応度に従って，交配プールに追加
    	for (int i = 0; i < boids.size(); i++) {
      		float fitnessNormal = map(boids.get(i).fitness, 0, maxFitness, 0, 1);
      		int n = (int) (fitnessNormal * 100);  // 0から100までの範囲
      		for (int j = 0; j < n; j++) {
        		matingPool.add(boids.get(i)); // 交配プールに追加
      		}
    	}
  	}

  	// 有性生殖
  	DNA reproduction() {
      	int a = int( random(matingPool.size()) );
      	int b = int( random(matingPool.size()) );
      
      	// 交配プールから，親を２つピックアップ
      	Boid parentA = matingPool.get(a);
      	Boid parentB = matingPool.get(b);

      	// 親のDNAを取得
      	DNA genesA = parentA.dna;
      	DNA genesB = parentB.dna;
      
      	// 交叉
      	DNA childDNA = genesA.crossover(genesB);
      
      	// 突然変異(1%)
      	childDNA.mutate(0.01);

		return childDNA;
  	}
}