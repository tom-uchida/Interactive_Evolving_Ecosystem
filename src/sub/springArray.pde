class SpringsArray {
	ArrayList<Spring> springs;
	ArrayList<Ball> balls;

	float restLength;

	ArrayList<SpringsArray> children;

	SpringsArray( int _numSprings, int _numBalls, PVector _pos, PVector _vel, float _restLength ) {
		springs = new ArrayList<Spring>();
		balls = new ArrayList<Ball>();

		addBalls(_pos, _vel, _numBalls);
		addSprings(_numSprings);
		
		restLength = _restLength;
	}

	void addBalls(PVector pos, PVector vel, int numBalls) {
		for ( int i = 0; i < numBalls; i++ ) {
			balls.add( new Ball( pos, vel, i, restLength ) );
		}
	}

	void addSprings(int numSprings) {
		for ( int i = 0; i < numSprings; i++ ) {
			springs.add( new Spring( balls.get(i), balls.get(i+1), restLength ) );
		}
	}

	// 分岐
	void createChildren() {
		children = new ArrayList<SpringsArray>();
	}

	void update() {
		for ( Spring s : springs ) {
			s.update();
		}

		for ( Ball b : balls ) {
			b.update();
		}
	}

	void display() {
		for ( Spring s : springs ) {
			s.display();
		}

		for ( Ball b : balls ) {
			b.display();
		}
	}
}