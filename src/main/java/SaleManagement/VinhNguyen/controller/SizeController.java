package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.SizeRequest;
import SaleManagement.VinhNguyen.response.ColorResponse;
import SaleManagement.VinhNguyen.response.SizeResponse;
import SaleManagement.VinhNguyen.service.SizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
public class SizeController {

    @Autowired
    private SizeService sizeService;

    @GetMapping
    public List<SizeResponse> getAll(){
        return sizeService.getAll();
    }
    @GetMapping("/{id}")
    public SizeResponse getById(@PathVariable Long id) {
        return sizeService.getById(id);
    }

    @PostMapping
    public SizeResponse create(
            @RequestBody @Valid SizeRequest request
    ){
        return sizeService.create(request);
    }

    @PutMapping("/{id}")
    public SizeResponse update(
            @PathVariable Long id,
            @RequestBody @Valid SizeRequest request
    ){
        return sizeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id){
        sizeService.delete(id);
        return "Delete successfully";
    }
}