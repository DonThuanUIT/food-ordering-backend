package com.foodorderingapp.backend.modules.cart;

import com.foodorderingapp.backend.modules.cart.dto.request.CartRequest;
import com.foodorderingapp.backend.modules.cart.dto.request.UpdateCartQuantityRequest;
import com.foodorderingapp.backend.modules.cart.dto.response.CartResponse;
import com.foodorderingapp.backend.modules.cart.CartService;
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
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @PathVariable java.util.UUID cartItemId,
            Principal principal
    ){
        cartService.deleteCartItem(cartItemId, principal.getName());
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/shops/{shopId}")
    public ResponseEntity<Void> clearShopCart(
            @PathVariable java.util.UUID shopId,
            Principal principal
    ) {
        cartService.clearShopCart(shopId, principal.getName());
        return ResponseEntity.noContent().build();
    }


}
