package be.hikage.maven.plugin.xmlmerge.utils;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;


/**
 * Created by IntelliJ IDEA. User: hikage Date: 16/01/12 Time: 12:48 To change
 * this template use File | Settings | File Templates.
 */
public class Dom4JUtilsTest
{

	@Test
	public void testReadDocument() throws Exception
	{

		URL      xmlFile = getClass()
			.getClassLoader()
			.getResource("sample.xml");

		Document result  = Dom4JUtils.readDocument(xmlFile);
		Assert.assertNotNull(result);

	}


	@Test(
		expected = DocumentException.class
	)
	public void testReadDocument_textinprolognothandled() throws Exception
	{

		URL      xmlFile = getClass()
			.getClassLoader()
			.getResource("sample_textinprolog.xml");

		Document result  = Dom4JUtils.readDocument(xmlFile);

	}


	@Test
	public void testReadDocument_textinprologhandled() throws Exception
	{
		// === CONSTANTS ===
		var expectedUnix    = "TEXT CONTENT NOT ALLOWED IN PROLOG\nTEXT CONTENT NOT ALLOWED IN PROLOG2";
		var expectedWindows = "TEXT CONTENT NOT ALLOWED IN PROLOG\r\nTEXT CONTENT NOT ALLOWED IN PROLOG2";

		// === SETUP ===
		var xmlFile         = getClass()
			.getClassLoader()
			.getResource("sample_textinprolog.xml");

		var prolog          = new StringBuilder();
		var result          = Dom4JUtils.readDocument(xmlFile, prolog);

		// === ASSERTIONS ===
		Assert.assertNotNull(result);
		Assert.assertTrue(prolog.length() > 0);

		Assert
			.assertTrue(
				expectedUnix.equals(prolog.toString())
					|| expectedWindows.equals(prolog.toString())
			);

	}
}
