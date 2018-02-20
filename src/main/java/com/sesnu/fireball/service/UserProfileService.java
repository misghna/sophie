package com.sesnu.fireball.service;

import java.util.List;

import com.sesnu.fireball.model.UserProfile;


public interface UserProfileService {

	UserProfile findById(int id);

	UserProfile findByType(String type);
	
	List<UserProfile> findAll();
	
}
