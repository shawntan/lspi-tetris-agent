import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

public class Trainer {
	private static int ROUNDS = 30;
	private static int SPIN_STEP_DELAY = 2500;
	
	public static void main(String[] args) throws IOException {
		PlayerSkeleton player = new PlayerSkeleton();
		player.learns = true;
		State s;
		TFrame frame = null;
		double[] weights = new double[BasisFunction.FEATURE_COUNT];
		BasisFunction bf = player.getBasisFunctions();
		double[] defWeights  = bf.weight;
		System.arraycopy(defWeights, 0,weights, 0, BasisFunction.FEATURE_COUNT);
		String score = "";
		double bestAvg = 0;

		// keep on training!
		while(true) {
			int prevLength = 0;
			
			System.arraycopy(weights, 0,defWeights, 0, BasisFunction.FEATURE_COUNT);
			System.out.println("Training for " + ROUNDS + " rounds...");
			double totalTrainingScore = 0;
			double totalTSSquared = 0;
			for(int i=0;i<ROUNDS;i++){

				s = new State();
				if(frame==null) frame = new TFrame(s);
				else frame.bindState(s);
				playGame(s,player,score);
				int cleared = s.getRowsCleared();
				
				totalTrainingScore = totalTrainingScore + cleared;
				totalTSSquared = totalTSSquared + (double)Math.pow(cleared, 2);
				
				score = Integer.toString(cleared);
				System.out.print("\r  ");
				System.out.print(score);
				for(int j=0;j<=prevLength-score.length();j++) System.out.print(' ');
				prevLength = score.length();

			}
			
			double avg = (totalTrainingScore/(double)ROUNDS);
			double sd = (double)Math.sqrt((totalTSSquared - totalTrainingScore*avg)/(ROUNDS-1));
			System.out.print("\rAverage training score: ");
			System.out.print(avg);
			System.out.print(" s.d.: ");
			System.out.print(sd);
			
			
			System.out.println();
			
			if(avg>bestAvg) bestAvg = avg;
			
			bf.computeWeights();
			for(int i=0;i<weights.length;i++) {
				weights[i] = 0.1 * weights[i] + 0.9 * defWeights[i];
				weights[i] = 0.001 * (weights[i]*(0.5 - Math.random()));
			}
			//System.out.println("Weights:"+Arrays.toString(bf.weight));
			System.out.println("Testing for "+ROUNDS+" rounds...");
			double totalTestingScore = 0;
			double totalTSquared = 0;
			prevLength = 0;
			for(int i=0;i<ROUNDS;i++){
				s = new State();
				frame.bindState(s);
				playGame(s,player,score);
				int cleared =s.getRowsCleared();
				totalTestingScore += cleared;
				totalTSquared += Math.pow(cleared,2);
				score = Integer.toString(s.getRowsCleared());
				System.out.print("\r  ");
				System.out.print(score);
				for(int j=0;j<=prevLength-score.length();j++) System.out.print(' ');
				prevLength = score.length();
			}
			double testAvg = ((double)totalTestingScore/ROUNDS);
			double testSd = (double)Math.sqrt((totalTSquared - totalTestingScore*avg)/(ROUNDS-1));
			System.out.print("\rAverage testing score: ");
			System.out.print(testAvg);
			System.out.print(" s.d.: ");
			System.out.print(testSd);
			System.out.println();
			
			if(testAvg > bestAvg) {
				bestAvg = testAvg;
				System.out.println("Test average better than best ever training average, swapping weights...");
			
				System.arraycopy(defWeights, 0,weights, 0, BasisFunction.FEATURE_COUNT);
				doNewWeightActions(weights);
			}
			
		}
	}
	
	private static void doNewWeightActions(double[] weights) {
		for(int i=0;i<weights.length;i++) {
			if(i != 0) System.out.println(',');
			System.out.print('\t');
			System.out.print(weights[i]);
		}
		System.out.println();

	}
	
	private static char[] rotating = new char[] {'-','\\','|','/'};
	private static Writer out = null;
	private static void playGame(State s,PlayerSkeleton player,String prevScore) throws IOException {
		if(out==null) out = new PrintWriter(new File("scores.log"));
		int i = 0;
		int spin = 0;
		while(!s.hasLost()){
			s.makeMove(player.pickMove(s, s.legalMoves()));
			if(i == SPIN_STEP_DELAY) {
				System.out.print("\r");
				System.out.print(rotating[spin]);
				System.out.print(' ');
				System.out.print(prevScore);
				spin = (spin+1)%rotating.length;
				i=0;
			}
			i++;
		}
		out.write(Integer.toString(s.getRowsCleared()));
		out.write('\n');
		out.flush();
		s.draw();
		s.drawNext(0,0);
	}

	private static void drawBoard(State s, TFrame t) {
		t.bindState(s);
		s.draw();
		s.drawNext(0,0);
	}
}
