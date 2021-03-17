class DNA {
	float[] genes; 

	DNA (int _num) {
		genes = new float[_num];
		for (int i = 0; i < genes.length; i++) {
			genes[i] = random(0, 1);
		}
	}

	DNA (float[] newGenes) {
        genes = newGenes;
    }

    // 交叉(有性生殖)
    DNA crossover(DNA partner) {
        float[] childGenes = new float[genes.length];
        int midPoint = int(random(genes.length)); // 区切りとなる点を決める
        // Take "half" from one and "half" from the other
        for (int i = 0; i < genes.length; i++) {
            if (i > midPoint)  childGenes[i] = genes[i];
            else               childGenes[i] = partner.genes[i];
        }
        DNA newGenes = new DNA(childGenes);
      
        return newGenes;
    }

    // 無性生殖の場合のみ使用
	// Instead of crossover
	DNA copy() {
		float[] newGenes = new float[genes.length];
        arrayCopy(genes, newGenes);
   
        return new DNA(newGenes);
    }

	// Mutation
    void mutate(float mutateRate) {
        for (int i = 0; i < genes.length; i++) {
            if ( random(1) < mutateRate ) {
                genes[i] = random(0, 1);
            }
        }
    }
}