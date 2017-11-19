<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
	xmlns:elmo="http://bp4mc2.org/elmo/def#"
	xmlns:elmo2="http://dotwebstack.org/def/elmo#"
>

<xsl:output method="xml" indent="yes"/>

<xsl:template match="*" mode="type">
	<xsl:variable name="appearance">
		<xsl:value-of select="rdf:type[@rdf:resource!='http://dotwebstack.org/def/elmo#Appearance'][1]/@rdf:resource"/>
	</xsl:variable>
	<xsl:if test="$appearance!=''">
		<elmo:appearance rdf:resource="http://bp4mc2.org/elmo/def#{substring-after($appearance,'#')}"/>
	</xsl:if>
</xsl:template>

<xsl:template match="/">
	<rdf:RDF>
  
    <xsl:for-each select="appearances/appearance">
      <xsl:variable name="appearance" select="id"/>
      <rdf:Description rdf:about="{$appearance}">
        <elmo:query>INFOPROD</elmo:query>
        <xsl:for-each select="rdf:RDF/rdf:Description[@rdf:about=$appearance]">
          <xsl:apply-templates select="." mode="type"/>
          <xsl:for-each select="elmo2:fragment">
            <elmo:fragment rdf:nodeID="{@rdf:nodeID}"/>
          </xsl:for-each>
        </xsl:for-each>
      </rdf:Description>
    </xsl:for-each>
    
		<xsl:for-each select="appearances/appearance/rdf:RDF/rdf:Description[exists(elmo2:appliesTo)]">
			<rdf:Description rdf:nodeID="{@rdf:nodeID}">
				<elmo:applies-to><xsl:value-of select="elmo2:appliesTo"/></elmo:applies-to>
				<xsl:copy-of select="rdfs:label"/>
			</rdf:Description>
		</xsl:for-each>
    
	</rdf:RDF>
</xsl:template>

</xsl:stylesheet>