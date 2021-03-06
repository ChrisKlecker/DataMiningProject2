package Project2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class GroupNodeCls{

    private String Nodes[];
    private RealMatrix AdjMatrix;
    private RealMatrix P_Matrix;
    private RealMatrix B_Matrix;
    private EigenDecomposition eig;
    private RealMatrix EigenVectors;
    private double EigenValues[];
    private double MValue;
    private double ZValue;
    private double[] Ranks;

    public double[] getRanks() {
        return Ranks;
    }

    public void setRanks(double[] Ranks) {
        this.Ranks = Ranks;
    }

    public double getZValue() {
        return ZValue;
    }

    public void setZValue(double ZValue) {
        this.ZValue = ZValue;
    }

    public String[] getNodes() {
        return Nodes;
    }

    public void setNodes(String Nodes[]) {
        this.Nodes = Nodes;
    }

    public RealMatrix getAdjMatrix() {
        return AdjMatrix;
    }

    public void setAdjMatrix(RealMatrix AdjMatrix) {
        this.AdjMatrix = AdjMatrix;
    }

    public RealMatrix getP_Matrix() {
        return P_Matrix;
    }

    public void setP_Matrix(RealMatrix P_Matrix) {
        this.P_Matrix = P_Matrix;
    }

    public RealMatrix getB_Matrix() {
        return B_Matrix;
    }

    public void setB_Matrix(RealMatrix B_Matrix) {
        this.B_Matrix = B_Matrix;
    }

    public EigenDecomposition getEig() {
        return eig;
    }

    public void setEig(EigenDecomposition eig) {
        this.eig = eig;
    }

    public RealMatrix getEigenVectors() {
        return EigenVectors;
    }

    public void setEigenVectors(RealMatrix EigenVectors) {
        this.EigenVectors = EigenVectors;
    }

    public double[] getEigenValues() {
        return EigenValues;
    }

    public void setEigenValues(double[] EigenValues) {
        this.EigenValues = EigenValues;
    }

    public double getMValue() {
        return MValue;
    }

    public void setMValue(double MValue) {
        this.MValue = MValue;
    }

    /**
     * Constructor function for creating a new GroupNode with an adjacency matrix
     * 
     * @param Node
     * @param AdjMatrixSubSet 
     */
    public GroupNodeCls(String[] Node, RealMatrix AdjMatrixSubSet){
        this.Nodes = Node;
        this.AdjMatrix = AdjMatrixSubSet;
        
        CreateBMatrix();
        CreateEigenInformation();
    }

    /**
     * Constructor function for creating a new GroupNode with a BMatrix. 
     * 
     * @param Node
     * @param BMatrix
     * @param BMatrixFlag 
     */
    public GroupNodeCls(String[] Node, RealMatrix BMatrix, boolean BMatrixFlag){
        this.Nodes = Node;
        this.B_Matrix = BMatrix;
        
        CreateEigenInformation();
    }
    
    /**
     * Calculates the B Matrix from the Adjacency Matrix. It does by first getting a summation of the adjacency values then adding these
     * values together to get an M value. We then multiple the summation column matrix and row matrix to get a full matrix called p. We
     * then calculate our b matrix using our m value and subtracting it from the Adjacency matrix value to finally arrive at our Bmatrix. 
     * 
     */
    public void CreateBMatrix(){
        
        ArrayList<Double> SumArrayX = new ArrayList<>();
        ArrayList<Double> SumArrayY = new ArrayList<>();
                       
        P_Matrix = MatrixUtils.createRealMatrix(AdjMatrix.getColumnDimension(), AdjMatrix.getRowDimension());
        B_Matrix = MatrixUtils.createRealMatrix(AdjMatrix.getColumnDimension(), AdjMatrix.getRowDimension());
        
        //Calcuate summation of adjacency values for the column
        for(int i=0; i<AdjMatrix.getColumnDimension(); i++){

            Double b = 0.0;
            for(int j=0; j<AdjMatrix.getRowDimension(); j++){
                
                b += AdjMatrix.getEntry(i, j);
            }
            SumArrayX.add(b);
        }
        
        //Calcuate summation of adjacency values for the row
        for(int i=0; i<AdjMatrix.getRowDimension(); i++){

            Double b = 0.0;
            for(int j=0; j<AdjMatrix.getColumnDimension(); j++){
                
                b += AdjMatrix.getEntry(i, j);
            }
            SumArrayY.add(b);
        }
        
        //calculate our m value
        MValue = 0.0;
        for(int i=0; i<SumArrayX.size(); i++){
            MValue += SumArrayX.get(i);
        }
        
        //Create our P matrix. 
        for(int i=0; i<SumArrayX.size(); i++){
            
            for(int j=0; j<SumArrayY.size(); j++){
                P_Matrix.setEntry(i, j, SumArrayX.get(i)*SumArrayY.get(j));
            }
        }

        //Calculate our B Matrix
        for(int i=0; i<SumArrayX.size(); i++){
            
            for(int j=0; j<SumArrayY.size(); j++){
                double x = P_Matrix.getEntry(i, j);
                double y = 1/MValue;
                double z = AdjMatrix.getEntry(i, j) - (y*x);
                z = Double.valueOf(String.format("%.5f", z));
                B_Matrix.setEntry(i, j, z);
            }
        }
    } 
    
    /** 
     * This creates our Eigen Class object which contains all the Eigen value and vector values we will be using
     * 
     * @return 
     */
    public EigenDecomposition CreateEigenInformation(){
               
        eig = new EigenDecomposition(B_Matrix);
        EigenVectors = eig.getV();
        EigenValues = eig.getRealEigenvalues();
        
        for (int i=0; i<EigenValues.length; i++){
            EigenValues[i] = Double.parseDouble(String.format("%.5f ", EigenValues[i]));
        }
        
        return eig;
    }
    
    /**
     * Returns the index of the highest positive found eigenvalue
     * 
     * @return 
     */
    public int ReturnHighestEigenValueIndex(){
        double max = Double.MIN_EXPONENT;
        int indexReturn = -1;
        
        for (int i=0; i<EigenValues.length; i++){

            if(EigenValues[i] > max){
                max = EigenValues[i];
                indexReturn = i;
            }
        }
        
        return indexReturn;  
    }
    
    /**
     * Returns an double[] of the EigenVector found at index
     * 
     * @param Index
     * @return 
     */
    public double[] ReturnEigenVector(int Index){
        List<Double> a = new ArrayList<>();
        
        for(int i=0; i<EigenVectors.getRowDimension(); i++){
            a.add(EigenVectors.getEntry(i, Index));
        }

        double[] target = new double[a.size()];
        for (int i = 0; i < target.length; i++) {

            target[i] = a.get(i);                // java 1.5+ style (outboxing)
            
        }       
        return target;
    }
    
    /**
     * This generates for me a bitmatrix. Instead of having to compare node values I create a bitmatrix based on the old
     * nodes and the newly split group nodes. For example, say I have for a split group node ABCDE and my split node group 
     * contains BDE. Therefore if I send my new nodes BDE to this function it will return 01011. 
     * 
     * @param NewNodes
     * @return 
     */
    public int[] CreateBitMatrix(ArrayList<String> NewNodes){
        
        int[] ret = new int[Nodes.length];

        for(int i=0; i<this.Nodes.length; i++)
            ret[i] = (NewNodes.contains(Nodes[i]) == true) ? 1 : 0;
        
        return ret;
    }

    /**
     * This calculates the z modularity value for the split group
     * 
     * @param Matrix1
     * @param NewNodes
     * @return 
     */
    public double CalculateModularityValue(RealMatrix Matrix1, ArrayList<String> NewNodes){
        
        double x = (1 / ((0.5 * MValue) * 4)); //(1/4m) where m = 1/2M from step 1
        int S[] = CreateBitMatrix(NewNodes);
        
        ArrayList<Double> a = new ArrayList<>();
        
        for(int j=0; j<Matrix1.getColumnDimension(); j++){
    
            double Sum = 0.0;
            for(int i=0; i<Matrix1.getRowDimension(); i++){
                
                Sum += Matrix1.getEntry(i, j);
            }
            a.add(Sum);
        }        
        
        double S_Sum = 0.0;
        
        for(int i=0; i<S.length; i++){
        
            if(S[i] == 1)
                S_Sum += a.get(i);
        }
        
        double Z = x * S_Sum;
        
        return Z;
    }
};
