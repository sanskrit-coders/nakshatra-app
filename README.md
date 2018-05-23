***Note:*** it is first coded with all class, method, variable, resource names in translitered telugu, and not in english. i refactored class, method names and major part of code to english. still at very few places may encounter translitered telugu or sanskrit tatsamas. they will be refactored gradually.

###supports
supports stardict format.  
supports .syn file.  
supports pseudo random access on .dict.dz compressed files.  

###packages intro
**in.bhumiputra.nakshatra** package contains facade interface  activities, content providers, etc.

**in.bhumiputra.nakshatra.nighantu.anga** package contains classes modelling stardict dictionary components(*Idx*, *Syn*, *Index*, *Dict*, *Dz*, *Ifo*, *Dictionary*, etc..), and some datastructures(*Address*, *Entry*, *Tuple*, *IntTuple*, *SortedListsMap* etc.). classes in this package contains documentation.

**in.bhumiputra.nakshatra.nighantu** package contains classes which operates on all *Dictionary* objects. like *Search*, *Suggestions*, *Document* etc. and a container class *Dictionaries* , which scans and loads dictionaries as *Dictionary* objects, and provide access to them. and *Document* class takes *Dictionaries* object as member, and generates final result page.

###working
**NighantuProvider** is main class which is a content provider, and provides search suggestions, and result webpage. It delegates *Dictionaries* , and *Document* objects for interface with stardict dictionaries. after creating this provider, we can just load result webpage from a WebView, and display suggestions in SearchView etc. On top of this main underlying functionality we build remaining interface.

For suggestions, normal suggestions are just computed normally. But  for Fuzzy suggestions we use FuzzyWuzzy library. as of now we just use only one function to compute nearness between two words. but entire library is included(which is very small).

###interface flow
*InitActivity* is launcher Activity. it manages permissions, and then launches *IndexerActivity* activity.

*IndexerActivity* scans dictionaries, and compare with previous scan information stored, and computes details about any updated dictionaries, or newly installed dictionaries, or deleted dictionaries. based on them it takes appropriate action. if no dictionaries avialable, it shows some help message. If any dictionaries are updated from previous scan (checks timestamps), it silently reindex them. If any new dictionaries found, then it gives user interface to select dictionaries to index, and then indexes selected dictionaries, and exits.

then *NakshatraActivity* will be launched, and loads main interface. from here we can goto remaining Activities, or use functionalities within it.

###features
* supports syn, dz files too.
* pulls synonyms from remaining dictionaries too. First displays exact results from all dictionaries, and then synonyms from all dictionaries. this is to avoid clumsyness.
* right side navigation drawer gives a miniature titles view of result page. if many results are there, then it will be very useful. we can jsut click on header in dictionary block we want, then page will be scrolled to that result.
* supports navigation in alphabeticle order. just swype right or left. or click on prev, next fading icons. supports hyperlinks. and if no hyperlinks, then just select word by long click, and a pop up will open at bottom with options like *search*, *pronounce*.
* popup window on text selection, and share. it opens atop of any app on text selection and share. pop up also supports above alphabeticle navigattion by swyping left or rightt.
* gives auto complete suggestions while typing. if no results starts with what you typed, then it switches to fuzzy suggestions. and give suggestions very close to typed word. so no problem even if wrong spelling entered.
* when you searched for word not in dictionaries, then it creates result page with close words, words starting with, and splitted words etc.
* word of the day app widget
* bookmarks, history, random word, pronounciations, easy copying and sharing, theming, etc.

###major TODOs
* supporting builtin transliteration for indian languages.
* some much better ways of indexing.
* i18n.




