FROM debian
RUN apt-get update && apt-get install -y \
    lua5.2 \
    luarocks \
 && rm -rf /var/lib/apt/lists/*
RUN luarocks install fennel
WORKDIR /var/factorio-blueprint-tools
ADD selection-boxes.fnl /var/factorio-blueprint-tools
ENTRYPOINT ["sh", "-c", "fennel selection-boxes.fnl > /var/extract-result/selection-boxes.edn"]
