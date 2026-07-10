package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.CommentRequest;
import SaleManagement.VinhNguyen.response.CommentResponse;
import SaleManagement.VinhNguyen.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/product/{productId}")
    public List<CommentResponse> getCommentByProductId(@PathVariable Long productId) {
        return commentService.getCommentByProductId(productId);
    }

    @PostMapping
    public CommentResponse addComment(@RequestParam Long productId,
                                      @RequestBody CommentRequest commentRequest) {
        return commentService.addComment(productId, commentRequest);
    }

    @PutMapping("/{id}")
    public CommentResponse updateComment(@PathVariable Long id,
                                         @RequestBody CommentRequest commentRequest) {
        return commentService.updateComment(id, commentRequest);
    }

    @DeleteMapping("/{id}")
    public String deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return "Delete comment successfully!";
    }
}