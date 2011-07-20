
public class Markov {
	private static double[][][] sum = new double[State.N_PIECES][State.N_PIECES][State.N_PIECES];
	private static double[][] mul = new double[1][State.N_PIECES];
	static {
		for(int i=0;i<State.N_PIECES;i++) mul[0][i] = 1;
	}
	public static void printField(double[][] field){
		for(int i=0;i<field.length;i++) {
			for(int j=0;j<field[0].length;j++){
				System.out.print(" "+field[i][j]);
			}
			System.out.println();
		}
	}
	public static void main(String[] args) {
		int[] sequence = new int[] {1,3,4,2,6,0,5};
		int p = 0;
		
		int[] prevStates = new int[3];
		for(int i=0;i<prevStates.length;i++) prevStates[i]=(int)(Math.random()*State.N_PIECES);
		for(int i=prevStates.length;i<10000-prevStates.length;i++){
			p = (p+1)%sequence.length;
			prevStates[i%3] = sequence[p];
			sum[prevStates[(i-2)%3]][prevStates[(i-1)%3]][prevStates[i%3]]++;
		}
		for(int i=0;i<State.N_PIECES;i++){
			for(int j=0;j<State.N_PIECES;j++){
				for(int k=0;k<State.N_PIECES;k++){
					System.out.println("["+i+","+j+"] \t "+sum[i][j][k]);
				}
			}
		}
	}
}
