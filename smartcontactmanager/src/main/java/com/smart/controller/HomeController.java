package com.smart.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpMethod;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.util.*;
import com.smart.service.UserService;
import cn.apiclub.captcha.Captcha;
import net.bytebuddy.utility.RandomString;
@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userservice;
	
	

	// set captcha
	
		private void setupCaptcha(User e ) {
			Captcha captcha=CaptchaUtil.createCaptcha(250, 80);
			e.setHidden(captcha.getAnswer());// image value 
			e.setCaptcha("");// user entered value
			e.setImage(CaptchaUtil.encodeBase64(captcha));
			
		}
		
	
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	//show registration page
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		User u=new User();
     	setupCaptcha(u);
		model.addAttribute("user", u);
	//	model.addAttribute("user", new User());
		
		return "signup";
	}

	// (save user )handler for registering user
	
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session,HttpServletRequest request) {
	    
		
		try {

			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}

			if (result1.hasErrors()) {
				System.out.println("ERROR " + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			if(user.getCaptcha().equals(user.getHidden())) {
				
					
//				user.setRole("ROLE_USER");
//				user.setImageUrl("default.png");
//				user.setPassword(passwordEncoder.encode(user.getPassword()));
//				
				// email verification
				String url=request.getRequestURL().toString();
				url=url.replace(request.getServletPath(), "");
				
				User us=userservice.createUser(user, url);
				
//				userservice.sendVerificationMail(user,url);
//
//				// enable if verify by email
//				user.setEnabled(false);
//				RandomString rn=new RandomString();
//				user.setVerificationCode(rn.make(64));
//
				System.out.println("Agreement " + agreement);
				System.out.println("USER " + us);
//
//				User result = this.userRepository.save(user);
//				
//				
				model.addAttribute("user", new User());
//
        		session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			    return "redirect:/signin";
			}else {
				model.addAttribute("message", "invalid captcha");
				setupCaptcha(user);
        		model.addAttribute("user", user);
//				session.setAttribute("message","Invalid captcha");
			}
			
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something Went wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

	
	
	// for email verification
	@GetMapping("/verify")
	public String verifyAccount(@Param("code") String code) {
		
		if(userservice.verifyAccount(code)) {
			return "verify_success";
			
		}else {
			return"failed";
			
		}
	
	}
	
	
	
	
	//handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
		return "login";
	}
	
	
	
}
