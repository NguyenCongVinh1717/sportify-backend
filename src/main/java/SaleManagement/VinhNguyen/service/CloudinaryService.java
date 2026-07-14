package SaleManagement.VinhNguyen.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Tải file lên Cloudinary và trả về tên file sạch dạng "ten_file.đuôi"
     */
    public String uploadImage(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "products", // Đưa ảnh vào thư mục "products" trên Cloudinary
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            // Lấy public_id (Ví dụ trả về: "products/1711245_cvrong")
            String publicId = (String) uploadResult.get("public_id");
            String format = (String) uploadResult.get("format");

            // Trích xuất phần tên file cuối (Ví dụ: "1711245_cvrong.jpg") để tương thích với DB hiện tại của bạn
            return publicId.substring(publicId.lastIndexOf("/") + 1) + "." + format;
        } catch (IOException e) {
            throw new RuntimeException("Upload file lên Cloudinary thất bại: " + e.getMessage());
        }
    }

    /**
     * Nhận tên file từ Database và tiến hành xóa trực tiếp trên Cloudinary
     */
    public void deleteImage(String urlOrFileName) {
        if (urlOrFileName == null || urlOrFileName.isEmpty()) return;

        // Trích xuất lấy tên file thuần túy đề phòng url truyền vào chứa đường dẫn
        String fileName = urlOrFileName.contains("/")
                ? urlOrFileName.substring(urlOrFileName.lastIndexOf("/") + 1)
                : urlOrFileName;

        // Cloudinary yêu cầu định danh xóa (public_id) không được chứa đuôi định dạng (.jpg/.png)
        String cleanName = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf("."))
                : fileName;

        // Đường dẫn đầy đủ của ảnh trên Cloud (vì ta lưu trong thư mục "products")
        String publicId = "products/" + cleanName;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println(">>> Đã xóa file thành công trên Cloudinary: " + publicId);
        } catch (IOException e) {
            System.err.println(">>> Xóa file Cloudinary thất bại: " + e.getMessage());
        }
    }
}