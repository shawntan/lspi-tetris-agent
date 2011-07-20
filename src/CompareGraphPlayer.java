import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class CompareGraphPlayer {
	private static double GAMES_PER_SAMPLE=5;
	public static void main(String[] args) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new File("scores.log"));
		PlayerSkeleton playerTrained = new PlayerSkeleton();
		playerTrained.learns = true;
		PlayerSkeleton playerNoob = new PlayerSkeleton();
		int gamesPlayed=0;
		while(true){
			int totalTrained = 0;
			int totalNoob = 0;
			for(int i=0;i<GAMES_PER_SAMPLE;i++){
				State s1 = new State();
				State s2 = new State();
				while(!s1.hasLost()||!s2.hasLost()) {
					s1.nextPiece = s2.nextPiece = (int)(Math.random()*State.N_PIECES);
					s1.makeMove(playerTrained.pickMove(s1,s1.legalMoves()));
					if(!s2.hasLost()) s2.makeMove(playerNoob.pickMove(s2,s2.legalMoves()));
					assert(s1.nextPiece==s2.nextPiece);
				}
				System.out.println(s1.getRowsCleared()+"\t"+s2.getRowsCleared());
				totalTrained += s1.getRowsCleared();
				totalNoob += s2.getRowsCleared();
			}
			playerTrained.bs.computeWeights();
			gamesPlayed+=GAMES_PER_SAMPLE;
			out.println(gamesPlayed+"\t"+totalTrained/GAMES_PER_SAMPLE+"\t"+totalNoob/GAMES_PER_SAMPLE);
			out.flush();
		}

	}
}
