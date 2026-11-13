package edu.piet.smartcampus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping({"/", "/dashboard", "/students", "/faculty", "/departments", "/courses",
                 "/subjects", "/rooms", "/timeslots", "/timetable", "/autogenerator", "/reports", "/settings"})
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
