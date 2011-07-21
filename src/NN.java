
public class NN {
	final private static float ALPHA = 1;
	
	private float neurons[][];
	private float deltas[][];
	private float weights[][][];
	private float out[];
	/**
	 * i represents the layer index, j represents the neuron within that layer.
	 * weights[i][j] represents the weights of the previous layer going _into_ neuron i,j.
	 * @param nodesPerLayer
	 */
	public NN(int...nodesPerLayer){
		this.neurons = new float[nodesPerLayer.length][];
		this.deltas =  new float[nodesPerLayer.length][];
		this.weights = new float[nodesPerLayer.length][][];
		
		for(int i=0;i<nodesPerLayer.length;i++)	{
			neurons[i] = new float[nodesPerLayer[i]];
			deltas[i]  = new float[nodesPerLayer[i]];
			weights[i] = new float[nodesPerLayer[i]][];
			
			if(i>0) for(int j=0;j<neurons[i].length;j++) {
				weights[i][j] = new float[neurons[i-1].length];
				for(int k=0;k<weights[i][j].length;k++)
					weights[i][j][k] = 0;
			}
		}
		this.out = neurons[neurons.length-1];
	}
	
	private float calculateNeuron(int i, int j){
		return logistic(dot(neurons[i-1],weights[i][j]));
	}
	
	private void forwardprop(){
		for(int i=1;i<neurons.length;i++)
			for(int j=0;j<neurons[i].length;j++)
				neurons[i][j] = calculateNeuron(i, j);
	}
	
	public float[] classify(float...input){
		this.neurons[0] = input;
		forwardprop();
		return this.out;
	}
	/**
	 * Uses neurons[][] to hold delta values.
	 * @param ans
	 */
	private void backprop(float[] ans) {
		/**
		 * Find delta for output
		 */
		for(int i=0;i<ans.length;i++){
		/**
		 *  |-- output layer delta --|	 |-derivative-| |----diff-----|
		 */
			deltas[deltas.length-1][i] = (out[i]*(1-out[i]))*(ans[i]-out[i]);
			System.out.println("Output delta: "+deltas[deltas.length-1][i]);
		}
		/**
		 * Assigning deltas
		 */
		for(int l=deltas.length-2;l>0;l--){
			for(int n=0;n<deltas[l].length;n++){
				float s = 0;
				for(int i=0;i<deltas[l+1].length;i++)
					s += deltas[l+1][i]*weights[l+1][i][n];
				deltas[l][n] = neurons[l][n]*(1-neurons[l][n])*s;
			}
		}
		/**
		 * Adjusting weights
		 */
		for(int l=1;l<neurons.length;l++){
			System.out.println("Layer "+l);
			for(int n=0;n<neurons[l].length;n++){
				System.out.println("\tNeuron "+n);
				for(int i=0;i<weights[l][n].length;i++){
					weights[l][n][i] += ALPHA * neurons[l-1][i] * deltas[l][n];
					System.out.println("\t\tFrom neuron "+i+": "+weights[l][n][i]);
				}
			}
		}
	}
	public void learn(float[] d,float...ans){
		this.neurons[0] = d;
		forwardprop();
		backprop(ans);
		for(int l=neurons.length-1;l>0;l--){
			for(int n=0;n<neurons[l].length;n++){
				neurons[l][n] = 0;
				deltas[l][n]  = 0;
			}
		}
	}

	
	public static float logistic(float z) {
		return 1/(1+(float)Math.pow((double)Math.E,(double)-1*z));
	}
	
	public static float logderivative(float z) {
		return logistic(z)*(1-logistic(z));
	}
	public static float gprime(float gval){
		return gval*(1-gval);
	}
	public static float dot(float[] w, float[] x) {
		if (w.length != x.length)
			throw new RuntimeException("Cannot dot. Different length");
		float r=0;
		for(int i=0;i<w.length;i++)	r += w[i]*x[i];
		return r;
	}
	public static void minus(float[] w, float[] x){
		if (w.length != x.length)
			throw new RuntimeException("Cannot dot. Different length");
		for(int i=0;i<w.length;i++)	w[i]=  w[i]-x[i];
	}
	public static void main(String[] args) {
		NN n = new NN(3,3,3,1);
		
		n.learn(new float[] {0,0,0}, 0);
		n.learn(new float[] {0,1,1}, 0);
		n.learn(new float[] {0,1,1}, 0);
		n.learn(new float[] {0,1,1}, 0);
		n.learn(new float[] {1,1,0}, 0);
		n.learn(new float[] {1,0,1}, 0);
		n.learn(new float[] {1,1,1}, 0);
		n.learn(new float[] {1,1,1}, 0);
		n.learn(new float[] {1,1,1}, 0);
		n.learn(new float[] {1,0,0}, 1);
		n.learn(new float[] {0,1,0}, 1);
		n.learn(new float[] {0,0,1}, 1);
		n.learn(new float[] {1,0,0}, 1);
		n.learn(new float[] {0,1,0}, 1);
		n.learn(new float[] {0,0,1}, 1);
		n.learn(new float[] {1,0,0}, 1);
		n.learn(new float[] {0,1,0}, 1);
		n.learn(new float[] {0,0,1}, 1);
		
		System.out.println(n.classify(1,0,0)[0]);
		System.out.println(n.classify(0,1,0)[0]);
		System.out.println(n.classify(1,1,1)[0]);
	}
	
	
}
