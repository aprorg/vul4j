package org.springframework.web.filter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.test.MockFilterChain;
import org.springframework.mock.web.test.MockHttpServletRequest;
import org.springframework.mock.web.test.MockHttpServletResponse;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link RelativeRedirectFilter}.
 *
 * @author Rob Winch
 * @author Juergen Hoeller
 */
public class RelativeRedirectFilterTests {

	private RelativeRedirectFilter filter = new RelativeRedirectFilter();

	private HttpServletResponse response = Mockito.mock(HttpServletResponse.class);


	@Test(expected = IllegalArgumentException.class)
	public void sendRedirectHttpStatusWhenNullThenIllegalArgumentException() {
		this.filter.setRedirectStatus(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendRedirectHttpStatusWhenNot3xxThenIllegalArgumentException() {
		this.filter.setRedirectStatus(HttpStatus.OK);
	}

	@Test
	public void doFilterSendRedirectWhenDefaultsThenLocationAnd303() throws Exception {
		String location = "/foo";
		sendRedirect(location);

		InOrder inOrder = Mockito.inOrder(this.response);
		inOrder.verify(this.response).setStatus(HttpStatus.SEE_OTHER.value());
		inOrder.verify(this.response).setHeader(HttpHeaders.LOCATION, location);
	}

	@Test
	public void doFilterSendRedirectWhenCustomSendRedirectHttpStatusThenLocationAnd301() throws Exception {
		String location = "/foo";
		HttpStatus status = HttpStatus.MOVED_PERMANENTLY;
		this.filter.setRedirectStatus(status);
		sendRedirect(location);

		InOrder inOrder = Mockito.inOrder(this.response);
		inOrder.verify(this.response).setStatus(status.value());
		inOrder.verify(this.response).setHeader(HttpHeaders.LOCATION, location);
	}

	@Test
	public void wrapOnceOnly() throws Exception {
		HttpServletResponse original = new MockHttpServletResponse();

		MockFilterChain chain = new MockFilterChain();
		this.filter.doFilterInternal(new MockHttpServletRequest(), original, chain);

		HttpServletResponse wrapped1 = (HttpServletResponse) chain.getResponse();
		assertNotSame(original, wrapped1);

		chain.reset();
		this.filter.doFilterInternal(new MockHttpServletRequest(), wrapped1, chain);
		HttpServletResponse current = (HttpServletResponse) chain.getResponse();
		assertSame(wrapped1, current);

		chain.reset();
		HttpServletResponse wrapped2 = new HttpServletResponseWrapper(wrapped1);
		this.filter.doFilterInternal(new MockHttpServletRequest(), wrapped2, chain);
		current = (HttpServletResponse) chain.getResponse();
		assertSame(wrapped2, current);
	}


	private void sendRedirect(String location) throws Exception {
		if (!isUrlRelative(location)) {
			throw new IllegalArgumentException("Invalid redirect URL: " + location);
		}
		MockFilterChain chain = new MockFilterChain();
		this.filter.doFilterInternal(new MockHttpServletRequest(), this.response, chain);

		HttpServletResponse wrappedResponse = (HttpServletResponse) chain.getResponse();
		wrappedResponse.sendRedirect(location);
	}

	private boolean isUrlRelative(String location) {
		return location != null && !location.startsWith("http://") && !location.startsWith("https://");
	}

}
