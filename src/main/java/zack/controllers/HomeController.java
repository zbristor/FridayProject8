package zack.controllers;

import zack.configs.CloudinaryConfig;
import zack.models.Photo;
import zack.models.User;
import zack.repositories.PhotoRepository;
import zack.services.UserService;
import zack.validators.UserValidator;
import com.cloudinary.utils.ObjectUtils;
import com.google.common.collect.Lists;
import it.ozimov.springboot.mail.model.Email;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.internet.InternetAddress;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

@Controller
public class HomeController {

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoRepository photoRepo;

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping(value="/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @RequestMapping("/memelink/{id}")
    public String linktoMeme(@PathVariable("id") Long id, Model model){
        Photo p = photoRepo.findById(id);
        List<Photo> plist = new ArrayList<Photo>();
        plist.add(p);
        model.addAttribute("images",plist);
        return "gallery";
    }

    @RequestMapping(value="/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) throws UnsupportedEncodingException {

        model.addAttribute("user", user);
        userValidator.validate(user, result);

        if (result.hasErrors()) {
            return "registration";
        } else {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Successfully Created");
        }

        return "index";
    }

    public UserValidator getUserValidator() {
        return userValidator;
    }

    public void setUserValidator(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("p", new Photo());
        return "upload";
    }

    @PostMapping("/upload")
    public String singleImageUpload(@RequestParam("file") MultipartFile file, @RequestParam("filter") String filter
            , RedirectAttributes redirectAttributes, Model model, @ModelAttribute Photo p, Principal principal){

        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("message","Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {
            Map uploadResult =  cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));

            model.addAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");
            String filename = uploadResult.get("public_id").toString() + "." + uploadResult.get("format").toString();
            List<String> filterList = new ArrayList<String>();
            filterList.add("e_grayscale/");
            filterList.add("e_pixelate:5/");
            if(filterList.contains(filter))
            {
                p.setImage("http://res.cloudinary.com/zbristor/image/upload/"+filter+filename);
            } //<img src=
            else
            {
                p.setImage("<img src='http://res.cloudinary.com/zbristor/image/upload/" + filename + "' width='500px'/>");
            }
            //System.out.printf("%s\n", cloudc.createUrl(filename,900,900, "fit"));
            p.setCreatedAt(new Date());
            p.setUsername(principal.getName());
            photoRepo.save(p);
            System.out.println("image" + p.getImage());
            setupGallery(model);
        } catch (IOException e){
            e.printStackTrace();
            model.addAttribute("message", "Sorry I can't upload that!");
        }
        return "redirect:/preview";
    }
    @GetMapping("/preview")
    public String getPreview(Model model, Photo photo, Principal principal)
    {
        model.addAttribute("photo",new Photo());
        List<Photo> photoList = photoRepo.findAllByUsernameByOrderByDateAsc(principal.getName());
        System.out.printf("%s\n", "photoList" + photoList);
        //photoRepo.findFirstByPhotoList(photoList);
        System.out.printf("%s\n", photoList.get(0).getFilter());
        model.addAttribute("photoList", photoList);
        return "preview";
    }
    @PostMapping("/preview")
    public String postPreview(Model model,Photo photo, Principal principal)
    {
        model.addAttribute("photo",new Photo());
        List<Photo> photoList = photoRepo.findAllByUsernameByOrderByDateAsc(principal.getName());
        System.out.printf("%s\n", "photoList" + photoList);
        //photoRepo.findFirstByPhotoList(photoList);
        System.out.printf("%s\n", photoList.get(0).getFilter());
        model.addAttribute("photoList", photoList);
        return "preview";
    /*
    @PostMapping("/preview")
    public String postPreview(Model model, Photo photo, Principal principal) {

        return "preview";
    }
    */
    @RequestMapping("/img/{id}")
    public String something(@PathVariable("id") long id, Model model){
        model.addAttribute("photo", photoRepo.findById(id));
        return "textgen";
    }
    @RequestMapping("/gallery")
    public String gallery(Model model){
        setupGallery(model);
        return "gallery";
    }

    @RequestMapping("/textgen")
    public String textgen(Model model){
        model.addAttribute("photo", new Photo());
        return "textgen";
    }

    @PostMapping("/creatememe")
    public String creatememe(@ModelAttribute Photo photo, Model model, Principal principal) throws UnsupportedEncodingException {
        User u = userService.findByUsername(principal.getName());
        photoRepo.save(photo);
        setupGallery(model);
        model.addAttribute("Meme created");
        sendEmailWithoutTemplating(u.getUsername(), u.getEmail(), photo.getId());
        return "gallery";
    }

    @RequestMapping("/select/{id}")
    public String selectSomthign(@PathVariable("id") String type, Model model){
                List<Photo> list = photoRepo.findAllByType(type);
                model.addAttribute("images", list);
                return "makememe";
    }

    @GetMapping("/makememe")
    public String getMeme(Model model){
        Iterable<Photo> list = photoRepo.findAllByBotmessageEqualsAndTopmessageEquals(null, null);
        List<Photo> list2 = new ArrayList<Photo>();
        for(Photo p : list){
            boolean check = true;
            for(Photo p2 : list2){
                if(p2.getType().equals(p.getType())){
                    System.out.printf("1 %s %s\n", p2.getType(), p.getType());
                    check = false;
                    break;
                }
                else{
                    System.out.printf("2 %s %s\n", p2.getType(), p.getType());
                    check = true;
                }
            }
            if(check){
                list2.add(p);

            }
            System.out.printf("3 %s\n", p.getType());
        }
        Set<Photo> myList = new HashSet<Photo>();
        for(Photo p2 : list2){
            //System.out.printf("%s\n", p2.getType());
            myList.add(p2);
        }


        model.addAttribute("photoList", myList);
        return "makememe";
    }

    private void setupGallery(Model model){
        Iterable<Photo> photoList = photoRepo.findAllByBotmessageIsNotAndTopmessageIsNot("","");

        model.addAttribute("images", photoList);
    }
    @Autowired
    public EmailService emailService;
    public void sendEmailWithoutTemplating(String username, String email2, Long id) throws UnsupportedEncodingException {
        final Email email = DefaultEmail.builder()
                .from(new InternetAddress("daylinzack@gmail.com", "Admin Darth Vader"))
                .to(Lists.newArrayList(new InternetAddress(email2, username)))
                .subject("Your meme is here and ready for consumption")
                .body("Hi youre meme is: localhost:3000/memelink/" + String.valueOf(id) )
                .encoding("UTF-8").build();
        emailService.send(email);
    }

    @RequestMapping("/searchusers")
    public String mapUser(Model model)
    {
        model.addAttribute("user", new User());
        return "searchusers";
    }

    @PostMapping("/searchusers")
    public String postUser(@ModelAttribute User user, Model model)
    {
        User us = userService.findByUsername(user.getUsername());
        model.addAttribute("us",us);
        return "searchusers";
    }

}
