.#-----------------------------------------------------
.input|xhtml/1.0
.# Verify that SUB tag parameters are recognized.
.#-----------------------------------------------------
<html><p><sub data-xwiki-translated-attribute-a="b">something</sub></p></html>
.#-----------------------------------------------------
.expect|event/1.0
.#-----------------------------------------------------
beginDocument
beginParagraph
beginFormat [SUBSCRIPT] [[a]=[b]]
onWord [something]
endFormat [SUBSCRIPT] [[a]=[b]]
endParagraph
endDocument
.#-----------------------------------------------------
.expect|xhtml/1.0
.#-----------------------------------------------------
<p><sub><span data-xwiki-translated-attribute-a="b">something</span></sub></p>
.#-----------------------------------------------------
.expect|xwiki/2.0
.#-----------------------------------------------------
(% a="b" %),,something,,