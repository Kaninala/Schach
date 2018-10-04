package schach.backend;

import java.util.HashSet;

import schach.daten.FigurEnum;
import schach.daten.SpielEnum;
import schach.daten.ZugEnum;

public final class Regelwerk {

	/**
	 * Aufstellen des Spielfeldes am anfang.
	 */
	public static void setStartbelegung(Belegung b){
		/*
		Belegt für den anfang die Baueren
		 */
		for (int i=1;i<=8;i++){
			b.addFigur(new Figur(FigurEnum.Bauer,true),i,2); // weisse Bauern
			b.addFigur(new Figur(FigurEnum.Bauer,false),i,7); // schwarze Bauern
		}
		int j=1;
		boolean weiss=true;
		// Die anderen Figuren werden belegt
		for (int i=1;i<=2;i++){ // restliche Figuren
			b.addFigur(new Figur(FigurEnum.Turm,weiss),"a"+j);
			b.addFigur(new Figur(FigurEnum.Springer,weiss),"b"+j);
			b.addFigur(new Figur(FigurEnum.Laeufer,weiss),"c"+j);
			b.addFigur(new Figur(FigurEnum.Dame,weiss),"d"+j);
			b.addFigur(new Figur(FigurEnum.Koenig,weiss),"e"+j);
			b.addFigur(new Figur(FigurEnum.Laeufer,weiss),"f"+j);
			b.addFigur(new Figur(FigurEnum.Springer,weiss),"g"+j);
			b.addFigur(new Figur(FigurEnum.Turm,weiss),"h"+j);
			j=8; 
			weiss=false;
		} 
	}

	/**
	 * Gibt an ob der Angreifer die gegnerische Figur schlagen kann.
	 * @param b
	 * @param posAngreifer
	 * @param posOpfer
	 * @return
	 */
	public static boolean kannSchlagen(Belegung b,String posAngreifer,String posOpfer) {
		// Wenn akktuelle Position leer ist
		if (b==null) return false;
		// Wenn Angreifer eine gültige Figur ist
		if ((posAngreifer==null)||(posAngreifer.length()!=2)) return false;
		// Wenn Opfer eine gültige Figur ist und gibt einen erlaubten Zug ohne Angriff zu
		if ((posOpfer==null)||(posOpfer.length()!=2)) return false;
		HashSet<Zug> zuege=getErlaubteZuegeOhneSchach(b,posAngreifer,false);
		// Wenn kein weiterer Zug möglich ist
		if ((zuege==null)||(zuege.size()==0)) return false;

		for(Zug zug:zuege){
			if (posOpfer.equals(zug.getNach())) return true;
		}
		return false;
	}

	/**
	 * Moegliche Zuege der Figur f incl. Beachtung, dass der Spieler von f nicht selbst ins Schach geraten kann.
	 * @param b
	 * @param f
	 * @return
	 */
	public static HashSet<Zug> getErlaubteZuege(Belegung b,Figur f){
		HashSet<Zug> zuege=new HashSet<Zug>();
		if ((b==null)||(f==null)||(f.isGeschlagen())) return zuege;
		SpielEnum zugDavorStatus=b.getStatus();
		if ((zugDavorStatus!=null)&&
				(zugDavorStatus.equals(SpielEnum.WeissSchachMatt)||zugDavorStatus.equals(SpielEnum.SchwarzSchachMatt)||zugDavorStatus.equals(SpielEnum.Patt))){
			return zuege;	
		}
		zuege=getErlaubteZuegeOhneSchach(b,f,true);
		if ((zuege==null)||(zuege.size()==0)) return new HashSet<Zug>();
		HashSet<Zug> zuLoeschen=new HashSet<Zug>();
		for(Zug zug:zuege){
			Belegung bTest=b.zieheTestweise(zug.getVon(),zug.getNach());
			if (bTest.isSchach(f.isWeiss())) zuLoeschen.add(zug);
		}
		zuege.removeAll(zuLoeschen);
		return zuege;
	}

	/**
	 *  Moegliche Zuege der Figur f ohne Beachtung, ob der Spieler von f selbst ins Schach geraet
	 * @param b
	 * @param position
	 * @param inclRochadenCheck
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchach(Belegung b,String position,boolean inclRochadenCheck){
		Figur f=b.getFigur(position);
		if (f==null) return new HashSet<Zug>();
		return getErlaubteZuegeOhneSchach(b,f,inclRochadenCheck);
	}

	/**
	 * Gibt alle erlaubten Zuege mit gewaelter Figur wieder die ohne Angriff erfolgen kann
	 * @param b
	 * @param f
	 * @param inclRochadenCheck
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchach(Belegung b,Figur f,boolean inclRochadenCheck){
		HashSet<Zug> zuege=new HashSet<Zug>();
		switch (f.getTyp()){
		case Bauer:
			zuege=getErlaubteZuegeOhneSchachBauer(b,f);
			break;
		case Laeufer:
			zuege=getErlaubteZuegeOhneSchachLaeufer(b,f);
			break;
		case Turm:
			zuege=getErlaubteZuegeOhneSchachTurm(b,f);
			break;
		case Dame:
			zuege=getErlaubteZuegeOhneSchachDame(b,f);
			break;
		case Springer:
			zuege=getErlaubteZuegeOhneSchachSpringer(b,f);
			break;
		case Koenig:
			zuege=getErlaubteZuegeOhneSchachKoenig(b,f,inclRochadenCheck);
			break;
		default:
			break;
		}
		return zuege;
	}

	/**
	 * Gibt vom gewaeltem Bauer die alle erlaubten Zuege wieder
	 * @param b
	 * @param f
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchachBauer(Belegung b,Figur f){
		HashSet<Zug> zuege=new HashSet<Zug>();
		String posStart=f.getPosition();
		int x=Belegung.toArrayNotation(posStart)[0];
		int y=Belegung.toArrayNotation(posStart)[1];
		String posDazwischen=null;
		String posZiel=null;
		if ((y==1)||(y==8)) return zuege; // Bauer ist am Ende angekommen -> kein Zug moeglich
		int richtung=1;
		if (f.isSchwarz()) richtung=-1;

		posZiel=Belegung.toSchachNotation(x,y+richtung*1); // erster Schritt

		if ((posZiel!=null)&&(!b.hasFigur(posZiel))){
			Zug z=new Zug(posStart,x,y+richtung*1);
			zuege.add(z);
		}

		if(!f.isBereitsBewegt()){ // 2 Schritte
			posZiel=Belegung.toSchachNotation(x,y+richtung*2);
			posDazwischen=Belegung.toSchachNotation(x,y+richtung*1);
			if ((posZiel!=null)&&(!b.hasFigur(posZiel))){
				if ((posDazwischen!=null)&&(!b.hasFigur(posDazwischen))){
					Zug z=new Zug(posStart,x,y+richtung*2);
					zuege.add(z);
				}
			}
		}

		if (x>1){ // links schlagen
			posZiel=Belegung.toSchachNotation(x-1,y+richtung*1);
			if ((posZiel!=null)&&(b.hasGegnerFigur(posZiel,f.isWeiss()))){
				Zug z=new Zug(posStart,x-1,y+richtung*1);
				zuege.add(z);
			}
		}
		if (x<8){ // rechts schlagen
			posZiel=Belegung.toSchachNotation(x+1,y+richtung*1);
			if ((posZiel!=null)&&(b.hasGegnerFigur(posZiel,f.isWeiss()))){
				Zug z=new Zug(posStart,x+1,y+richtung*1);
				zuege.add(z);			
			}
		}

		// en passant moeglich?
		// Überprüfung vom Doppelsprung an der anfangs Position
		if ((b.getBemerkung()!=null)&&(ZugEnum.BauerDoppelschritt.equals(b.getBemerkung()))){
			int koordinatenAlt[]=Belegung.toArrayNotation(b.getNach());
			if (koordinatenAlt[1]==y){
				if (koordinatenAlt[0]==x-1){
					Zug z=new Zug(posStart,x-1,y+richtung*1);
					zuege.add(z);			
				}
				else if (koordinatenAlt[0]==x+1){
					Zug z=new Zug(posStart,x+1,y+richtung*1);
					zuege.add(z);
				}				
			}
		}
		return zuege;
	}

	/**
	 *  Gibt vom gewaeltem Laeufer die alle erlaubten Zuege wieder
	 * @param b
	 * @param f
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchachLaeufer(Belegung b,Figur f){
		HashSet<Zug> felder=new HashSet<Zug>();
		String posStart=f.getPosition();
		int x=Belegung.toArrayNotation(posStart)[0];
		int y=Belegung.toArrayNotation(posStart)[1];
		int i,j;

		//addZug ist für Figuren die Mehrere Schritte laufen können
		for (i=x+1,j=y+1;((i<=8)&&(j<=8));i++,j++){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		for (i=x-1,j=y-1;((i>=1)&&(j>=1));i--,j--){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		for (i=x-1,j=y+1;((i>=1)&&(j<=8));i--,j++){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		for (i=x+1,j=y-1;((i<=8)&&(j>=1));i++,j--){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		return felder;
	}

	/**
	 * Gibt vom gewaeltem Turm die alle erlaubten Zuege wieder
	 * @param b
	 * @param f
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchachTurm(Belegung b,Figur f){
		HashSet<Zug> felder=new HashSet<Zug>();
		String posStart=f.getPosition();
		int x=Belegung.toArrayNotation(posStart)[0];
		int y=Belegung.toArrayNotation(posStart)[1];
		int i;

		//addZug ist für Figuren die Mehrere Schritte laufen können
		for (i=y+1;i<=8;i++){
			if (!addZug(b,posStart,felder,x,i)) break;
		}
		for (i=y-1;i>=1;i--){
			if (!addZug(b,posStart,felder,x,i)) break;
		}
		for (i=x+1;i<=8;i++){
			if (!addZug(b,posStart,felder,i,y)) break;
		}
		for (i=x-1;i>=1;i--){
			if (!addZug(b,posStart,felder,i,y)) break;
		}
		return felder;
	}

	/**
	 * Gibt vom gewaeltem Dame die alle erlaubten Zuege wieder
	 * @param b
	 * @param f
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchachDame(Belegung b,Figur f){
		HashSet<Zug> felder=new HashSet<Zug>();
		String posStart=f.getPosition();
		int x=Belegung.toArrayNotation(posStart)[0];
		int y=Belegung.toArrayNotation(posStart)[1];
		int i,j;

		//addZug ist für Figuren die Mehrere Schritte laufen können
		for (i=y+1;i<=8;i++){
			if (!addZug(b,posStart,felder,x,i)) break;
		}
		for (i=y-1;i>=1;i--){
			if (!addZug(b,posStart,felder,x,i)) break;
		}
		for (i=x+1;i<=8;i++){
			if (!addZug(b,posStart,felder,i,y)) break;
		}
		for (i=x-1;i>=1;i--){
			if (!addZug(b,posStart,felder,i,y)) break;
		}
		for (i=x+1,j=y+1;((i<=8)&&(j<=8));i++,j++){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		for (i=x-1,j=y-1;((i>=1)&&(j>=1));i--,j--){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		for (i=x-1,j=y+1;((i>=1)&&(j<=8));i--,j++){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		for (i=x+1,j=y-1;((i<=8)&&(j>=1));i++,j--){
			if (!addZug(b,posStart,felder,i,j)) break;
		}
		return felder;
	}

	/**
	 * Gibt vom gewaeltem Springer die alle erlaubten Zuege wieder
	 * @param b
	 * @param f
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchachSpringer(Belegung b,Figur f){
		HashSet<Zug> felder=new HashSet<Zug>();
		String posStart=f.getPosition();
		int x=Belegung.toArrayNotation(posStart)[0];
		int y=Belegung.toArrayNotation(posStart)[1];

		addZug(b,posStart,felder,x+2,y+1);		
		addZug(b,posStart,felder,x+2,y-1);		
		addZug(b,posStart,felder,x+1,y+2);		
		addZug(b,posStart,felder,x+1,y-2);		
		addZug(b,posStart,felder,x-1,y+2);		
		addZug(b,posStart,felder,x-1,y-2);		
		addZug(b,posStart,felder,x-2,y+1);		
		addZug(b,posStart,felder,x-2,y-1);		
		return felder;
	}

	/**
	 * Gibt vom gewaeltem König die alle erlaubten Zuege wieder mit der Möglichkeit Rochade
	 * @param b
	 * @param f
	 * @param inclRochadenCheck
	 * @return
	 */
	private static HashSet<Zug> getErlaubteZuegeOhneSchachKoenig(Belegung b,Figur f,boolean inclRochadenCheck){
		HashSet<Zug> felder=new HashSet<Zug>();
		String posStart=f.getPosition();
		int x=Belegung.toArrayNotation(posStart)[0];
		int y=Belegung.toArrayNotation(posStart)[1];

		addZug(b,posStart,felder,x-1,y+1);		
		addZug(b,posStart,felder,x,y+1);		
		addZug(b,posStart,felder,x+1,y+1);		
		addZug(b,posStart,felder,x-1,y);		
		addZug(b,posStart,felder,x+1,y);		
		addZug(b,posStart,felder,x-1,y-1);		
		addZug(b,posStart,felder,x,y-1);		
		addZug(b,posStart,felder,x+1,y-1);		
		
		// Rochade
		// bei dem König und Turm einer Farbe bewegt werden (Doppelzug)
		Figur turm;
		if (((!f.isBereitsBewegt()) && (inclRochadenCheck))){ // ich darf mich nicht bereits bewegt haben
			boolean binImSchach=false;
			if (b.getStatus()!=null){
				if (f.isWeiss()&&(b.getStatus().equals(SpielEnum.WeissImSchach))) binImSchach=true;
				if (f.isSchwarz()&&(b.getStatus().equals(SpielEnum.SchwarzImSchach))) binImSchach=true;
			}
			// 1. Rochade: lange
			turm=b.getFigur(1,y); // der Turm muss noch da sein und durfte nicht bewegt worden sein
			if ((turm!=null)&&(turm.getTyp().equals(FigurEnum.Turm))&&(!turm.isBereitsBewegt())){
				if (!binImSchach){ // Rochade geht nur, wenn ich nicht gerade im Schach stehe
					if (f.isWeiss()){
						// Rochade zwischen a1 und e1
						if ((b.getFigur("b1")==null)&&(b.getFigur("c1")==null)&&(b.getFigur("d1")==null)){
							// König über kein Feld ziehen muss, das durch eine feindliche Figur bedroht wird:
							if ((!isPositionBedrohtFuerRochade(b,"c1",!f.isWeiss()))&&(!isPositionBedrohtFuerRochade(b,"d1",!f.isWeiss())))
								addZug(b,posStart,felder,"c1"); // Koenig landet dann auf c1
						}
					}
					else{
						// Rochade zwischen a8 und e8
						if ((b.getFigur("b8")==null)&&(b.getFigur("c8")==null)&&(b.getFigur("d8")==null)){
							// König über kein Feld ziehen muss, das durch eine feindliche Figur bedroht wird:
							if ((!isPositionBedrohtFuerRochade(b,"c8",!f.isWeiss()))&&(!isPositionBedrohtFuerRochade(b,"d8",!f.isWeiss())))
									addZug(b,posStart,felder,"c8"); // Koenig landet dann auf c8
						}						
					}
				}
			}
			// 2. Rochade: kurze
			turm=b.getFigur(8,y); // der Turm muss noch da sein und durfte nicht bewegt worden sein
			if ((turm!=null)&&(turm.getTyp().equals(FigurEnum.Turm))&&(!turm.isBereitsBewegt())){
				if (!binImSchach){ // Rochade geht nur, wenn ich nicht gerade im Schach stehe
					if (f.isWeiss()){
						// Rochade zwischen e1 und h1
						if ((b.getFigur("f1")==null)&&(b.getFigur("g1")==null)){
							// König über kein Feld ziehen muss, das durch eine feindliche Figur bedroht wird:
							if ((!isPositionBedrohtFuerRochade(b,"f1",!f.isWeiss()))&&(!isPositionBedrohtFuerRochade(b,"g1",!f.isWeiss())))
								addZug(b,posStart,felder,"g1"); // Koenig landet dann auf g1
						}						
					}
					else{
						// Rochade zwischen e8 und h8
						if ((b.getFigur("f8")==null)&&(b.getFigur("g8")==null)){
							// König über kein Feld ziehen muss, das durch eine feindliche Figur bedroht wird:
							if ((!isPositionBedrohtFuerRochade(b,"f8",!f.isWeiss()))&&(!isPositionBedrohtFuerRochade(b,"g8",!f.isWeiss())))
								addZug(b,posStart,felder,"g8"); // Koenig landet dann auf g8
						}						
					}
				}
			}
		}
		return felder;
	}

	/**
	 *Zeigt ob die Rochade moeglichkeit vom Gegner bedroht wird
	 * @param b
	 * @param postion
	 * @param durchWeiss
	 * @return
	 */
	private static boolean isPositionBedrohtFuerRochade(Belegung b,String postion,boolean durchWeiss) {
		if ((postion==null)||(postion.length()!=2)) return false;
		HashSet<Zug> bedroht=new HashSet<Zug>();
		for (Figur fGegner:b.getFigurenAufBrett(durchWeiss)){
			HashSet<Zug> bedrohtVonFigur=new HashSet<Zug>(); 
			if (fGegner.getTyp().equals(FigurEnum.Koenig)){
				//gegnerischer Koenig wird separat behaldelt, um Endlos-Rekursion im Rochaden-Check zu verhindern
				if (fGegner.isBereitsBewegt()) bedrohtVonFigur=b.getErlaubteZuege(fGegner.getPosition());
			}
			else{
				bedrohtVonFigur=b.getErlaubteZuege(fGegner.getPosition());
			}			
			bedroht.addAll(bedrohtVonFigur);				
		}
		for(Zug z:bedroht){
			if (postion.equals(z.getNach())) return true;
		}
		return false;
	}
	
	//=false: auf dieser Linie keine weiteren Zuege mehr erlaubt
	private static boolean addZug(Belegung b,String posStart,HashSet<Zug> felder,int x,int y){
		if ((x<1)||(x>8)||(y<1)||(y>8)) return false;
		Figur f=b.getFigur(posStart);
		String posZiel=Belegung.toSchachNotation(x, y);
		if (b.hasFigur(posZiel)){
			if (b.hasGegnerFigur(posZiel,f.isWeiss())){
				Zug z=new Zug(posStart,x,y);
				felder.add(z);
			}
			return false;
		}
		else{
			Zug z=new Zug(posStart,x,y);
			felder.add(z);
			return true;
		}
	}
	private static boolean addZug(Belegung b,String posStart,HashSet<Zug> felder,String position){
		int x=Belegung.toArrayNotation(position)[0];
		int y=Belegung.toArrayNotation(position)[1];
		return addZug(b,posStart,felder,x,y);
	}

}
