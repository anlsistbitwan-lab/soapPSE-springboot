# Etapa 1: Build con el Maven Wrapper (mvnw)
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copiamos todo el proyecto
COPY . .

# Damos permisos de ejecución al wrapper (importante para Linux)
RUN chmod +x mvnw

# Compilamos y empaquetamos el proyecto (sin ejecutar tests)
RUN ./mvnw clean package -DskipTests

# Etapa 2: Imagen final (solo contiene el jar)
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiamos el jar desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto de tu aplicación (por defecto 8080)
EXPOSE 8080

# Permite inyectar el perfil de Spring y la URL del WSDL dinámicamente
ENV SPRING_PROFILES_ACTIVE=dev

# Comando de inicio
#ENTRYPOINT ["java", "-jar", "app.jar "]
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]