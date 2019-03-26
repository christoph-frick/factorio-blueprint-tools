FROM abaez/luarocks
RUN luarocks install fennel
WORKDIR /var/factorio-blueprint-tools
ADD selection-boxes.fnl /var/factorio-blueprint-tools
ENTRYPOINT ["sh", "-c", "fennel selection-boxes.fnl > /var/extract-result/selection-boxes.edn"]
