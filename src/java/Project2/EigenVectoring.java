package Project2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author David Klecker
 */

//Create an arraylist of matricies. 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class EigenVectoring {
        
    private ArrayList<GroupNodeCls> groupNodeList;
    private GroupNodeCls groupNodeLocal;

    public ArrayList<GroupNodeCls> getGroupNodeList() {
        return groupNodeList;
    }

    public void setGroupNodeList(ArrayList<GroupNodeCls> groupNodeList) {
        this.groupNodeList = groupNodeList;
    }

    public GroupNodeCls getGroupNodeLocal() {
        return groupNodeLocal;
    }

    public void setGroupNodeLocal(GroupNodeCls groupNodeLocal) {
        this.groupNodeLocal = groupNodeLocal;
    }
    
    public EigenVectoring(){
        groupNodeList = new ArrayList<>();
    }
    
    public void ProcessRequest(HttpServletRequest request) throws IOException{
              
        String realPath = request.getServletContext().getRealPath("/");
        realPath = realPath.concat("\\adjacencyMatrix.csv");
        
        GroupNodeCls groupNode = ReadAdjacencyMatrix(realPath);
        
        groupNodeList.add(groupNode);

        setGroupNodeLocal(groupNode);
                        
        boolean x = SplitIntoGroups(groupNode);
        int g = 0;
    }
    
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
        
    public boolean SplitIntoGroups(GroupNodeCls groupNode){
             
        ArrayList<String> Group1 = new ArrayList<>();
        ArrayList<String> Group2 = new ArrayList<>();

        double[] Vector = groupNode.ReturnEigenVector(groupNode.ReturnHighestEigenValueIndex());
        for(int i=0; i<Vector.length; i++){
            if(Vector[i] > 0)
                Group1.add(groupNode.getNodes()[i]);
            else
                Group2.add(groupNode.getNodes()[i]);
        }
                
        if(Group1.size()>0 && Group2.size()>0){
            
            RealMatrix Matrix1 = CreateNewGroup(Group1, groupNode);
            RealMatrix Matrix2 = CreateNewGroup(Group2, groupNode);

            double Z1 = groupNode.CalculateModularityValue(Matrix1, Group1);
            
//          GroupNodeCls groupNode1 = new GroupNodeCls(Group1.toArray(new String[Group1.size()]), CreateNewMatrix(Group1, groupNode), true);

            GroupNodeCls groupNode1 = new GroupNodeCls(Group1.toArray(new String[Group1.size()]), CreateNewMatrix(Group1, groupNode)); 
            groupNode1.setRanks(GetRanks(Group1, groupNode));
            groupNode1.setZValue(Z1);
            groupNodeList.add(groupNode1);

            if(Z1 > 0){
                boolean split = SplitIntoGroups(groupNode1);
            }

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
        
        return true;
    }
    
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
