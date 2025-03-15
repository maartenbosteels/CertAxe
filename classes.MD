# Classes

* Certificate
    * holds all the fields were interested in
    * can convert a X509Certificate object to a Certificate object

* CertificateAppender
    * interface for appending certs to something

* Entry
    * represents an Entry on the CT log

* EntryFetcher
    * ftech() method to fetch an interval using HttpClient

* Interval
    * Data class that holds a LogList, a start and an end

* LogList
    * Data class that holds

* CertificateDatabase: done

* Event            : done
    * start
    * finished
    * type     : can be Fetched, Appended
    * list_url
    * start
    * end

* EventLogger
    * stores Event's in a duckdb table (persistent)
    * for now just via SLF4J