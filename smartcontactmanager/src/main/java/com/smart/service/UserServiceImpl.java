package com.smart.service;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import com.smart.entities.User;

import net.bytebuddy.utility.RandomString;

import com.smart.dao.UserRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepo;
	@Autowired
    private JavaMailSender mailSender;

	

	@Override
	public User createUser(User user,String url) {
		
		user.setRole("ROLE_USER");
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		

		// enable if verify by email
		user.setEnabled(false);
		RandomString rn=new RandomString();
		user.setVerificationCode(rn.make(64));
		
		
		User us=userRepo.save(user);
		sendVerificationMail(user,url);
		return us;
	}
	
	
	@Override
	public void sendVerificationMail(User user, String url) {
		String from="minakshithalet553@gmail.com";
		String to=user.getEmail();
		String subject="Account verification";
		String content= "Dear [[name]],<br>"+
		                "please click the below link for verify your account: <br>"+
				        "<h3><a href=\"[[URL]]\"target=\"_self\"> verify</a></h3>"
				        + "Thank you,<br>";
		
		try {
			
			MimeMessage message=mailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message);
			
			helper.setFrom(from,"Ankush");
			helper.setTo(to);
			helper.setSubject(subject);
			
			content=content.replace("[[name]]",user.getName() );
			
			String siteUrl=url+"/verify?code=" +user.getVerificationCode();
			content=content.replace("[[URL]]", siteUrl);
			helper.setText(content,true);
			mailSender.send(message);
			
		}catch(Exception e){
			e.printStackTrace();
		}
				       
		
		
	}


	@Override
	public boolean checkEmail(String email) {
		
		return userRepo.existsByEmail(email);
	}


	@Override
	public boolean verifyAccount(String code) {
		User user=userRepo.findByVerificationCode(code);
		if(user!=null) {
			user.setEnabled(true);
			user.setVerificationCode(null);
			userRepo.save(user);
			return true;
		}
		
		
		return false;
	}

	

}
