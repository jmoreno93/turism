package com.moises.turism.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/", "/extranet", "/extranet/", "/intranet", "/intranet/"})
    public String resolveFrontend(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith("/extranet")) {
            return "forward:/extranet/index.html";
        }

        if (path.startsWith("/intranet")) {
            return "forward:/intranet/index.html";
        }

        return "forward:/index.html";
    }
}
