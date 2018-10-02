package schach.backend;

import schach.daten.D_Figur;
import schach.daten.FigurEnum;

public class Figur {
	private D_Figur daten;
	
	private Figur(){
		daten=new D_Figur();
	}
	
	public Figur(D_Figur daten){
		this.daten=(D_Figur)daten.clone();
	}
	
	public Figur(FigurEnum typ,boolean isWeiss){
		this();
		setTyp(typ);
		setWeiss(isWeiss);
		setPosition("");
		setBereitsBewegt(false);
	}
	
	public D_Figur getDaten(){
		return daten;
	}
	
	private void setTyp(FigurEnum typ){
		daten.setString("typ",""+typ);
	}
	public FigurEnum getTyp(){
		return FigurEnum.toEnumFromString(daten.getString("typ"));
	}
	public String getKuerzel(){
		return FigurEnum.toKuerzel(FigurEnum.toEnumFromString(daten.getString("typ")));
	}
	
	private void setWeiss(boolean isWeiss){
		daten.setBool("isWeiss",isWeiss);
	}
	public boolean isWeiss(){
		return daten.getBool("isWeiss");
	}
	public boolean isSchwarz(){
		return !daten.getBool("isWeiss");
	}
	
	public void setPosition(String position){
		daten.setString("position",position);
	}
	public void setPosition(int x,int y){
		daten.setString("position",Belegung.toSchachNotation(x,y));
	}
	public String getPosition(){
		return daten.getString("position");
	}
	public boolean isGeschlagen(){
		return (daten.getString("position")==null)||(daten.getString("position").length()!=2);
	}
	
	public void setBereitsBewegt(boolean isBereitsBeweggt){
		daten.setBool("bereitsBewegt",isBereitsBeweggt);
	}
	public boolean isBereitsBewegt(){
		return daten.getBool("bereitsBewegt");
	}
	
	@Override
	public String toString(){
		return getKuerzel()+getPosition();
	}
	
	@Override
	public boolean equals(Object o){
		if (!(o instanceof Figur)) return false;
		Figur f=(Figur)o;
		return this.daten.equals(f.daten);
	}
	
	@Override 
	public Figur clone(){
		Figur f=new Figur((D_Figur)daten.clone());
		return f;
	}
	
	public String toXml(){
		return daten.toXml();
	}
}
