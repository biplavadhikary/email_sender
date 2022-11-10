package com.example.email.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EmailHelper {
	
	private EmailHelper() {}

	public static String embedSignatureImage(String emailBody) {
		if(emailBody.contains("<html>")) return embedInHtml(emailBody);
		else return embedInPlain(emailBody);
	}

	private static String embedInPlain(String emailBody) {
		return "<html><body><p>" + emailBody + "</p><img src='http://www.programacion.net/files/article/20160124010121_url1.jpg'></body></html>";
	}

	private static String embedInHtml(String emailBody) {
		Document document = Jsoup.parse(emailBody);
		document.body().appendElement("img").attr("src", "http://www.programacion.net/files/article/20160124010121_url1.jpg");
		return document.toString();
	}
}