package com.cinemahub.controller;

import com.cinemahub.dto.RegistrationDto;
import com.cinemahub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Login page rendering + public self-registration (Function 1).
 * Actual credential checking on login is handled entirely by Spring
 * Security (see SecurityConfig#filterChain) - this controller only shows
 * the login form and processes registration.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute RegistrationDto registrationDto,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerCustomer(registrationDto);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
        return "redirect:/auth/login?registered";
    }
}
