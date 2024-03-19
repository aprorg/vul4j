/*
 * Copyright 2002-2016 the original author or authors.
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

package org.springframework.test.web.servlet.samples.standalone;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.validation.Valid;

import org.junit.Test;

import org.springframework.stereotype.Controller;
import org.springframework.test.web.Person;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

/**
 * Tests with {@link Filter}'s.
 *
 * @author Rob Winch
 */
public class FilterTests {

	@Test
	public void whenFiltersCompleteMvcProcessesRequest() throws Exception {
		standaloneSetup(new PersonController())
			.addFilters(new ContinueFilter()).build()
			.perform(post("/persons").param("name", "Andy"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/person/1"))
				.andExpect(model().size(1))
				.andExpect(model().attributeExists("id"))
				.andExpect(flash().attributeCount(1))
				.andExpect(flash().attribute("message", "success!"));
	}

	@Test
	public void filtersProcessRequest() throws Exception {
		standaloneSetup(new PersonController())
			.addFilters(new ContinueFilter(), new RedirectFilter()).build()
			.perform(post("/persons").param("name", "Andy"))
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	public void filterMappedBySuffix() throws Exception {
		standaloneSetup(new PersonController())
			.addFilter(new RedirectFilter(), "*.html").build()
			.perform(post("/persons.html").param("name", "Andy"))
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	public void filterWithExactMapping() throws Exception {
		standaloneSetup(new PersonController())
			.addFilter(new RedirectFilter(), "/p", "/persons").build()
			.perform(post("/persons").param("name", "Andy"))
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	public void filterSkipped() throws Exception {
		standaloneSetup(new PersonController())
			.addFilter(new RedirectFilter(), "/p", "/person").build()
			.perform(post("/persons").param("name", "Andy"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/person/1"))
				.andExpect(model().size(1))
				.andExpect(model().attributeExists("id"))
				.andExpect(flash().attributeCount(1))
				.andExpect(flash().attribute("message", "success!"));
	}

	@Test
	public void filterWrapsRequestResponse() throws Exception {
		standaloneSetup(new PersonController())
			.addFilters(new WrappingRequestResponseFilter()).build()
			.perform(post("/user"))
				.andExpect(model().attribute("principal", WrappingRequestResponseFilter.PRINCIPAL_NAME));
	}


	@Controller
	private static class PersonController {
	
		@Autowired
		private CsrfTokenRepository csrfTokenRepository;

		@RequestMapping(value="/persons", method=RequestMethod.POST)
		public String save(@Valid Person person, Errors errors, RedirectAttributes redirectAttrs, HttpServletRequest request) {
			if (errors.hasErrors()) {
				return "person/add";
			}
			CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
			if (csrfToken != null) {
				redirectAttrs.addAttribute(csrfToken.getParameterName(), csrfToken.getToken());
			}
			redirectAttrs.addAttribute("id", "1");
			redirectAttrs.addFlashAttribute("message", "success!");
			return "redirect:/person/{id}";
		}

		@RequestMapping(value="/user")
		public ModelAndView user(Principal principal, HttpServletRequest request) {
			ModelAndView modelAndView = new ModelAndView("user/view", "principal", principal.getName());
			CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
			if (csrfToken != null) {
				modelAndView.addObject(csrfToken.getParameterName(), csrfToken.getToken());
			}
			return modelAndView;
		}

		@RequestMapping(value="/forward")
		public String forward(HttpServletRequest request) {
			CsrfToken csrfToken = csrfTokenRepository.loadToken(request);
			if (csrfToken != null) {
				request.setAttribute(csrfToken.getParameterName(), csrfToken.getToken());
			}
			return "forward:/persons";
		}
	}

	private class ContinueFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

			filterChain.doFilter(request, response);
		}
	}

	private static class WrappingRequestResponseFilter extends OncePerRequestFilter {

		public static final String PRINCIPAL_NAME = "WrapRequestResponseFilterPrincipal";

		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

			filterChain.doFilter(new HttpServletRequestWrapper(request) {
				@Override
				public Principal getUserPrincipal() {
					return new Principal() {
						@Override
						public String getName() {
							return PRINCIPAL_NAME;
						}
					};
				}
			}, new HttpServletResponseWrapper(response));
		}
	}

	private class RedirectFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

			response.sendRedirect("/login");
		}
	}
}
