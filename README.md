clj-clu is a library to define and dispatch to multiple command line
utilities in a single Clojure uberjar, based on command line args.

Usage
-----

To install via [Leiningen](http://github.com/technomancy/leiningen),
add the following to your project.clj file:

    [clj-clu "1.0.0"]

Example:

    (ns clj-clu-test
      (:require [clj-clu.core :as clu])
      (:gen-class))

    (clu/deftool test1
        "test1 clu tool"
        "test1 [switches] file1 file2"
        [opts [file1 file2] doc]
        [["-f" "--[no-]flag" "boolean test flag" :default false]]
      (if (and file1 file2)
        (do
          ;; do something here
          )
        (throw-usage-exception "missing required args")))

    (defn -main [& args]
      (clu/process-command-line args))

Build and test the uberjar, with the following:

    lein uberjar
    java -jar test-standalone.jar help
