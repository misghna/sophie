package com.sesnu.fireball.dao;

import java.util.List;

import com.sesnu.fireball.model.UserProfile;


public interface UserProfileDao {

	List<UserProfile> findAll();
	
	UserProfile findByType(String type);
	
	UserProfile findById(int id);
}
