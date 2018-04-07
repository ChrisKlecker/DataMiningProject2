<%-- 
    Document   : EigenVectors
    Created on : Apr 2, 2018, 1:51:40 PM
    Author     : David Klecker
--%>

<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import = "Project2.EigenVectoring" %>
<%@ page import = "Project2.GroupNodeCls" %>
<jsp:useBean id = "EigenVectoring" class = "Project2.EigenVectoring" scope = "session" ></jsp:useBean>
<jsp:setProperty name = "EigenVectoring" property = "*" /> 

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%
            EigenVectoring.ProcessRequest(request);
        %>
        <%
            for (int i = 0; i < EigenVectoring.getGroupNodeList().size(); i++) {
                GroupNodeCls p = EigenVectoring.getGroupNodeList().get(i);
                if (i == 0) {
        %>
                    <table><tr>
                    <% for (int j = 0; j < p.getNodes().length; j++) {%>
                    <td><%=p.getNodes()[j]%></td>
                    <%}%>
                    </tr></table>
            <%
                } else {
            %>    
                <h2>Split (Z Value for this group = <%= p.getZValue()%>)</h2>
                <table>
                    <% for (int j = 0; j < p.getRanks().length; j++) {%>
                    <tr><td><%=p.getNodes()[j]%></td><td>With Rank</td><td><%= p.getRanks()[j]%></td></tr>
                    <%}%>
                </table>
            <%
                }
            %>

        <%--
        <h2> P Matrix </h2>
        <%=p.getMValue()%>
        <table>
            <%
                for (int j = 0; j < p.getP_Matrix().getColumnDimension(); j++) {
            %>
            <tr>
                <%
                    for (int k = 0; k < p.getP_Matrix().getRowDimension(); k++) {
                %>
                <td><%=p.getP_Matrix().getEntry(j, k)%></td>
                <%
                    }
                %>
            </tr>
            <%  }%>
        </table>

        <h2> B Matrix </h2>
        <table>
            <%
                for (int j = 0; j < p.getB_Matrix().getColumnDimension(); j++) {
            %>
            <tr>
                <%
                    for (int k = 0; k < p.getB_Matrix().getRowDimension(); k++) {
                %>
                <td><%=p.getB_Matrix().getEntry(j, k)%></td>
                <%
                    }
                %>
            </tr>
            <%  }%>
        </table>

        <h2> EigenValues </h2>
        <table>
            <tr>
                <%
                    for (int j = 0; j < p.getEigenValues().length; j++) {
                %>
                <td><%=p.getEigenValues()[j]%></td>
                <%
                    }
                %>
            </tr>
        </table>

        <h2> Eigen Vectors </h2>
        <table>
            <%
                for (int j = 0; j < p.getEigenVectors().getColumnDimension(); j++) {
            %>
            <tr>
                <%
                    for (int k = 0; k < p.getEigenVectors().getRowDimension(); k++) {
                %>
                <td><%=p.getEigenVectors().getEntry(j, k)%></td>
                <%
                    }
                %>
            </tr>
            <%  }%>
        </table>
        --%>

        <%}%>
    </body>
</html>
