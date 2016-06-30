# Access Point Atomizer

This is a GATE plugin for processing access points. It creates the following annotations in a list of access points:
* `Access Point` an entire access point (line)
* `Atom` an atomic component of an access point (phrase)
* `Token` a token within a single atom (word)

The `Atom` and `Token` annotations have a `fingerprint` feature which is a simplified form of their string content for easier matching.
