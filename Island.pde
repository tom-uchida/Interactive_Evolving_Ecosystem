class Island {
	PVector pos;
	FloatList vertX;
	FloatList vertY;

	PVector offset1, offset2;
	color c1Island = color( 75, 255, 75 );
    color c2Island = color( 0, 50, 10 );
	float radian;
	float size;
	float scale = 0.04;
	float x, y;

	int j = 0;

	Island(PVector _offset1, PVector _offset2, PVector _pos, float _size) {
		offset1 = _offset1;
		offset2 = _offset2;
		pos = _pos;
		size = _size;
		vertX = new FloatList();
		vertY = new FloatList();

		setVertex();
	}

	void setVertex() {
    	pushMatrix();
    	translate( pos.x, pos.y );
    
    	for ( float radius = size; radius > 0; radius -= (int)(size*0.25) ) {
      		for ( float angle = 0; angle < 360; angle+=10 ) {
          		radian = radians(angle);  
          		x = radius * cos(radian);
          		y = radius * sin(radian);

          		// 頂点情報
          		vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y),   0, 1,   -60, 60) );
          		vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y),   0, 1,   -60, 60) );

          		if ( radius == size ) {
         			rocksAroundIsland[j] = new PVector( vertX.get(j)+width*0.5, vertY.get(j)+height*0.5 );
          			j++;
        		}
      		}
    	}

    	popMatrix();
  	}

  	void display() {
  		// ローカル座標系に移動してから描画
  		pushMatrix();
    	translate( pos.x, pos.y );

  		int i = 0;
  		for ( float radius = size; radius > 0; radius -= (int)(size*0.25) ) {
  			fill( map(radius, 0, width, red(c1Island),   red(c2Island) ),
            	  map(radius, 0, width, green(c1Island), green(c2Island) ),
            	  map(radius, 0, width, blue(c1Island),  blue(c2Island) ) 
          		);

  			beginShape();
      		for ( float angle = 0; angle < 360; angle += 10 ) {
          		vertex( vertX.get(i), vertY.get(i) );
          		i++;
      		}
      		endShape(CLOSE);
    	}

    	popMatrix();
  	}
}