package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Brand;
import SaleManagement.VinhNguyen.request.BrandRequest;
import SaleManagement.VinhNguyen.response.BrandResponse;

public class BrandMapper {
    public static Brand toEntity(BrandRequest brandRequest){
        return Brand.builder().brandCode(brandRequest.getBrandCode())
                .brandName(brandRequest.getBrandName()).build();
    }
    public static BrandResponse toResponse(Brand brand){
        return BrandResponse.builder().id(brand.getId())
                .brandCode(brand.getBrandCode())
                .brandName(brand.getBrandName()).build();
    }
}
