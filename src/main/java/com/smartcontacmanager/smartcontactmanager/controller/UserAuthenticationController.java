package com.smartcontacmanager.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontacmanager.smartcontactmanager.helper.Message;
import com.smartcontacmanager.smartcontactmanager.model.Contact;
import com.smartcontacmanager.smartcontactmanager.model.User;
import com.smartcontacmanager.smartcontactmanager.repository.ContactRepository;
import com.smartcontacmanager.smartcontactmanager.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserAuthenticationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @ModelAttribute
    private void addCommonData(Model model, Principal principal) {

        String userName = principal.getName();
        model.addAttribute("title", "Dashboard");
        User user = userRepository.getUserByUserName(userName);
        model.addAttribute("user", user);

    }

    // dashboard view
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    private String dashboard(Model model, Principal principal) {
        return "normal/dashboard";

    }

    // addcontact view
    @GetMapping("/add/contacts")
    // @RequestMapping(value = "/add/contacts", method = RequestMethod.GET)
    private String addContacts(Model model) {

        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add-contact";
    }

    // Add Contact
    @PostMapping("/process-contact")
    // @RequestMapping(value = "/process-contact", method = RequestMethod.POST)
    private String processContact(@ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile multipartFile, Principal principal, HttpSession session) {
        try {
            String username = principal.getName();
            User user = userRepository.getUserByUserName(username);

            if (multipartFile.isEmpty()) {
                contact.setImage("contact.jpg");
            } else {
                String originalFilename = multipartFile.getOriginalFilename();
                String uniqueFilename = user.getId() + "_" + UUID.randomUUID().toString()
                        + originalFilename.substring(originalFilename.lastIndexOf("."));
                contact.setImage(uniqueFilename);

                File file = new ClassPathResource("/static/img").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator + uniqueFilename);

                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            user.getContacts().add(contact);
            contact.setUser(user);

            userRepository.save(user);

            System.out.println("Data " + contact);

            session.setAttribute("message", new Message("Your contact is added !! Add more", "success"));
        } catch (Exception ex) {
            ex.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !! Try again", "danger"));
        }
        return "normal/add-contact";
    }

    // Show Contacts
    @GetMapping("/show/contacts/{page}")
    // @RequestMapping(value = "/process-contact", method = RequestMethod.GET)
    private String showContacts(@PathVariable("page") Integer page,
        @RequestParam(required = false) String search,
    Model model, Principal principal) {
        model.addAttribute("title", "Contacts - Smart Contact Manager");

        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);

        Pageable pageable = PageRequest.of(page, 2);

        Page<Contact> contacts;
        
        if(search != null && !search.isEmpty()){
            contacts = contactRepository.findContactByUserAndName(user.getId(), search, pageable);
        } else {

         contacts = contactRepository.findContactByUser(user.getId(), pageable);
        }

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show-contacts";
    }

    // show contact details
    @RequestMapping("/{cId}/contact/")
    private String getContactDetails(@PathVariable("cId") Integer cId, Model model) {
        System.out.println("Contact id :" + cId);
        model.addAttribute("title", "Details - Smart Contact Manager");
        Optional<Contact> contactO = contactRepository.findById(cId);

        if (contactO.isPresent()) {
            Contact contact = contactO.get();
            model.addAttribute("contact", contact);
        }
        return "normal/contact-details";
    }

    // delete contact
    @Transactional
    @GetMapping("/delete/{cId}")
    // @RequestMapping(value = "/process-contact", method = RequestMethod.GET)

    private String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) throws Exception {
        Optional<Contact> contactO = contactRepository.findById(cId);
        if (contactO.isEmpty()) {
            throw new Exception("Inavlid Contact Id");
        } else {
            Contact contact = contactO.get();

            User user = userRepository.getUserByUserName(principal.getName());

            user.getContacts().remove(contact);
            String image = contact.getImage();
            userRepository.save(user);

            // image delete code
            String imagePath = "/static/img/" + image;

            try {

                File imageFile = new ClassPathResource(imagePath).getFile();

                if (imageFile.exists()) {
                    imageFile.delete();
                }

            } catch (Exception e) {
                throw new Exception("Exception occured while finding the image: " + e.getMessage());
            }
            session.setAttribute("message", new Message("Contact Deleted Successfully", "success"));
            session.removeAttribute("message");
        }
        return "redirect:/user/show/contacts/0";
    }

    // open update form handler
    @PostMapping("/update-contact/{cId}")
    // @RequestMapping(value = "/update-contact/{cId}", method = RequestMethod.POST)

    private String updateForm(@PathVariable("cId") Integer cId, Model m) {
        m.addAttribute("title", "Update Contact");

        Contact contact = contactRepository.findById(cId).get();

        m.addAttribute("contact", contact);
        return "normal/update-form";
    }

    // process update handler
    @PostMapping("/process-update")
    // @RequestMapping(value = "/process-update" , method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile multipartFile, Model model , HttpSession httpSession, Principal principal) throws Exception{

        try {
            
            Contact oldContact = contactRepository.findById(contact.getcId()).get();

            if(!multipartFile.isEmpty()){
                String originalFilename = multipartFile.getOriginalFilename();
                String uniqueFilename = contact.getUser().getId() + "_" + UUID.randomUUID().toString()
                        + originalFilename.substring(originalFilename.lastIndexOf("."));
                contact.setImage(uniqueFilename);

                File file = new ClassPathResource("/static/img").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator + uniqueFilename);

                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } else {
                contact.setImage(oldContact.getImage());
            }

            User user = userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);

            contactRepository.save(contact);

            httpSession.setAttribute("message", new Message("Contact updated successfully", "success"));

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    // your profile handler
    // @GetMapping("/profile")
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String yourProfile(Model m, Principal principal){
        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);
        m.addAttribute("user", user);
        m.addAttribute("title", "Profile page");
        return "/normal/profile";
    }
}