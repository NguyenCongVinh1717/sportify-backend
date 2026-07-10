package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.BrandRequest;
import SaleManagement.VinhNguyen.response.BrandResponse;
import SaleManagement.VinhNguyen.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
public class BrandController {
    @Autowired
    private BrandService brandService;
    @GetMapping
    public List<BrandResponse> getAllBrands(){
        return brandService.getAllBrands();
    }
    @GetMapping("/{id}")
    public BrandResponse getBrandById(@PathVariable Long id){
        return brandService.getBrandById(id);
    }
    @PostMapping
    public BrandResponse createBrand(@RequestBody BrandRequest brandRequest){
        return brandService.createBrand(brandRequest);
    }
    @PutMapping("/{id}")
    public BrandResponse updateBrand(@PathVariable Long id,@RequestBody BrandRequest brandRequest){
        return brandService.updateBrand(id,brandRequest);
    }
    @DeleteMapping("/{id}")
    public String deleteBrand(@PathVariable Long id){
        brandService.deleteBrand(id);
        return "Deleted successfully";
    }
}
