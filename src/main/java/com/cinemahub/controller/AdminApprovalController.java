package com.cinemahub.controller;

import com.cinemahub.dto.PendingApprovalItem;
import com.cinemahub.model.CinemaHall;
import com.cinemahub.model.Movie;
import com.cinemahub.model.Promotion;
import com.cinemahub.model.Showtime;
import com.cinemahub.model.User;
import com.cinemahub.service.CinemaHallService;
import com.cinemahub.service.MovieService;
import com.cinemahub.service.PromotionService;
import com.cinemahub.service.ShowtimeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SYSTEM_ADMIN-only queue for approving/rejecting the Promotions, Movies,
 * Showtimes, and Cinema Halls submitted by PROMOTION_MANAGER / MANAGER - see
 * each service's {@code applyApprovalStatus} for where the PENDING default
 * is set. Restricted via the existing "/admin/**" rule in SecurityConfig.
 */
@Controller
@RequestMapping("/admin/approvals")
public class AdminApprovalController {

    private static final DateTimeFormatter SHOWTIME_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final PromotionService promotionService;
    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final CinemaHallService cinemaHallService;

    public AdminApprovalController(PromotionService promotionService, MovieService movieService,
                                    ShowtimeService showtimeService, CinemaHallService cinemaHallService) {
        this.promotionService = promotionService;
        this.movieService = movieService;
        this.showtimeService = showtimeService;
        this.cinemaHallService = cinemaHallService;
    }

    @GetMapping
    public String queue(Model model) {
        List<PendingApprovalItem> items = new ArrayList<>();

        for (Promotion promotion : promotionService.findPending()) {
            items.add(new PendingApprovalItem("Promotion", "promotion", promotion.getId(),
                    promotion.getCode() + " — " + promotion.getDescription(),
                    createdByName(promotion.getCreatedBy()), promotion.getCreatedAt()));
        }
        for (Movie movie : movieService.findPending()) {
            items.add(new PendingApprovalItem("Movie", "movie", movie.getId(), movie.getTitle(),
                    createdByName(movie.getCreatedBy()), movie.getCreatedAt()));
        }
        for (Showtime showtime : showtimeService.findPending()) {
            String name = showtime.getMovie().getTitle() + " — " + showtime.getDateTime().format(SHOWTIME_LABEL_FORMAT);
            items.add(new PendingApprovalItem("Showtime", "showtime", showtime.getId(), name,
                    createdByName(showtime.getCreatedBy()), showtime.getCreatedAt()));
        }
        for (CinemaHall hall : cinemaHallService.findPending()) {
            items.add(new PendingApprovalItem("Cinema Hall", "hall", hall.getId(), hall.getName(),
                    createdByName(hall.getCreatedBy()), hall.getCreatedAt()));
        }

        items.sort(Comparator.comparing(PendingApprovalItem::getSubmittedAt));
        model.addAttribute("items", items);
        return "admin/approvals";
    }

    @PostMapping("/promotion/{id}/approve")
    public String approvePromotion(@PathVariable Long id) {
        promotionService.approve(id);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/promotion/{id}/reject")
    public String rejectPromotion(@PathVariable Long id, @RequestParam(required = false) String reason) {
        promotionService.reject(id, reason);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/movie/{id}/approve")
    public String approveMovie(@PathVariable Long id) {
        movieService.approve(id);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/movie/{id}/reject")
    public String rejectMovie(@PathVariable Long id, @RequestParam(required = false) String reason) {
        movieService.reject(id, reason);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/showtime/{id}/approve")
    public String approveShowtime(@PathVariable Long id) {
        showtimeService.approve(id);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/showtime/{id}/reject")
    public String rejectShowtime(@PathVariable Long id, @RequestParam(required = false) String reason) {
        showtimeService.reject(id, reason);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/hall/{id}/approve")
    public String approveHall(@PathVariable Long id) {
        cinemaHallService.approve(id);
        return "redirect:/admin/approvals";
    }

    @PostMapping("/hall/{id}/reject")
    public String rejectHall(@PathVariable Long id, @RequestParam(required = false) String reason) {
        cinemaHallService.reject(id, reason);
        return "redirect:/admin/approvals";
    }

    private String createdByName(User createdBy) {
        return createdBy != null ? createdBy.getName() : "—";
    }
}
