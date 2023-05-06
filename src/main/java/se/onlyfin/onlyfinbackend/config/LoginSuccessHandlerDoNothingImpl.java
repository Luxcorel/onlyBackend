package se.onlyfin.onlyfinbackend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

/**
 * This class is used to override the default behavior of Spring Security's login success handler.
 * This is to disable the redirect to the default login success page.
 */
public class LoginSuccessHandlerDoNothingImpl implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        //this implementation does nothing on successful logins - it lets the caller handle it instead
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
