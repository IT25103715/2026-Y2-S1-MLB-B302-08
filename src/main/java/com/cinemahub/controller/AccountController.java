package com.cinemahub.controller;

import com.cinemahub.dto.AccountForm;
import com.cinemahub.model.User;
import com.cinemahub.model.UserStatus;
import com.cinemahub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Basic self-service "my account" pages (Function 1) - view/update contact
 * details, or deactivate/delete the account. This used to be its own module
 * (Function 4, "Customer Profile Management") but that function was
 * repurposed to Payment & Transaction Management - see
 * {@link PaymentController} for "My Payment History" - so this now lives as
 * a thin controller directly over {@link User}, with no separate Profile
 * entity behind it.
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String view(Authentication authentication, Model model) {
        model.addAttribute("user", currentUser(authentication));
        return "account/view";
    }

    @GetMapping("/edit")
    public String editForm(Authentication authentication, Model model) {
        User user = currentUser(authentication);

        AccountForm form = new AccountForm();
        form.setName(user.getName());
        form.setPhone(user.getPhone());
        form.setAddress(user.getAddress());

        model.addAttribute("accountForm", form);
        return "account/edit";
    }

    @PostMapping("/edit")
    public String update(@ModelAttribute AccountForm accountForm, Authentication authentication) {
        User user = currentUser(authentication);
        userService.updateContactInfo(user.getId(), accountForm);
        return "redirect:/account";
    }

    @PostMapping("/deactivate")
    public String deactivate(Authentication authentication, HttpServletRequest request) throws jakarta.servlet.ServletException {
        User user = currentUser(authentication);
        userService.updateStatus(user.getId(), UserStatus.SUSPENDED);
        request.logout();
        return "redirect:/?deactivated";
    }

    @PostMapping("/delete")
    public String delete(Authentication authentication, HttpServletRequest request,
                          RedirectAttributes redirectAttributes) throws jakarta.servlet.ServletException {
        User user = currentUser(authentication);
        try {
            userService.deleteUser(user.getId());
        } catch (IllegalStateException ex) {
            // Booking/payment history can't be erased - stay signed in and explain, instead of logging out first.
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/account";
        }
        request.logout();
        return "redirect:/?accountDeleted";
    }

    private User currentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }
}
