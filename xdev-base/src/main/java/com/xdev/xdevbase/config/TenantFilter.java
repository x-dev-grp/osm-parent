//package com.xdev.xdevbase.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.UUID;
//
//@Component
//public class TenantFilter extends OncePerRequestFilter {
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain)
//            throws ServletException, IOException {
//
//        try {
//
//            if(request.getHeader("X-Tenant-ID") != null) {
//                UUID tenantId = UUID.fromString(request.getHeader("X-Tenant-ID"));
//                TenantContext.setCurrentTenant(tenantId);
//            }
//            filterChain.doFilter(request, response);
//        } finally {
//            TenantContext.clear();
//        }
//    }
//}
