package server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class Rest extends Application{
	@Override
	public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        classes.add(schach.server.Spiel.class);
        classes.add(schach.server.SpielAdmin.class);
        return classes;
    }
}



 


