package Project2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: David Klecker, Alan Young
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class EigenVectoring {

    //This holds all of my groups/splits starting with the full group as the root. Everything is stored inside of an array
    //with a recursive split algorithm so my guess it can be iterated in a pre traversal method. 
    private ArrayList<GroupNodeCls> groupNodeList;

    public ArrayList<GroupNodeCls> getGroupNodeList() {
        return groupNodeList;
    }

    public void setGroupNodeList(ArrayList<GroupNodeCls> groupNodeList) {
        this.groupNodeList = groupNodeList;
    }
    
    //Constructor
    public EigenVectoring(){
        groupNodeList = new ArrayList<>();
    }
    
    /**
     * ProcessRequest is just a method which links my JSP to this java class. From here I run the main engine of our Network Modularity Algorithm. 
     * 
     * @param request
     * @throws IOException 
     */
    public void ProcessRequest(HttpServletRequest request) throws IOException{
              
        //Get the path to the adjacencyMatrix file and pass that path to ReadAdjacencyMatrix
        String realPath = request.getServletContext().getRealPath("/");
        realPath = realPath.concat("\\adjacencyMatrix.csv");
        
        //Returns an object GroupNodeCls which I add to my array and start spliting into groups. 
        GroupNodeCls groupNode = ReadAdjacencyMatrix(realPath);
        
        groupNodeList.add(groupNode);
                        
        boolean x = SplitIntoGroups(groupNode);
    }
    
    /**
     * Takes a path to the csv file storing our adjacency matrix and reads it to a double array. It then generates a RealMatrix and 
     * creates a new GroupNodeCls and returns this object. 
     * 
     * @param realPath
     * @return
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public GroupNodeCls ReadAdjacencyMatrix(String realPath) throws FileNotFoundException, IOException{
        
        BufferedReader br = new BufferedReader(new FileReader(realPath));
        String st;
        int count = 0;
        String Nodes[] = null;
        List<ArrayList<Double>> tempArray = new ArrayList<>();
        
        while ((st = br.readLine()) != null){
            if(count == 0){
                Nodes = st.substring(1).split(",");
            }
            else{
                String[] Values = st.split(",");
                List<Double> row = new ArrayList<>();
                
                for(int i=1; i<Values.length; i++){
                    row.add(Double.parseDouble(Values[i]));
                }
                tempArray.add((ArrayList<Double>) row);
            }
            count++;
        }

        double[][] d = new double[tempArray.size()][tempArray.size()];
        for(int i=0; i<tempArray.size(); i++){
            List<Double> x = tempArray.get(i);
            
            for(int j=0; j<x.size();j++){
                d[i][j]= x.get(j);
            }
        } 
        
        return new GroupNodeCls(Nodes, MatrixUtils.createRealMatrix(d));
    }
        
    /**
     * Takes a GroupNode and calculates a Z Modularity value to see if it could be split further. There is an additional 
     * check to see if there are two groups created from the EigenVector return. If so, then we split the GroupNode
     * into two groups and then check the Z modularity to see if we continue to split. 
     * 
     * @param groupNode
     * @return 
     */
    public boolean SplitIntoGroups(GroupNodeCls groupNode){
             
        //Initialize two groups. 
        ArrayList<String> Group1 = new ArrayList<>();
        ArrayList<String> Group2 = new ArrayList<>();

        //Return the EigenVector values for the highest positive eigenvalue. 
        double[] Vector = groupNode.ReturnEigenVector(groupNode.ReturnHighestEigenValueIndex());
        
        //Split into two groups were negative values in the eigenvector go to one group and positive 
        //values go into another group. 
        for(int i=0; i<Vector.length; i++){
            if(Vector[i] > 0)
                Group1.add(groupNode.getNodes()[i]);
            else
                Group2.add(groupNode.getNodes()[i]);
        }
                
        //Check if we have successfully split our group. If not we are done. 
        if(Group1.size()>0 && Group2.size()>0){
            
            //Create two new Matricies from our new groups. 
            RealMatrix Matrix1 = CreateNewGroup(Group1, groupNode);
            RealMatrix Matrix2 = CreateNewGroup(Group2, groupNode);

            //Calculate Z Modularity on the first group
            double Z1 = groupNode.CalculateModularityValue(Matrix1, Group1);
            
//          GroupNodeCls groupNode1 = new GroupNodeCls(Group1.toArray(new String[Group1.size()]), CreateNewMatrix(Group1, groupNode), true);

            //Create a new group node for our new group1 by passing it the new nodes and a new matrix that is the 
            //size of the node matrix. Grab the ranks which are just the eigenvectors for the nodes in our split. Set
            //the Z Value for this group so we have a record. 
            GroupNodeCls groupNode1 = new GroupNodeCls(Group1.toArray(new String[Group1.size()]), CreateNewMatrix(Group1, groupNode)); 
            groupNode1.setRanks(GetRanks(Group1, groupNode));
            groupNode1.setZValue(Z1);
            
            //Add this groupnode to our list. 
            groupNodeList.add(groupNode1);

            //If the Z1 value is greater than 0 then called SplitIntoGroups again with group1 as our new groupNode
            if(Z1 > 0){
                boolean split = SplitIntoGroups(groupNode1);
            }

            //Perform the same operations as above but now we are working on Group2. 
            double Z2 = groupNode.CalculateModularityValue(Matrix2, Group2);

//          GroupNodeCls groupNode1 = new GroupNodeCls(Group1.toArray(new String[Group1.size()]), CreateNewMatrix(Group1, groupNode), true);

            GroupNodeCls groupNode2 = new GroupNodeCls(Group2.toArray(new String[Group2.size()]), CreateNewMatrix(Group2, groupNode));
            groupNode2.setRanks(GetRanks(Group2, groupNode));
            groupNode2.setZValue(Z2);
            groupNodeList.add(groupNode2);

            if(Z2 > 0){
                boolean split = SplitIntoGroups(groupNode2);
            }
        }
        
        //return true when we are done spliting this iteration. 
        return true;
    }
    
    /**
     * Returns the eigenvector values into a double[] array. We print these values to our JSP
     * 
     * @param Group
     * @param GroupNode
     * @return 
     */
    public double[] GetRanks(ArrayList<String> Group, GroupNodeCls GroupNode){
        int[] a = GroupNode.CreateBitMatrix(Group);
        double v[] = GroupNode.ReturnEigenVector(GroupNode.ReturnHighestEigenValueIndex());
        
        int sumLength = 0;
        for(int i=0;i<a.length; i++){
            if(a[i] == 1)
                sumLength++;
        }
        double d[] = new double[sumLength];
        for(int i=0, j=0;i<a.length; i++){
            if(a[i] == 1){
                d[j++] = v[i];
            }
        }
        
        return d;
    }
    
    /**
     * This creates for us a new Adjacency matrix from our new Group Array in contrast with the previous group. 
     * We are interested in a submatrix where we have only the node and edges that are a part of the new 
     * group. 
     * 
     * @param Group
     * @param GroupNode
     * @return 
     */
    public RealMatrix CreateNewMatrix(ArrayList<String> Group, GroupNodeCls GroupNode){
        
        int[] a = GroupNode.CreateBitMatrix(Group);
        RealMatrix newM = MatrixUtils.createRealMatrix(Group.size(), Group.size());
        
        int counti = 0;
        
        //Change this to BMatrix to get the BMatrix values
        
        for(int i=0; i<GroupNode.getAdjMatrix().getColumnDimension(); i++){
            
            if(a[i] == 1){
                RealMatrix colM = GroupNode.getAdjMatrix().getColumnMatrix(i);
                double x1[] = new double[Group.size()];
                for(int j = 0, k = 0; j<colM.getRowDimension(); j++){
                    if(a[j] == 1)
                        x1[k++] = colM.getEntry(j, 0);
                }
                newM.setColumn(counti++, x1);
            }
        }
        
        return newM;
    }
    
    /**
     * This creates a new submatrix group which contains only the rows we need to calculate the Z modularity value. 
     * This submatrix is not symmetrical. It contains only the rows which match up the correct eigenvector binary values
     * that match for the group. For example if my EigenVector Values return the binary vector (1, 0, 0 1, 1, 0) then I create
     * a 3x6f matrix containing rows 1, 4, and 5. 
     * 
     * @param NewNodes
     * @param groupNode
     * @return 
     */
    public RealMatrix CreateNewGroup(ArrayList<String> NewNodes, GroupNodeCls groupNode){
       
        RealMatrix newMatrix = MatrixUtils.createRealMatrix(NewNodes.size(), groupNode.getNodes().length);
        
        for(int i=0; i<NewNodes.size(); i++){
            
            for(int j=0; j<groupNode.getNodes().length; j++){
                if(NewNodes.get(i).compareTo(groupNode.getNodes()[j]) == 0){
                    
                    newMatrix.setRowMatrix(i, groupNode.getB_Matrix().getRowMatrix(j));
                    break;
                }
            }
        }        

        return newMatrix;       

    }

}
