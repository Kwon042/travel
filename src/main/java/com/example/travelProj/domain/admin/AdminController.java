package com.example.travelProj.domain.admin;

import com.example.travelProj.domain.like.boardlike.ReviewBoardLikeService;
import com.example.travelProj.domain.like.commentlike.CommentLikeService;
import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserRole;
import com.example.travelProj.domain.user.UserService;
import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.board.ReviewBoardService;
import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final ReviewBoardService reviewBoardService;
    private final ReviewBoardLikeService reviewBoardLikeService;
    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal SiteUser user, Model model) {
        long totalUsers = userService.countUsers();
        long totalAdmins = userService.countAdmins();
        long totalPosts = reviewBoardService.countPosts();
        long totalComments = commentService.countComments();
        long totalBoardsLikes = reviewBoardLikeService.countTotalLikes();
        long totalCommentsLikes = commentLikeService.countTotalLikes();

        Iterable<SiteUser> users = userService.findAllUsers();
        Iterable<SiteUser> userList = userService.findByRole(UserRole.USER);
        Iterable<SiteUser> adminList = userService.findByRole(UserRole.ADMIN);

        // 모델에 데이터 추가
        model.addAttribute("user", user);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("users", users);
        model.addAttribute("userList", userList);
        model.addAttribute("adminList", adminList);
        model.addAttribute("totalBoardsLikes", totalBoardsLikes);
        model.addAttribute("totalCommentsLikes", totalCommentsLikes);

        return "admin/dashboard";
    }

    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/dashboard";
    }
}
