package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Cart;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.CartMapper;
import SaleManagement.VinhNguyen.repository.CartRepository;
import SaleManagement.VinhNguyen.response.CartResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    public Cart getOrCreateCart(User user){
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart c=new Cart();
            c.setUser(user);
            return cartRepository.save(c);
        });
    }

    public Cart getCart(User user){
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
    }
}
