# Para ejecutar demostración 1
En la consola se debe estar en la ruta: "./selenium-implicit-wait-maven"

```
docker compose up --build
```

Ir al navegador e ir a la ruta http://localhost:7900

**La contraseña para ingresar es:** secret

En caso de que se quiera probar varias veces el test, se puede ejecutar el comando:
```
docker compose up --build tests
```

Para separar la ejecución de los servicios web y selenium, se puede ejecutar los comandos en el siguiente orden:

1.
```
docker compose up --build web selenium
```
2.
```
docker compose up --build tests
```

---

# Para ejecutar demostración 2

Se debe levantar contenedor de DB y API REST, Ejecutar el proyecto frontend en local y luego ejecutar los test de Selenium.

1. DB 

En la consola se debe estar en la ruta: "./proyecto-real/DB

Luego se ejecuta el comando de docker
```
docker compose up --build
```

2. API REST

En la consola se debe estar en la ruta: "./proyecto-real/Reports-query"

Luego se ejecuta el comando de docker
```
docker compose up --build
```

3. Frontend

En la consola se debe estar en la ruta: "./proyecto-real/Frontend"

Luego se ejecuta el comando (versión mínima de node 20)
```
npm run dev
```

4. Tests de Selenium

En la consola se debe estar en la ruta: "./proyecto-real/selenium-tests"

Necesario tener Java 17 SDK y gradle
```
gradle clean test
```

# Para ejecutar demostración 3
En la consola se debe estar en la ruta: "./tiempo de espera implícito"

primero se deben levantar lso contenedores web y selenium.
```
docker compose up -d web chrome
```

Luego se construye la imagen de los tests de selenium
```
docker compose --profile tests build --no-cache tests
```

por último se ejecutan los tests sin headless para poder verlos en noVNC
```
docker compose --profile tests run  -it --rm --no-deps tests
```

**Importante**: Ir al navegador e ir a la ruta http://localhost:7900 para visualizar noVNC.

**La contraseña para ingresar será:** secret