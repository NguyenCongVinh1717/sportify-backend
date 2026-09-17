package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Brand;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.BrandMapper;
import SaleManagement.VinhNguyen.repository.BrandRepository;
import SaleManagement.VinhNguyen.request.BrandRequest;
import SaleManagement.VinhNguyen.response.BrandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    // Cache toàn bộ danh sách Thương hiệu
    @Cacheable(value = "brands_all")
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(BrandMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Cache chi tiết Thương hiệu theo ID
    @Cacheable(value = "brand_detail", key = "#id")
    public BrandResponse getBrandById(Long id){
        Brand brand = brandRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return BrandMapper.toResponse(brand);
    }

    // Xóa cache danh sách khi THÊM MỚI Thương hiệu
    @CacheEvict(value = "brands_all", allEntries = true)
    public BrandResponse createBrand(BrandRequest brandRequest){
        boolean check = brandRepository.existsByBrandCode(brandRequest.getBrandCode());
        if(check){
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }
        Brand newBrand = BrandMapper.toEntity(brandRequest);
        brandRepository.save(newBrand);
        return BrandMapper.toResponse(newBrand);
    }

    // Xóa cả cache danh sách VÀ cache chi tiết (brand_detail) khi CẬP NHẬT Thương hiệu
    @CacheEvict(value = {"brands_all", "brand_detail"}, allEntries = true)
    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest brandRequest){
        Brand oldBrand = brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXISTED));
        if(!oldBrand.getBrandCode().equals(brandRequest.getBrandCode())
                && brandRepository.existsByBrandCode(brandRequest.getBrandCode())){
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }
        oldBrand.setBrandCode(brandRequest.getBrandCode());
        oldBrand.setBrandName(brandRequest.getBrandName());
        return BrandMapper.toResponse(oldBrand);
    }

    // Xóa sạch cache liên quan khi XÓA Thương hiệu
    @CacheEvict(value = {"brands_all", "brand_detail"}, allEntries = true)
    @Transactional
    public void deleteBrand(Long id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        if(!brand.getProducts().isEmpty()){
            throw new AppException(ErrorCode.BRAND_IN_USE);
        }
        brandRepository.delete(brand);
    }
}