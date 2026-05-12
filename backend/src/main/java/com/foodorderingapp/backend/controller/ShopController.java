package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import com.foodorderingapp.backend.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ShopResponse> createShop(
            @Valid @RequestBody ShopCreateRequest request,
            Principal principal
    ) {
        String ownerPhone = principal.getName();

        ShopResponse response = shopService.createShop(request, ownerPhone);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/vendor")
    public ResponseEntity<List<ShopResponse>> getVendorShops(Principal principal) {

        String ownerPhone = principal.getName();

        List<ShopResponse> responses = shopService.getVendorShops(ownerPhone);

        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<ShopResponse>> getShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(shopService.getAllShops(pageable, keyword));
    }
}
