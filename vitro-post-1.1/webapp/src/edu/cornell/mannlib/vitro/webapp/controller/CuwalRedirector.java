/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CuwalRedirector extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {        
        //forward to /edit/login.jsp to work around CUWebAuth bug
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/edit/login.jsp");
        rd.forward(req, resp);
        return;        
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        //forward to /edit/login.jsp to work around CUWebAuth bug
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/edit/login.jsp");
        rd.forward(req, resp);
        return;
    }

    
}
