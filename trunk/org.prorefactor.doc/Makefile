
# /c/progfile/fop-0.20.5/fop.bat -xml manual.xml -xsl c:/usr/share/xsl/docbook/fo/docbook.xsl -pdf manual.pdf

html/index.html: manual.xml joanju.xsl Makefile
	rm -rf html
	xmlto -o html -x joanju.xsl html manual.xml
	cp -r images html
	cp -r screenshots html
	cp *.css html
	rm -rf html/images/.svn
	rm -rf html/images/callouts/.svn
	rm -rf html/screenshots/.svn
	cp html/toc.xml .
	cd html ; zip -r ../doc.zip *

