package com.openhtmltopdf.testcases;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.util.Charsets;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import com.openhtmltopdf.DOMBuilder;
import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder.TextDirection;
import com.openhtmltopdf.util.JDKXRLogger;
import com.openhtmltopdf.util.XRLog;
import com.openhtmltopdf.util.XRLogger;

public class TestcaseRunner {

	/**
	 * Runs our set of manual test cases. You can specify an output directory with
	 * -DOUT_DIRECTORY=./output
	 * for example. Otherwise, the current working directory is used.
	 * Test cases must be placed in src/main/resources/testcases/
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		/*
		 * Note: The RepeatedTableSample optionally requires the font file NotoSans-Regular.ttf
		 * to be placed in the resources directory. 
		 * 
		 * This sample demonstrates the failing repeated table header on each page.
		 */
//		runTestCase("RepeatedTableSample");
//
//		/*
//		 * This sample demonstrates the -fs-pagebreak-min-height css property
//		 */
//		runTestCase("FSPageBreakMinHeightSample");
//		
//		runTestCase("color");
//		runTestCase("background-color");
//		runTestCase("background-image");
//		runTestCase("invalid-url-background-image");
//		runTestCase("text-align");
//		runTestCase("font-family-built-in");
		//runTestCase("form-controls");
		runTestCase("test3");

		/* Add additional test cases here. */
	}
	
	/**
	 * Will throw an exception if a SEVERE or WARNING message is logged.
	 * @param testCaseFile
	 * @throws Exception
	 */
	public static void runTestWithoutOutput(String testCaseFile) throws Exception {
		runTestWithoutOutput(testCaseFile, false);
	}
	
	/**
	 * Will silently let ALL log messages through.
	 * @param testCaseFile
	 * @throws Exception
	 */
	public static void runTestWithoutOutputAndAllowWarnings(String testCaseFile) throws Exception {
		runTestWithoutOutput(testCaseFile, true);
	}

	private static void runTestWithoutOutput(String testCaseFile, boolean allowWarnings) throws Exception {
		System.out.println("Trying to run: " + testCaseFile);
		
		byte[] htmlBytes = IOUtils.toByteArray(TestcaseRunner.class
				.getResourceAsStream("/testcases/" + testCaseFile + ".html"));
		String html = new String(htmlBytes, Charsets.UTF_8);
		OutputStream outputStream = new ByteArrayOutputStream(4096);
		
		// We wan't to throw if we get a warning or severe log message.
		final XRLogger delegate = new JDKXRLogger();
		final java.util.List<RuntimeException> warnings = new ArrayList<RuntimeException>();
		XRLog.setLoggerImpl(new XRLogger() {
			@Override
			public void setLevel(String logger, Level level) {
			}
			
			@Override
			public void log(String where, Level level, String msg, Throwable th) {
				if (level.equals(Level.WARNING) ||
					level.equals(Level.SEVERE)) {
					warnings.add(new RuntimeException(where + ": " + msg, th));
				}
				delegate.log(where, level, msg, th);
			}
			
			@Override
			public void log(String where, Level level, String msg) {
				if (level.equals(Level.WARNING) ||
					level.equals(Level.SEVERE)) {
					warnings.add(new RuntimeException(where + ": " + msg));
				}
				delegate.log(where, level, msg);
			}
		});
		
		try {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
			builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
			builder.defaultTextDirection(TextDirection.LTR);
			builder.withHtmlContent(html, TestcaseRunner.class.getResource("/testcases/").toString());
			builder.toStream(outputStream);
			builder.run();
		} finally {
			outputStream.close();
		}
		
		if (!warnings.isEmpty() && !allowWarnings) {
			throw warnings.get(0);
		}
	}
	
	private static void addFonts(PdfRendererBuilder builder){
	      File f = new File("D:/workspace/ctms-web/src/main/webapp/static/src/font");
	      if (f.isDirectory()) {
	          final File[] files = f.listFiles(new FilenameFilter() {
	              @Override
					public boolean accept(File dir, String name) {
	                  String lower = name.toLowerCase();
	                  return lower.endsWith(".otf") || lower.endsWith(".ttf") || lower.endsWith(".ttc");
	              }
	          });
	          
	          Map<String, String> map = new HashMap();
	          map.put("SIMSUN.TTC", "SimSun");
	          map.put("MSYH.TTC", "Microsoft YaHei");
	          
//	          FontResolver fontResolver = renderer.getSharedContext ().getFontResolver();
	          for (int i = 0; i < files.length; i++) {
	       	   if(!map.containsKey(files[i].getName())){
	       		   continue;
	       	   }
	       	   
	       	   builder.useFont(new FSSupplier<InputStream>(){

	      				@Override
	      				public InputStream supply() {
	      					try {
								return new FileInputStream(files[0]);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
	      					return null;
	      				}
	              }, map.get(files[i].getName()));
	          }
	      }
		}
	
	public static void runTestCase(String testCaseFile) throws Exception {
		byte[] htmlBytes = IOUtils.toByteArray(TestcaseRunner.class
				.getResourceAsStream("/testcases/" + testCaseFile + ".html"));
		String html = new String(htmlBytes, Charsets.UTF_8);
		
		URL url = TestcaseRunner.class
		.getResource("/testcases/" + testCaseFile + ".html");
		
		String outDir = System.getProperty("OUT_DIRECTORY", ".");
		String testCaseOutputFile = outDir + "/" + testCaseFile + ".pdf";
		FileOutputStream outputStream = new FileOutputStream(testCaseOutputFile);
		
		try {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			//addFonts(builder);
			builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
			builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
			builder.defaultTextDirection(TextDirection.LTR);
			
			
			org.w3c.dom.Document doc = html5ParseDocument(url, 1000);
			
			System.out.println(TestcaseRunner.class.getResource("/testcases/").toString());
			
			builder.withW3cDocument(doc, TestcaseRunner.class.getResource("/testcases/").toString());
			
			//builder.withHtmlContent(html, TestcaseRunner.class.getResource("/testcases/").toString());
			builder.toStream(outputStream);
			builder.run();
		} finally {
			outputStream.close();
		}
		System.out.println("Wrote " + testCaseOutputFile);
	}
	
	public static org.w3c.dom.Document html5ParseDocument(URL url, int timeoutMs) throws IOException 
    {
        org.jsoup.nodes.Document doc;

        if (url.getProtocol().equalsIgnoreCase("file")) {
            doc = Jsoup.parse(new File(url.getPath()), "UTF-8");
        }
        else {
            doc = Jsoup.parse(url, timeoutMs);  
        }
        
        return DOMBuilder.jsoup2DOM(doc);
    }
	
	public static String toStringFromDoc(Document document) {  
        String result = null;  
  
        if (document != null) {  
            StringWriter strWtr = new StringWriter();  
            StreamResult strResult = new StreamResult(strWtr);  
            TransformerFactory tfac = TransformerFactory.newInstance();  
            try {  
                javax.xml.transform.Transformer t = tfac.newTransformer();  
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
                t.setOutputProperty(OutputKeys.INDENT, "yes");  
                t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,  
                // text  
                t.setOutputProperty(  
                        "{http://xml.apache.org/xslt}indent-amount", "4");  
                t.transform(new DOMSource(document.getDocumentElement()),  
                        strResult);  
            } catch (Exception e) {  
                System.err.println("XML.toString(Document): " + e);  
            }  
            result = strResult.getWriter().toString();  
            try {  
                strWtr.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        return result;  
    }  
}
