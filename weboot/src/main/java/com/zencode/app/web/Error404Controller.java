import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;


@Controller
public class Error404Controller implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            model.addAttribute("status", status);
            return "error";
        //Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        //if (status != null) {
        //    int statusCode = Integer.parseInt(status.toString());

        //    if (statusCode == HttpStatus.NOT_FOUND.value()) {
        //        return "error-not-found-page"; // Thymeleaf or JSP view name
        //    } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        //        return "error-internal-error-page";
        //    }
        //}
        //return "generic-error-page";
    }
}

