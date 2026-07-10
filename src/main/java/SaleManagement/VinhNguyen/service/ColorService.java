package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Color;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.ColorMapper;
import SaleManagement.VinhNguyen.repository.ColorRepository;
import SaleManagement.VinhNguyen.request.ColorRequest;
import SaleManagement.VinhNguyen.response.ColorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorService {

    @Autowired
    private ColorRepository colorRepository;

    public List<ColorResponse> getAll() {
        return colorRepository.findAll()
                .stream()
                .map(ColorMapper::toResponse)
                .toList();
    }
    public ColorResponse getById(Long colorId){
        Color color= colorRepository.findById(colorId).orElseThrow(() -> new AppException(
                ErrorCode.COLOR_NOT_FOUND
        ));
        return ColorMapper.toResponse(color);
    }

    public ColorResponse create(ColorRequest request) {

        if(colorRepository.existsByColorCode(request.getColorCode())){
            throw new AppException(ErrorCode.COLOR_EXISTED);
        }

        Color color = ColorMapper.toEntity(request);

        return ColorMapper.toResponse(
                colorRepository.save(color)
        );
    }

    @Transactional
    public ColorResponse update(Long id, ColorRequest request){

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        if(!request.getColorCode().equalsIgnoreCase(color.getColorCode())){
            if(colorRepository.existsByColorCode(request.getColorCode())){
                throw new AppException(ErrorCode.COLOR_EXISTED);
            }
            color.setColorCode(request.getColorCode());
        }
        color.setColorName(request.getColorName());

        return ColorMapper.toResponse(color);
    }

    public void delete(Long id){

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        colorRepository.delete(color);
    }
}