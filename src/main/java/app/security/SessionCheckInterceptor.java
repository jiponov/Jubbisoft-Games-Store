package app.security;

import app.user.model.*;
import app.user.service.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.*;

import java.util.*;


// class SessionCheckInterceptor  свързан с  class WebMvcConfiguration
@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    private final Set<String> UNAUTHENTICATED_ENDPOINTS = Set.of("/", "/login", "/register", "/error");
    private final Set<String> ADMIN_ENDPOINTS = Set.of("/users", "/reports");

    private final UserService userService;


    @Autowired
    public SessionCheckInterceptor(UserService userService) {
        this.userService = userService;
    }


    // Този метод ще се изпълни преди всяка заявка
    // HttpServletRequest request - заявката, която се праща към нашето приложение
    // HttpServletResponse response - отговор, който връщаме
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Endpoint до който ще се изпрати
        String endpoint = request.getServletPath();

        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            // Ако иска да достъпи ендпойнт, за който не ни трябва сесия, пускаме заявката напред да се обработи
            return true;     // ПУСКАЙ го напред към зададената страница, метода ВРЪЩА TRUE
        }

        // request.getSession() - вземам сесията, ако няма се създава нова!!!   това е стандартното поведение все едно имаме HttpSession session
        // request.getSession(false) - вземам сесията, ако има, ако пък няма се връща null!!!  НЕ създавай НОВА СЕСИЯ АКО НЯМА ТАКАВА
        HttpSession currentUserSession = request.getSession(false);

        // РЕДИРЕКТВА към LOGIN понеже НЯМАМЕ СЕСИЯ а искаме да достъпим например /home
        if (currentUserSession == null) {
            response.sendRedirect("/login");     // редиректва ни към login а не към например /home щото нямаме сесия.
            return false;       // НЕ  го пускай НАПРЕД, ВРЪЩАМЕ FALSE
        }

        UUID userId = (UUID) currentUserSession.getAttribute("user_id");
        User user = userService.getById(userId);

        if (!user.isActive()) {

            currentUserSession.invalidate();
            response.sendRedirect("/");
            return false;    // НЕ го пускай напред USERA ако е със STATUS INACTIVE; прати го на "/" начална стр.
        }

        // НАЧИН 1:
        if (ADMIN_ENDPOINTS.contains(endpoint) && user.getRole() != UserRole.ADMIN) {

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access denied, you don't have the necessary permissions!");
            return false;
        }

        return true;
    }

}


// НАЧИН 2:  some errors CANNOT CAST (handler method) - RUN console.....
//        HandlerMethod handlerMethod = (HandlerMethod) handler;
//        if (handlerMethod.hasMethodAnnotation(RequireAdminRole.class) && user.getRole() != UserRole.ADMIN) {
//
//            response.setStatus(HttpStatus.FORBIDDEN.value());
//            response.getWriter().write("Access denied, you don't have the necessary permissions!");
//            return false;
//        }

//        return true;