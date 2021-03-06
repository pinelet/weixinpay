package weixinpay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.beust.jcommander.internal.Maps;
import com.pinelet.weixinpay.util.XMLDocumentService;
import com.pinelet.weixinpay.wxservice.ApplicationContextManager;

public class EventMsgTest {

	DocumentBuilder dp = null;
	Transformer transfer = null;
	Logger loger = LoggerFactory.getLogger(getClass());
	XPath xpath = null;
	
	
	@Before
	public void setUp() throws Exception {
		dp = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		transfer = TransformerFactory.newInstance().newTransformer();
		xpath = XPathFactory.newInstance().newXPath();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testParsexml() {
		String xml ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><xml><appid><![CDATA[wxaea199a4b0a6faf8]]></appid><attach><![CDATA[13611303594]]></attach><bank_type><![CDATA[CFT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[Y]]></is_subscribe><mch_id><![CDATA[1283996101]]></mch_id><nonce_str><![CDATA[Z55KF0tQqpYbo8hXKfEbMsmfXZIKa6Iz]]></nonce_str><openid><![CDATA[o0B3pt_3Ulxw8aJ6WOqwv0h6IxU8]]></openid><out_trade_no><![CDATA[Z55KF0tQqpYbo8hXKfEbMsmfXZIKa6Iz]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[969A5A8A05CDEA49325FB490BD0270A3]]></sign><time_end><![CDATA[20160508151034]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[4007292001201605085634162261]]></transaction_id></xml>";
		XMLDocumentService xmlservice = XMLDocumentService.getInstance();
		Document doc = xmlservice.parse(new StringBufferInputStream(xml));
		Map<String, String> map = xmlservice.parseXML("xml", doc);
		map.remove("sign");
		System.out.println(map);
	}

	/**
	 * <xml>
		<ToUserName><![CDATA[toUser]]></ToUserName>
		<FromUserName><![CDATA[FromUser]]></FromUserName>
		<CreateTime>123456789</CreateTime>
		<MsgType><![CDATA[event]]></MsgType>
		<Event><![CDATA[SCAN]]></Event>
		<EventKey><![CDATA[SCENE_VALUE]]></EventKey>
		<Ticket><![CDATA[TICKET]]></Ticket>
		</xml>
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws TransformerException 
	 */
	@Test
	public void msgEventScanTest() throws MalformedURLException, IOException, TransformerException {
		Map<String, String> map = Maps.newHashMap();
		map.put("ToUserName", "touser");
		map.put("FromUserName","formuser");
		map.put("CreateTime","1348831860");
		map.put("MsgType","event");
		map.put("Event","subscribe");
		map.put("EventKey","qrscene_123123");
		map.put("Ticket","t");
		HttpURLConnection con = (HttpURLConnection)new URL("http://localhost:9090/pay/main").openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.connect();
		OutputStream out = con.getOutputStream();
		transfer.transform(new DOMSource(map2xml(map)), new StreamResult(out));
		out.flush();
		out.close();
		
		loger.info("返回：" + con.getResponseMessage());
		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String res = null;
		while((res = reader.readLine()) != null) {
			loger.info("明细 :" + res);
		}
		con.disconnect();
	}
	
	private Document map2xml(Map<String, String> info) {
		Document doc = dp.newDocument();
		Element xml = doc.createElement("xml");
		doc.appendChild(xml);
		Element element = null;
		for(Entry<String, String> node : info.entrySet()) {
			element = doc.createElement(node.getKey());
			element.setTextContent(node.getValue());
			xml.appendChild(element);
		}
		return doc;
	}
	
	@Test
	public void msgEventSub() throws XPathExpressionException {
		Map<String, String> info = Maps.newHashMap();
		info.put("return_code", "success");
		info.put("return_msg", "message" + System.currentTimeMillis()/1000);
		System.out.print(xpath.evaluate("/xml/return_code", map2xml(info)));
	}
	
	/**
	 * 为测试发送设备操作通知到pengda
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void dotestrequesti() throws MalformedURLException, IOException {
		String notifyUrl = "http://localhost:8080/pengda/rs/pay/act";
		Map<String, String> result = new HashMap<String, String>();
		result.put("total_fee", "100");
		result.put("attach", "13611303594");
		result.put("cash_fee", "100");
		result.put("transaction_id", "1217752501201407033233368018");
		result.put("time_end", "20160430224525");
		
		//通知设备动作事件
		String total_fee = result.get("total_fee");
		String id = result.get("attach");
		String cash_fee = result.get("cash_fee");
		String wxOrderid = result.get("transaction_id");
		String time_end = result.get("time_end");
		HttpURLConnection http = (HttpURLConnection)new URL(notifyUrl).openConnection();
		http.setRequestMethod("POST");
		http.setDoOutput(true);
		http.setDoInput(true);
		http.setUseCaches(false); 
		StringBuffer params = new StringBuffer("data=");
        //data:机器ID，交易类型（1充值，2打水），流水号（10位)，金额(分)，客户信息，操作时间
        params.append(id).append(",2,")
        				 .append(System.currentTimeMillis()/1000).append(",")
        				 .append(total_fee).append(",")
        				 .append(wxOrderid).append(",")
        				 .append(time_end.substring(4));
        http.connect();
        OutputStream out = http.getOutputStream();
        out.write(params.toString().getBytes());
        out.flush();
        out.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
        String str = null;
        do {
        	in.readLine();
        	System.out.println("first in: " + str);
        }while(str != null);
        System.exit(0);
	}
	
	@Test
	public void float2int() {
		float n = 0.02f;
		System.out.println((int)n*100);
		System.out.println((int)(n*100));
	}
	@Test
	public void map2String() {
		Map<String, String> map = Maps.newHashMap();
		map.put("s1", "v1");
		map.put("s2", "v2");
		System.out.println(ApplicationContextManager.map2String(map,true));
	}

}
