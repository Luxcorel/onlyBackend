package se.onlyfin.onlyfinbackend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

public class LoginFailureHandlerDoNothingImpl implements org.springframework.security.web.authentication.AuthenticationFailureHandler{
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

    }
}
