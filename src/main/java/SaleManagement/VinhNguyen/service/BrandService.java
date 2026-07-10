package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Brand;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.BrandMapper;
import SaleManagement.VinhNguyen.repository.BrandRepository;
import SaleManagement.VinhNguyen.request.BrandRequest;
import SaleManagement.VinhNguyen.response.BrandResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BrandService {
    @Autowired
    private BrandRepository brandRepository;

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(BrandMapper::toResponse)
                .collect(Collectors.toList());
    }
    public BrandResponse getBrandById(Long id){
        Brand brand=brandRepository.findById(id).orElseThrow(
                () -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        return BrandMapper.toResponse(brand);
    }
    public BrandResponse createBrand(BrandRequest brandRequest){
        boolean check=brandRepository.existsByBrandCode(brandRequest.getBrandCode());
        if(check){
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }
        Brand newBrand=BrandMapper.toEntity(brandRequest);
        brandRepository.save(newBrand);
        return BrandMapper.toResponse(newBrand);
    }
    @Transactional
    public BrandResponse updateBrand(Long id,BrandRequest brandRequest){
        Brand oldBrand=brandRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXISTED));
        if(!oldBrand.getBrandCode().equals(brandRequest.getBrandCode())
                && brandRepository.existsByBrandCode(brandRequest.getBrandCode())){
            throw new AppException(ErrorCode.BRAND_EXISTED);
        }
        oldBrand.setBrandCode(brandRequest.getBrandCode());
        oldBrand.setBrandName(brandRequest.getBrandName());
        return BrandMapper.toResponse(oldBrand);
    }
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
