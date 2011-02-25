import java.text.DecimalFormat;
import java.util.Arrays;




public class PlayerSkeleton {
	//implement this function to have a working system

	private FutureState fs;
	private DecimalFormat formatter = new DecimalFormat("#00");
	
	private double[] feature = new double[BasisFunction.FEATURE_COUNT];
	private double[] past = null;
	
	public int pickMove(State s, int[][] legalMoves) {
		if(fs==null) fs = new FutureState();
		fs.resetToCurrentState(s);
		int maxMove = pickBestMove(s, legalMoves, feature);
		//simulate next step
		if(past == null) past = new double[BasisFunction.FEATURE_COUNT]; 
		else BasisFunction.updateMatrices(s, past, feature);
		double[] tmp = feature;
		feature = past;
		past = tmp;
		return maxMove;
	}
	
	public int pickBestMove(State s, int[][] legalMoves, double[] feature){
		double score;
		double maxScore = Double.NEGATIVE_INFINITY;
		int maxMove = -1;
		for(int m = 0;m<legalMoves.length;m++){
			fs.makeMove(m);
			double[] f = BasisFunction.getFeatureArray(s, fs);
			score = score(f);
			if(maxScore < score) {
				maxScore = score;
				maxMove = m;
				System.arraycopy(f,0,feature,0,f.length);
			}
			fs.resetToCurrentState(s);
		}
		return maxMove;
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
		PlayerSkeleton p = new PlayerSkeleton();
		/*
		for(int i=0;i<11000;i++){
			State s = new State();
			while(!s.hasLost())s.makeMove(p.pickMove(s,s.legalMoves()));
			count++;
		}*/
		System.out.println(Arrays.toString(BasisFunction.weight));
		State s = new State();
		TFrame t = new TFrame(s);
		
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//BasisFunction.computeWeights();
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		
	}

}
