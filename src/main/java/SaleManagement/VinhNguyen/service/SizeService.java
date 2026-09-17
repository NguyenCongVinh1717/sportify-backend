package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Size;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.SizeMapper;
import SaleManagement.VinhNguyen.repository.SizeRepository;
import SaleManagement.VinhNguyen.request.SizeRequest;
import SaleManagement.VinhNguyen.response.SizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService {

    @Autowired
    private SizeRepository sizeRepository;

    // Cache toàn bộ danh sách Size
    @Cacheable(value = "sizes_all")
    public List<SizeResponse> getAll() {
        return sizeRepository.findAll()
                .stream()
                .map(SizeMapper::toResponse)
                .toList();
    }

    // Cache chi tiết từng Size theo ID
    @Cacheable(value = "size_detail", key = "#id")
    public SizeResponse getById(Long id){
        Size size = sizeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));
        return SizeMapper.toResponse(size);
    }

    // Xóa cache danh sách khi THÊM MỚI Size
    @CacheEvict(value = {"sizes_all", "products_filter", "products_page", "products_all", "products_by_brand_page", "products_by_brand", "products_search", "products_related", "product_detail"}, allEntries = true)
    public SizeResponse create(SizeRequest request) {

        if(sizeRepository.existsBySizeCode(request.getSizeCode())){
            throw new AppException(ErrorCode.SIZE_EXISTED);
        }

        Size size = SizeMapper.toEntity(request);

        return SizeMapper.toResponse(
                sizeRepository.save(size)
        );
    }

    // Xóa cả cache danh sách VÀ cache chi tiết khi CẬP NHẬT Size
    @CacheEvict(value = {"sizes_all", "size_detail", "products_filter", "products_page", "products_all", "products_by_brand_page", "products_by_brand", "products_search", "products_related", "product_detail"}, allEntries = true)
    @Transactional
    public SizeResponse update(Long id, SizeRequest request){

        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));
        if(!request.getSizeCode().equalsIgnoreCase(size.getSizeCode())){
            if(sizeRepository.existsBySizeCode(request.getSizeCode()))
                throw new AppException(ErrorCode.SIZE_EXISTED);
            size.setSizeCode(request.getSizeCode());
        }
        size.setSizeName(request.getSizeName());

        return SizeMapper.toResponse(size);
    }

    // Xóa sạch mọi cache liên quan khi XÓA Size
    @CacheEvict(value = {"sizes_all", "size_detail", "products_filter", "products_page", "products_all", "products_by_brand_page", "products_by_brand", "products_search", "products_related", "product_detail"}, allEntries = true)
    public void delete(Long id){

        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        sizeRepository.delete(size);
    }
}