package schach.backend;

import schach.daten.D_Belegung;
import schach.daten.D_Zug;
import schach.daten.SpielEnum;
import schach.daten.ZugEnum;

public class Zug {
	private D_Zug daten;

	private Zug(){
		daten=new D_Zug();
	}

	public Zug(D_Belegung daten){
		this();
		setVon(daten.getString("von"));
		setNach(daten.getString("nach"));
		setBemerkung(daten.getString("bemerkung"));
		setStatus(daten.getString("status"));			
	}

	public Zug(String von,String nach){
		this();
		setVon(von);
		setNach(nach);
		setBemerkung("");
		setStatus("");
	}
	
	public Zug(String von,int nachX,int nachY){
		this(von,Belegung.toSchachNotation(nachX,nachY));
	}
	
	public D_Zug getDaten(){
		return daten;
	}
	
	public void setVon(String von){
		if (von==null) von="";
		daten.setString("von",von);
	}
	public String getVon(){
		return daten.getString("von");
	}
	
	public void setNach(String nach){
		if (nach==null) nach="";
		daten.setString("nach",nach);
	}
	public String getNach(){
		return daten.getString("nach");
	}
	
	public void setBemerkung(ZugEnum bemerkung){
		daten.setString("bemerkung",""+bemerkung);
	}
	public void setBemerkung(String bemerkung){
		if (bemerkung==null) bemerkung="";
		daten.setString("bemerkung",bemerkung);
	}
	public ZugEnum getBemerkung(){
		if (daten==null) return null;
		return ZugEnum.toEnumFromString(daten.getString("bemerkung"));
	}
	
	public void setStatus(SpielEnum status){
		daten.setString("status",""+status);
	}
	public void setStatus(String status){
		if (status==null) status="";
		daten.setString("status",status);
	}
	public SpielEnum getStatus(){
		if (daten==null) return null;
		return SpielEnum.fromString(daten.getString("status"));
	}
	
	@Override
	public String toString(){
		return daten.getString("von")+daten.getString("nach");
	}
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof Zug)) return false;
		Zug z=(Zug)o;
		return this.daten.equals(z.daten);
	}
	
	@Override
	public int hashCode(){
		return toString().hashCode();
	}
	
	@Override
	public Zug clone(){
		Zug z=new Zug();
		z.daten=(D_Zug)daten.clone();
		return z;
	}
	
	public String toXml(){
		return daten.toXml();
	}
}
