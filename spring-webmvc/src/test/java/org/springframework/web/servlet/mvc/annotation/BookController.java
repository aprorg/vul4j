/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.mvc.annotation;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Used for testing the combination of ControllerClassNameHandlerMapping/SimpleUrlHandlerMapping with @RequestParam in
 * {@link ServletAnnotationControllerTests}. Implemented as a top-level class (rather than an inner class) to make the
 * ControllerClassNameHandlerMapping work.
 *
 * @author Arjen Poutsma
 */
@Controller
@SessionAttributes("csrfToken")
public class BookController {

    @ModelAttribute("csrfToken")
    public String getCsrfToken(HttpServletRequest request) {
        RequestDataValueProcessor processor = RequestContextUtils.getRequestDataValueProcessor(request);
        return processor != null ? processor.getExtraHiddenFields(request).get("_csrf") : "";
    }

    @RequestMapping("list")
    public void list(@RequestParam("_csrf") String csrfToken, @ModelAttribute("csrfToken") String sessionCsrfToken, Writer writer, SessionStatus status) throws IOException {
        if (csrfToken.equals(sessionCsrfToken)) {
            writer.write("list");
            status.setComplete(); // Clear the session attribute after successful use
        } else {
            writer.write("CSRF token mismatch");
        }
    }

    @RequestMapping("show")
    public void show(@RequestParam(required = true) Long id, @RequestParam("_csrf") String csrfToken, @ModelAttribute("csrfToken") String sessionCsrfToken, Writer writer, SessionStatus status) throws IOException {
        if (csrfToken.equals(sessionCsrfToken)) {
            writer.write("show-id=" + id);
            status.setComplete(); // Clear the session attribute after successful use
        } else {
            writer.write("CSRF token mismatch");
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create(Writer writer) throws IOException {
        writer.write("create");
    }

}
