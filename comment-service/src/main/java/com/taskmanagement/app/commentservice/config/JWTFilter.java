package com.taskmanagement.app.commentservice.config;
import com.taskmanagement.app.commentservice.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException; import io.jsonwebtoken.MalformedJwtException; import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.HttpServletRequest; import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException; import java.util.List;
@Component
public class JWTFilter extends OncePerRequestFilter {
    @Autowired private JWTUtil jwtUtil;
    @Override protected boolean shouldNotFilter(HttpServletRequest r) {
        String p=r.getServletPath(); return p.startsWith("/swagger-ui/")||p.startsWith("/v3/api-docs")||p.startsWith("/actuator");
    }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h=req.getHeader("Authorization");
        if(h==null||!h.startsWith("Bearer ")){ chain.doFilter(req,res); return; }
        try {
            String t=h.substring(7);
            if(jwtUtil.validateToken(t)&&SecurityContextHolder.getContext().getAuthentication()==null)
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(jwtUtil.extractUsername(t),null,
                        List.of(new SimpleGrantedAuthority("ROLE_"+jwtUtil.extractRole(t)))));
        } catch(ExpiredJwtException|MalformedJwtException|SignatureException ex) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); res.getWriter().write("Invalid or expired JWT"); return;
        }
        chain.doFilter(req,res);
    }
}
