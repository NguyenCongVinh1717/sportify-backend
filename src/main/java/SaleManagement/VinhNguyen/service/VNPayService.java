package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.configuration.VNPayConfig;
import SaleManagement.VinhNguyen.entity.Order;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    public String createPaymentUrl(Order order, HttpServletRequest request) {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan don hang " + order.getId();
        String vnp_OrderType = "other";
        String vnp_TxnRef = String.valueOf(order.getId());

        String vnp_IpAddr = vnPayConfig.getIpAddress(request);
        long amount = Math.round(order.getTotalPrice() * 100);

        Map<String, String> vnp_Params = new HashMap<>();

        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);


        // timezone Việt Nam
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);

        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringJoiner hashData = new StringJoiner("&");
        StringJoiner query = new StringJoiner("&");

        try {
            for (String fieldName : fieldNames) {

                String fieldValue = vnp_Params.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {

                    hashData.add(
                            URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())
                                    + "=" +
                                    URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())
                    );

                    query.add(
                            URLEncoder.encode(fieldName, StandardCharsets.UTF_8)
                                    + "=" +
                                    URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String vnp_SecureHash =
                vnPayConfig.hmacSHA512(
                        vnPayConfig.vnp_HashSecret,
                        hashData.toString()
                );

        String paymentUrl =
                vnPayConfig.vnp_PayUrl
                        + "?"
                        + query
                        + "&vnp_SecureHash="
                        + vnp_SecureHash;

        System.out.println("====== [SPORTIFY VNPAY DEBUG] ======");
        System.out.println("TmnCode = " + vnPayConfig.vnp_TmnCode);
        System.out.println("HashSecret = " + vnPayConfig.vnp_HashSecret);
        System.out.println("HashData = " + hashData);
        System.out.println("Hash = " + vnp_SecureHash);
        System.out.println("PaymentURL = " + paymentUrl);
        System.out.println("====================================");

        return paymentUrl;
    }
}