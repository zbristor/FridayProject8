package zack.controllers;

import zack.configs.CloudinaryConfig;
import zack.models.Comments;
import zack.models.Followers;
import zack.models.Photo;
import zack.models.User;
import zack.repositories.CommentsRepository;
import zack.repositories.FollowersRepository;
import zack.repositories.PhotoRepository;
import zack.repositories.UserRepository;
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
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import javax.validation.Valid;
import javax.xml.stream.events.Comment;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;
import java.util.List;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowersRepository followersRepository;

    @Autowired
    private CommentsRepository commentsRepository;

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
                p.setImage("http://res.cloudinary.com/zbristor/image/upload/" + filename);
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
        //System.out.printf("%s\n", "photoList" + photoList);
        //photoRepo.findFirstByPhotoList(photoList);
        //System.out.printf("%s\n", photoList.get(0).getFilter());
        model.addAttribute("photoList", photoList);

        return "preview";
    }
    @PostMapping("/preview")
    public String postPreview(Model model,Photo photo, Principal principal) {

        return "preview";
    }

    @GetMapping("/mypics")
    public String getPics(Model model, Photo photo, Principal principal, @ModelAttribute Comments comments)
    {
        model.addAttribute("comments",new Comments());
        model.addAttribute("photo",new Photo());
        Iterable<Photo> photoList = photoRepo.findAllByUsername(principal.getName());
        model.addAttribute("photoList",photoList);
        return "mypics";
    }

    @RequestMapping("/img/{id}")
    public String something(@PathVariable("id") long id, Model model){
        model.addAttribute("photo", photoRepo.findById(id));
        return "textgen";
    }
    @RequestMapping("/gallery")
    public String gallery(Model model){
        //setupGallery(model);
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

    @PostMapping("/searchusers") //, params = { "follow"}
    public String postUser(@RequestParam(required=false,value="follow") String follow,@ModelAttribute User user, Model model, Principal principal)
    {
        User us = userService.findByUsername(user.getUsername());
        model.addAttribute("us",us);
        return "searchresults";
    }
    @GetMapping("/searchresults/{username}")
    public String mapFollow(@PathVariable("username") String type, Model model, User user, Principal principal, Followers followers)
    {

        User us = userService.findByUsername(type);
        User currentUser = userService.findByUsername(principal.getName());
        followers.setFollowerName(us.getUsername());
        followers.setUsername(currentUser.getUsername());
        followersRepository.save(followers);
        return "searchusers";
    }
    @GetMapping("/myfeed")
    public String Pics(Model model, Photo photo, Principal principal)
    {
        model.addAttribute("photo",new Photo());
        model.addAttribute("comments",new Comments());
        Iterable<Photo> photoList = photoRepo.FindAllByFollower(principal.getName(),principal.getName());
        model.addAttribute("photoList",photoList);
        return "myfeed";
    }
    @RequestMapping("/picture/{id}")
    public String selectImage(@PathVariable("id") long id, Model model,Principal principal)
    {
        model.addAttribute("comments",new Comments());
        Iterable<Comments> commentList=commentsRepository.findAllByPhotoID(id);
        model.addAttribute("images", photoRepo.findById(id));
        model.addAttribute("commentList",commentList);
        return "gallery";
    }

    @GetMapping("/createcomment")
    public String getComment(Model model)
    {
        model.addAttribute("comments",new Comments());
        return "createcomment";
    }

    @PostMapping("/createcomment")
    public String createComment(Model model, @ModelAttribute Comments comments,Principal principal)
    {
        comments.setUsername(principal.getName());
        commentsRepository.save(comments);
        return "redirect:/myfeed";
    }

    @RequestMapping("/commentlist")
    public String mapList(Model model)
    {
        model.addAttribute("comments", new Comments());
        return "commentlist";
    }
    @PostMapping("/commentlist")
    public String commentList(HttpServletRequest request,@ModelAttribute Comments comments, Model model)
    {
        String matter = request.getParameter("photoID");
        Iterable<Comments> commentList=commentsRepository.findAllByPhotoID(Long.valueOf(matter));
        model.addAttribute("commentList",commentList);
        return "commentlist";
    }


}
