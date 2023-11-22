package com.smart.service;

import com.smart.entities.User;

public interface UserService {
	public User createUser(User user,String url);
	public boolean checkEmail(String email);
	public void sendVerificationMail(User user,String url);
	public boolean verifyAccount(String code);
	
}
