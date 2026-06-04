# Usa Node.js para construir Angular
FROM node:18 AS build

WORKDIR /app

# Copia solo los archivos necesarios para instalar dependencias
COPY package.json package-lock.json ./
RUN npm install

# Ahora copia el resto del código
COPY . .

# Construye el proyecto Angular
RUN npm run build --configuration=production

# Usa Nginx para servir Angular
FROM nginx:alpine
COPY --from=build /app/dist/front-los-lopez /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
