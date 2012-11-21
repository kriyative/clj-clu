(ns clj-clu.test.core
  (:use [clojure.test])
  (:require
   [clj-clu.core :as clu]))

(clu/deftool test1
    "test1 clu tool"
    "test1 [switches] file1 file2"
    [opts [file1 file2] doc]
    [["-f" "--[no-]flag" "boolean test flag" :default false]]
  {:opts opts :files [file1 file2] :doc doc})

(defn process-command-line-nodoc [cmd & [env]]
  (dissoc (clu/process-command-line cmd env) :doc))

(deftest basic
  (is (= {:opts {:help false :flag false} :files [nil nil]}
         (process-command-line-nodoc ["test1"])))
  (is (= {:opts {:help false :flag false} :files ["file1" nil]}
         (process-command-line-nodoc ["test1" "file1"])))
  (is (= {:opts {:help false :flag true} :files ["file1" "file2"]}
         (process-command-line-nodoc ["test1" "--flag" "file1" "file2"]))))
