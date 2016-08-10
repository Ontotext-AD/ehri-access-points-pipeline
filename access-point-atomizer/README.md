# Access Point Atomizer

This is a GATE plugin for processing EHRI access points. Access points are essentially keyphrases extracted from archival descriptions, e.g.:
* Deportation
* Central Office for Jewish Emigration in Vienna
* II(Außenpolitik)
* Neustadt,<>,Bavaria,Germany
* industrie--belgique--1940-1944
* Pleven,מחנה
* Jews--Ukraine--Kam'i︠a︡net︠s︡ʹ-Podilʹsʹkyĭ.
* Antisemitism--Untied States.

It creates the following annotations in a list of access points:
* `Access Point` an entire access point (line)
* `Atom` an atomic component of an access point (phrase)
* `Token` a token within a single atom (word)

The `Atom` and `Token` annotations have a `fingerprint` feature which is a simplified form of their string content for easier matching.
