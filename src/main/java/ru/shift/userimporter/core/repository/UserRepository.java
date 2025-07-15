package ru.shift.userimporter.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.shift.userimporter.core.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE (:phone IS NULL OR u.phone = :phone) " +
            "AND (:name IS NULL OR u.firstName = :name) " +
            "AND (:lastName IS NULL OR u.lastName = :lastName) " +
            "AND (:email IS NULL OR u.email = :email)")
    List<User> findByFilters(@Param("phone") String phone,
                             @Param("name") String name,
                             @Param("lastName") String lastName,
                             @Param("email") String email);
}