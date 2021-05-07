FROM debian
RUN apt-get update && apt-get install -y \
    lua5.3 \
    luarocks \
 && rm -rf /var/lib/apt/lists/*
RUN luarocks install fennel
WORKDIR /var/factorio-blueprint-tools
ADD selection-boxes.fnl /var/factorio-blueprint-tools
COPY __base__/ /var/factorio-blueprint-tools/__base__/
ENTRYPOINT ["sh", "-c", "fennel selection-boxes.fnl > /var/extract-result/selection-boxes.edn"]
