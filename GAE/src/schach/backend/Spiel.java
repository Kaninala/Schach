package schach.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;

import schach.daten.*;

public class Spiel {
	private D_Spiel daten;
	private ArrayList<Belegung> belegungen;

	public Spiel(){
		daten=new D_Spiel();
		belegungen=new ArrayList<Belegung>();
	}
	
	public Spiel(String pfad) {
		this();
		BufferedReader br=null;
		try {
			pfad=URLDecoder.decode(""+pfad,"ISO-8859-1");
			StringBuffer xml=new StringBuffer();
			br=new BufferedReader(new FileReader(pfad));
			String zeile=br.readLine(); 
	    while (zeile!=null){
	    	xml.append(zeile+"/n");
	      zeile=br.readLine(); 
	    } 
	    ArrayList<D> spielDaten=Xml.toArray(xml.toString());
	    int counter=0;
	    // Daten des Spiels
	    daten=(D_Spiel)spielDaten.get(counter);
	    counter++;
	    for(int i=0;i<=daten.getInt("anzahlZuege");i++){
	    	// Belegungen
		    Belegung b=new Belegung();
		    D_Belegung datenBelegung=(D_Belegung)spielDaten.get(counter);
		    counter++;
		    b.setDaten(datenBelegung); 
		    // Figuren dieser Belegung auf dem Brett
		    for(int j=1;j<=datenBelegung.getInt("anzahlFigurenAufBrett");j++){
		    	D_Figur datenFigur=(D_Figur)spielDaten.get(counter);
	  	    counter++;
	  	    Figur figur=new Figur(datenFigur);
	  	    b.setFigurAufBrett(figur,datenFigur.getString("position"));
		    }
		    // geschlagene Figuren dieser Belegung
		    for(int j=1;j<=datenBelegung.getInt("anzahlFigurenGeschlagen");j++){
		    	D_Figur datenFigur=(D_Figur)spielDaten.get(counter);
	  	    counter++;
	  	    Figur figur=new Figur(datenFigur);
	  	    b.setFigurGeschlagen(figur);
		    }
		    belegungen.add(b);
	    }	    	
		}
    catch (Exception e){
			throw new RuntimeException("Fehler beim Laden des Spiels von "+pfad+": "+e.getMessage());
		} 	
		finally{
			try {
				br.close();
			} catch (Exception e) {}			
		}
	}
	
	public D_Spiel getDaten(){
		return daten;
	}

	public void initStandardbelegung(){
		Belegung belegung=new Belegung();
		Regelwerk.setStartbelegung(belegung);
		belegungen.add(belegung);
	}
	
	public int getAnzahlZuege(){
		return belegungen.size()-1;
	}

	public boolean isWeissAmZug(){
		return daten.getInt("anzahlZuege")%2==0;
	}

	public boolean iSchwarzAmZug(){
		return !isWeissAmZug();
	}

	public Belegung getAktuelleBelegung(){
		return belegungen.get(belegungen.size()-1);
	}

	public Belegung getBelegung(int nummer){
		return belegungen.get(nummer);
	}

	public ArrayList<String> getZugHistorie() {
		ArrayList<String> zugHistorie=new ArrayList<String>();
		for(int i=1;i<=daten.getInt("anzahlZuege");i++){
			zugHistorie.add(getZugAlsNotation(i));			
		}
		return zugHistorie;
	}
	
	public HashSet<Zug> getAlleErlaubteZuege(){
		return getAktuelleBelegung().getAlleErlaubteZuege(isWeissAmZug());
	}

	public HashSet<Zug> getErlaubteZuege(String position){
		return getAktuelleBelegung().getErlaubteZuege(position);
	}
	
	public boolean isWeissImSchach(){
		return getAktuelleBelegung().isSchach(true);
	}

	public boolean isSchwarzImSchach(){
		return getAktuelleBelegung().isSchach(false);
	}
	
	public boolean isWeissSchachMatt(){
		return getAktuelleBelegung().isSchachMatt(true);
	}

	public boolean isSchwarzSchachMatt(){
		return getAktuelleBelegung().isSchachMatt(false);
	}
	
	public boolean isPatt(){
		return getAktuelleBelegung().isPatt(isWeissAmZug());
	}
	


	public Belegung ziehe(String von,String nach) {
		Belegung b=getAktuelleBelegung();
		Belegung bNeu=null;
		Zug zNeu=null;
		boolean isEnPassant=false;

		bNeu=b.clone();
		Figur f=bNeu.getFigur(von);
		// keine Figur zum Ziehen
		if (f==null)
			throw new RuntimeException("ziehe: Auf diesem Feld ist keine Figur!");
		// Spiel ist bereits beendet
		SpielEnum zugDavorStatus=b.getStatus();
		if ((zugDavorStatus!=null)&&
				(zugDavorStatus.equals(SpielEnum.WeissSchachMatt)||zugDavorStatus.equals(SpielEnum.SchwarzSchachMatt)||zugDavorStatus.equals(SpielEnum.Patt))){
			throw new RuntimeException("ziehe: Das Spiel ist bereits zu Ende: "+zugDavorStatus);
		}
		// ich bin nicht am Zug
		if(f.isWeiss()!=isWeissAmZug())
			throw new RuntimeException("ziehe: Sie sind nicht am Zug!");
		// ist der Zug erlaubt
		HashSet<Zug> erlaubteZuege=b.getErlaubteZuege(f.getPosition());
		if (!erlaubteZuege.contains(new Zug(von,nach)))
			throw new RuntimeException("ziehe: Der Zug "+f.getTyp()+" von "+von+" nach "+nach+" ist nicht erlaubt!");
		
		// ZIEHEN:
		bNeu.moveFigur(f,nach);
		// Zug registrieren in der Belegung...
		zNeu=new Zug(von,nach);
		
		// Rochade
		if (f.getTyp().equals(FigurEnum.Koenig)){
			int xAlt=Belegung.toArrayNotation(von)[0];
			int xNeu=Belegung.toArrayNotation(nach)[0];
			if ((xAlt==xNeu+2)||(xAlt==xNeu-2)){
				if (nach.equals("c1")){ // Turm mitziehen
					bNeu.moveFigur(bNeu.getFigur("a1"),"d1");
					zNeu.setBemerkung(ZugEnum.RochadeLang);
				}else if (nach.equals("g1")){
					bNeu.moveFigur(bNeu.getFigur("h1"),"f1");
					zNeu.setBemerkung(ZugEnum.RochadeKurz);
				}else if (nach.equals("c8")){
					bNeu.moveFigur(bNeu.getFigur("a8"),"d8");
					zNeu.setBemerkung(ZugEnum.RochadeLang);
				}else{
					bNeu.moveFigur(bNeu.getFigur("h8"),"f8");
					zNeu.setBemerkung(ZugEnum.RochadeKurz);
				}
			}
		}
		
		// en passant
		if ((b.getBemerkung()!=null)&&(ZugEnum.BauerDoppelschritt.equals(b.getBemerkung()))){
			if ((f.getTyp().equals(FigurEnum.Bauer))&&(b.getFigur(nach)==null)){
				int xAlt=Belegung.toArrayNotation(von)[0];
				int xNeu=Belegung.toArrayNotation(nach)[0];
				if (xAlt!=xNeu){
					bNeu.removeBauerBeiEnPassant(b.getNach());
					isEnPassant=true;						
				}
			}
		}
		
		// Bauernumwandlung
		if (bNeu.isBauerUmwandlungImGange(nach)){
			bNeu.getDaten().decInt("anzahlFigurenAufBrett"); // der alte Bauer ist weg
			Figur fNeu=new Figur(FigurEnum.Dame,isWeissAmZug());
			bNeu.addFigur(fNeu,nach); // dafuer kommt die neue Dame hinzu
			bNeu.setBemerkung(ZugEnum.BauerUmgewandelt);
			zNeu.setBemerkung(ZugEnum.BauerUmgewandelt);
		}

		// Spielstatus hinzufuegen
		if (bNeu.isSchach(true)){
			zNeu.setStatus(SpielEnum.WeissImSchach);
			bNeu.setStatus(SpielEnum.WeissImSchach);
			if (bNeu.isSchachMatt(true)){
				zNeu.setStatus(SpielEnum.WeissSchachMatt);
				bNeu.setStatus(SpielEnum.WeissSchachMatt);
			}
		} else if (bNeu.isSchach(false)){
			zNeu.setStatus(SpielEnum.SchwarzImSchach);
			bNeu.setStatus(SpielEnum.SchwarzImSchach);
			if (bNeu.isSchachMatt(false)){
				zNeu.setStatus(SpielEnum.SchwarzSchachMatt);
				bNeu.setStatus(SpielEnum.SchwarzSchachMatt);
			}
		} else if (bNeu.isPatt(isWeissAmZug())){
			zNeu.setStatus(SpielEnum.Patt);
			bNeu.setStatus(SpielEnum.Patt);
		}
		// Spielbemerkung hinzufuegen
		if (b.isBauerDoppelschritt(von,nach)) zNeu.setBemerkung(ZugEnum.BauerDoppelschritt);
		if (isEnPassant) zNeu.setBemerkung(ZugEnum.EnPassant);
		
		// Spieldaten aktualisieren
		bNeu.setZugDavor(zNeu);
		belegungen.add(bNeu);
		daten.incInt("anzahlZuege");
		daten.setString("bemerkung",""+zNeu.getBemerkung());
		daten.setString("status",""+zNeu.getStatus());

		return bNeu;	
	}
	
	public String speichern(String pfad){
		PrintWriter pw=null;
		try {
			pfad=URLDecoder.decode(""+pfad,"ISO-8859-1");
			if (!pfad.endsWith(".xml")) pfad=pfad+".xml";
			pw=new PrintWriter(new FileWriter(pfad));
			pw.println(Xml.verpacken(toXml()));
			return Xml.verpacken(Xml.fromD(new D_OK("Spiel erfolgreich gespeichert.")));
		} catch (Exception e) {
			e.printStackTrace();
			return Xml.verpacken(Xml.fromD(new D_Fehler(e.getMessage())));
		}
		finally{
			pw.close();			
		}
	}
	
	public String toXml(){
		StringBuffer s=new StringBuffer(daten.toXml());
		for(int i=0;i<=daten.getInt("anzahlZuege");i++){
			s.append(belegungen.get(i).toXml());
		}
		return s.toString();
	}
	
	private String getZugAlsNotation(int nummer){
		if ((nummer<1)||(nummer>belegungen.size()))
			throw new RuntimeException("getZugAlsNotation: Diese Zugnummer existiert nicht!");
		String s="";
		Belegung bVorher=getBelegung(nummer-1);
		Belegung bNachher=getBelegung(nummer);
		ZugEnum zugBemerkung=bNachher.getBemerkung();
		SpielEnum zugStatus=bNachher.getStatus();
		
		Figur fBewegt=bVorher.getFigur(bNachher.getVon());
		Figur fGeschlagen=null;
		ArrayList<Figur> g1=bVorher.getGeschlageneFiguren();
		ArrayList<Figur> g2=bNachher.getGeschlageneFiguren();
		if (g2.size()>g1.size()) fGeschlagen=g2.get(g2.size()-1);	

		if ((zugBemerkung!=null)&&(ZugEnum.RochadeKurz.equals(zugBemerkung))) return "0-0";
		if ((zugBemerkung!=null)&&(ZugEnum.RochadeLang.equals(zugBemerkung))) return "0-0-0";
		
		s+=fBewegt.getKuerzel();
		s+=bNachher.getVon();
		if (fGeschlagen==null)
			s+="-";
		else
			s+="x";
		s+=bNachher.getNach();
		
		if ((zugBemerkung!=null)&&(ZugEnum.BauerUmgewandelt.equals(zugBemerkung)))
			s+=bNachher.getFigur(bNachher.getNach()).getKuerzel();
		else if ((zugBemerkung!=null)&&(ZugEnum.EnPassant.equals(zugBemerkung)))
			s+=" e.p.";
		
		if ((zugStatus!=null)&&(SpielEnum.Patt.equals(zugStatus)))
			s+="=";
		else if ((zugStatus!=null)&&(SpielEnum.WeissImSchach.equals(zugStatus)||SpielEnum.SchwarzImSchach.equals(zugStatus)))
			s+="+";
		else if ((zugStatus!=null)&&(SpielEnum.WeissSchachMatt.equals(zugStatus)||SpielEnum.SchwarzSchachMatt.equals(zugStatus)))
			s+="++";

		return s;
	}
}
