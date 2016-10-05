/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import edu.cornell.mannlib.vitro.webapp.controller.Controllers;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JSPPageHandler {
    public static void renderBasicPage(HttpServletRequest req, HttpServletResponse res, String bodyJsp) throws ServletException, IOException {
        req.setAttribute("bodyJsp", bodyJsp);
        RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
        rd.forward(req, res);
    }

    public static void renderPlainInclude(HttpServletRequest req, HttpServletResponse res, String pageJsp) throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher(pageJsp);
        rd.include(req, res);
    }

    public static void renderPlainPage(HttpServletRequest req, HttpServletResponse res, String pageJsp) throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher(pageJsp);
        rd.forward(req, res);
    }
}
