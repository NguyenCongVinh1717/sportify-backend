package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Comment;
import SaleManagement.VinhNguyen.request.CommentRequest;
import SaleManagement.VinhNguyen.response.CommentResponse;

public class CommentMapper {
    public static Comment toEntity(CommentRequest commentRequest){
        return Comment.builder()
                .rating(commentRequest.getRating())
                .content(commentRequest.getContent())
                .build();
    }
    public static CommentResponse toResponse(Comment comment){
        CommentResponse.CommentResponseBuilder commentResponseBuilder=CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .rating(comment.getRating());
        if(comment.getUser()!=null){
            commentResponseBuilder.userName(comment.getUser().getFullName());
        }
        return commentResponseBuilder.build();
    }
}
