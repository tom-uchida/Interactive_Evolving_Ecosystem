class RockSystem {
	ArrayList<Rock> rocks;

	RockSystem(int _num) {
		rocks = new ArrayList<Rock>();
		addCenterRocks(_num);
		addRocksAroundIsland();
	}

	// Center rocks
	void addCenterRocks(int rockNum) {
		for (int i = 0; i < rockNum; i++) {
			// Obstacle Rocks
			PVector initCenterPos;
			float radius;
			while (true) {
				initCenterPos = PVector.random2D();
				initCenterPos.mult(random(width*0.05, width*0.33)); // 初期位置の決定
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
	void addRocksAroundIsland() {
		for ( int i = 0; i < rocksAroundIsland.length; i++ ) {
			rocks.add( new Rock( new PVector(random(1000), random(1000)), 
					   new PVector(random(1000), random(1000)), 
					   rocksAroundIsland[i], 
					   random(50, 80) ) 
					);
		}
	}

	void display() {
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