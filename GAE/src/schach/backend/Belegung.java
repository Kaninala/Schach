package schach.backend;

import java.util.ArrayList;
import java.util.HashSet;

import schach.daten.D_Belegung;
import schach.daten.FigurEnum;
import schach.daten.SpielEnum;
import schach.daten.ZugEnum;

public class Belegung {
	private D_Belegung daten;
	private Figur[][] figurenAufBrett=new Figur[9][9];
	private ArrayList<Figur> figurenGeschlagen;

	public static int[] toArrayNotation(String schachNotation){
		try{
			char[] daten=schachNotation.toCharArray();
			int x=Integer.parseInt(""+(daten[0]-96));
			int y=Integer.parseInt(""+daten[1]);
			return new int[]{x,y};
		}
		catch (Exception e){
			return null;
		}
	}
	
	public static String toSchachNotation(int x,int y){
		return toZeichen(x)+y;
	}
	
	public static String toZeichen(int wert){
		return ""+(char)(96+wert);
	}

	public Belegung(){
		daten=new D_Belegung();
		figurenAufBrett=new Figur[9][9];
		figurenGeschlagen=new ArrayList<Figur>();
	}

	public D_Belegung getDaten(){
		return daten;
	}
	public void setDaten(D_Belegung daten){
		this.daten=daten;
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

	public void setZugDavor(Zug zNeu) {
		daten.setString("von",zNeu.getDaten().getString("von"));
		daten.setString("nach",zNeu.getDaten().getString("nach"));
		daten.setString("status",zNeu.getDaten().getString("status"));
		daten.setString("bemerkung",zNeu.getDaten().getString("bemerkung"));
	}
	
	public void addFigur(Figur figur,String position){
		addFigur(figur,toArrayNotation(position));
	}
	public void addFigur(Figur figur,int x,int y){
		figurenAufBrett[x][y]=figur;
		daten.incInt("anzahlFigurenAufBrett");
		figur.setPosition(toSchachNotation(x,y));	
	}
	public void addFigur(Figur figur,int[] xy){
		if (xy==null){
			figurenGeschlagen.add(figur);
			figur.setPosition("");
			daten.decInt("anzahlFigurenAufBrett");
			daten.incInt("anzahlFigurenGeschlagen");
		}
		else
			addFigur(figur,xy[0],xy[1]);
	}

	public void setFigurAufBrett(Figur figur,String position){
		int[] xy=toArrayNotation(position);
		figurenAufBrett[xy[0]][xy[1]]=figur;
	}
	public void setFigurGeschlagen(Figur figur){
		figurenGeschlagen.add(figur);
	}

	public void removeBauerBeiEnPassant(String position) {
		Figur f=getFigur(position);
		if ((f==null)||(!f.getTyp().equals(FigurEnum.Bauer)))
			throw new RuntimeException("removeBauerBeiEnPassant: Figur auf der Position ist ungueltig!");
		addFigur(f,"");
	}

	public void moveFigur(Figur figur,String positionNeu){
		String positionAlt=figur.getPosition();
		int[] xyAlt=toArrayNotation(positionAlt);
		int[] xyNeu=toArrayNotation(positionNeu);
		figurenAufBrett[xyAlt[0]][xyAlt[1]]=null;
		if (hasGegnerFigur(positionNeu,figur.isWeiss())){ // schlage ggf. die Figur auf dem Zielfeld
			Figur fGeschlagen=figurenAufBrett[xyNeu[0]][xyNeu[1]];
			addFigur(fGeschlagen,"");
		}
		figurenAufBrett[xyNeu[0]][xyNeu[1]]=figur;
		figur.setPosition(positionNeu);
		figur.setBereitsBewegt(true);
	}
	
	public boolean hasFigur(String position){
		return figurenAufBrett[toArrayNotation(position)[0]][toArrayNotation(position)[1]]!=null;
	} 
	
	public boolean hasGegnerFigur(String position,boolean binWeiss){
		if (!hasFigur(position)) return false;
		Figur f=figurenAufBrett[toArrayNotation(position)[0]][toArrayNotation(position)[1]];
		return f.isWeiss()!=binWeiss;
	}

	public Figur getFigur(String position){
		return getFigur(toArrayNotation(position)[0],toArrayNotation(position)[1]);
	}
	public Figur getFigur(int x,int y){
		return figurenAufBrett[x][y];
	}

	public ArrayList<Figur> getAlleFigurenAufBrett(){
		ArrayList<Figur> ergebnis=new ArrayList<Figur>();
		for(int y=8;y>=1;y--){
			for(int x=1;x<=8;x++){
				Figur f=figurenAufBrett[x][y];
				if (f!=null) ergebnis.add(f);
			}
		}
		return ergebnis;
	}
	public ArrayList<Figur> getFigurenAufBrett(boolean weisse){
		ArrayList<Figur> ergebnis=new ArrayList<Figur>();
		for(Figur f:getAlleFigurenAufBrett()){
			if (f.isWeiss()==weisse) ergebnis.add(f);
		}
		return ergebnis;
	}
	public ArrayList<Figur> getGeschlageneFiguren(){
		ArrayList<Figur> ergebnis=new ArrayList<Figur>();
		for(Figur f:figurenGeschlagen) ergebnis.add(f);
		return ergebnis;
	}

	public ArrayList<Figur> getGeschlageneFiguren(boolean weisse){
		ArrayList<Figur> ergebnis=new ArrayList<Figur>();
		for(Figur f:figurenGeschlagen){
			if (f.isWeiss()==weisse) ergebnis.add(f);
		}		
		return ergebnis;
	}
	
	public Figur getKoenig(boolean isWeiss){
		for(Figur f:getFigurenAufBrett(isWeiss)){
			if (f.getTyp().equals(FigurEnum.Koenig)) return f;
		}
		throw new RuntimeException("getKoenig: Kein Koenig mehr auf dem Brett?");
	}

	public HashSet<Zug> getAlleErlaubteZuege(boolean isWeiss){
		HashSet<Zug> zuege=new HashSet<Zug>(); 
		for(Figur f:getFigurenAufBrett(isWeiss)){
			zuege.addAll(Regelwerk.getErlaubteZuege(this,f));
		}
		return zuege;
	}
	
	public HashSet<Zug> getErlaubteZuege(String position){
		Figur f=getFigur(position);
		if (f==null) return new HashSet<Zug>();
		return Regelwerk.getErlaubteZuege(this,f);
	}

	public boolean isSchach(boolean isWeiss){
		Figur meinKoenig=getKoenig(isWeiss);
		for(Figur f:getFigurenAufBrett(!isWeiss)){
			// f ist gegnerische Figur -> kann f jetzt meinen Koenig schlagen?
			if (Regelwerk.kannSchlagen(this,f.getPosition(),meinKoenig.getPosition())) return true;
		}		
		return false;
	}

	public boolean isSchachMatt(boolean isWeiss){
		if (!isSchach(isWeiss)) return false;
		HashSet<Zug> zuege=getAlleErlaubteZuege(isWeiss);
		if ((zuege==null)||(zuege.size()==0)) return true;
		return false;
	}
	
	public boolean isPatt(boolean weissAmZug){
		if (isSchach(weissAmZug)) return false;
		HashSet<Zug> zuege=getAlleErlaubteZuege(weissAmZug);
		if ((zuege==null)||(zuege.size()==0)) return true;
		return false;
	}
	
	public boolean isBauerDoppelschritt(String von,String nach){
		Figur f=getFigur(von);
		if (f==null) return false;
		if (FigurEnum.Bauer.equals(f.getTyp())){
			int[] vonArray=Belegung.toArrayNotation(von);
			int[] nachArray=Belegung.toArrayNotation(nach);
			return ((vonArray[1]==nachArray[1]+2)||(vonArray[1]==nachArray[1]-2));
		}
		return false;
	}

	public boolean isBauerUmwandlungImGange(String nach) {
		Figur f=getFigur(nach);
		if (f==null) return false;
		if (FigurEnum.Bauer.equals(f.getTyp())){
			int y=toArrayNotation(nach)[1];
			return (y==1)||(y==8);
		}
		return false;
	}

	public Belegung zieheTestweise(String von,String nach) {
		Belegung bNeu=this.clone();
		Figur f=bNeu.getFigur(von);
		if (f==null)
			throw new RuntimeException("ziehe: Auf diesem Feld ist keine Figur!");
		bNeu.moveFigur(f,nach);
		return bNeu;
	}

	public String toStringSichtVonWeiss(){
		StringBuffer s=new StringBuffer("-----------------------------------------\n");
		for(int y=8;y>=1;y--){
			for(int x=1;x<=8;x++){
				Figur f=figurenAufBrett[x][y];
				if (f!=null){
					if (f.isWeiss())
						s.append("|w"+f);
					else
						s.append("|s"+f);
				}
				else
					s.append("|    ");
			}
			s.append("|\n");
			s.append("-----------------------------------------\n");
		}
		return s.toString();
	}

	public String toStringSichtVonSchwarz(){
		StringBuffer s=new StringBuffer("-----------------------------------------\n");
		for(int y=1;y<=8;y++){
			for(int x=8;x>=1;x--){
				Figur f=figurenAufBrett[x][y];
				if (f!=null){
					if (f.isWeiss())
						s.append("|w"+f);
					else
						s.append("|s"+f);
				}
				else
					s.append("|    ");
			}
			s.append("|\n");
			s.append("-----------------------------------------\n");
		}
		return s.toString();
	}

	@Override
	public String toString(){
		return toStringSichtVonWeiss();
	}
	
	@Override
	public Belegung clone(){
		D_Belegung datenKlon=(D_Belegung)daten.clone();
		datenKlon.setInt("anzahlFigurenAufBrett",0);
		datenKlon.setInt("anzahlFigurenGeschlagen",0);
		Belegung b=new Belegung();
		b.daten=datenKlon;
		for(Figur f:getAlleFigurenAufBrett()){
			Figur f2=f.clone();
			b.addFigur(f2,f2.getPosition());
		}
		for(Figur f:getGeschlageneFiguren()){
			Figur f2=f.clone();
			b.figurenGeschlagen.add(f2);
			b.daten.incInt("anzahlFigurenGeschlagen");
		}
		return b;
	}
	
	public String toXml(){
		StringBuffer s=new StringBuffer(daten.toXml());
		for(int y=8;y>=1;y--){
			for(int x=1;x<=8;x++){
				Figur f=figurenAufBrett[x][y];
				if (f!=null) s.append(f.toXml());
			}
		}
		if(figurenGeschlagen!=null){
			for(Figur f:figurenGeschlagen){
				s.append(f.toXml());				
			}
		}			
		return s.toString();
	}
}