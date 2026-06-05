package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.CartRequest;
import com.foodorderingapp.backend.dto.request.UpdateCartQuantityRequest;
import com.foodorderingapp.backend.dto.response.CartResponse;
import com.foodorderingapp.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @PostMapping("/items")
    public ResponseEntity<Void> addToCart(@Valid @RequestBody CartRequest request, Principal principal) {
        cartService.addToCart(request.foodId(), request.quantity(), request.note(), principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }
    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItemQuantity(
            @PathVariable java.util.UUID cartItemId,
            @Valid @RequestBody UpdateCartQuantityRequest request,
            Principal principal
    ) {

        cartService.updateCartItemQuantity(cartItemId, request.quantity(), principal.getName());
        return ResponseEntity.ok().build();
    }


}
