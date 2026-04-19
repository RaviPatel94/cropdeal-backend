package com.cropdeal.paymentservice.service;
 
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
 
import java.math.BigDecimal;
 
@Service
public class StripeService implements StripeServiceI {
 
    @Value("${stripe.success.url}")
    private String successUrl;
 
    @Value("${stripe.cancel.url}")
    private String cancelUrl;
 
    @Override
    public StripePaymentResult createCheckoutSession(Long dealId, BigDecimal amount, String currency) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?dealId=" + dealId + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl + "?dealId=" + dealId)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(amount.multiply(new BigDecimal("100")).longValue())
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("CropDeal Payment - Deal #" + dealId)
                                        .build())
                                .build())
                        .build())
                .build();
 
        Session session = Session.create(params);
 
        return new StripePaymentResult() {
            @Override public String getStatus() { return session.getStatus(); }
            @Override public String getId() { return session.getId(); }
            @Override public String getPaymentUrl() { return session.getUrl(); }
        };
    }
}