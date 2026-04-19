package com.foodorderingapp.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component // Biến class này thành 1 Bean để Spring quản lý
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Anh bảo vệ nhìn vào Header của Request để tìm cái thẻ tên là "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String phone;

        // 2. Nếu khách không mang thẻ, hoặc thẻ không đúng chuẩn "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Cho đi tiếp (nhưng lát nữa tới cửa phòng ban, hệ thống sẽ đá văng ra vì không có dấu mộc)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Khách có mang thẻ: Cắt bỏ chữ "Bearer " (7 ký tự đầu) để lấy mã Token thật
        jwt = authHeader.substring(7);

        try {
            // 4. Cho thẻ vào "Máy đọc" để lấy ra số điện thoại
            phone = jwtUtil.extractUsername(jwt);

            // 5. Nếu đọc được số điện thoại VÀ người này chưa được xác thực trong phiên làm việc hiện tại
            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Nhờ "Người tra cứu" lấy hồ sơ của khách từ Database lên
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(phone);

                // 6. Đưa thẻ vào "Máy quét" xem thẻ có khớp với hồ sơ không, có hết hạn không
                if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {

                    // 7. THẺ XỊN! Tạo một tờ "Giấy thông hành" đóng mộc đỏ (UsernamePasswordAuthenticationToken)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Ghi chú thêm khách này đến từ IP nào, dùng trình duyệt gì...
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // LƯU VÀO SỔ TRỰC BAN (SecurityContextHolder): Để các Controller biết khách này đã được duyệt
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Đã cấp quyền đi lại cho user: {}", phone);
                }
            }
        } catch (Exception e) {
            log.warn("Thẻ JWT có vấn đề hoặc bị giả mạo: {}", e.getMessage());
        }

        // 8. Làm thủ tục xong, mời khách đi tiếp vào bên trong Tòa nhà
        filterChain.doFilter(request, response);
    }
}