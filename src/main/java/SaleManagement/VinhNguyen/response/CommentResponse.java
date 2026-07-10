package SaleManagement.VinhNguyen.response;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CommentResponse {
    private Long id;
    private String userName;
    private int rating;
    private String content;
}
