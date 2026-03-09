package com.shopnest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shopnest.entities.Product;
import com.shopnest.repositories.ProductCategoryRepositorty;
import com.shopnest.repositories.ProductRepository;
import com.shopnest.service.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProductRepository productRepo;
    private final ProductCategoryRepositorty categoryRepo;

    public AdminController(AdminService adminService,
                           ProductRepository productRepo,
                           ProductCategoryRepositorty categoryRepo) {
        this.adminService = adminService;
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    // -------- ADMIN LOGIN --------
    @GetMapping("/login")
    public String adminLoginPage() {
        return "adminLogin";
    }

    @PostMapping("/login")
    public String processAdminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        boolean authenticated = adminService.authenticate(username, password);

        if (authenticated) {
            session.setAttribute("admin", username);
            return "redirect:/admin/admindashboard";
        }

        model.addAttribute("error", true);
        return "adminLogin";
    }

    // -------- ADMIN DASHBOARD --------
    @GetMapping("/admindashboard")
    public String adminDashboard(HttpSession session, Model model) {

        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        // ✅ LOAD DATA FOR VIEW
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("categories", categoryRepo.findAll());

        return "adminDashboard";
    }

    // -------- ADMIN LOGOUT --------
    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
    
    @GetMapping("/category/{id}")
    public String adminCategory(@PathVariable Long id,
                                HttpSession session,
                                Model model) {

        if (session.getAttribute("admin") == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("products",
                productRepo.findAll().stream()
                    .filter(p -> p.getCategory().getId().equals(id))
                    .toList());

            model.addAttribute("categories", categoryRepo.findAll());

        return "admindashboard";
    }
    
 // ================= ADD PRODUCT PAGE =================
    @GetMapping("/products/add")
    public String addProductPage(Model model) {

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepo.findAll());

        return "addProduct";
    }

    // ================= SAVE PRODUCT =================
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product) {

        productRepo.save(product);

        return "redirect:/admin/admindashboard";
    }

    // ================= EDIT PRODUCT =================
    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {

        Product product = productRepo.findById(id).orElse(null);

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepo.findAll());

        return "editProduct";
    }

    // ================= UPDATE PRODUCT =================
    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute Product product) {

        productRepo.save(product);

        return "redirect:/admin/admindashboard";
    }

    // ================= DELETE PRODUCT =================
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {

        Product product = productRepo.findById(id).orElse(null);

        if(product != null){
            product.setActive(false);
            productRepo.save(product);
        }

        return "redirect:/admin/admindashboard";
    }
    
    
}
