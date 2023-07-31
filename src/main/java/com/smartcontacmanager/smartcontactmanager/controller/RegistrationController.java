package com.smartcontacmanager.smartcontactmanager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontacmanager.smartcontactmanager.helper.Message;
import com.smartcontacmanager.smartcontactmanager.model.User;
import com.smartcontacmanager.smartcontactmanager.repository.UserRepository;

@Controller
public class RegistrationController {

    @Autowired
    private BCryptPasswordEncoder bcryptCryptPasswordEncoder;

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/do-register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute("user") User user,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
            BindingResult result, HttpSession session) {

        try {
            if (!agreement) {
                System.out.println("You have not agreed the terms and conditions");
                throw new Exception("You have not agreed the terms and conditions");
            }

            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
                model.addAttribute("user", user);
                return "signup"; 
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            user.setPassword(bcryptCryptPasswordEncoder.encode(user.getPassword()));

            userRepository.save(user);

            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));

            System.out.println("Agreement " + agreement);
            System.out.println("User " + user);
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
            return "signup";
        }
        
    }

}
