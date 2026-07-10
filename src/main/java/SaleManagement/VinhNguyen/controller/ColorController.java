package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.ColorRequest;
import SaleManagement.VinhNguyen.response.ColorResponse;
import SaleManagement.VinhNguyen.service.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colors")
@RequiredArgsConstructor
public class ColorController {

    @Autowired
    private ColorService colorService;

    @GetMapping
    public List<ColorResponse> getAll(){
        return colorService.getAll();
    }
    @GetMapping("/{id}")
    public ColorResponse getById(@PathVariable Long id) {
        return colorService.getById(id);
    }

    @PostMapping
    public ColorResponse create(
            @RequestBody @Valid ColorRequest request
    ){
        return colorService.create(request);
    }

    @PutMapping("/{id}")
    public ColorResponse update(
            @PathVariable Long id,
            @RequestBody @Valid ColorRequest request
    ){
        return colorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id){
        colorService.delete(id);
        return "Delete successfully";
    }
}