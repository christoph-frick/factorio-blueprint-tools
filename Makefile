all: release

build-release: force
	lein do clean, cljsbuild once min

build-functionaltest:build-release
	rsync -av --delete resources/public/ functionaltest/public/

quick-run-functionaltest: force
	cd functionaltest && docker-compose pull && docker-compose run --rm test-runner && docker-compose down

run-functionaltest:build-functionaltest quick-run-functionaltest

run-test:force
	lein do clean, test

test:run-test run-functionaltest

release:test
	rsync -av --delete resources/public/factorio-blueprint-tools/ docs/

resources/selection-boxes.edn:force
	cd extract && ./extract.sh

changelog: force
	git log --date=format:'%Y-%m-%d'  --pretty='- `%ad` | Update: %s'

force:
