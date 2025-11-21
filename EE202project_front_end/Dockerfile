# ---- Build Stage ----
FROM node:20 AS build

WORKDIR /app
COPY package*.json ./

RUN npm ci  # 比 npm install 更適合正式版

COPY . .
RUN npm run build

# ---- Production Stage ----
FROM nginx:stable

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
