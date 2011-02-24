import java.text.DecimalFormat;




public class PlayerSkeleton {
	//implement this function to have a working system

	private FutureState fs;
	private DecimalFormat formatter = new DecimalFormat("#00");
	private boolean firstMove = true;
	public int pickMove(State s, int[][] legalMoves) {
		if(fs==null) fs = new FutureState();
		fs.resetToCurrentState(s);
		double score;
		double maxScore = Double.NEGATIVE_INFINITY;
		int maxMove = -1;
		for(int m = 0;m<legalMoves.length;m++){
			fs.makeMove(m);
			/*
			System.out.println("---------------");
			printField(fs.getField());
			System.out.println("---------------");
			 */
			double[] f = BasisFunction.getFeatureArray(s, fs);
			score = score(f);
			if(maxScore < score) {
				maxScore = score;
				maxMove = m;
				System.arraycopy(f,0,features,0,f.length);
			}
			//System.out.println(maxScore);
			fs.resetToCurrentState(s);
		}
		if(firstMove){
			firstMove = false;
			swapFArrays();
		}
		else updateWeights();
		return maxMove;
	}
	private double[] prevFeatures = new double[BasisFunction.FEATURE_COUNT];
	private double[] features = new double[BasisFunction.FEATURE_COUNT];
	private void swapFArrays() {
		double[] tmp = prevFeatures;
		prevFeatures = features;
		features = tmp;
	}
	private void updateWeights(){
		BasisFunction.updateWeights(prevFeatures, features);
		swapFArrays();
	}
	private double score(double[] features){
		//System.out.println(Arrays.toString(features));
		double total=0;
		for(int i=0;i<features.length;i++) total+=features[i]*BasisFunction.weight[i];
		return total;
	}



	private void printField(int[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+formatter.format(field[i][j]));
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		
		for(int i=0;i<1;i++){
			State s = new State();
			TFrame t = new TFrame(s);
			PlayerSkeleton p = new PlayerSkeleton();
			while(!s.hasLost()) {
				s.makeMove(p.pickMove(s,s.legalMoves()));
				s.draw();
				s.drawNext(0,0);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("You have completed "+s.getRowsCleared()+" rows.");
			//t.dispose();
			//t.setVisible(false);
		}
	}

}
