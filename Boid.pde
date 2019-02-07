

// Boidクラス
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

	// ライフポイント
	float lifePoint;

	// 生物のサイズ
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

	color col;
	color shadowCol;

	boolean born;
	boolean dead;

	// 高速化(ループの外で定義)
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

	// コンストラクタ
	Boid (PVector _pos, PVector _vel, color _col) {
		pos = _pos;
		vel = _vel;
		acc = new PVector(0, 0);

		r = random(2.0, 25);
		mass = 2*r;

		col = _col;
	}

	// 生物が死んでいるか確認する関数
  	boolean isDead() {
  		if ( lifePoint <= 20.0 ) {
      		return true;

    	} else {
      		return false;
    	}
	}

	void update() {
		vel.add(acc);
		vel.limit(maxFlockSpeed);
		pos.add(vel);
		acc.mult(0);
	}

	void applyForce(PVector force) {
		acc.add(force.div(mass));
	}

	void flock(ArrayList<Boid> boids) {
		sep = separate(boids);
		coh = cohesion(boids);
		ali = align(boids);

		sep.mult(5.0);
		coh.mult(1.0);
		ali.mult(1.0);

		applyForce(sep);
		applyForce(coh);
		applyForce(ali);
	}

	PVector separate(ArrayList<Boid> boids) {
		steer = new PVector(0,0);
		count = 0;

		for ( Boid other : boids ) {
			d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForSeparation ) {
				diff = PVector.sub(pos, other.pos); //最初に引き算しておく
				diff.normalize();
				diff.div(d); // Weight by distance 調節
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

	PVector cohesion(ArrayList<Boid> boids) {
		steer = new PVector(0,0);
		count = 0;

		for ( Boid other : boids ) {
			d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForCohesion ) {
				diff = PVector.sub(other.pos, pos); //最初に引き算しておく　引き算逆
				diff.normalize();
				diff.div(d); // Weight by distance 調節
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

	PVector align(ArrayList<Boid> boids) {
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

	PVector searchNearestTarget(ArrayList<Boid> boids, Boid hunter, boolean bHunt) {
		nearestPos = centerPos; // random large value
		minD2 = 1.0e10; // random large value
		float tmpMinD2 = 0.0;

		Boid target;
		float d2;
		for ( int i = boids.size()-1; i >= 0; i-- ) {
			target = boids.get(i);

			diff = PVector.sub(target.pos, pos);
			d2 = diff.magSq();

			if ( d2 < minD2 ) {
				tmpMinD2 = minD2;
				//minD2 = d2; // update

				// 視野の概念(内積)
				PVector tmpDiff = diff.copy();
				PVector tmpVel = vel.copy();
				tmpDiff.normalize();
				tmpVel.normalize();
				float dot = tmpVel.dot(tmpDiff); // 内積値:cos

				// 現在の仮の最短posが，視界内かつ
				// エリア内であれば，nearestPosをupdate
				if ( dot > 0 ) { // > 0.5で視界120°  > 0で180°
					if ( height*0.6 > PVector.dist(centerPos, target.pos) ) {
						minD2 = d2; 
						nearestPos = target.pos.copy(); // update
					}

				// 現在の仮の最短posが，視界外であれば，posを更新しない
				} else {
					minD2 = tmpMinD2; // not update
					continue;
				}

				// Hunt
				if ( minD2 < 1000 && bHunt ) {
					boids.remove(i);

					hunter.pos.add(vel.mult(1.01)); // 加速
					lifePoint += random(50, 100);
				}
			}
		}

		return nearestPos.copy(); // Not nearestPos;
	}

	// G.A.
	Boid searchNearestBoid(ArrayList<Boid> boids) {
		Boid nearestBoid = new Boid(new PVector(0,0), new PVector(0, 0), color(0) );
		nearestPos = new PVector(2000, 0);
		minD2 = 1.0e10;

		float d2;
		for ( Boid b : boids) {
			diff = PVector.sub(b.pos, pos);
			d2 = diff.magSq();

			if (d2 < minD2 && d2 != 0.0) { // 自分自身は除く
				// update
				minD2 = d2; 
				nearestPos = b.pos.copy();
				nearestBoid = b;
			}
		}

		return nearestBoid;
		//return nearestPos.copy(); // Not nearestPos;
	}

	PVector searchNearestFlower(ArrayList<Boid> boids, ArrayList<Flower> flowers) {
		// random large value
		nearestPos = new PVector(2000, 0);
		minD2 = 1.0e10;
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

	void eatButterfly(ArrayList<Boid> butterflys) {
        record = 10000;
        nearestPos = null;

        Boid b;
        for ( int i = butterflys.size()-1; i >= 0; i-- ) {
        	b = butterflys.get(i);
            dist = pos.dist(b.pos);

            // 接触
            if ( dist < 3*maxSeekSpeed ) {
            	butterflys.remove(i);
                lifePoint += b.lifePoint; // 相手のlifePoint分回復

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
	void behaviors(ArrayList<Flower> good, ArrayList<Rock> bad) {
        steerGood = eatFlower( good, random(30, 100), flowerPerception);
        steerBad = eatRock( bad, random(-20, -10), rockPerception);

        steerGood.mult(flowerWeight);
        steerBad.mult(rockWeight);

        applyForce(steerGood);
        applyForce(steerBad);
    }

    PVector eatFlower(ArrayList<Flower> flowers, float nutrition, float perception) {
        record = 10000;
        nearestPos = null;

        for ( int i = flowers.size()-1; i >= 0; i-- ) {
        	Flower f = flowers.get(i);
            dist = pos.dist(f.pos);

            // 接触
            if ( dist < 3*maxSeekSpeed ) {
            	f.dead = true; // gradually disappear
                lifePoint += nutrition; // 摂取

                // Flowerの評価値（重み）上げる
                flowerWeight += 0.05;
                flowerWeight = constrain(flowerWeight, -2.0, 2.0);

            } else {
                // 知覚範囲内
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

    PVector eatRock(ArrayList<Rock> rocks, float nutrition, float perception) {
        record = 10000;
        nearestPos = null;

        for ( int i = rocks.size()-1; i >= 0; i-- ) {
        	Rock r = rocks.get(i);
            dist = pos.dist(r.pos);

            // 接触
            if ( dist < 0.9*r.size ) {
            	// fill(0);
            	// ellipse(pos.x, pos.y, 30, 30);

                lifePoint += nutrition;

                // Rockの評価値（重み）下げる
                rockWeight -= 0.1;
                rockWeight = constrain(rockWeight, -2.0, 2.0);

            } else {
                // 知覚範囲内
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

    PVector brainSeek(PVector targetPos) {
        desired = PVector.sub(targetPos, pos);
        desired.setMag(maxSeekSpeed);

        steer = PVector.sub(desired, vel);
        steer.limit(maxSeekForce);

        return steer;
    }

	void seek(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);

		desired.normalize();
		desired.mult(maxSeekSpeed);

		steer = PVector.sub(desired, vel);
		steer.limit(maxSeekForce);

		applyForce(steer);
	}

	PVector brainRunAway(Rock targetRock) {
		diff = PVector.sub(targetRock.pos, pos);
		float distance = diff.mag();

		if ( distance < 2*targetRock.size ) {
			stroke(0);
			line(pos.x, pos.y, targetRock.pos.x, targetRock.pos.y);

			diff.normalize();
			diff.mult(maxRunawaySpeed);

			steer = PVector.sub(diff, vel);
			steer.mult(-5.0*maxRunawayForce);
			//steer.mult(-1.0); // Run away

			return steer;
		}

		return new PVector(0, 0);
	}

	void flee(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);
		float distance = desired.mag();

		if ( distance < 100 ) {
			desired.normalize();
			desired.mult(maxRunawaySpeed);

			steer = PVector.sub(desired, vel);
			steer.limit(5.0*maxRunawayForce);
			steer.mult(-1.0); // Run away

			applyForce(steer);
		}
	}

	void fleeFromHand(PVector handPos) {
		desired = PVector.sub(handPos, pos);
		float distance = desired.mag();

		if ( distance < 300 ) {
			desired.normalize();
			desired.mult(maxRunawaySpeed);

			steer = PVector.sub(desired, vel);
			steer.limit(5.0*maxRunawayForce);
			steer.mult(-3.0); // Run away 要調整

			applyForce(steer);
		}
	}

	void arrive(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);
		float distance = desired.mag();

		if ( distance < 500 ) {
			float m = map(distance, 0, 500, 0, maxFlockSpeed);
			desired.setMag(m);
		} else {
			desired.setMag(maxFlockSpeed); // seekと同じこと
		}

		steer = PVector.sub(desired, vel);
		steer.limit(maxFlockForce);

		applyForce(steer);
	}

	void avoid(ArrayList<Boid> boids) {
		float desiredseparation = 30.0f; // 基準の距離

		steer = new PVector(0, 0);
		count = 0;

		float distance;
		for ( Boid b : boids ) {
			distance = PVector.dist(pos, b.pos);

			distance -= 100; // Obstacleの表面との距離にするため

			if ( (distance > 0) && (distance < desiredseparation) ) {
				// 重要
				diff = PVector.sub(b.pos, pos);

				float angle = PVector.angleBetween(diff, vel); //内積
				if ( angle > HALF_PI ) continue; //次のループへ

				PVector crss = vel.cross(diff); //外積を用いて避ける向きを判定

				int dir = 1;
				if ( crss.z > 0 ) dir = -1;

				diff.rotate(dir * HALF_PI);

				diff.normalize();
				diff.div(distance);

				steer.add(diff);
				count++;
			}
		}

		// 平均
		if ( count > 0 ) {
			steer.div((float)count);
		}

		if ( steer.mag() > 0 ) {
			steer.normalize();
			steer.mult(5.5*maxFlockSpeed); // 5.5は調整
			steer.sub(vel);
			steer.limit(5.5*maxFlockForce);
		}

		applyForce(steer);
	}

	PVector avoidObs(ArrayList<Rock> rocks) {
		float desiredSeparation = 30.0f; // 基準の距離

		steer = new PVector(0, 0);
		count = 0;

		float distance;
		for ( Rock r : rocks ) {
			distance = PVector.dist(pos, r.pos);

			distance -= 3*r.size; // Rockの表面との距離にするため

			if ( distance > 0 && distance < desiredSeparation ) {
				// 重要
				diff = PVector.sub(r.pos, pos);

				float angle = PVector.angleBetween(diff, vel); //内積
				if ( angle > HALF_PI ) continue; //次のループへ

				PVector crss = vel.cross(diff); //外積を用いて避ける向きを判定

				int dir = 1;
				if ( crss.z > 0 ) dir = -1;

				diff.rotate(dir * PI/3);

				diff.normalize();
				diff.div(distance);

				steer.add(diff);
				count++;

				// デバッグ
				// if ( !bPaintDetails ) {
				// 	strokeWeight(7);
				// 	stroke(255, 255, 0);
				// 	line(f.pos.x, f.pos.y, pos.x, pos.y);
				// }
			}
		}

		// 平均
		if ( count > 0 ) {
			steer.div((float)count);
		}

		if ( steer.mag() > 0 ) {
			steer.normalize();
			steer.mult(7.5*maxFlockSpeed); // 5.5は調整
			steer.sub(vel);
			steer.limit(7.5*maxFlockForce);
		}

		//applyForce(steer);
		return steer;
	}

	void paint(){
		noStroke();
		fill(col, lifePoint);

		float ang = atan2(vel.y, vel.x);
		PVector v1 = new PVector( pos.x + 0.8*mass*cos(ang), pos.y + mass*sin(ang) );
		PVector v2 = new PVector( pos.x + 0.8*mass*0.5*cos(ang+radians(90+45)), pos.y + 0.8*mass*0.5*sin(ang+radians(90+45)) );
		PVector v3 = new PVector( pos.x + 0.8*mass*0.5*cos(ang+radians(270-45)), pos.y + 0.8*mass*0.5*sin(ang+radians(270-45)) );
		triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
	}
}