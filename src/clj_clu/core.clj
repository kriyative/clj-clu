(ns clj-clu.core
  (:require
   [clojure.string :as str]
   [clojure.tools.cli :as cli]))

(def ^:private tools (atom (sorted-map)))

(def usage-exception-cause (Throwable. "usage-exception"))
(defn make-usage-exception [msg] (RuntimeException. msg usage-exception-cause))

(defn throw-usage-exception [msg]
  (throw (make-usage-exception msg)))

(defn add-tool! [name doc usage options body-fn]
  (swap! tools
         assoc
         name
         {:usage usage
          :doc doc
          :handler (fn [args]
                     (let [parsed-opts  (apply cli/cli
                                               (vec args)
                                               (conj options
                                                     ["-h" "--[no-]help" "Print help"]))
                           [opts _ doc] parsed-opts
                           handler-fn (fn [print-help]
                                        (if print-help
                                          (do
                                            (println usage)
                                            (println doc))
                                          (apply body-fn parsed-opts)))]
                       (try
                         (handler-fn (:help opts))
                         (catch RuntimeException ex
                           (if (= (.getCause ex) usage-exception-cause)
                             (do
                               (println "Error:" (.getMessage ex))
                               (handler-fn true))
                             (throw ex))))))}))

(defmacro deftool [name doc usage bindings options & body]
  (let [name# (str name)]
    `(add-tool! ~name#
                ~doc
                ~usage
                ~options
                (fn [~@bindings] ~@body))))

(def ^{:dynamic true :private true} *env* nil)

(defmacro with-environment [env & body]
  `(binding [*env* ~env]
     ~@body))

(defn get-environment [& ks] (get-in *env* ks))

(deftool help
    "Show available commands"
    "help cmd [cmds...]"
    [opts cmds doc]
    []
  (let [command-name (or (get-environment :command-name) "clu")]
    (if (not-empty cmds)
      (doseq [c cmds]
        (if-let [tool (get @tools c)]
          (do
            (println (:doc tool))
            ((:handler tool) ["--help"])
            (println))
          (println c (format "is not a valid '%s' command" command-name))))
      (do
        (println (format "Usage: %s command [options]\n" command-name))
        (println "  available commands:")
        (doseq [[k v] @tools]
          (println (format "  %-10s - %s" k (:doc v))))
        (println)))))

(defn process-command-line [args & [env]]
  (with-environment env
    (if-let [tool (get @tools (first args))]
      ((:handler tool) (rest args))
      ((:handler (get @tools "help")) []))))
