// URL relativa (sin protocolo/host/puerto): así el frontend llama siempre
// al mismo origen desde donde se cargó la página, sin importar si es
// localhost:4200 en desarrollo (vía el proxy de ng serve, ver
// proxy.conf.json), la IP de la compu en la red local, o un túnel remoto
// (ngrok/Cloudflare) -- nunca hay que tocar este archivo ni reconstruir
// solo porque cambió la dirección desde la que se accede.
export const environment = {
  production: false,
  apiUrl: '/Los_Lopez/api/v1'
};
