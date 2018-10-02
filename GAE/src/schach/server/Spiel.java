package schach.server;

import java.util.ArrayList;
import java.util.HashSet;

import schach.backend.Figur;
import schach.backend.Zug;
import schach.daten.D;
import schach.daten.D_ZugHistorie;
import schach.daten.Xml;
import schach.interfaces.iBackendSpiel;
import server.Tools;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.ResourceConfig;


@Path("schach/spiel")
public class Spiel extends ResourceConfig implements iBackendSpiel{
	private static schach.backend.Spiel[] spiele=new schach.backend.Spiel[32];

	public Spiel(){
	}
	
	public static schach.backend.Spiel getSpiel(int id){
		return Spiel.spiele[id];
	}
	
	public static void setSpiel(int id,schach.backend.Spiel spiel){
		Spiel.spiele[id]=spiel;
	}
	
	@GET
	@Path("/")
	@Produces("application/xml")
	public String getDienste(){
		return Tools.getDienste(this.getClass());
	}

	@GET
	@Path("getAktuelleBelegung/{id}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getAktuelleBelegung(
			@PathParam("id")int id) {
		try{
			String xml=getSpiel(id).getAktuelleBelegung().toXml();
			return Xml.verpacken(xml);
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}
	
	@GET
	@Path("getBelegung/{id}/{nummer}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getBelegung(
			@PathParam("id")int id,
			@PathParam("nummer")int nummer) {
		try{
			String xml=getSpiel(id).getBelegung(nummer).toXml();
			return Xml.verpacken(xml);
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}
	
	@GET
	@Path("getSpielDaten/{id}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getSpielDaten(
			@PathParam("id")int id) {
		try{
			String xml=getSpiel(id).getDaten().toXml();
			return Xml.verpacken(xml);
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}
	

	
	@GET
	@Path("getAlleErlaubtenZuege/{id}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getAlleErlaubtenZuege(
			@PathParam("id")int id) {
		try{
			HashSet<Zug> zuege=getSpiel(id).getAlleErlaubteZuege();
			ArrayList<D> zuegeDaten=new ArrayList<D>();
			if (zuege!=null){
				for(Zug zug:zuege){
					zuegeDaten.add(zug.getDaten());
				}
			}
			return Xml.verpacken(Xml.fromArray(zuegeDaten));
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}

	@GET
	@Path("getFigur/{id}/{position}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getFigur(
			@PathParam("id")int id,
			@PathParam("position")String position) {
		try{
			Figur figur=getSpiel(id).getAktuelleBelegung().getFigur(position);
			if (figur==null) throw new RuntimeException("Keine Figur auf dem Feld "+position+" vorhanden!");
			return Xml.verpacken(figur.toXml());
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}

	@GET
	@Path("getErlaubteZuege/{id}/{position}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getErlaubteZuege(
			@PathParam("id")int id,
			@PathParam("position")String position) {
		try{
			HashSet<Zug> zuege=getSpiel(id).getAktuelleBelegung().getErlaubteZuege(position);
			ArrayList<D> zuegeListe=new ArrayList<D>();
			if (zuege!=null){
				for(Zug z:zuege){
					zuegeListe.add(z.getDaten());
				}
			}
			return Xml.verpacken(Xml.fromArray(zuegeListe));
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}

	@GET
	@Path("ziehe/{id}/{von}/{nach}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String ziehe(
			@PathParam("id")int id,
			@PathParam("von")String von,
			@PathParam("nach")String nach) {
			try{
				getSpiel(id).ziehe(von,nach);
				return Xml.verpackeOK("Zug erfolgreich durchgefuehrt.");
			} catch (Exception e) {
				return Xml.verpackeFehler(e);
			}
	}

	@GET
	@Path("getZugHistorie/{id}")
	@Consumes("text/plain")
	@Produces("application/xml")
	@Override
	public String getZugHistorie(
			@PathParam("id")int id) {
		try{
			ArrayList<String> zugListe=getSpiel(id).getZugHistorie();
			ArrayList<D> zugHistorie=new ArrayList<D>();
			if(zugListe!=null){
				for(String zug:zugListe){
					D_ZugHistorie d=new D_ZugHistorie();
					d.setString("zug",zug);
					zugHistorie.add(d);
				}				
			}
			return Xml.verpacken(Xml.fromArray(zugHistorie));
		} catch (Exception e) {
			return Xml.verpackeFehler(e);
		}
	}
}
