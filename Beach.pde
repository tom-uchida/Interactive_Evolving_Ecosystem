class Beach {
	PVector pos;
	FloatList vertX;
	FloatList vertY;

	PVector offset1, offset2;
	color beachCol = color( 200, 160, 110, 255 );
	float radian;
	float size;
	float scale = 0.04;
	float x, y;

	Beach (PVector _offset1, PVector _offset2, PVector _pos, float _size) {
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
    
    	for ( float radius = size; radius > 0; radius -= (int)size ) {
      		for ( float angle = 0; angle < 360; angle+=15 ) {
          		radian = radians(angle);  
          		x = radius * cos(radian);
          		y = radius * sin(radian);

          		// 頂点情報
          		vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y),   0, 1,   -80, 80) );
          		vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y),   0, 1,   -80, 80) );
      		}
    	}

    	popMatrix();
  	}

  	void display() {
  		// ローカル座標系に移動してから描画
  		pushMatrix();
    	translate( pos.x, pos.y );

  		int i = 0;
  		for ( float radius = size; radius > 0; radius-=(int)size ) {
  			fill(beachCol);

  			beginShape();
      		for ( float angle = 0; angle < 360; angle+=15 ) {
          		vertex( vertX.get(i), vertY.get(i) );
          		i++;
      		}
      		endShape(CLOSE);
    	}

    	popMatrix();
  	}
}