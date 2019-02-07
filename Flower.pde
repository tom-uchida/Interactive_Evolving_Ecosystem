class Flower {
    int num = 100; //頂点の描画回数
    float scale; //花の大きさ調整用

    PVector pos;

    float theta_A = 0;
    float delta_A = 1.0;
    float theta_B = 0;
    float delta_B;

    float theta_A_goal = theta_A + delta_A;
    float theta_B_goal;

    float angle;
    color col;

    float lifePoint;
    boolean dead;
    boolean born;

    Flower(PVector _initPos, float _delta_B, float _scale, color _col, boolean _isInit) {
        pos = _initPos;

        delta_B = _delta_B; // 0.1 or 0.2 or 0.25
        theta_B_goal = theta_B + delta_B;
        angle = random(PI);

        scale = _scale;
        col = _col;

        if ( _isInit ) {
            lifePoint = 255;
            dead = false;
        } else {
            lifePoint = 0;
            born = true;
        }
    }

    boolean isDead() {
        if ( lifePoint < 0.0 ) {
            return true;
        } else {
            return false;
        }
    }

    void update() {
        if ( born && lifePoint < 255.0 && !dead ) lifePoint += 5.0;
        if ( dead ) lifePoint -= 30.0;
    }

    void display() {
        fill(col, lifePoint);

        pushMatrix();
        translate(pos.x, pos.y);
        rotate( angle );
        beginShape();
        for (int i = 0; i < num; i++) {
            float r = sin(theta_A) / 2 + 1;
            float x = scale * r * sin(theta_B);
            float y = scale * r * cos(theta_B);

            theta_A += delta_A;
            theta_B += delta_B;

            curveVertex(x, y);
        }
        theta_A = theta_A_goal;
        theta_B = theta_B_goal;
        endShape();

        // 花の中心
        noStroke();
        fill(255, 100, 0, lifePoint);
        ellipse(0, 0, scale*0.8, scale*0.8);

        popMatrix();
    }

    void paint() {
        fill(col, lifePoint);
        ellipse(pos.x, pos.y, 3*scale, 3*scale);
    }
}