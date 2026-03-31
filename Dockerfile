# Usamos Java 21 sobre Alpine para que pese poco
FROM eclipse-temurin:21-jdk-alpine

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copiamos el jar compilado al contenedor
COPY target/trabajo-0.0.1-SNAPSHOT.jar app.jar

# Puerto que expone la aplicación
EXPOSE 5000

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]