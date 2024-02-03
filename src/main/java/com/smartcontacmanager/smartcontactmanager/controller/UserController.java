package com.smartcontacmanager.smartcontactmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartcontacmanager.smartcontactmanager.model.User;

@Controller
public class UserController {
    
    // handler for home page
    @RequestMapping("/home")
    private String home(Model model){
    model.addAttribute("title","Home - Smart Contact Manager");
        return "home";
    }

    // handler for about page
    @RequestMapping("/about")
    private String about(Model model){
    model.addAttribute("title","About - Smart Contact Manager");
        return "about";
    }

    // handler for signup page
    @RequestMapping("/signup")
    private String signup(Model model){
    model.addAttribute("title","Sign Up - Smart Contact Manager");
    model.addAttribute("user", new User());
        return "signup";
    }

     // handler for login page
     @RequestMapping("/signin")
     private String customLogin(Model model){
         model.addAttribute("title", "Login - Smart Contact Manager");
         return "login";
     }

}
