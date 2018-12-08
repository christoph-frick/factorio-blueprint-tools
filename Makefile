release:
	lein do clean, cljsbuild once min
	rsync -av --delete resources/public/ docs/

extract/selection_boxes.edn:extract/selection_boxes.fnl
	docker run -ti --rm -v `realpath factorio-data`:/var/factorio-data:z -v `realpath ./extract`:/var/factorio-blueprint-tools:z abaez/luarocks /bin/sh -c 'luarocks install fennel >/dev/null 2>/dev/null && cd /var/factorio-blueprint-tools && fennel --compile selection_boxes.fnl | lua' > $@
