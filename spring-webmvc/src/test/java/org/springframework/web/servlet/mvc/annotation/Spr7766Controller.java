package org.springframework.web.servlet.mvc.annotation;

import java.awt.Color;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.context.request.ServletWebRequest;

@Controller
@SessionAttributes("csrf")
public class Spr7766Controller {

    @InitBinder
    public void initBinder(WebDataBinder binder, HttpServletRequest request) {
        String csrfToken = (String) request.getSession().getAttribute("CSRF_TOKEN");
        if (csrfToken == null) {
            csrfToken = java.util.UUID.randomUUID().toString();
            request.getSession().setAttribute("CSRF_TOKEN", csrfToken);
        }
        binder.setDisallowedFields("csrfToken");
    }

    @ModelAttribute("csrf")
    public String getCsrfToken(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("CSRF_TOKEN");
    }

    @RequestMapping(value = "/colors", method = RequestMethod.POST)
    public @ResponseBody void handler(@RequestParam List<Color> colors, @RequestParam("csrfToken") String csrfToken, HttpServletRequest request) {
        String sessionCsrfToken = (String) request.getSession().getAttribute("CSRF_TOKEN");
        Assert.isTrue(csrfToken.equals(sessionCsrfToken), "CSRF token does not match.");
        Assert.isTrue(colors.size() == 2);
        Assert.isTrue(colors.get(0).equals(Color.WHITE));
        Assert.isTrue(colors.get(1).equals(Color.BLACK));
    }
}
