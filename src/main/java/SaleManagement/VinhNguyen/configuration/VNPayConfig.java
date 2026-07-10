package SaleManagement.VinhNguyen.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
public class VNPayConfig {
    @Value("${vnpay.tmnCode}")
    public String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    public String vnp_HashSecret;

    @Value("${vnpay.baseUrl}")
    public String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    public String vnp_ReturnUrl;

    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    public boolean verifySignature(Map<String, String> fields, String signValue) {

        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        try {

            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {

                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {

                    hashData.append(
                            URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())
                    );

                    hashData.append("=");

                    hashData.append(
                            URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())
                    );

                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                }
            }

            String checkSign =
                    hmacSHA512(vnp_HashSecret, hashData.toString());

            System.out.println("===== VERIFY =====");
            System.out.println("HashData = " + hashData);
            System.out.println("Sign from VNPAY = " + signValue);
            System.out.println("Sign generated = " + checkSign);
            System.out.println("==================");

            return checkSign.equalsIgnoreCase(signValue);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getIpAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}