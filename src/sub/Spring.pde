class Spring { 
  //PVector anchor; // Location
  float length; // Rest length
  float k = SPRING_CONSTANT;
  
  Ball ballA;
  Ball ballB;

  Spring ( Ball _ballA, Ball _ballB, float _restLength) {
    ballA = _ballA;
    ballB = _ballB;
    length = _restLength; // rest length
  }

  // Constrain the distance between ball and anchor between min and max
  void constrainLength (float minlen, float maxlen) {
    PVector dir = PVector.sub(ballB.location, ballA.location);
    float distance = dir.mag();

    // Is it too short?
    if ( distance < minlen ) {
      dir.normalize();
      dir.mult(minlen);
      // Reset location and stop from moving (not realistic physics)
      ballB.location = PVector.add(ballA.location, dir);
      ballB.velocity.mult(0);

      // Is it too long?
    } else if ( distance > maxlen ) {
      dir.normalize();
      dir.mult(maxlen);
      // Reset location and stop from moving (not realistic physics)
      ballB.location = PVector.add(ballA.location, dir);
      ballB.velocity.mult(0);
    }
  }

  void update() {
    PVector springForce = PVector.sub(ballA.location, ballB.location);
    float currentLength = springForce.mag();
    float stretchLength = currentLength - length;
    springForce.normalize();
    springForce.mult(-1 * k * stretchLength);
    ballA.applyForce(springForce);
    springForce.mult(-1);
    ballB.applyForce(springForce);

    constrainLength( MIN_LENGTH, MAX_LENGTH ); // バネの長さを制御
  }

  void display() {
    strokeWeight(1);
    stroke(0);
    line(ballA.location.x, ballA.location.y, ballB.location.x, ballB.location.y);
  }
}