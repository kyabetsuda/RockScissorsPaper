package com.Tsuda.springboot;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.Tsuda.springboot.User.Authority;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Controller
public class HeloController {



	@Autowired
	UserRepository repository;


	@RequestMapping(value="/")
	public ModelAndView index(
			ModelAndView mav){
		mav.setViewName("index");
		mav.addObject("msg","this is the top page");
		mav.addObject("register","/register");
		mav.addObject("login","/login");
		mav.addObject("logout","/logout");
		mav.addObject("battle","/battle");
		mav.addObject("ranking","/ranking");
		mav.addObject("data","/data");
		mav.addObject("chat","/chat");



		return mav;

}

	@PostConstruct
	public void init(){
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		jedis.del("zset");
		User d1 = new User();
		d1.setUsername("a");
		d1.setPassword("a");
		d1.setAuthority(Authority.ROLE_ADMIN);
		jedis.zadd("zset", 0, d1.getUsername());
		pool.destroy();
		repository.saveAndFlush(d1);
	}

	@RequestMapping(value ="/login", method=RequestMethod.GET)
	public ModelAndView login(ModelAndView mav){
		mav.setViewName("login");
		mav.addObject("msg","Type your name and login");

		return mav;
	}

	@RequestMapping(value ="/loggedout", method=RequestMethod.GET)
	public ModelAndView loggedout(ModelAndView mav){
		mav.setViewName("loggedout");
		mav.addObject("msg","Type your name and login");

		return mav;
	}

	@RequestMapping(value = "/register", method=RequestMethod.GET)
	public ModelAndView showRegister(

			ModelAndView mav){
		mav.setViewName("register");
		mav.addObject("msg","Register Page");
		return mav;
	}

	@RequestMapping(value = "/register", method=RequestMethod.POST)
	public ModelAndView register(
			@ModelAttribute("formModel")
			@Validated User user,
			BindingResult result,
			ModelAndView mav){
		if(!result.hasErrors()){
			JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
			Jedis jedis = pool.getResource();
			user.setAuthority(Authority.ROLE_USER);
			repository.saveAndFlush(user);
			jedis.zadd("zset", 0, user.getUsername());
			mav.addObject("msg","Successfully registered!");
			pool.destroy();
		}else{
			mav.addObject("msg","some errors occured");
		}
		return mav;
	}

	@RequestMapping(value = "/battle", method = RequestMethod.GET)
	public ModelAndView showBattle(
			ModelAndView mav){
		mav.setViewName("battle");
		mav.addObject("msg","Press the button below to start RSP");

		return mav;
	}

	@RequestMapping(value = "/battle", method = RequestMethod.POST)
	public ModelAndView battle(
			HttpServletRequest request,
			Principal principal,
			ModelAndView mav){
		String judge = request.getParameter("hoge");
		if(judge.equals("win")){
		User user = repository.findByUsername(principal.getName());
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		jedis.zincrby("zset", 1, user.getUsername());
		pool.destroy();
		repository.saveAndFlush(user);
		}
		mav.addObject("msg", judge);
		return mav;
	}

	@RequestMapping(value = "/data")
	public ModelAndView data(
			Principal principal,
			ModelAndView mav){
		mav.addObject("msg","Your Data");
		User user = repository.findByUsername(principal.getName());
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		mav.addObject("datalist", user);
		mav.addObject("score",jedis.zscore("zset", user.getUsername()));

		return mav;
	}

	@RequestMapping(value = "/ranking")
	@Secured("ROLE_ADMIN")
	public ModelAndView ranking(ModelAndView mav){
		mav.setViewName("ranking");
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		Set<String> setName = jedis.zrevrange("zset", 0, -1);
		Object[] iterator = setName.toArray();
		ArrayList<ArrayList<String>> setScore = new ArrayList<>();
		for(int i = 0; i<setName.size(); i++){
			ArrayList<String> data = new ArrayList<String>();
			data.add(String.valueOf(i + 1));
			data.add(iterator[i].toString());
			data.add(jedis.zscore("zset", iterator[i].toString()).toString());
			setScore.add(data);
		}

		mav.addObject("scorelist", setScore);
		return mav;
	}

	@RequestMapping(value = "/chat", method = RequestMethod.GET)
	public ModelAndView showChat(
			ModelAndView mav){
		mav.setViewName("chat");
		return mav;
	}


}
