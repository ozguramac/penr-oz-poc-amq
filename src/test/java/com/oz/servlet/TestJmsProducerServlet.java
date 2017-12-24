package com.oz.servlet;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RunWith(MockitoJUnitRunner.class)
public class TestJmsProducerServlet {
	
	@Mock(name = "jmsConnFactory")
	private ConnectionFactory mockJmsConnFactory;
	@InjectMocks
	private JmsProducerServlet servlet;
	
	@Mock
	private HttpServletRequest mockReq;
	@Mock
	private HttpServletResponse mockResp;

	@Mock
	private Connection mockJmsConn;
	@Mock
	private Session mockJmsSession;
	@Mock
	private MessageProducer mockProducer;

	@Rule 
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public final void beforeTest() throws Exception {
		Mockito.when(mockJmsConnFactory.createConnection()).thenReturn(mockJmsConn);
		Mockito.when(mockJmsConn.createSession(Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(mockJmsSession);
		Mockito.when(mockJmsSession.createProducer(Mockito.any(Destination.class))).thenReturn(mockProducer);
	}

	@Test
	public final void testDoGoodPost() throws Exception {
		final String msg = "TESTING!!!";
		final JsonObject jsonReq = new JsonObject();
		jsonReq.addProperty("mode", "PUBLISH_TO_TOPIC");
		jsonReq.addProperty("msg", msg);

		Mockito.when(mockReq.getReader()).thenReturn(new BufferedReader(new StringReader(
			new Gson().toJson(jsonReq)
		)));

		final TextMessage mockTxtMsg = Mockito.mock(TextMessage.class);
		Mockito.when(mockJmsSession.createTextMessage(Mockito.eq(msg))).thenReturn(mockTxtMsg);

		final StringWriter out = new StringWriter();
		Mockito.when(mockResp.getWriter()).thenReturn(new PrintWriter(out));

		servlet.doPost(mockReq, mockResp);

		Mockito.verify(mockProducer).send(mockTxtMsg);
		Assert.assertEquals("Success message", JmsProducerServlet.SUCCESS_MSG+'\n', out.toString());
	}

	@Test
	public final void testDoBadPost() throws Exception {
		final JsonObject jsonReq = new JsonObject();
		jsonReq.addProperty("mode", "UNKNOWN");

		Mockito.when(mockReq.getReader()).thenReturn(new BufferedReader(new StringReader(
			new Gson().toJson(jsonReq)
		)));

		final StringWriter out = new StringWriter();
		Mockito.when(mockResp.getWriter()).thenReturn(new PrintWriter(out));

		servlet.doPost(mockReq, mockResp);

		Assert.assertEquals("Wrong usage message", JmsProducerServlet.WRONG_USAGE_ERR_MSG+'\n', out.toString());
	}

	@Test
	public final void testCommErrorOnStartPost() throws Exception {
		final String commErrRsn = "Testing JMS Comm Error";
		final JMSException jmsException = new JMSException(commErrRsn);
		Mockito.doThrow(jmsException).when(mockJmsConn).start();

		thrown.expect(ServletException.class);
		thrown.expectMessage(JmsProducerServlet.JMS_COMM_ERR_MSG);
		thrown.expectCause(Is.is(jmsException));

		servlet.doPost(mockReq, mockResp);
	}
}
