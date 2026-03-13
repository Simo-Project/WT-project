package com.tus.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/admin/requests",
            "/admin/requests/{id}",
            "/resident/my-requests",
            "/resident/requests/{id}"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}