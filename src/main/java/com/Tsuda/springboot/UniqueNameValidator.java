package com.Tsuda.springboot;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

public class UniqueNameValidator implements ConstraintValidator<UniqueName, String> {


	private UniqueName uni;

	@Autowired
	UserRepository repository;

	@Override
	public void initialize(UniqueName unique){
		this.uni = unique;
	}

	@Override
	public boolean isValid(String input, ConstraintValidatorContext cxt){
		boolean flg = false;
		try{
			User user = repository.findByUsername(input);
			if( user == null ){
				flg = true;
			}
		}catch(NullPointerException e){
			flg = true;
		}finally{
			return flg;
		}
	}

}
