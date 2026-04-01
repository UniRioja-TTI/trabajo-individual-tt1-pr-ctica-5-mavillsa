package servicios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final Logger logger;

    public ContactoSimService(Logger logger) {
        this.entidades = new ArrayList<>();
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
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://servicio-consumible:5000/Solicitud/Solicitar?nombreUsuario=alumnoPrueba";
            // Construimos el body según el esquema "Solicitud" del swagger:
            // { "cantidadesIniciales": [1,2,3], "nombreEntidades": ["Parámetro 1", ...] }
            List<Integer> cantidades = new ArrayList<>();
            List<String> nombres = new ArrayList<>();

            for (Map.Entry<Integer, Integer> entry : sol.getNums().entrySet()) {
                int id = entry.getKey();
                int cantidad = entry.getValue();
                entidades.stream()
                        .filter(e -> e.getId() == id)
                        .findFirst()
                        .ifPresent(e -> {
                            cantidades.add(cantidad);
                            nombres.add(e.getName());
                        });
            }

            Map<String, Object> body = new HashMap<>();
            body.put("cantidadesIniciales", cantidades);
            body.put("nombreEntidades", nombres);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            Map response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && Boolean.TRUE.equals(response.get("done"))) {
                return (Integer) response.get("tokenSolicitud");
            } else {
                logger.error("La VM rechazó la solicitud: " + (response != null ? response.get("errorMessage") : "sin respuesta"));
                return -1;
            }
        } catch (Exception e) {
            logger.error("Error al solicitar simulación: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public DatosSimulation descargarDatos(int ticket) {
        DatosSimulation ds = new DatosSimulation();
        Map<Integer, List<Punto>> puntos = new HashMap<>();
        int maxTiempo = 0;

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Ahora es POST /Resultados con tok y nombreUsuario como query params
            String url = "http://servicio-consumible:5000/Resultados?nombreUsuario=alumnoPrueba&tok=" + ticket;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            Map response = restTemplate.postForObject(url, request, Map.class);
            if (response != null && Boolean.TRUE.equals(response.get("done"))) {
                String respuesta = (String) response.get("data");
                System.out.println("ESTO ME DEVUELVE LA MÁQUINA: \n" + respuesta);

                if (respuesta != null && !respuesta.trim().isEmpty()) {
                    String[] lineas = respuesta.split("\n");

                    int anchoTablero = Integer.parseInt(lineas[0].trim());
                    ds.setAnchoTablero(anchoTablero);

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

                            if (tiempo > maxTiempo) maxTiempo = tiempo;
                        }
                    }
                }
            } else {
                logger.error("La VM devolvió error: " + (response != null ? response.get("errorMessage") : "sin respuesta"));
                ds.setAnchoTablero(10);
            }
        } catch (Exception e) {
            logger.error("No se pudo conectar con la máquina virtual: " + e.getMessage());
            ds.setAnchoTablero(10);
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