build-release: force
	lein do clean, cljsbuild once min

build-functionaltest:build-release
	rsync -av --delete resources/public/ functionaltest/public/

quick-run-functionaltest: force
	cd functionaltest && docker-compose run --rm test-runner && docker-compose down

run-functionaltest:build-functionaltest quick-run-functionaltest

run-test:force
	lein do clean, test

test:run-test run-functionaltest

release:test
	rsync -av --delete resources/public/ docs/

resources/selection_boxes.edn:extract/selection_boxes.fnl
	docker run -ti --rm -v `realpath factorio-data`:/var/factorio-data:z -v `realpath ./extract`:/var/factorio-blueprint-tools:z abaez/luarocks /bin/sh -c 'luarocks install fennel >/dev/null 2>/dev/null && cd /var/factorio-blueprint-tools && fennel --compile selection_boxes.fnl | lua' > $@

force:
