package com.computershop.main.services;

import com.computershop.main.entities.Order;
import com.computershop.main.entities.PaymentTransaction;
import com.computershop.main.entities.User;
import com.computershop.main.repositories.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MomoPaymentService {

    @Value("${payment.momo.receiver-phone}")
    private String receiverPhone;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private QrCodeService qrCodeService;

    public Optional<PaymentTransaction> getLatestMomoTransactionForOrder(Integer orderId) {
        return paymentTransactionRepository.findTopByOrderOrderIdAndProviderOrderByCreatedAtDesc(
                orderId, PaymentTransaction.PROVIDER_MOMO
        );
    }

    @Transactional
    public PaymentTransaction createMomoTransaction(Order order, User user, BigDecimal amount, String note) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setProvider(PaymentTransaction.PROVIDER_MOMO);
        tx.setStatus(PaymentTransaction.STATUS_PENDING);
        tx.setReceiverAccount(receiverPhone);
        tx.setAmount(amount);
        tx.setOrder(order);
        tx.setUser(user);
        tx.setNote(note);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setUpdatedAt(LocalDateTime.now());

        String paymentCode = buildMomoDeepLink(receiverPhone, amount, "ORDER_" + order.getOrderId());
        tx.setPaymentCode(paymentCode);

        return paymentTransactionRepository.save(tx);
    }

    @Transactional
    public PaymentTransaction markSuccess(Long txId) {
        PaymentTransaction tx = paymentTransactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found: " + txId));
        tx.setStatus(PaymentTransaction.STATUS_SUCCESS);
        return paymentTransactionRepository.save(tx);
    }

    @Transactional
    public PaymentTransaction markFailed(Long txId) {
        PaymentTransaction tx = paymentTransactionRepository.findById(txId)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found: " + txId));
        tx.setStatus(PaymentTransaction.STATUS_FAILED);
        return paymentTransactionRepository.save(tx);
    }

    public String getQrDataUriForTransaction(PaymentTransaction tx) {
        return qrCodeService.toPngDataUri(tx.getPaymentCode(), 280);
    }

    /**
     * NOTE: This builds an unofficial MoMo "pay-to-phone" deep link.
     * It usually works when scanned/opened on a phone with MoMo installed, but is not a guaranteed official API.
     * Later you can replace this with MoMo's official merchant gateway callback flow.
     */
    private String buildMomoDeepLink(String phone, BigDecimal amountVnd, String comment) {
        // MoMo typically expects integer VND amount (no decimals)
        String amount = amountVnd.setScale(0, RoundingMode.HALF_UP).toPlainString();
        String encComment = URLEncoder.encode(comment == null ? "" : comment, StandardCharsets.UTF_8);
        return "momo://?action=payWithApp&phone=" + phone + "&amount=" + amount + "&comment=" + encComment;
    }
}

