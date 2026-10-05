package SaleManagement.VinhNguyen.service.payment;

import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    // Spring tự động tiêm tất cả Bean triển khai PaymentStrategy vào List này
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getPaymentMethodName().toUpperCase(),
                        Function.identity()
                ));
    }

    public PaymentStrategy getStrategy(String paymentMethod) {
        PaymentStrategy strategy = strategies.get(paymentMethod.toUpperCase());
        if (strategy == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        return strategy;
    }
}