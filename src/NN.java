import java.util.Arrays;


public class NN {
	private static double ALPHA = 0.1;


	private int layers[][];
	private double neurons[];
	private double deltas[];
	private double weights[][];
	private boolean synapse[][];

	/**
	 * [code]i[/code] represents the layer index, j represents the neuron within that layer.
	 * weights[i][j] represents the weights of the previous layer going _into_ neuron i,j.
	 * @param nodesPerLayer
	 */
	public NN(int...nodesPerLayer){
		int neuronCount = 1;
		this.layers = new int[nodesPerLayer.length][];
		for(int l=0;l<nodesPerLayer.length;l++) {
			layers[l] = new int[nodesPerLayer[l]];
			for(int i=0;i<layers[l].length;i++) layers[l][i] = neuronCount++;
		}
		this.neurons = new double[neuronCount];
		this.deltas  = new double[neuronCount];
		this.weights = new double[neuronCount][neuronCount];
		this.synapse = new boolean[neuronCount][neuronCount];
		for(int l=1;l<layers.length;l++){
			for(int n=0;n<layers[l].length;n++)	{
				synapse[0][layers[l][n]] = true;
				weights[0][layers[l][n]] = Math.random()*2-1;
				for(int i=0;i<layers[l-1].length;i++) {
					synapse[layers[l-1][i]][layers[l][n]] = true;
					weights[layers[l-1][i]][layers[l][n]] = Math.random()*2-1;
				}
			}
		}

		//this is the bias neuron.
		this.neurons[0] = 1;
	}

	private double calculateNeuron(int j){
		double s=0;
		for(int i=0;i<neurons.length;i++){
			s += (synapse[i][j])?neurons[i]*weights[i][j]:0;
		}
		return sigmoid(s);
	}

	private void forwardprop(){
		for(int i=layers[1][0];i<neurons.length;i++)
			neurons[i] = calculateNeuron(i);
	}

	public double[] classify(double...input){
		System.arraycopy(input, 0, neurons, 1, input.length);
		forwardprop();
		double res[];
		System.arraycopy(
				neurons,
				layers[layers.length-1][0],
				res = new double[layers[layers.length-1].length],
				0,
				layers[layers.length-1].length
		);
		return res;
	}
	/**
	 * Uses neurons[][] to hold delta values.
	 * @param ans
	 */
	private void backprop(double[] ans) {
		/**
		 * Find delta for output
		 */
		for(int i=0;i<layers[layers.length-1].length;i++){
			int j = layers[layers.length-1][i];
			deltas[j] = differential(neurons[j])*(ans[i]-neurons[j]);
		}
		/**
		 * Assigning deltas
		 */
		for(int l=layers.length-2;l>=1;l--) {
			for(int n=0;n<layers[l].length;n++) {
				int i = layers[l][n];
				double s = 0;
				for(int k=0;k<layers[l+1].length;k++){
					int j = layers[l+1][k];
					s += weights[i][j]*deltas[j];
				}
					
				deltas[i] = differential(neurons[i])*s;
			}
		}
		/**
		 * Adjusting weights
		 */
		for(int i=0;i<neurons.length;i++)
			for(int j=layers[1][0];j<neurons.length;j++)
				weights[i][j] = (synapse[i][j])?(weights[i][j] + ALPHA*neurons[i]*deltas[j]):0;
	}
	public void learn(double[] d,double...ans){
		//Matrix.printField(weights);
		System.arraycopy(d, 0, neurons, 1, d.length);
		forwardprop();
		//System.out.println(Arrays.toString(neurons));
		backprop(ans);
		//System.out.println(Arrays.toString(deltas));
		for(int i=1;i<neurons.length;i++){
			neurons[i] = 0;
			deltas[i]  = 0;
		}
		
	}


	public static double sigmoid(double z) {
		return 1/(1 + Math.exp(-z));
	}

	public static double differential(double g){
		return g * (1 - g);
	}

	public static void main(String[] args) {
		NN n = new NN(2,2,1);
		double[] i0 = new double[] {0,0};
		double[] i1 = new double[] {0,1};
		double[] i2 = new double[] {1,0};
		double[] i3 = new double[] {1,1};
		
		for(int i=0;i<10000;i++){
			n.learn(i0, 0);
			n.learn(i1, 1);
			n.learn(i2, 1);
			n.learn(i3, 0);
			//n.learn(l, 1);
		}
		Matrix.printField(n.weights);
		System.out.println(n.classify(1,1)[0]);
		System.out.println(n.classify(0,0)[0]);
		System.out.println(n.classify(1,0)[0]);
		System.out.println(n.classify(0,1)[0]);
	}


}
