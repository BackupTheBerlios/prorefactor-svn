<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:import href="c:/usr/share/xsl/docbook/eclipse/eclipse.xsl"/>

<!-- Prevent eclipse.xsl from overwriting plugin.xml -->
<xsl:template name="plugin.xml"></xsl:template>

<xsl:param name="html.stylesheet" select="'joanju.css'"/>
<xsl:param name="shade.verbatim" select="1"/>
<xsl:param name="chunk.first.sections" select="1"/>
<xsl:param name="chunk.section.depth" select="1"/>

<xsl:param name="eclipse.plugin.name">ProRefactor Documentation</xsl:param>
<xsl:param name="eclipse.plugin.id">org.prorefactor.doc</xsl:param>
<xsl:param name="eclipse.plugin.provider">ProRefactor.org</xsl:param>

<xsl:param name="section.autolabel" select="1"></xsl:param>
<xsl:param name="section.label.includes.component.label" select="1"></xsl:param>
<xsl:param name="xref.with.number.and.title" select="1"></xsl:param>

<xsl:param name="use.id.as.filename" select="1"></xsl:param>

</xsl:stylesheet>
