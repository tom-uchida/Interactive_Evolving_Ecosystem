class FlowerSystem {
	ArrayList<Flower> flowers;

	float flowerType;
	color flowerCol;
	color red = color(255, 30, 30);
	color orange = color(255, 148, 0);
	color yellow = color(255, 240, 30);
	color blue = color(53, 81, 255);
	color purple = color(180, 100, 255);
	color pink = color(236, 83, 157);
	color pink2 = color(255, 157, 202);

	FlowerSystem(int _num) {
		flowers = new ArrayList<Flower>();
		addFlowers(_num);
	}

	void addFlowers(int flowerNum) {
		PVector initPos;
		float scale;
		for ( int i = 0; i < flowerNum; i++ ) {
			while (true) {
				initPos = PVector.random2D();
				initPos.mult( random(0, width*0.3) ); // 初期位置の決定
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
					flowerType = decideType(flowerType); // 種類の決定
					float size = FLOWER_SIZE+random(10); // 大きさの決定
					flowerCol = decideColor(flowerCol); // 色の決定
					flowers.add( new Flower(initPos, flowerType, size, flowerCol, true) );
					break;
				}
			}
		}
	}

	void run() {
		Flower f;
		for (int i = flowers.size()-1; i >= 0; i--) {
			f = flowers.get(i);

			// 花が死んでいる場合
			if (f.isDead()) flowers.remove(i);

			f.update();
		}

		PVector bornPos;
		float size;
		// 一定の確率で花を追加する
    	if ( random(1) < FLOWER_APPEARANCE ) {
    		// 花が咲く位置の決定
    		bornPos = PVector.random2D();
			bornPos.mult( random(width*0.3) );
			bornPos.add( centerPos );

    		flowerType = decideType(flowerType); // 種類の決定
			size = FLOWER_SIZE+random(5); // 大きさの決定
			flowerCol = decideColor(flowerCol); // 色の決定
			flowers.add( new Flower(bornPos, flowerType, size, flowerCol, false) );
    	}
	}

	void display() {
		for ( Flower f : flowers ) {
			if ( bPaintDetails ) f.display();
			else f.paint();
		}
	}

	color decideColor(color col) {
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

	float decideType(float t) {
		int tmp = (int)random(0, 3);

		if ( tmp == 0 ) {
			t = 0.1;
		} else if ( tmp == 1 ) {
			t = 0.2;
		} else if ( tmp == 2 ) {
			t = 0.25;
		}

		return t;
	}
}