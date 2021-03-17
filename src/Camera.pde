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

		// 要調節
		maxSpeed = 5.0;
		maxForce = 0.1;
	}

	void update() {
		vel.add(acc);
		vel.limit(maxSpeed);
		pos.add(vel);
		acc.mult(0);
	}

	void applyForce(PVector force) {
		acc.add(force);
	}

	// 重心計算
	PVector searchCenterPos(ArrayList<Boid> butterflys, ArrayList<Boid> birds, ArrayList<Boid> dragons) {
		centerPosX = 0;
		centerPosY = 0;

		for ( Boid b : butterflys ) {
			centerPosX += 0.5*b.pos.x;
			centerPosY += 0.5*b.pos.y;
		}

		for ( Boid b : birds ) {
			centerPosX += b.pos.x;
			centerPosY += b.pos.y;
		}

		for ( Boid b : dragons ) {
			// Dragonに重みを付ける
			centerPosX += 20*b.pos.x;
			centerPosY += 20*b.pos.y;
		}

		centerPosX = centerPosX / ( 0.5*butterflys.size() + birds.size() + 20*dragons.size() );
		centerPosY = centerPosY / ( 0.5*butterflys.size() + birds.size() + 20*dragons.size());

		PVector centerPos = new PVector(centerPosX, centerPosY);
		return centerPos;
	}

	// targetPos is centerPos
	void seek(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);

		desired.normalize();
		desired.mult(maxSpeed);

		steer = PVector.sub(desired, vel);
		steer.mult(maxForce);

		applyForce(steer);
	}

	void arrive(PVector targetPos) {
		desired = PVector.sub(targetPos, pos);
		float distance = desired.mag();

		if ( distance < 300 ) {
			float m = map(distance, 0, 300, 0, maxSpeed);
			desired.setMag(m);
		} else {
			desired.setMag(maxSpeed); // seekと同じこと
		}

		steer = PVector.sub(desired, vel);
		steer.limit(maxForce);

		applyForce(steer);
	}

	void display() {
		strokeWeight(3);
		stroke(0);
		line(width*0.5, -height, width*0.5, 2*height);
		line(-width, height*0.5, 2*width, height*0.5);

		noFill();
		ellipse(pos.x, pos.y, 20, 20);


		textSize(40);
		fill(0);
		text("G", pos.x+15, pos.y-15);
	}
}