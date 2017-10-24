release:
	lein do clean, cljsbuild once min
	rsync -av --delete resources/public/ docs/
