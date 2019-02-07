class Sea {
	PVector offset1, offset2;
	PVector pos;
    FloatList vertX;
    FloatList vertY;
    float size;
	float scale = 0.04;
	color c1Sea = color( 0, 50, 220 );
    color c2Sea = color( 50, 230, 255 );

    float radian;
    float x, y, nx, ny;

	Sea ( PVector _offset1, PVector _offset2, PVector _centre, float _size) {
		offset1 = _offset1;
		offset2 = _offset2;
		pos = _centre;
        size = _size;
        vertX = new FloatList();
        vertY = new FloatList();

        setVertex();
	}

    void setVertex() {
        pushMatrix();
        translate( pos.x, pos.y );
    
        for ( float radius = (int)size; radius > (int)size*0.25; radius -= (int)size*0.1 ) {
            for ( float angle = 0; angle < 360; angle+=20 ) {
                radian = radians(angle);  
                x = radius * cos(radian);
                y = radius * sin(radian);

                // 頂点情報
                vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y, frameCount * 0.01),   0, 1,   -150, 150) );
                vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y, frameCount * 0.01),   0, 1,   -150, 150) );
            }
        }

        popMatrix();
    }

    void display() {
        // ローカル座標系に移動してから描画
        pushMatrix();
        translate( pos.x, pos.y );

        int i = 0;
        for ( float radius = (int)size; radius > (int)size*0.25; radius -= (int)size*0.1 ) {
            fill( map(radius, (int)size*0.3, size, red(c1Sea), red(c2Sea) ),
                  map(radius, (int)size*0.3, size, green(c2Sea), green(c1Sea) ),
                  map(radius, (int)size*0.3, size, blue(c2Sea), blue(c1Sea) ) 
                );

            beginShape();
            for ( float angle = 0; angle < 360; angle += 20 ) {
                vertex( vertX.get(i), vertY.get(i) );
                i++;
            }
            endShape(CLOSE);
        }

        popMatrix();
    }

	void runSea() {
		pushMatrix();
		translate( pos.x, pos.y );

  	    for ( float radius = (int)size; radius > (int)size*0.3; radius -= (int)size*0.08 ) {
            fill( map(radius, (int)size*0.3, size, red(c1Sea), red(c2Sea) ),
                    map(radius, (int)size*0.3, size, green(c2Sea), green(c1Sea) ),
                    map(radius, (int)size*0.3, size, blue(c2Sea), blue(c1Sea) ) 
                );
    		
            beginShape();
            for ( float angle = 0; angle < 360; angle += 30 ) {
                radian = radians(angle);  
                x = radius * cos(radian);
                y = radius * sin(radian);
      	        nx = x + map(noise(x * scale + offset1.x, y * scale + offset1.y, frameCount * 0.01), 	0, 1, 	-150, 150);
      	        ny = y + map(noise(x * scale + offset2.x, y * scale + offset2.y, frameCount * 0.01), 	0, 1, 	-150, 150);
      	        vertex(nx, ny);
    	    }
    	    endShape(CLOSE);
		}
		popMatrix();
	}
}