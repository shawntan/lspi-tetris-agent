import java.text.DecimalFormat;
import java.util.Arrays;




public class PlayerSkeleton {
	//implement this function to have a working system
	
	private FutureState fs;
	private DecimalFormat formatter = new DecimalFormat("#00");

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
			score = score(BasisFunction.getFeatureArray(s, fs));
			
			if(maxScore < score) {
				maxScore = score;
				maxMove = m;

			}
			//System.out.println(maxScore);
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
		State s = new State();
		new TFrame(s);
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
	}
	
}
