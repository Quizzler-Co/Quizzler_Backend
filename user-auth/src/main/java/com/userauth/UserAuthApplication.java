package com.userauth;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.userauth.domain.User;
import com.userauth.domain.dtos.UserDto;

@EnableDiscoveryClient
@SpringBootApplication
public class UserAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAuthApplication.class, args);
	}
	
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.LOOSE)
				.setSkipNullEnabled(true)
				.setAmbiguityIgnored(true);
		
		// Configure explicit mapping for User -> UserDto (username -> userName)
		// Fixed syntax: using lambda src -> src.getUsername() instead of method reference
		// LOOSE strategy will automatically skip fields that don't exist in UserDto
		// (createdAt, updatedAt, password, roles)
		modelMapper.typeMap(User.class, UserDto.class)
				.addMappings(mapper -> {
					mapper.map(src -> src.getUsername(), UserDto::setUserName);
				});
		
		return modelMapper;
	}
}
