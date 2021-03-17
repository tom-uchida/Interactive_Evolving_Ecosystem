class Perceptron {
	FloatList weights; // 重みリスト
	float c; // 学習率の制御を行う学習定数

	Perceptron(float _c, int _initNum) {
		weights = new FloatList();
		c = _c;

		for (int i = 0; i < _initNum; i++) {
	 		weights.append( random(-1, 1) ); // 最初はランダムな重み
	 	}
	}

	// 入力(ベクトル)に対して重み付けをして，出力(ベクトル)を返す
	PVector feedforward(PVector[] forces) {
		PVector sum = new PVector();

		// 入力に対して重み付け
		for (int i = 0; i < weights.size(); i++) {
			forces[i].mult( weights.get(i) ); // 重み
			sum.add(forces[i]); // 加算
		}

		return sum;
	}

	// 強化学習
	void train(PVector[] forces, PVector error) {
		// Boidは自身のエラー(目標との差)を評価するため，
		// エラーを計算する必要はなく，引数としてエラーを受け取るだけ
		for (int i = 0; i < weights.size(); i++) { 
			weights.add( i, c * error.x * forces[i].x );
			weights.add( i, c * error.y * forces[i].y );
			weights.set( i, constrain(weights.get(i), 0, 1) );
		}
	}
}