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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService {

    @Autowired
    private SizeRepository sizeRepository;

    public List<SizeResponse> getAll() {
        return sizeRepository.findAll()
                .stream()
                .map(SizeMapper::toResponse)
                .toList();
    }

    public SizeResponse getById(Long id){
        Size size=sizeRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));
        return SizeMapper.toResponse(size);
    }

    public SizeResponse create(SizeRequest request) {

        if(sizeRepository.existsBySizeCode(request.getSizeCode())){
            throw new AppException(ErrorCode.SIZE_EXISTED);
        }

        Size size = SizeMapper.toEntity(request);

        return SizeMapper.toResponse(
                sizeRepository.save(size)
        );
    }

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

    public void delete(Long id){

        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        sizeRepository.delete(size);
    }
}