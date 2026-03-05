package com.computershop.main.services;

import com.computershop.main.entities.Order;
import com.computershop.main.entities.User;
import com.computershop.main.entities.OrderDetail;
import com.computershop.main.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderDetailService orderDetailService;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }
    
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }
    
    public List<Order> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserUserId(userId);
    }
    
    public List<Order> getUserOrdersRecent(Integer userId) {
        return orderRepository.findByUserUserIdOrderByOrderDateDesc(userId);
    }
    
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }
    
    public List<Order> getTodayOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return orderRepository.findTodayOrders(startOfDay, endOfDay);
    }
    
    public List<Order> getOrdersFromLastDays(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findOrdersFromLastDays(startDate);
    }
    
    public List<Order> getRecentOrders() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return orderRepository.findRecentOrders(thirtyDaysAgo);
    }
    
    public Order createOrder(Order order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }
    
    public Order createOrderForUser(Integer userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    public Order updateOrder(Integer orderId, Order orderDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        if (orderDetails.getUser() != null) {
            order.setUser(orderDetails.getUser());
        }
        if (orderDetails.getOrderDate() != null) {
            order.setOrderDate(orderDetails.getOrderDate());
        }
        if (orderDetails.getStatus() != null) {
            order.setStatus(orderDetails.getStatus());
        }
        
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Integer orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }
    
    public long countOrdersByUser(User user) {
        return orderRepository.countByUser(user);
    }
    
    public long countOrdersByUserId(Integer userId) {
        return orderRepository.countByUserUserId(userId);
    }
    
    public List<Object[]> getOrdersWithTotalAmount() {
        return orderRepository.findOrdersWithTotalAmount();
    }
    
    public Double calculateOrderTotal(Integer orderId) {
        List<OrderDetail> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId);
        return orderDetails.stream()
                .mapToDouble(od -> od.getPrice().doubleValue() * od.getQuantity())
                .sum();
    }
    
    public OrderStatistics getOrderStatisticsForUser(Integer userId) {
        long totalOrders = countOrdersByUserId(userId);
        List<Order> recentOrders = getUserOrdersRecent(userId);
        
        double totalSpent = 0.0;
        for (Order order : recentOrders) {
            totalSpent += calculateOrderTotal(order.getOrderId());
        }
        
        return new OrderStatistics(totalOrders, totalSpent, recentOrders.size());
    }
    
    public static class OrderStatistics {
        private long totalOrders;
        private double totalSpent;
        private int recentOrdersCount;
        
        public OrderStatistics(long totalOrders, double totalSpent, int recentOrdersCount) {
            this.totalOrders = totalOrders;
            this.totalSpent = totalSpent;
            this.recentOrdersCount = recentOrdersCount;
        }
        
        public long getTotalOrders() { return totalOrders; }
        public double getTotalSpent() { return totalSpent; }
        public int getRecentOrdersCount() { return recentOrdersCount; }
    }
    
    public List<Order> getRecentOrdersByUserId(Integer userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findRecentOrdersByUserId(userId, pageable);
    }
    
    public double getTotalSpentByUserId(Integer userId) {
        return orderRepository.getTotalSpentByUserId(userId);
    }
    
    public long getTotalOrders() {
        return orderRepository.count();
    }
    
    public double getTotalRevenue() {
        try {
            List<Order> orders = orderRepository.findAll();
            return orders.stream()
                    .mapToDouble(order -> {
                        return order.getOrderDetails().stream()
                                .mapToDouble(detail -> detail.getPrice().doubleValue() * detail.getQuantity())
                                .sum();
                    })
                    .sum();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public List<Order> getRecentOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderRepository.findRecentOrdersForAdmin(pageable);
    }
}