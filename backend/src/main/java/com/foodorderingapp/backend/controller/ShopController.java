package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import com.foodorderingapp.backend.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/vendor/shops")
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

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getVendorShops(Principal principal) {

        String ownerPhone = principal.getName();

        List<ShopResponse> responses = shopService.getVendorShops(ownerPhone);

        return ResponseEntity.ok(responses);
    }
}
