package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Size;
import SaleManagement.VinhNguyen.request.SizeRequest;
import SaleManagement.VinhNguyen.response.SizeResponse;

public class SizeMapper {

    public static Size toEntity(SizeRequest request) {
        return Size.builder()
                .sizeCode(request.getSizeCode())
                .sizeName(request.getSizeName())
                .build();
    }

    public static SizeResponse toResponse(Size size) {
        return SizeResponse.builder()
                .id(size.getId())
                .sizeCode(size.getSizeCode())
                .sizeName(size.getSizeName())
                .build();
    }
}