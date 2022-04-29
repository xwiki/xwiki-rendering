.#-----------------------------------------------------
.input|xwiki/2.0
.#-----------------------------------------------------
(% param1="value1" param2="value2" %)
* item
.#-----------------------------------------------------
.expect|event/1.0
.#-----------------------------------------------------
beginDocument
beginList [BULLETED] [[param1]=[value1][param2]=[value2]]
beginListItem
onWord [item]
endListItem
endList [BULLETED] [[param1]=[value1][param2]=[value2]]
endDocument
.#-----------------------------------------------------
.expect|xhtml/1.0
.#-----------------------------------------------------
<ul data-xwiki-translated-attribute-param1="value1" data-xwiki-translated-attribute-param2="value2"><li>item</li></ul>
.#-----------------------------------------------------
.expect|xwiki/2.0
.#-----------------------------------------------------
(% param1="value1" param2="value2" %)
* item
.#-----------------------------------------------------
.input|xhtml/1.0
.#-----------------------------------------------------
<html><ul data-xwiki-translated-attribute-param1="value1" data-xwiki-translated-attribute-param2="value2"><li>item</li></ul></html>