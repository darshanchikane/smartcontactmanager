package com.smartcontacmanager.smartcontactmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/user")
public class UserAuthenticationController {
    
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String dashboard(){
        return "normal/user-dashboard";
    }

    // handler for login page
    @RequestMapping("/signin")
    public String customLogin(Model model){
        model.addAttribute("title", "Login - Smart Contact Manager");
        return "login";
    }
}
