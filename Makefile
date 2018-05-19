FACTORIO_DATA='../../ext/factorio-data'

release:
	lein do clean, cljsbuild once min
	rsync -av --delete resources/public/ docs/

extract/selection_boxes.json:extract/selection_boxes.lua
	docker run -ti --rm --user `id -u` -v `realpath $(FACTORIO_DATA)`:/var/factorio-data:z -v `realpath ./extract`:/var/factorio-blueprint-tools:z abaez/lua:5.2 /bin/sh -c 'cd /var/factorio-blueprint-tools && ./selection_boxes.lua' > $@
