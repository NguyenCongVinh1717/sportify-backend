package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllOrderByIdDesc(Pageable pageable);
}