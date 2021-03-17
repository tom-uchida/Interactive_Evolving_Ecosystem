/*
class Tail {
	PVector pos;
	PVector vel;

    boolean isInit = false; // Tailの先頭かどうか
    Ball ball;

    ArrayList<Tail> childTails; // 直結する子供のTailオブジェクトをリストで持っている

    Tail ( PVector _pos, PVector _vel, int _i) {
    	// Ballオブジェクトの生成
      	ball = new Ball(_pos, _vel, _i);

      	pos = _pos;
    }

    // 枝分かれのときに使う
    void createChildTails() {
    }

    void runBall() {
    	ball.update();
    	ball.display();
    }

    // 先頭をsin関数で動かす
   	void sinBall() {
  		ball.location.x = pos.x + 100*sin(frameCount * 0.05);
   	} 
}
*/