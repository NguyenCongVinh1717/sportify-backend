package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Color;
import SaleManagement.VinhNguyen.request.ColorRequest;
import SaleManagement.VinhNguyen.response.ColorResponse;

public class ColorMapper {

    public static Color toEntity(ColorRequest request) {
        return Color.builder()
                .colorCode(request.getColorCode())
                .colorName(request.getColorName())
                .build();
    }

    public static ColorResponse toResponse(Color color) {
        return ColorResponse.builder()
                .id(color.getId())
                .colorCode(color.getColorCode())
                .colorName(color.getColorName())
                .build();
    }
}