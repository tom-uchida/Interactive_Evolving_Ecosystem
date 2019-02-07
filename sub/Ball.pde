class Ball {
    PVector location = new PVector();
    PVector velocity = new PVector();
    PVector acceleration = new PVector();
    float mass = MASS_DEFAULT;
    float damping = 0.95;

    Ball (PVector _pos, PVector _vel, int _i, float _restLength) {
      location.x = _pos.x;
      location.y = _pos.y + _i*_restLength;
      velocity = _vel;
    }

    void applyForce(PVector force) {
      PVector forceCopy = force.copy();
      forceCopy.div(mass); // F = m * a より、 acc = force / mass
      acceleration.add(forceCopy);
    }

    void update() {
      velocity.add(acceleration);
      velocity.mult(damping);
      location.add(velocity);
      acceleration.mult(0); // Reset for next iteration
    }

    void display() {    
      strokeWeight(1.0);
      stroke(0);
      fill(200, 50, 255);
      float ballSize = mass * MASS_SCALAR;
      ellipse(location.x, location.y, ballSize, ballSize);
    }
}