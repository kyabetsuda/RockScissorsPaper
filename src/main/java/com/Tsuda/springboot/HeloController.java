package com.Tsuda.springboot;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
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
		mav.addObject("msg","トップページ");
		mav.addObject("register","/register");
		mav.addObject("login","/login");
		mav.addObject("logout","/logout");
		mav.addObject("battle","/battle");
		mav.addObject("ranking","/ranking");
		mav.addObject("data","/data");
		mav.addObject("chat","/chat");
		mav.addObject("userlist","/users");



		return mav;

}

	@PostConstruct
	public void init(){
		//Redis起動と初期化
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		jedis.del("zset");
		User d1 = new User();
		d1.setUsername("a");
		d1.setPassword("a");
		d1.setAuthority(Authority.ROLE_ADMIN);
		//Redisのテーブル作り直し
		jedis.zadd("zset", 0, d1.getUsername());
		pool.destroy();
		repository.saveAndFlush(d1);
	}

	@RequestMapping(value ="/login", method=RequestMethod.GET)
	public ModelAndView login(ModelAndView mav){
		mav.setViewName("login");
		mav.addObject("msg","名前を入力してログインしてください。");

		return mav;
	}

	@RequestMapping(value ="/log_out", method=RequestMethod.GET)
	public ModelAndView logout(ModelAndView mav){
		mav.setViewName("log_out");
		mav.addObject("msg","Type your name and login");

		return mav;
	}

	@RequestMapping(value = "/register", method=RequestMethod.GET)
	public ModelAndView showRegister(

			ModelAndView mav){
		mav.setViewName("register");
		mav.addObject("msg","名前とパスワードを入力して登録してください。");
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
			//登録と同時にRedisにも名前登録
			jedis.zadd("zset", 0, user.getUsername());
			mav.addObject("msg","登録に成功しました。");
			pool.destroy();
		}else{
			mav.addObject("msg","エラーが発生しました。");
		}
		return mav;
	}

	@RequestMapping(value = "/battle", method = RequestMethod.GET)
	public ModelAndView showBattle(
			ModelAndView mav){
		mav.setViewName("battle");
		mav.addObject("msg","コンピュータと対戦します。");

		return mav;
	}

	@RequestMapping(value = "/battle", method = RequestMethod.POST)
	public ModelAndView battle(
			HttpServletRequest request,
			Principal principal,
			ModelAndView mav){
		String judge = request.getParameter("hoge");
		if(judge.equals("win")){
		//送られてきたユーザ情報から名前を取得
		User user = repository.findByUsername(principal.getName());
		//Redisを起動し、スコアを上昇
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
		mav.addObject("msg","あなたのデータ");
		User user = repository.findByUsername(principal.getName());
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		mav.addObject("datalist", user);
		mav.addObject("score",jedis.zscore("zset", user.getUsername()));

		return mav;
	}

	@RequestMapping(value = "/ranking")
	public ModelAndView ranking(ModelAndView mav){
		mav.setViewName("ranking");
		JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
		Jedis jedis = pool.getResource();
		Set<String> setName = jedis.zrevrange("zset", 0, -1);
		//ランク、名前、スコアを横並びに表示させるために、配列を作る。
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
		mav.addObject("msg","部屋に接続してチャットができます。");
		return mav;
	}

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	//"ROLE_ADMIN"を持ったユーザだけがアクセス可能
	@Secured("ROLE_ADMIN")
	public ModelAndView users(
			ModelAndView mav){
		mav.setViewName("users");
		List<User> users = repository.findAll();
		mav.addObject("datalist",users);
		return mav;
	}

}
