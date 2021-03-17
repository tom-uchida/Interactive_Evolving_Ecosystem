// class Wisteria {	
// 	PVector pos, vel, direction;
// 	float age = 0;
// 	color col;

// 	Wisteria(PVector _pos, color _col) {
// 		pos = _pos;
// 		vel = new PVector();
// 		age = 0;
// 		col = _col;
// 	}

// 	boolean isDead() {
// 		if ( age > 20 ) return true;
// 		else return false;
// 	}

// 	void update() { 
// 		colorMode(HSB, 360, 100, 100, 100);
// 		col = color(hue(col), saturation(col), age*5, 50+age*2.5);

// 		age += random(0.75, 2.0);
//     	vel.add( directions[(int)random(VECFIELD)]  );
//     	pos.add(vel);
// 	}

// }