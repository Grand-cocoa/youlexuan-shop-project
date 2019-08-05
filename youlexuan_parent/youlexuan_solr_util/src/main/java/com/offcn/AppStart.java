package com.offcn;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppStart {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
		FromDatabaseToSolr solr = (FromDatabaseToSolr) applicationContext.getBean("fromDatabaseToSolr");
		solr.deleteAllSolr();
		solr.importSolr();
	}
}
