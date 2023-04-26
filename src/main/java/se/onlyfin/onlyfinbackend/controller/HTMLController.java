package se.onlyfin.onlyfinbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

/**
 * This class is responsible for handling all HTML page endpoints.
 */
@CrossOrigin(origins = "https://onlyfrontend-production.up.railway.app", allowCredentials = "true")
@Controller
public class HTMLController {

    /**
     * This method is responsible for returning the index page.
     *
     * @return The index page.
     */
    @GetMapping("/")
    public ModelAndView indexPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    /**
     * This method is responsible for returning the register page.
     *
     * @return The login page.
     */
    @GetMapping("/register")
    public ModelAndView registerPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        return modelAndView;
    }

    /**
     * This method is responsible for returning the logged in/user page.
     *
     * @param principal The user that is logged in.
     * @return The logged in page.
     */
    @GetMapping("/user")
    @ResponseBody
    public String loggedInPage(Principal principal) {
        return "Logged in as " + principal.getName();
    }

}
