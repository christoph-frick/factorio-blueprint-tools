FROM debian
RUN apt-get update && apt-get install -y \
    lua5.3 \
    luarocks \
 && rm -rf /var/lib/apt/lists/*
RUN luarocks install fennel
WORKDIR /var/factorio-blueprint-tools
COPY *.fnl /var/factorio-blueprint-tools/
COPY __base__/ /var/factorio-blueprint-tools/__base__/
COPY extract-all /var/factorio-blueprint-tools/
ENTRYPOINT ["./extract-all"]
