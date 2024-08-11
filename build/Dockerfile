FROM clojure:tools-deps

WORKDIR /build

ENV HOME=/build
ENV JDK_JAVA_OPTIONS=-Duser.home=/build

RUN apt-get update && apt-get install -y \
    npm \
 && rm -rf /var/lib/apt/lists/*

RUN npm config set cache /build/.npm --global

RUN npm install -g shadow-cljs
