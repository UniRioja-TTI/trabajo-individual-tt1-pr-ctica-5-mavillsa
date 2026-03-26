package servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import interfaces.InterfazContactoSim;
import modelo.DatosSimulation;
import modelo.DatosSolicitud;
import modelo.Entidad;
import modelo.Punto;

@Service 
public class ContactoSimService implements InterfazContactoSim {
	private List<Entidad> entidades;
    private Map<Integer, DatosSolicitud> solicitudesGuardadas;
    private final Random random;
    private final Logger logger; 
    public ContactoSimService(Logger logger) {
        this.entidades = new ArrayList<>();
        this.solicitudesGuardadas = new HashMap<>();
        this.random = new Random();
        this.logger = logger;
        inicializarEntidades();
    }
    private void inicializarEntidades() {
        Entidad e1 = new Entidad();
        e1.setId(1);
        e1.setName("Parámetro 1");
        e1.setDescripcion("Controlan la temperatura ambiental.");
        entidades.add(e1);

        Entidad e2 = new Entidad();
        e2.setId(2);
        e2.setName("Parámetro 2");
        e2.setDescripcion("Miden el nivel de humedad en el aire.");
        entidades.add(e2);

        Entidad e3 = new Entidad();
        e3.setId(3);
        e3.setName("Parámetro 3");
        e3.setDescripcion("Sistemas de vigilancia por video.");
        entidades.add(e3);
    }

    
    
    @Override
    public int solicitarSimulation(DatosSolicitud sol) {
    	int token = 1000 + random.nextInt(9000);
        solicitudesGuardadas.put(token, sol);
        return token;
    }

    @Override
    public DatosSimulation descargarDatos(int ticket) {
        DatosSimulation ds = new DatosSimulation();
        Map<Integer, List<Punto>> puntos = new HashMap<>();
        int maxTiempo = 0;

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // OJO: Esta es la llamada a tu máquina virtual. Asumo que la tienes en el 8080.
            // Cumplimos la norma de usar un usuario constante ("alumnoPrueba")
            String url = "http://localhost:8084/grid?tok=" + ticket + "&user=alumnoPrueba"; 
            
            String respuesta = restTemplate.getForObject(url, String.class);

            if (respuesta != null && !respuesta.trim().isEmpty()) {
                String[] lineas = respuesta.split("\n");
                
                // El primer número es el ancho de la matriz
                int anchoTablero = Integer.parseInt(lineas[0].trim());
                ds.setAnchoTablero(anchoTablero);
                
                // Procesamos el resto de líneas (tiempo, y, x, color)
                for (int i = 1; i < lineas.length; i++) {
                    String linea = lineas[i].trim();
                    if (linea.isEmpty()) continue;
                    
                    String[] partes = linea.split(",");
                    if (partes.length >= 4) {
                        int tiempo = Integer.parseInt(partes[0].trim());
                        int y = Integer.parseInt(partes[1].trim());
                        int x = Integer.parseInt(partes[2].trim());
                        String color = partes[3].trim();
                        
                        Punto punto = new Punto();
                        punto.setX(x);
                        punto.setY(y);
                        punto.setColor(color);
                        
                        puntos.computeIfAbsent(tiempo, k -> new ArrayList<>()).add(punto);
                        
                        if (tiempo > maxTiempo) {
                            maxTiempo = tiempo;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("No se pudo conectar con la máquina virtual: " + e.getMessage());
            ds.setAnchoTablero(10); // Valor de seguridad para que no explote la web
        }

        ds.setPuntos(puntos);
        ds.setMaxSegundos(maxTiempo + 1);

        return ds;
    }

    @Override
    public List<Entidad> getEntities() {
        return entidades;
    }

    @Override
    public boolean isValidEntityId(int i) {
    	return entidades.stream().anyMatch(e -> e.getId() == i);
    }
}