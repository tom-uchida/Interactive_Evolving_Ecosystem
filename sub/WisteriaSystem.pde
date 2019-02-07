// class WisteriaSystem {
// 	ArrayList<Wisteria> wisterias;
// 	ArrayList<WisteriaTrack> wisteriaTracks;

// 	WisteriaSystem() {
//     	wisterias = new ArrayList<Wisteria>();
//     	wisteriaTracks = new ArrayList<WisteriaTrack>();
// 	}

// 	void addWisterias(int wisteriaNum, PVector removePos, color col) {
// 		for ( int i = 0; i < wisteriaNum; i++ ) {
// 			wisterias.add(new Wisteria( new PVector(removePos.x+random(-20, 20), removePos.y+random(-20, 20)), col ) );
// 		}
// 	}

// 	void run() {
// 		for (int i = 0; i < wisterias.size(); i++) {
//         	Wisteria w = wisterias.get(i);

//         	if ( w.isDead() == false ) {
//             	w.update();
//             	//w.draw();

//             	// About Track
//             	PVector pos = w.pos;
//             	color col = w.col;
//             	float age = w.age;
//             	wisteriaTracks.add( new WisteriaTrack(pos, col, age) );

//         	} else {
//             	wisterias.remove(i);
//         	}
//     	}

// 		for ( WisteriaTrack wt : wisteriaTracks ) {
// 			wt.display();
// 		}
// 	}
// }