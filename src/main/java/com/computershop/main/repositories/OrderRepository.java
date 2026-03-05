package com.computershop.main.repositories;

import com.computershop.main.entities.Order;
import com.computershop.main.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserUserId(Integer userId);
    
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    List<Order> findByUserUserIdOrderByOrderDateDesc(Integer userId);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startOfDay AND o.orderDate < :endOfDay")
    List<Order> findTodayOrders(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :startDate")
    List<Order> findOrdersFromLastDays(@Param("startDate") LocalDateTime startDate);
    
    long countByUser(User user);
    
    long countByUserUserId(Integer userId);
    
    @Query("SELECT o, SUM(od.price * od.quantity) as totalAmount FROM Order o " +
           "JOIN o.orderDetails od GROUP BY o.orderId")
    List<Object[]> findOrdersWithTotalAmount();
    
    @Query("SELECT o FROM Order o WHERE o.orderDate >= :thirtyDaysAgo ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Integer userId, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(od.price * od.quantity), 0) FROM Order o " +
           "JOIN o.orderDetails od WHERE o.user.userId = :userId")
    double getTotalSpentByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersForAdmin(Pageable pageable);
}