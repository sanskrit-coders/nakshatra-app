package in.bhumiputra.nakshatra.nighantu;

import android.content.Context;

/**
 * java script static variables to be used in result html page. as some times we may have to compute at run time it is not kept in external file.
 * TODO move it to external file.
 * TODO make it all english from translitered telugu.
 */

public class Script {
    public static final String modati_rata= "function marchu(nighId, ims) {\n" +
            "  var nigh= document.getElementById(nighId);\n" +
            "  var chupu= nigh.style.display;\n" +
            "  imJ= ims.getElementsByClassName(\"marchu_bomma\");\n" +
            "  im= imJ[0];\n" +
            "  \n" +
            "  if(chupu.match(\"^$\")) {\n" +
            "    nigh.style.display= \"block\";\n" +
            "    chupu= nigh.style.display;\n" +
            "  }\n" +
            "  \n" +
            "  if(chupu.match(\"block\")) {\n" +
            "    nigh.style.display= \"none\";\n" +
            "    //im.src=\"file:///android_asset/downarrow.png\";\n" +
            "    im.innerHTML='" + Document.downArrowIcon + "';\n" +
            "  }\n" +
            "  \n" +
            "  else if(chupu.match(\"none\")) {\n" +
            "    nigh.style.display= \"block\";\n" +
            "    //im.src=\"file:///android_asset/arrow.png\";\n" +
            "    im.innerHTML='" + Document.upArrowIcon + "';\n" +
            "  }\n" +
            "  \n" +
            "}\n" +
            "empika= '';\n" +
            "document.addEventListener('selectionchange', function() {\n" +
            "  empika= document.getSelection().toString();\n" +
            "  pelalu= document.getElementById('pelalu_bottom');\n" +
            "  \n" +
            "  if((empika== null) || (!empika) || (empika.match('^$'))) {\n" +
            "    /*window.Js.logWebView(\"empika is null\");*/\n" +
            "    pelalu.style.display= 'none';\n" +
            "  }\n" +
            "  \n" +
            "  else {\n" +
            "    /*window.Js.logWebView('empika is '+ empika);*/\n" +
            "    pelalu.style.display= 'block';\n" +
            "  }\n" +
            "}, false);\n" +
            "//window.onload= chettu; \n" +
            "function chettu() {\n" +
            "  //window.Js.logWebView(\"chettu lo undi.\");" +
            "  patra= document.getElementById(\"patra\");\n" +
            "  shiraP= document.getElementById(\"shiravibhaga\");\n" +
            "  paryayaP= document.getElementById(\"paryayavibhaga\");\n" +
            "  pettelu= [shiraP, paryayaP];\n" +
            "  \n" +
            "  petteNo= 0;\n" +
            "  for(pette of pettelu) {\n" +
            "    if(pette) {\n" +
            "      nighJ= pette.getElementsByClassName(\"nighantu\");\n" +
            "      \n" +
            "      if(nighJ.length) {\n" +
            "        for(i= 0; i< nighJ.length; i++) {\n" +
            "          nigh= nighJ.item(i);\n" +
            "          nighId= nigh.id;\n" +
            "          window.Js.addDictionaryToTree(petteNo, i, nighId, nigh.title);\n" +
            "          //window.Js.logWebView(\"chettukiNighKalupu: \"+ nighId+ \", \"+ nigh.title);\n" +
            "          aropaJ= nigh.getElementsByClassName(\"aropa\");\n" +
            "          \n" +
            "          if(aropaJ.length) {\n" +
            "            for(j= 0;  j< aropaJ.length; j++) {\n" +
            "              aropa= aropaJ.item(j);\n" +
            "              aropaId= aropa.id;\n" +
            "              window.Js.addEntryToDictionary(petteNo, nighId, nigh.title, j, aropaId, aropa.title);\n" +
            "              //window.Js.logWebView(\"nighkiAropaKalupu: \"+ aropaId+ \",\"+ aropa.title);\n" +
            "            }\n" +
            "          }\n" +
            "          \n" +
            "        }\n" +
            "      }\n" +
            "      \n" +
            "    }\n" +
            "    petteNo++;\n" +
            "  }\n" +
            "  \n" +
            "}\n" +
            "function chupu(id) {\n" +
            "  pette= document.getElementById(id);\n" +
            "  pette.scrollIntoView({behavior: 'smooth', block: 'center', inline: 'nearest' });\n" +
            "  //pette.scrollIntoView();\n" +
            "}\n" +
            "\n" +
            "function aropamEnchukonu (nakalu) {\n" +
            "  aropa= nakalu.parentElement.parentElement.parentElement;\n" +
            "  //addaGeeta= nakalu.nextElementSibling;\n" +
            "  vyapti = document.createRange();\n" +
            "  vyapti.selectNodeContents(aropa);\n" +
            "  //vyapti.setStartAfter(addaGeeta);\n" +
            "  //vyapti.setEndAfter(aropa);\n" +
            "  empika = window.getSelection();\n" +
            "  empika.removeAllRanges();\n" +
            "  empika.addRange(vyapti);\n" +
            "  pathyam= empika.toString();\n" +
            "  empika.removeAllRanges();\n" +
            "  window.Js.copy(pathyam);\n" +
            "}\n" +
            "\n" +
            "/*window.addEventListener(\"load\", function() {\n" +
            "  apadana= document.getElementById(\"apadana_pathyam\");\n" +
            "  atuku(apadana);\n" +
            "});\n" +
            "window.addEventListener(\"resize\", function() {\n" +
            "  apadana= document.getElementById(\"apadana_pathyam\");\n" +
            "  atuku(apadana);\n" +
            "});*/\n" +
            "\n" +
            "\n" +
            "function atuku(mulakam) {\n" +
            "  garishta= window.innerHeight;\n" +
            "  dc= mulakam.getBoundingClientRect();\n" +
            "  kinda= dc.bottom;\n" +
            "  //window.Js.logWebView(\"kinda: \"+ kinda+ \", garishta: \"+ garishta);\n" +
            "  if(garishta> kinda+20) {\n" +
            "    mulakam.style.position= \"absolute\";\n" +
            "    mulakam.style.bottom= \"0\";\n" +
            "  }\n" +
            "  else {\n" +
            "    mulakam.style.position= \"static\";\n" +
            "    mulakam.style.bottom= \"\";\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "CHARYA_NAKALU= 1;\n" +
            "CHARYA_PANCHUKONU= 2;\n" +
            "\n" +
            "function mottamEnchukonu(vibhagam, charya) {\n" +
            "  patra= document.getElementById(vibhagam);\n" +
            "  vyapti = document.createRange();\n" +
            "  vyapti.selectNodeContents(patra);\n" +
            "  empika = window.getSelection();\n" +
            "  empika.removeAllRanges();\n" +
            "  empika.addRange(vyapti);\n" +
            "  pathyam= empika.toString();\n" +
            "  empika.removeAllRanges();\n" +
            "  if(charya== CHARYA_NAKALU) {\n" +
            "    window.Js.copy(pathyam);\n" +
            "  }\n" +
            "  else if(charya== CHARYA_PANCHUKONU) {\n" +
            "    window.Js.share(pathyam);\n" +
            "  }\n" +
            "}\n" +
            "\n"
            ;



    public static final String bayati= "";



}
