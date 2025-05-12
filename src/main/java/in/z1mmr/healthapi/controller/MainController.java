package in.z1mmr.healthapi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String redirect(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/login";
        } else {
            return "index";
        }
    }

}