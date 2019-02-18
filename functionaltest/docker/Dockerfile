FROM cypress/base:10
WORKDIR /app
COPY package.json .
ENV CI=1
RUN npm install
RUN npx cypress verify
