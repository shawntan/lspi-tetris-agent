import java.text.DecimalFormat;
import java.util.Arrays;




public class PlayerSkeleton {
	//implement this function to have a working system

	private FutureState fs;
	private FutureState s2;
	private DecimalFormat formatter = new DecimalFormat("#00");

	private double[] feature = new double[BasisFunction.FEATURE_COUNT];
	private double[] past = null;
	protected BasisFunction bs = new BasisFunction();
	protected boolean learns = false;


	public int pickMove(State s, int[][] legalMoves) {
		if(fs==null) fs = new FutureState();
		if(s2==null) s2 = new FutureState();
		fs.resetToCurrentState(s); //set the state to the current to prepare to simulate next move.
		int maxMove = pickBestMove(s, legalMoves, feature);  //pick best move returns the highest scoring weighted features
		fs.makeMove(maxMove);//simulate next step
		if(learns) {
			if(past == null) past = new double[BasisFunction.FEATURE_COUNT]; 
			else {
				bs.updateMatrices(s, past, feature); //updates the matrices - adds the current "instance" into its training data
			}
		}
		double[] tmp = feature;		//swap the past and present around - reuse, reduce and recycle arrays.:)
		feature = past;
		past = tmp;

		return maxMove;
	}

	BasisFunction getBasisFunctions() {
		return bs;
	}


	/**
	 * Given the current state, and the set of legal moves, decide the index of the move to be taken.
	 * Score is essentially does multiplies each feature with its corresponding weight, and sums it all up.
	 * 
	 * @param s Current state
	 * @param legalMoves Available moves to be made
	 * @param feature Temporary array for storing features
	 * @return
	 */
	public int pickBestMove(State s, int[][] legalMoves, double[] feature){
		double score;
		double maxScore = Double.NEGATIVE_INFINITY;

		int d = legalMoves.length;
		int init = (int)(Math.random()*d); //randomise the starting point to look at so that its not always the first highest score
		int m = (init+1)%d;
		int maxMove = m ;

		while(m != init){
			fs.makeMove(m);
			if(!fs.hasLost()) {
			double[] f = bs.getFeatureArray(s, fs,legalMoves[m]);
			score = score(f);// + minMoveScores(fs,s2,maxScore)/State.N_PIECES;
			if(maxScore < score) {
				maxScore = score;
				maxMove = m;
				if(learns) System.arraycopy(f,0,feature,0,f.length);
			}
			}
			fs.resetToCurrentState(s);
			m = (m+1)%d;
		}
		return maxMove;
	}
	/*
	public double minMoveScores(FutureState s1,FutureState s2,double maxScore){
		double minScore = Double.MAX_VALUE;
		for(int p=0;p<State.N_PIECES;p++){
			s2.setNextPiece(p);
			int legalMoves = s2.legalMoves().length;
			double bestScore = 0;
			for(int i=0;i<legalMoves;i++) {
				s2.resetToCurrentState(s1);
				s2.setNextPiece(p);
				s2.makeMove(i);
				double[] f = bs.getFeatureArray(s1, s2,legla);
				double score = score(f);
				
				bestScore = Math.max(score, bestScore);
				if(bestScore > minScore) break;
			}
			minScore = Math.min(bestScore, minScore);
			if(minScore < maxScore) break;
		}
		
		//System.out.println("\t\t"+worsePiece);
		return minScore;
	}*/

	private double score(double[] features){
		//System.out.println(Arrays.toString(features));
		double total=0;
		for(int i=0;i<features.length;i++) total+=features[i]*bs.weight[i];
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

		State s = new State();
		TFrame t = new TFrame(s);

		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//BasisFunction.computeWeights();
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");

	}

}
