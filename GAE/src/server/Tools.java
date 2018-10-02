package server;

import java.util.ArrayList;
import java.util.Collections;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import schach.daten.Xml;

public class Tools {
	public static String getDienste(@SuppressWarnings("rawtypes") Class c) {
		ArrayList<String> dienste=new ArrayList<String>(); 
		for(Method m:c.getMethods()){
			Path wert = m.getAnnotation(javax.ws.rs.Path.class);
			if (wert!=null)
				dienste.add(wert.value());
		}
		Collections.sort(dienste);
		StringBuffer xml=new StringBuffer();
		xml.append(Xml.xmlHeaderAlone);
		xml.append("<dienste>\n");
		for(String dienst:dienste){
			xml.append("<dienst>"+dienst+"</dienst>");
		}
		xml.append("</dienste>\n");
		return xml.toString();
	}
}
