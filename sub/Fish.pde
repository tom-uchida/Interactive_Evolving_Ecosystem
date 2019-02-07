class Fish extends Boid {
  float headSize;

  Fish ( int _headSize, PVector _pos, PVector _vel, color _col) {
    super(_pos, _vel, _col); // Boidクラスのコンストラクタの呼び出し
    headSize = _headSize;
    lifePoint = 100;

    maxFlockSpeed = 3.0;
    maxFlockForce = 1.5;
    maxRunawaySpeed = 2.0;
    maxRunawayForce = 1.0;

    // パラメータ要調整
    distanceForSeparation = 2*r + headSize*2;
    distanceForCohesion = 2*r + 300;
    distanceForAlignment = 2*r + 300;
  }

  void display() {
    noStroke();
    fill(col, lifePoint);

    pushMatrix();
    translate(pos.x, pos.y);
    rotate(HALF_PI + atan2(vel.y, vel.x));
    beginShape();
      curveVertex(0, -0.8*headSize); // 頭
      curveVertex(0.3*headSize, 0);
      curveVertex(0.5*headSize, 0.1*headSize);
      curveVertex(1.0*headSize, 0.7*headSize); // 最右点
      curveVertex(0.4*headSize, 0.4*headSize);
      curveVertex(0.3*headSize, 0.6*headSize);
      curveVertex(0.25*headSize, 0.7*headSize);
      curveVertex(0.23*headSize, 0.8*headSize);
      curveVertex(0.21*headSize, 1.0*headSize);
      curveVertex(0.2*headSize, 1.2*headSize);
      curveVertex(0.15*headSize, 1.6*headSize);

      curveVertex(0.4*headSize, 1.9*headSize);
      curveVertex(0.5*headSize, 2.3*headSize); // 最下点(右)
      curveVertex(0, 1.8*headSize);
      curveVertex(-0.5*headSize, 2.3*headSize); // 最下点(左)
      curveVertex(-0.4*headSize, 1.9*headSize);

      curveVertex(-0.15*headSize, 1.6*headSize);
      curveVertex(-0.2*headSize, 1.2*headSize);
      curveVertex(-0.21*headSize, 1.0*headSize);
      curveVertex(-0.23*headSize, 0.8*headSize);
      curveVertex(-0.25*headSize, 0.7*headSize);
      curveVertex(-0.3*headSize, 0.6*headSize);
      curveVertex(-0.4*headSize, 0.4*headSize);
      curveVertex(-1.0*headSize, 0.7*headSize); // 最左点
      curveVertex(-0.5*headSize, 0.1*headSize);
      curveVertex(-0.3*headSize, 0);

      curveVertex(0, -0.8*headSize);
      curveVertex(0.1*headSize, 0);
    endShape();
    popMatrix();
  }

  void borderForFish() {
    PVector diffToCenter = PVector.sub( centerPos, pos );
    float distToCenter = diffToCenter.mag();

    // 島に近づきすぎると
    if ( distToCenter <= width*0.52 ) {
      lifePoint -= 10;

      vel.mult(-1.0);
      vel.rotate(QUARTER_PI);
      vel.add(pos);
    }

    if ( distToCenter >= width*0.60 ) {
      seek( new PVector(width*0.5, height*0.5) );
    }
  }
}