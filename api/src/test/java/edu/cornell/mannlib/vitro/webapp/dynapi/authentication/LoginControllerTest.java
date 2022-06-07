package edu.cornell.mannlib.vitro.webapp.dynapi.authentication;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BasicAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import stubs.javax.servlet.http.HttpServletResponseStub;
import stubs.javax.servlet.http.HttpSessionStub;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class LoginControllerTest {

    private LoginController loginController;

    @Mock
    private HttpServletRequest request;

    private HttpServletResponse response;

    @Before
    public void beforeEach() {
        loginController = new LoginController();
        response = new HttpServletResponseStub();
    }

    @Test
    public void testBadRequestWithNoParameters(){
        when(request.getParameter("username")).thenReturn(null);
        when(request.getParameter("password")).thenReturn(null);

        loginController.doPost(request, response);

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void testBadRequestNonExistentUsername(){
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");

        try (MockedConstruction<BasicAuthenticator> mocked = mockConstruction(BasicAuthenticator.class,
                (mock, context)-> {
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(null);
                })){
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void testUserNotPermittedToLogin(){
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");

        try (MockedConstruction<BasicAuthenticator> mocked = mockConstruction(BasicAuthenticator.class,
                (mock, context)->{
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(new UserAccount());
                    when(mock.isUserPermittedToLogin(any(UserAccount.class))).thenReturn(false);
                })){
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void testWithWrongPassword(){
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");

        try (MockedConstruction<BasicAuthenticator> mocked = mockConstruction(BasicAuthenticator.class,
                (mock, context)->{
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(new UserAccount());
                    when(mock.isUserPermittedToLogin(any(UserAccount.class))).thenReturn(true);
                    when(mock.isCurrentPasswordArgon2(any(UserAccount.class),eq("testPassword"))).thenReturn(false);
                })){
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void testCorrectCredentials(){
        when(request.getParameter("username")).thenReturn("testUsername");
        when(request.getParameter("password")).thenReturn("testPassword");
        when(request.getSession()).thenReturn(new HttpSessionStub());

        try (MockedConstruction<BasicAuthenticator> mocked = mockConstruction(BasicAuthenticator.class,
                (mock, context)->{
                    when(mock.getAccountForInternalAuth("testUsername")).thenReturn(new UserAccount());
                    when(mock.isUserPermittedToLogin(any(UserAccount.class))).thenReturn(true);
                    when(mock.isCurrentPasswordArgon2(any(UserAccount.class),eq("testPassword"))).thenReturn(true);
                })){
            loginController.doPost(request, response);
        }

        verify(request, times(1)).getParameter("username");
        verify(request, times(1)).getParameter("password");
        verify(request, times(1)).getSession();
        assertEquals(response.getStatus(), 200);
    }
}
