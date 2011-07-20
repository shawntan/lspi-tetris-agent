import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class GraphPlayer {
	//private static double GAMES_PER_SAMPLE=30;
	public static void main(String[] args) throws FileNotFoundException {
		
		PrintWriter out = new PrintWriter(new File("scores.log"));
		PlayerSkeleton playerTrained = new PlayerSkeleton();
		//playerTrained.learns = true;
		//PlayerSkeleton playerNoob = new PlayerSkeleton();
		//int gamesPlayed=0;
		while(true){
			//int totalTrained = 0;
			//int totalNoob = 0;
				State s1 = new State();
				//State s2 = new State();
				while(!s1.hasLost()) {
					s1.makeMove(playerTrained.pickMove(s1,s1.legalMoves()));
					//if(!s2.hasLost()) s2.makeMove(playerNoob.pickMove(s2,s2.legalMoves()));
					//System.out.println(s1.nextPiece==s2.nextPiece);
				}
				//System.out.println(s1.getRowsCleared()+"\t"+s2.getRowsCleared());
				//totalTrained += s1.getRowsCleared();
				//totalNoob += s2.getRowsCleared();
				out.println(s1.getRowsCleared());
				System.out.println(s1.getRowsCleared());
				out.flush();
			//playerTrained.bs.computeWeights();
			//gamesPlayed+=GAMES_PER_SAMPLE;
		}
		
	}
}
