class Rock {
	PVector pos;
	FloatList vertX;
	FloatList vertY;

	PVector offset1, offset2;
	float c1Rock = random(200, 255);
    float c2Rock = random(30, 100);
	float radian;
	float size;
	float scale = 0.04;
	float x, y;

	Rock(PVector _offset1, PVector _offset2, PVector _pos, float _size) {
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
      		for ( float angle = 0; angle < 360; angle+=15 ) {
          		radian = radians(angle);  
          		x = 1.1 * radius * cos(radian);
          		y = radius * sin(radian);

          		// 頂点情報
          		vertX.append( x + map(noise(x * scale + offset1.x, y * scale + offset1.y),   0, 1,   -20, 20) );
          		vertY.append( y + map(noise(x * scale + offset2.x, y * scale + offset2.y),   0, 1,   -20, 20) );
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
  			fill( map(radius, 0, size, c1Rock, c2Rock) );

  			beginShape();
      		for ( float angle = 0; angle < 360; angle += 15 ) {
          		vertex( vertX.get(i), vertY.get(i) );
          		i++;
      		}
      		endShape(CLOSE);
    	}

    	popMatrix();
  	}

  	void shadow() {
  		pushMatrix();
    	translate( pos.x + size*0.5, pos.y + size*0.5);

  		fill( 50, 50 );
  		beginShape();
      	for ( int i = 0; i < 24; i++ ) {
          	vertex( vertX.get(i), vertY.get(i) );
      	}
      	endShape(CLOSE);

    	popMatrix();
  	}

  	void paint() {
  		rectMode(CENTER);
    	fill(50);
    	rect(pos.x, pos.y, 1.8*size, 1.8*size);
  	}
}