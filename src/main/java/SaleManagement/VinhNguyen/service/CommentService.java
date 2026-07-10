package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Comment;
import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.CommentMapper;
import SaleManagement.VinhNguyen.repository.CommentRepository;
import SaleManagement.VinhNguyen.repository.ProductRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.request.CommentRequest;
import SaleManagement.VinhNguyen.response.CommentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    public List<CommentResponse> getCommentByProductId(Long productId){
        return commentRepository.findByProductIdOrderByIdDesc(productId).stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList());
    }
    public CommentResponse addComment(Long productId, CommentRequest commentRequest){
        //avoid sending userId by user because of hackers
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Comment comment = CommentMapper.toEntity(commentRequest);
        comment.setUser(user);
        comment.setProduct(product);

        commentRepository.save(comment);
        return CommentMapper.toResponse(comment);
    }
    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest commentRequest){
        Comment oldComment=commentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        oldComment.setRating(commentRequest.getRating());
        oldComment.setContent(commentRequest.getContent());
        return CommentMapper.toResponse(oldComment);
    }
    public void deleteComment(Long id){
        Comment oldComment=commentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        commentRepository.delete(oldComment);
    }
}
