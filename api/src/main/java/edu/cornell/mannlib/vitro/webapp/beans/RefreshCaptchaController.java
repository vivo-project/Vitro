/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.twelvemonkeys.servlet.HttpServlet;

@WebServlet(name = "refreshCaptcha", urlPatterns = {"/refreshCaptcha"}, loadOnStartup = 5)
public class RefreshCaptchaController extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String oldChallengeId = request.getParameter("oldChallengeId");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        CaptchaBundle newChallenge = CaptchaServiceBean.generateRefreshedChallenge();
        CaptchaServiceBean.getCaptchaChallenges().invalidate(oldChallengeId);
        CaptchaServiceBean.getCaptchaChallenges().put(newChallenge.getCaptchaId(), newChallenge);

        out.println("{\"challenge\": \"" + newChallenge.getB64Image() + "\", \"challengeId\": \"" +
            newChallenge.getCaptchaId() + "\"}");
    }

}
