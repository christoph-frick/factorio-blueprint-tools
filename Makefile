all: release

build-release: force
	./shadow-cljsw release app
	rsync -av --delete resources/public/factorio-blueprint-tools/ docs/
	cp target/factorio-blueprint-tools/main.js docs/

build-functionaltest:build-release
	rsync -av --delete docs/ functionaltest/public/factorio-blueprint-tools/

quick-run-functionaltest: force
	cd functionaltest && docker-compose pull && docker-compose run --rm test-runner && docker-compose down

run-functionaltest:build-functionaltest quick-run-functionaltest

run-test:force
	./cljw -M:test

test:run-test run-functionaltest

release:test

extract:force
	cd extract && ./extract.sh

changelog: force
	git log --date=format:'%Y-%m-%d'  --pretty='- `%ad` | Update: %s'

force:
