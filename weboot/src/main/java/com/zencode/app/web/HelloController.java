package com.zencode.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.zencode.app.services.ActorService;
import com.zencode.app.dao.beans.Actor;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class HelloController {
    @Autowired
    private ActorService actorService;

    @GetMapping("/api/hello")
    public String handleHello(Model model) {
        model.addAttribute("message", "Hello from here!");
        List<Actor> actors = actorService.getActors();
        model.addAttribute("actors", actors);
        return "hello-world";  // resolved as hello.html in templates directory
    }
}

