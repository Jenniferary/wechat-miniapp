package com.example.demo.controller;

import com.example.demo.entity.AfternoonTeaItem;
import com.example.demo.entity.CheckoutRequest;
import com.example.demo.entity.CheckoutResponse;
import com.example.demo.repository.AfternoonTeaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/afternoon-tea")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AfternoonTeaController {

    private final AfternoonTeaRepository repository;

    public AfternoonTeaController(AfternoonTeaRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/desserts")
    public List<AfternoonTeaItem> getDesserts() {
        return repository.findDesserts();
    }

    @GetMapping("/drinks")
    public List<AfternoonTeaItem> getDrinks() {
        return repository.findDrinks();
    }

    @PostMapping("/checkout")
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        int totalPrice = request.getNumAdults() * 50 +
                request.getNumChildren() * 25 +
                request.getNumSeniors() * 25;

        return new CheckoutResponse(totalPrice);
    }
}
