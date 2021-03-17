class Creature {
	// --- For Flocking ---
	PVector pos;
	PVector vel;
	PVector acc;

	float r;
	float mass;
	float maxForce;
	float maxSpeed;

	float distanceForSeparation;
	float distanceForCohesion;
	float distanceForAlignment;

	color col;


	// --- For SpringSystem ---
	ArrayList<SpringsArray> springsArrays;

	Creature (PVector _pos, PVector _vel) {
		// --- About Flocking ---
		pos = _pos;
		vel = _vel;
		acc = new PVector(0,0);

		r = random(2.5, 25);
		mass = 2*r;
		maxSpeed = 2.0;
		maxForce = 6.0;

		distanceForSeparation = 2*r; // 要調節
		distanceForCohesion = 2*r + 80;
		distanceForAlignment = 2*r + 100;

		col = color(50, 0, 255);


		// CREATURE_NUM の数だけ呼ばれる
		// creature 1体1体が持っているリスト
		// --- About SpringSystem ---
		springsArrays = new ArrayList<SpringsArray>();
		springsArrays.add( new SpringsArray(4, 5, _pos, _vel, 40) );
		//addSpringsArrays();
	}

	void addSpringsArrays() {
		springsArrays.add( new SpringsArray(4, 5, pos, vel, 40) );
	}

	void run() {
		// --- About Flocking ---
		update();
		//borders();
		display();

		// --- About SpringSystem ---
		runSpringsArray();
	}

	void runSpringsArray() {
		//println("springsArrays.size(): "+springsArrays.size());
		for ( SpringsArray sa : springsArrays ) {
			sa.update();
			sa.display();
		}
	}

	void update() {
		vel.add(acc);
		vel.limit(maxSpeed);
		pos.add(vel);
		acc.mult(0);
	}

	void applyForce(PVector force) {
		acc.add(force.div(mass));
	}

	void flock(ArrayList<Creature> creatures) {
		PVector sep = separate(creatures);
		PVector coh = cohesion(creatures);
		PVector ali = align(creatures);

		sep.mult(3.5);
		coh.mult(1.0);
		ali.mult(1.0);

		applyForce(sep);
		applyForce(coh);
		applyForce(ali);
	}

	PVector separate(ArrayList<Creature> creatures) {
		PVector steer = new PVector(0,0);
		int count = 0;

		for ( Creature other : creatures ) {
			float d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForSeparation ) {
				PVector diff = PVector.sub(pos, other.pos); //最初に引き算しておく
				diff.normalize();
				diff.div(d); // Weight by distance 要調節
				steer.add(diff);
				count++;
			}
		} 

		if ( count > 0 ) {
			steer.div(count);

			steer.normalize();
			steer.mult(maxSpeed);
			steer.sub(vel);
			steer.limit(maxForce);
		}

		return steer;
	}

	PVector cohesion(ArrayList<Creature> creatures) {
		PVector steer = new PVector(0,0);
		int count = 0;

		for ( Creature other : creatures ) {
			float d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForCohesion ) {
				PVector diff = PVector.sub(other.pos, pos); //最初に引き算しておく　引き算逆
				diff.normalize();
				diff.div(d); // Weight by distance 要調節
				steer.add(diff);
				count++;
			}
		} 

		if ( count > 0 ) {
			steer.div(count);

			steer.normalize();
			steer.mult(maxSpeed);
			steer.sub(vel);
			steer.limit(maxForce);
		}

		return steer;
	}

	PVector align(ArrayList<Creature> creatures) {
		PVector steer = new PVector(0,0);
		int count = 0;

		for ( Creature other : creatures ) {
			float d = PVector.dist(pos, other.pos);

			if ( d > 0 && d < distanceForAlignment ) {
				steer.add(other.vel);
				count++;
			}
		} 

		if ( count > 0 ) {
			steer.div(count);

			steer.normalize();
			steer.mult(maxSpeed);
			steer.sub(vel);
			steer.limit(maxForce);
		}

		return steer;
	}

	void display() {
		fill(col);
		stroke(col);
		ellipse(pos.x, pos.y, 3*r, 3*r);
	}
}