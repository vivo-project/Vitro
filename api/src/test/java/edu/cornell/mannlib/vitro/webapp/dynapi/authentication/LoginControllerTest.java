package edu.cornell.mannlib.vitro.webapp.dynapi.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BasicAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginControllerTest {

    private LoginController loginController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Before
    public void beforeEach() {
        loginController = new LoginController();
    }

    @Test
    public void testBadRequestWithNoParameters() {
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);

        loginController.doPost(request, response);

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");

        verify(response, times(1)).setStatus(400);
    }

    @Test
    public void testNonExistentUsername() {
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");

        try (MockedConstruction<BasicAuthenticator> mocked =
                mockConstruction(BasicAuthenticator.class, (mock, context) -> {
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(null);
                })) {
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");

        verify(response, times(1)).setStatus(401);
    }

    @Test
    public void testUserNotPermittedToLogin() {
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");

        try (MockedConstruction<BasicAuthenticator> mocked =
                mockConstruction(BasicAuthenticator.class, (mock, context) -> {
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(new UserAccount());
                    when(mock.isUserPermittedToLogin(any(UserAccount.class))).thenReturn(false);
                })) {
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");

        verify(response, times(1)).setStatus(401);
    }

    @Test
    public void testWithWrongPassword() {
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");

        try (MockedConstruction<BasicAuthenticator> mocked =
                mockConstruction(BasicAuthenticator.class, (mock, context) -> {
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(new UserAccount());
                    when(mock.isUserPermittedToLogin(any(UserAccount.class))).thenReturn(true);
                    when(mock.isCurrentPasswordArgon2(any(UserAccount.class), eq("testPassword"))).thenReturn(false);
                })) {
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");

        verify(response, times(1)).setStatus(401);
    }

    @Test
    public void testCorrectCredentials() {
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");
        UserAccount testUser = new UserAccount();
        testUser.setEmailAddress("test@mail.com");
        when(request.getSession()).thenReturn(session);

        try (MockedConstruction<BasicAuthenticator> mocked =
                mockConstruction(BasicAuthenticator.class, (mock, context) -> {
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(testUser);
                    when(mock.isUserPermittedToLogin(any(UserAccount.class))).thenReturn(true);
                    when(mock.isCurrentPasswordArgon2(any(UserAccount.class), eq("testPassword"))).thenReturn(true);
                })) {
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");
        verify(request, times(1)).getSession();

        verify(session, times(1)).setAttribute("user", testUser);

        verify(response, times(1)).setStatus(200);
    }
}
