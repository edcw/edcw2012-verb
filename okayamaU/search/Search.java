package search;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Search extends Thread{

	private static int searchCount = 0;

	private static String yahooSearchURL =
			"http://search.yahooapis.jp/PremiumWebSearchService/V1/webSearch?appid=";	// Yahoo検索API リクエストURL

	/* ***** ↓ここにアプリケーションIDを記述してください↓ ***** */
	private static String appid = ""; //  アプリケーションID

	private static String language = "&language=en";						// 言語指定パラメータ
	protected static boolean outXmlFile = false;;

	public static void VAILD_OUTPUT_XMLFILES(){ 		// 検索結果を xml ファイルを出力する
		outXmlFile = true;
	}

	public static void INVALID_OUTPUT_XMLFILES(){		// 検索結果の xml ファイルへの出力をやめる
		outXmlFile = false;
	}

	public static boolean isAppID () {
		return appid != null && appid.length() > 0;
	}

	private String 	requestURL; 		// URL
	protected String 	query; 			// クエリ
	protected int 	start; 		// 結果の返却位置の先頭
	private int 	count; 			// 返却結果数

	public static int getSearchCount () {
		return searchCount;
	}

	private void toXmlFile(Document doc){
		StringBuffer name = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH)+1;
		int d = cal.get(Calendar.DATE);
		int h = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		name.append(y);

		if (m < 10){ name.append("0"); }
		name.append(m);

		if (d < 10){ name.append("0"); }
		name.append(d);

		if (h < 10){ name.append("0"); }
		name.append(h);

		if (min < 10){ name.append("0"); }
		name.append(min);

		if (sec < 10){ name.append("0"); }
		name.append(sec);

		name.append("q");
		name.append(query);
		name.append("h");
		name.append(start);
		name.append("c");
		name.append(count);
		name.append(".xml");

		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = tfactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			File outfile = new File(".\\searchLog", new String(name));
			transformer.transform(new DOMSource(doc), new StreamResult(outfile));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	public void search(){
		try {
			URL url = new URL(requestURL);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();
			// 検索結果はXMLで返ってくるのでXMLパーサで解析
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbfactory.newDocumentBuilder();
			Document doc = builder.parse(is);
			if(outXmlFile){
				toXmlFile(doc);
			}
			// ルートノード
			root = doc.getDocumentElement();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		searchCount++;
	}

	private Element root; 		// 検索結果

	public String getTotal(){
		return root.getAttribute("totalResultsAvailable");
	}

	public double getTotalResults(){
		return new Double(root.getAttribute("totalResultsAvailable"));
	}

	public ArrayList<String> getSummaries(){
		ArrayList<String> summary = new ArrayList<String>(count);
		NodeList list = root.getElementsByTagName("Result");
		for (int i = 0; i < list.getLength(); i++) { // 各Resultノードに対して
			Element element = (Element) list.item(i);
			// Summaryノードを収集
			NodeList summaryList = element.getElementsByTagName("Summary");
			Element summaryElement = (Element) summaryList.item(0);
			// Summaryの値を取得
			if (summaryElement.getFirstChild() == null){
				continue;
			}
			String t = decodeXMLEntities(summaryElement.getFirstChild().getNodeValue(), 0);
			t = decodeXMLEntities(t);
			t = t.replaceAll("\\s+", " ");
			summary.add(t);
		}
		return summary;
	}

	/**
	 * xml定義済みの5種類の実態参照のデコードを行う．
	 * @param str 文字列
	 * @param index 初期値は0
	 * @return
	 */
	private String decodeXMLEntities(String str, int index){
		int nextIndex = 0;
		if ((nextIndex = str.indexOf("&", index)) == -1){
			return str;
		}

		if (str.indexOf("&#39;", nextIndex) != -1){
			return decodeXMLEntities(str.replaceAll("&#39;", "\'"), nextIndex);
		}
		if (str.indexOf("&amp;", nextIndex) != -1){
			return decodeXMLEntities(str.replaceAll("&amp;", "&"), nextIndex);
		}
		if (str.indexOf("&quot;", nextIndex) != -1){
			return decodeXMLEntities(str.replaceAll("&quot;", "\""), nextIndex);
		}
		if (str.indexOf("&lt;", nextIndex) != -1){
			return decodeXMLEntities(str.replaceAll("&lt;", "<"), nextIndex);
		}
		if (str.indexOf("&gt;", nextIndex) != -1){
			return decodeXMLEntities(str.replaceAll("&gt;", ">"), nextIndex);
		}

		return str;
	}

	/**
	 * xml定義済み以外の実態参照をデコードする．ただし，実装していない．
	 * @param str 実態参照を含む文字列
	 * @return デコード済みの文字列
	 */
	private String decodeXMLEntities(String str){
		// TODO 実態参照のデコードを行う．
		return str.replaceAll("&[^\\s]+;", " ");
	}

	private void Init(String q, int h, int c, boolean p){
		StringBuffer url = new StringBuffer(yahooSearchURL);
		url.append(appid);
		url.append("&query=");

		String utf;
		try {
			utf = URLEncoder.encode(q, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			utf = "";
		}
		url.append(utf);
		query = utf;

		if (p){
			url.append("&type=phrase");
		}

		if (c > 0){
			url.append("&results=");
			url.append(c);
			count = c;
		} else {
			count = 10;
		}

		if (h > 0 && h < 1000){
			url.append("&start=");
			url.append(h);
			start = h;
		} else {
			start = 1;
		}

		url.append(language);
		//System.out.println(url);
		requestURL = new String(url);
	}

	/**
	 *
	 * @param query 検索クエリ
	 */
	public Search(String query){
		Init(query, 0, 0, false);
	}

	/**
	 *
	 * @param query 検索クエリ
	 * @param count 返却結果数
	 */
	public Search(String query, int count){
		Init(query, 0, count, false);
	}

	/**
	 *
	 * @param query 検索クエリ
	 * @param head  返却結果の先頭位置，0でデフォルトの位置
	 * @param count 返却結果数，0でデフォルトの数
	 */
	public Search(String query, int head, int count){
		Init(query, head, count, false);
	}

	/**
	 *
	 * @param query  検索クエリ
	 * @param head   返却結果の先頭位置，0のときデフォルトの位置
	 * @param count  返却結果数，0のときデフォルトの数
	 * @param phrase フレーズ検索の有無，trueにするとqueryをフレーズとして扱う．
	 */
	public Search(String query, int head, int count, boolean phrase){
		Init(query, head, count, phrase);
	}


	public Search() {
	}

	public void run (){
		search();
	}
}
