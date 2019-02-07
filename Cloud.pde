class Cloud extends Boid {
    float[] dim;
    float[] vertX;
    float[] vertY;
    int mindim = 10;
    int maxdim = 100;
    int num;
    PVector desired;
    PVector steer;
  
    Cloud ( PVector _pos, PVector _vel, color _col) {
        super(_pos, _vel, _col);
        makeCloud();

        maxFlockSpeed = 2.0;
        maxFlockForce = 0.5;

        // パラメータ要調整
        distanceForSeparation = 2*r + 400;
        distanceForCohesion = 2*r + width*0.5;
        distanceForAlignment = 2*r + 500;
    }
  
    void makeCloud() {
        num = (int)random(20, 25);
        dim = new float[num];
        vertX = new float[num];
        vertY = new float[num];

        for ( int i = 0; i < num; i++ ) {
            dim[i] = random(mindim, maxdim);
            float x = 0.8*cos(TWO_PI/num * i);
            float y = 0.8*sin(TWO_PI/num * i);
            vertX[i] = x * dim[i];
            vertY[i] = y * dim[i];
        }
    }

    // 中心から一定距離離れると，中心をターゲットとするseek()を呼び出す
    void border() {
        PVector diffToCenter = PVector.sub( centerPos, pos );
        float distToCenter = diffToCenter.mag();

        if ( distToCenter >= width*0.35 ) {
            seek( centerPos );
            println("test\n");
        }
    }

    // A force to keep it on screen
  void boundaries() {
        desired = null;

        if ( pos.x < width*0.15 ) {
            desired = new PVector(maxSeekSpeed, vel.y);
        } else if ( pos.x > width*0.85 ) {
            desired = new PVector(-maxSeekSpeed, vel.y);
        }

        if ( pos.y < -height*0.1 ) {
            desired = new PVector(vel.x, maxSeekSpeed);
        } else if ( pos.y > height*1.1 ) {
            desired = new PVector(vel.x, -maxSeekSpeed);
        }

        if ( desired != null ) {
            desired.normalize();
            desired.mult(maxSeekSpeed);
            PVector steer = PVector.sub(desired, vel);
            steer.limit(maxSeekForce);
            
            applyForce(steer);
        }
    }
  
    void display() {
        pushMatrix();
        translate(pos.x, pos.y);
    
        fill(col);
        noStroke();
        for ( int i = 0; i < num; i++ ) {
            ellipse(vertX[i], vertY[i], 2*dim[i], 2*dim[i]);
        }

        popMatrix();
    }

    void shadow() {
        pushMatrix();
        translate(pos.x+100, pos.y);
    
        fill(100, 6);
        noStroke();
        for ( int i = 0; i < num; i++ ) {
            ellipse(vertX[i], vertY[i], 3*dim[i], 3*dim[i]);
        }

        popMatrix();
    }
}